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
import android.content.Intent;
import android.content.IntentFilter;

import hmatalonga.greenhub.managers.sampling.DataEstimator;
import io.realm.Realm;
import io.realm.RealmConfiguration;

import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * GreenHubApp
 */
public class GreenHubApp extends Application {

    private static final String TAG = makeLogTag(GreenHubApp.class);

    public boolean isServiceRunning;

    @Override
    public void onCreate() {
        super.onCreate();
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);

        isServiceRunning = false;

        // Database init
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    public void startGreenHubService(final DataEstimator estimator) {
        new Thread() {
            private IntentFilter intentFilter;

            public void run() {
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

                registerReceiver(estimator, intentFilter);

                // TODO: Add this config option to settings so user decides what to do
                if (Config.EXTRA_SCREEN_ACTIONS) {
                    intentFilter.addAction(Intent.ACTION_SCREEN_ON);
                    registerReceiver(estimator, intentFilter);
                    intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
                    registerReceiver(estimator, intentFilter);
                }

                isServiceRunning = true;
            }
        }.start();
    }

    public void stopGreenHubService(final DataEstimator estimator) {
        if (estimator != null) {
            unregisterReceiver(estimator);
            isServiceRunning = false;
        }
    }
}
