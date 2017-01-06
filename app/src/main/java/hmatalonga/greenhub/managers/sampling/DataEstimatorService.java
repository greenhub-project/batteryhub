/*
 * Copyright (c) 2011-2016, AMP Lab and University of Helsinki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 *
 * -------------------------------------------------------------------------------
 *
 * Copyright (C) 2016 Hugo Matalonga & João Paulo Fernandes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hmatalonga.greenhub.managers.sampling;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import org.greenrobot.eventbus.EventBus;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.events.StatusEvent;
import hmatalonga.greenhub.managers.storage.GreenHubDb;
import hmatalonga.greenhub.models.data.BatteryUsage;
import hmatalonga.greenhub.models.data.Sample;
import hmatalonga.greenhub.network.CommunicationManager;
import hmatalonga.greenhub.util.Notifier;
import hmatalonga.greenhub.util.SettingsUtils;

import static hmatalonga.greenhub.util.LogUtils.LOGI;
import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Data Estimator Service
 *
 * Created by hugo on 13-04-2016.
 */
public class DataEstimatorService extends IntentService {

    private static final String TAG = makeLogTag(DataEstimatorService.class);
    private double mDistance;

    public DataEstimatorService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // At this point SimpleWakefulReceiver is still holding a wake lock
        // for us. We can do whatever we need to here and then tell it that
        // it can release the wakelock. This sample just does some slow work,
        // but more complicated implementations could take their own wake
        // lock here before releasing the receiver's.
        //
        // Note that when using this approach you should be aware that if your
        // service gets killed and restarted while in the middle of such work
        // (so the Intent gets re-delivered to perform the work again), it will
        // at that point no longer be holding a wake lock since we are depending
        // on SimpleWakefulReceiver to that for us. If this is a concern, you
        // can
        // acquire a separate wake lock here.
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock =
                powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

        wakeLock.acquire();

        String action = null;
        Context context = getApplicationContext();

        if (intent != null) {
            action = intent.getStringExtra("OriginalAction");
        }

        if (action != null) {
            takeSampleIfBatteryLevelChanged(intent, context);
        }

        wakeLock.release();

