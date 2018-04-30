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

package com.hmatalonga.greenhub;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

import com.crashlytics.android.Crashlytics;
import com.hmatalonga.greenhub.managers.sampling.DataEstimator;
import com.hmatalonga.greenhub.managers.storage.GreenHubDbMigration;
import com.hmatalonga.greenhub.receivers.NotificationReceiver;
import com.hmatalonga.greenhub.tasks.DeleteSessionsTask;
import com.hmatalonga.greenhub.tasks.DeleteUsagesTask;
import com.hmatalonga.greenhub.util.LogUtils;
import com.hmatalonga.greenhub.util.SettingsUtils;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

import static com.hmatalonga.greenhub.util.LogUtils.logE;
import static com.hmatalonga.greenhub.util.LogUtils.logI;
import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * GreenHubApp Application class.
 */
public class GreenHubApp extends Application {

    private static final String TAG = makeLogTag(GreenHubApp.class);

    public static boolean isServiceRunning = false;

    public DataEstimator estimator;

    private AlarmManager mAlarmManager;

    private PendingIntent mNotificationIntent;

    @Override
    public void onCreate() {
        super.onCreate();

        // If running debug mode, enable logs
        if (BuildConfig.DEBUG) {
            LogUtils.LOGGING_ENABLED = true;
        }

        LogUtils.logI(TAG, "onCreate() called");

        // Init crash reports
        Fabric.with(this, new Crashlytics());

        // Database init
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(Config.DATABASE_VERSION)
                .migration(new GreenHubDbMigration())
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        LogUtils.logI(TAG, "Estimator new instance");
        estimator = new DataEstimator();

        Context context = getApplicationContext();

        if (SettingsUtils.isTosAccepted(context)) {
            // Start GreenHub Service
            LogUtils.logI(TAG, "startGreenHubService() called");
            startGreenHubService();

            // Delete old data history
            final int interval = SettingsUtils.fetchDataHistoryInterval(context);
            new DeleteUsagesTask().execute(interval);
            new DeleteSessionsTask().execute(interval);

            if (SettingsUtils.isPowerIndicatorShown(context)) {
                startStatusBarUpdater();
            }
        }
    }

    public void startGreenHubService() {
        if (!isServiceRunning) {
            LogUtils.logI(TAG, "GreenHubService starting...");

            final Context context = getApplicationContext();
            isServiceRunning = true;

            new Thread() {
                private IntentFilter mIntentFilter;

                public void run() {
                    mIntentFilter = new IntentFilter();
                    mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

                    registerReceiver(estimator, mIntentFilter);

                    if (SettingsUtils.isSamplingScreenOn(context)) {
                        mIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
                        registerReceiver(estimator, mIntentFilter);
                        mIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
                        registerReceiver(estimator, mIntentFilter);
                    }
                }
            }.start();
        } else {
            LogUtils.logI(TAG, "GreenHubService is already running...");
        }
    }

    public void stopGreenHubService() {
        try {
            if (estimator != null) {
                unregisterReceiver(estimator);
                isServiceRunning = false;
            }
        } catch (IllegalArgumentException e) {
            LogUtils.logE(TAG, "Estimator receiver is not registered!");
            e.printStackTrace();
        }
    }

    public void startStatusBarUpdater() {
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        mNotificationIntent = PendingIntent.getBroadcast(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        if (mAlarmManager == null) {
            mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        }
        mAlarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + Config.REFRESH_STATUS_BAR_INTERVAL,
                Config.REFRESH_STATUS_BAR_INTERVAL,
                mNotificationIntent
        );

    }

    public void stopStatusBarUpdater() {
        if (mAlarmManager != null) {
            mAlarmManager.cancel(mNotificationIntent);
        }
    }
}
