/*
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
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;

import java.util.Date;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.managers.storage.GreenHubDb;
import hmatalonga.greenhub.models.data.Sample;
import hmatalonga.greenhub.util.Notifier;

import static hmatalonga.greenhub.util.LogUtils.LOGI;
import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Data Estimator Service
 *
 * Created by hugo on 13-04-2016.
 */
public class DataEstimatorService extends IntentService {

    private static final String TAG = makeLogTag(DataEstimator.class);
    private double mDistance;

    public DataEstimatorService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DataEstimator estimator = new DataEstimator();

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

        Context context = getApplicationContext();

        String action = null;
        if (intent != null) {
            action = intent.getStringExtra("OriginalAction");
        }

        if (action != null) {
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                // NOTE: This is disabled to simplify how Carat behaves.
                SharedPreferences p = context.getSharedPreferences("SystemBootTime", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = p.edit();
                editor.putLong("bootTime", new Date().getTime());
                editor.apply();
                // onBoot(context);
            }

            if (action.equals(Config.ACTION_GREENHUB_SAMPLE)) {
                // set up sampling.
                // Let sampling happen on battery change
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
//                intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
//                intentFilter.addDataScheme("package"); // add addDataScheme

                // Unregister, since GreenHub may have been started multiple times since reboot
                try {
                    unregisterReceiver(estimator);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                registerReceiver(estimator, intentFilter);
            }

            takeSampleIfBatteryLevelChanged(intent, context);
        }

        wakeLock.release();

        if (intent != null) {
            DataEstimator.completeWakefulIntent(intent);
        }
    }

    /**
     * Some phones receive the batteryChanged very very often. We are interested
     * only in changes of the battery level
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
            GreenHubDb sampleDB = GreenHubDb.getInstance(context);
            Sample lastSample = sampleDB.getLastSample();

            if (lastSample != null) {
                Inspector.setLastBatteryLevel(lastSample.batteryLevel);
            }

            if (Inspector.getLastBatteryLevel() == 0) {
                // before taking the first sample in a batch, first record the battery level
                Inspector.setLastBatteryLevel(Inspector.getCurrentBatteryLevel());
                // take a sample and store it in the database
                getSample(context, intent, lastSample, sampleDB);
                // Notifier.toOpenApp(context);
            }

			/*
			 * Read the battery levels again, they are now changed. We just
			 * changed the last battery level (in the previous block of code).
			 * The current battery level might also have been changed while the
			 * device has been taking a sample.
			 */
            boolean batteryLevelChanged =
                    Inspector.getLastBatteryLevel() != Inspector.getCurrentBatteryLevel();

            if (batteryLevelChanged) {
				/* among all occurrence of the event BATTERY_CHANGED, only take a sample
				 * whenever a battery PERCENTAGE CHANGE happens
				 * (BATTERY_CHANGED happens whenever the battery temperature or voltage of other parameters change)
				 */
                LOGI(TAG, "The battery percentage changed. About to take a new sample (currentBatteryLevel=" + Inspector.getCurrentBatteryLevel() + ", lastBatteryLevel=" + Inspector.getLastBatteryLevel()+ ")");

                // take a sample and store it in the database
                getSample(context, intent, lastSample, sampleDB);

                // Notifier.toOpenApp(context);
            } else {
                if (Config.DEBUG) {
                    LOGI(TAG, "NO battery percentage change. currentBatteryLevel=" + Inspector.getCurrentBatteryLevel());
                }
            }
        } else {
            if (Config.DEBUG) {
                LOGI(TAG, "current battery level = 0");
            }
        }
    }

    /**
     * Takes a Sample and stores it in the database. Does not store the first ever samples
     * that have no battery info.
     * @param context from onReceive
     * @param intent from onReceive
     * @return the newly recorded Sample
     */
    private void getSample(Context context, Intent intent, Sample lastSample, GreenHubDb sampleDB) {
        // String action = intent.getStringExtra("OriginalAction");
        // Log.i("SamplerService.getSample()", "Original intent: " +action);
        String lastBatteryState = lastSample != null ? lastSample.batteryState : "Unknown";
        Sample sample = Inspector.getSample(context, intent, lastBatteryState);
        // Set mDistance to current mDistance value
        if (sample != null) {
            sample.distanceTraveled = mDistance;
            // FIX: Do not use same mDistance again.
            mDistance = 0;
        }

        // Write to database
        // But only after first real numbers
        if (sample != null && !sample.batteryState.equals("Unknown") && sample.batteryLevel >= 0) {
            // store the sample into the database
            // long id = sampleDB.putSample(sample);
            Log.i(TAG, "Took sample " + 0 + " for " + intent.getAction());
            //FlurryAgent.logEvent(intent.getAction());
            //  Log.d(TAG, "current battery level (just before quitting getSample() ): " + SamplingLibrary.getCurrentBatteryLevel());
        }

        // Send sample ??
    }
}