        if (intent != null) {
            DataEstimator.completeWakefulIntent(intent);
        }
    }

    /**
     * Some phones receive the batteryChanged very very often. We are interested
     * only in changes of the battery level.
     *
     * @param intent  The parent intent (the one passed from the DataEstimator)
     *				  (with one extra field set, called 'distance')
     *				  This intent should be the intent which is passed by the Android system to your
     *                broadcast receiver (which is registered with the BATTERY_CHANGED action).
     *                In our case, this broadcast receiver is 'Sampler'.
     * @param context
     */
    private void takeSampleIfBatteryLevelChanged(Intent intent, final Context context) {
        mDistance = intent.getDoubleExtra("distance", 0);

        // Make sure our new sample doesn't have a zero value as its current battery level
        if (Inspector.getCurrentBatteryLevel() > 0) {
            GreenHubDb database = new GreenHubDb();
            Sample lastSample = database.lastSample();

            // If intent has action Screen ON or Screen OFF don't check the change on battery level
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON) ||
                    intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                LOGI(TAG, "Getting new usage details");
                getBatteryUsage(context, intent, database, true);
                database.close();
                return;
            }

            // Set last Sample
            if (lastSample != null) {
                Inspector.setLastBatteryLevel(lastSample.batteryLevel);
            }

            // If last battery level = 0 then it is the first sample in the current instance
            if (Inspector.getLastBatteryLevel() == 0) {
                LOGI(TAG, "Getting a new sample. BatteryLevel => " + Inspector.getCurrentBatteryLevel());
                // before taking the first sample in a batch, first record the battery level
                Inspector.setLastBatteryLevel(Inspector.getCurrentBatteryLevel());
                // take a sample and store it in the database
                EventBus.getDefault().post(new StatusEvent("Getting new sample..."));
                getSample(context, intent, lastSample, database);
                LOGI(TAG, "Getting new usage details");
                getBatteryUsage(context, intent, database, false);
                if (Inspector.getCurrentBatteryLevel() == 1) {
                    Notifier.batteryFullAlert(context);
                }
            }

			/**
			 * Read the battery levels again, they are now changed. We just
			 * changed the last battery level (in the previous block of code).
			 * The current battery level might also have been changed while the
			 * device has been taking a sample.
			 */
            boolean batteryLevelChanged =
                    Inspector.getLastBatteryLevel() != Inspector.getCurrentBatteryLevel();

            /**
             * Among all occurrence of the event BATTERY_CHANGED, only take a sample
             * whenever a battery PERCENTAGE CHANGE happens
             * (BATTERY_CHANGED happens whenever the battery temperature or voltage of other parameters change)
             */
            if (batteryLevelChanged) {
                LOGI(TAG, "The battery percentage changed. About to take a new sample (currentBatteryLevel=" +
                        Inspector.getCurrentBatteryLevel() + ", lastBatteryLevel=" +
                        Inspector.getLastBatteryLevel()+ ")");

                // take a sample and store it in the database
                EventBus.getDefault().post(new StatusEvent("Getting new sample..."));
                getSample(context, intent, lastSample, database);
                getBatteryUsage(context, intent, database, false);
                if (Inspector.getCurrentBatteryLevel() == 1) {
                    Notifier.batteryFullAlert(context);
                }
            } else {
                if (Config.DEBUG) {
                    LOGI(TAG, "No battery percentage change. BatteryLevel => " + Inspector.getCurrentBatteryLevel());
                }
            }

            // Check if server url is stored in preferences
            if (!SettingsUtils.isServerUrlPresent(context)) {
                database.close();
                return;
            }

            // Check if is necessary to sendSamples samples >= SAMPLE_MAX_BATCH
            if (database.count(Sample.class) >= Config.SAMPLE_MAX_BATCH &&
                    !CommunicationManager.isUploading) {
                CommunicationManager manager = new CommunicationManager(context);
                manager.sendSamples();
            }

            // Finally close database access
            database.close();
        } else {
            if (Config.DEBUG) {
                LOGI(TAG, "current battery level = 0");
            }
        }
    }

    /**
     * Takes a Sample and stores it in the database. Does not store the first ever samples
     * that have no battery info.
     *
     * @param context from onReceive
     * @param intent from onReceive
     */
    private void getSample(Context context, Intent intent, Sample lastSample, GreenHubDb database) {
        String lastBatteryState = lastSample != null ? lastSample.batteryState : "Unknown";
        Sample sample = Inspector.getSample(context, intent, lastBatteryState);

        // Set mDistance to current mDistance value
        if (sample != null) {
            sample.distanceTraveled = mDistance;
            // FIX: Do not use same mDistance again.
            mDistance = 0;
        }

        // Write to database, but only after first real numbers
        if (sample != null && !sample.batteryState.equals("Unknown") && sample.batteryLevel >= 0) {
            // store the sample into the database
            database.saveSample(sample);
            LOGI(TAG, "Took sample " + sample.id + " for " + intent.getAction());
        }

        // Notify UI
        EventBus.getDefault().post(new StatusEvent(Config.STATUS_IDLE));
    }

    private void getBatteryUsage(Context context, Intent intent, GreenHubDb database, boolean isScreenIntent) {
        // if Intent is screen related, it is necessary to add extras from DataEstimator
        // since original intent has none
        if (isScreenIntent) {
            intent.putExtras(DataEstimator.getBatteryChangedIntent(context).getExtras());
        }

        BatteryUsage usage = Inspector.getBatteryUsage(context, intent);

        if (usage != null && !usage.state.equals("Unknown") && usage.level >= 0) {
            database.saveUsage(usage);
            LOGI(TAG, "Took usage details " + usage.id + " for " + intent.getAction());
        }
    }
}
