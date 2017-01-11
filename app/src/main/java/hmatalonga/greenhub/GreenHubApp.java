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

package hmatalonga.greenhub;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import hmatalonga.greenhub.managers.sampling.DataEstimator;
import hmatalonga.greenhub.util.Notifier;
import hmatalonga.greenhub.util.SettingsUtils;
import io.realm.Realm;
import io.realm.RealmConfiguration;

import static hmatalonga.greenhub.util.LogUtils.LOGI;
import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * GreenHubApp
 */
public class GreenHubApp extends Application {

    private static final String TAG = makeLogTag(GreenHubApp.class);

    public static boolean isServiceRunning = false;

    public DataEstimator estimator;

    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        // Database init
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(realmConfiguration);

        estimator = new DataEstimator();

        if (SettingsUtils.isTosAccepted(getApplicationContext())) {
            LOGI(TAG, "Notifier Status Bar called");
            mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Notifier.updateStatusBar(getApplicationContext());
                    mHandler.postDelayed(this, Config.REFRESH_STATUS_BAR_INTERVAL);
                }
            }, Config.REFRESH_STATUS_BAR_INTERVAL);

            LOGI(TAG, "startGreenHubService() called");
            startGreenHubService();
        }
    }

    public void startGreenHubService() {
        if (!isServiceRunning) {
            LOGI(TAG, "GreenHubService starting...");

            final Context context = getApplicationContext();

            // Display Status bar
            Notifier.startStatusBar(context);

            new Thread() {
                private IntentFilter intentFilter;

                public void run() {
                    intentFilter = new IntentFilter();
                    intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

                    registerReceiver(estimator, intentFilter);

                    if (SettingsUtils.isSamplingScreenOn(context)) {
                        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
                        registerReceiver(estimator, intentFilter);
                        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
                        registerReceiver(estimator, intentFilter);
                    }

                    isServiceRunning = true;
                }
            }.start();
        } else {
            LOGI(TAG, "GreenHubService is already running...");
        }
    }

    public void stopGreenHubService() {
        if (estimator != null) {
            unregisterReceiver(estimator);
            isServiceRunning = false;
        }
    }
}
