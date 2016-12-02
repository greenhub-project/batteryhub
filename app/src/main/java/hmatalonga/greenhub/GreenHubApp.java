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

import com.squareup.leakcanary.LeakCanary;

import hmatalonga.greenhub.managers.sampling.DataEstimator;
import hmatalonga.greenhub.util.SettingsUtils;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * GreenHubApp
 */
public class GreenHubApp extends Application {

    private static final String TAG = "GreenHubApp";

    public DataEstimator estimator;

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        // Database init
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(realmConfiguration);

        if (SettingsUtils.isTosAccepted(getApplicationContext())) {
            new Thread() {
                private IntentFilter intentFilter;

                public void run() {
                    intentFilter = new IntentFilter();
                    intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

                    estimator = new DataEstimator();

                    registerReceiver(estimator, intentFilter);
                }
            }.start();
        }
    }
}
