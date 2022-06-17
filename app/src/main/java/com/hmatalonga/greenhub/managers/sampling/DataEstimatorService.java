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

package com.hmatalonga.greenhub.managers.sampling;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;

import com.hmatalonga.greenhub.Config;
import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.events.StatusEvent;
import com.hmatalonga.greenhub.managers.storage.GreenHubDb;
import com.hmatalonga.greenhub.models.data.BatteryUsage;
import com.hmatalonga.greenhub.models.data.Sample;
import com.hmatalonga.greenhub.network.CommunicationManager;
import com.hmatalonga.greenhub.tasks.CheckNewMessagesTask;
import com.hmatalonga.greenhub.tasks.ServerStatusTask;
import com.hmatalonga.greenhub.util.LogUtils;
import com.hmatalonga.greenhub.util.Notifier;
import com.hmatalonga.greenhub.util.SettingsUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import static com.hmatalonga.greenhub.util.LogUtils.logI;
import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Data Estimator Service
 * <p>
 * Created by hugo on 13-04-2016.
 */
public class DataEstimatorService extends IntentService {

    private static final String TAG = makeLogTag(DataEstimatorService.class);

    public DataEstimatorService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getBaseContext();

        if (intent != null) {
            takeSampleIfBatteryLevelChanged(context, intent);
        }

    }

    /**
     * Some phones receive the batteryChanged very very often. We are interested
     * only in changes of the battery level.
     *
     * @param intent  The parent intent (the one passed from the DataEstimator)
     *                (with one extra field set, called 'distance')
     *                This intent should be the intent which is passed by the Android system
     *                to your broadcast receiver
     *                (which is registered with the BATTERY_CHANGED action).
     *                In our case, this broadcast receiver is 'Sampler'.
     * @param context
     */
    private void takeSampleIfBatteryLevelChanged(final Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        // Make sure our new sample doesn't have a zero value as its current battery level
        if (Inspector.getCurrentBatteryLevel() <= 0) return;

        GreenHubDb database = new GreenHubDb();
        Sample lastSample = database.lastSample();

        // If intent has action Screen ON or Screen OFF don't check the change on battery level
        if (action.equals(Intent.ACTION_SCREEN_ON) ||
                action.equals(Intent.ACTION_SCREEN_OFF)) {
            LogUtils.logI(TAG, "Getting new usage details");
            getBatteryUsage(context, intent, database, true);
            database.close();
            return;
        }

        // Set last sample, if exists extract the last battery level
        if (lastSample != null) {
            Inspector.setLastBatteryLevel(lastSample.batteryLevel);
        }

        /*
         * Read the battery levels again, they are now changed. We just
         * changed the last battery level (in the previous block of code).
         * The current battery level might also have been changed while the
         * device has been taking a sample.
         */
        boolean batteryLevelChanged =
                Inspector.getLastBatteryLevel() != Inspector.getCurrentBatteryLevel();

        /*
         * Among all occurrence of the event BATTERY_CHANGED, only take a sample
         * whenever a battery PERCENTAGE CHANGE happens
         * (BATTERY_CHANGED happens whenever the battery temperature
         * or voltage of other parameters change)
         */
        if (!batteryLevelChanged || Inspector.isSampling) {
            if (!batteryLevelChanged) {
                logI(TAG, "No battery percentage change. BatteryLevel=" +
                        Inspector.getCurrentBatteryLevel());
            } else if (Inspector.isSampling) {
                logI(TAG, "Inspector is already sampling...");
            }
        } else {
            String message =
                    "The battery percentage changed. " +
                            "About to take a new sample (currentBatteryLevel=" +
                            Inspector.getCurrentBatteryLevel() + ", lastBatteryLevel=" +
                            Inspector.getLastBatteryLevel() + ")";
            LogUtils.logI(TAG, message);

            // take a sample and store it in the mDatabase
            EventBus.getDefault().post(new StatusEvent(getString(R.string.event_new_sample)));

            getSample(context, intent, database);
            getBatteryUsage(context, intent, database, false);

            boolean isPlugged = 0 != intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);

            if (SettingsUtils.isBatteryAlertsOn(context) &&
                    SettingsUtils.isChargeAlertsOn(context)) {
                if (Inspector.getCurrentBatteryLevel() == 1 && isPlugged) {
                    Notifier.batteryFullAlert(context);
                } else if (Inspector.getCurrentBatteryLevel() == Config.BATTERY_LOW_LEVEL) {
                    Notifier.batteryLowAlert(context);
                }
            }

            // If last battery level = 0 then it is the first sample in the current instance
            if (Inspector.getLastBatteryLevel() == 0) {
                LogUtils.logI(
                        TAG,
                        "Last Battery Level = 0. Updating to BatteryLevel => " +
                                Inspector.getCurrentBatteryLevel()
                );
                // before taking the first sample in a batch, first record the battery level
                Inspector.setLastBatteryLevel(Inspector.getCurrentBatteryLevel());
            }
        }

        /*
         * Check upload constraints:
         * - Automatic uploads allowed
         * - Server url present
         * - Max uploadAttempts
         * - Not registered
         * - Not batteryChanged
         */
        if (CommunicationManager.uploadAttempts >= Config.UPLOAD_MAX_TRIES ||
                !SettingsUtils.isAutomaticUploadingAllowed(context) ||
                !SettingsUtils.isServerUrlPresent(context) ||
                !SettingsUtils.isDeviceRegistered(context) ||
                !batteryLevelChanged) {
            database.close();
            LogUtils.logI(TAG, "Database closed. No upload now.");
            return;
        }

        // Update server status
        new ServerStatusTask().execute(context);

        new CheckNewMessagesTask().execute(context);

        // Check if is necessary to sendSamples samples >= pref_upload_rate
        if (database.count(Sample.class) >= SettingsUtils.fetchUploadRate(context) &&
                !CommunicationManager.isUploading) {
            LogUtils.logI(TAG, "Enough samples to upload on background...");
            CommunicationManager manager = new CommunicationManager(context, true);
            manager.sendSamples();
        }

        // Check if automatic upload are off do DB clean up here...

        ActivityManager actvityManager = (ActivityManager)
                this.getApplicationContext().getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = actvityManager.getRunningAppProcesses();
        // Finally close mDatabase access
        database.close();
    }

    /**
     * Takes a Sample and stores it in the mDatabase. Does not store the first ever samples
     * that have no battery info.
     *
     * @param context  from onReceive
     * @param intent   from onReceive
     * @param database GreenHub database
     */
    private void getSample(Context context, Intent intent, GreenHubDb database) {
        Sample sample = Inspector.getSample(context, intent);

        // Write to mDatabase, but only after first real numbers
        if (sample != null && !sample.batteryState.equals("Unknown") && sample.batteryLevel >= 0) {
            // store the sample into the mDatabase
            database.saveSample(sample);
            LogUtils.logI(TAG, "Took sample " + sample.id + " for " + intent.getAction());
        }

        // Notify UI
        EventBus.getDefault().post(new StatusEvent(context.getString(R.string.event_idle)));
    }

    private void getBatteryUsage(Context context, Intent intent, GreenHubDb database,
                                 boolean isScreenIntent) {
        // if Intent is screen related, it is necessary to add extras from DataEstimator
        // since original intent has none
        if (isScreenIntent) {
            Bundle extras = DataEstimator.getBatteryChangedIntent(context).getExtras();
            if (extras != null) intent.putExtras(extras);
        }

        BatteryUsage usage = Inspector.getBatteryUsage(context, intent);

        if (usage != null && !usage.state.equals("Unknown") && usage.level >= 0) {
            database.saveUsage(usage);
            LogUtils.logI(TAG, "Took usage details " + usage.id + " for " + intent.getAction());
        }
    }
}
