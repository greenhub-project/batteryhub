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

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.SystemClock;
import com.hmatalonga.greenhub.managers.sampling.BatteryService;
import com.hmatalonga.greenhub.managers.sampling.DataEstimator;
import com.hmatalonga.greenhub.managers.storage.GreenHubDbMigration;
import com.hmatalonga.greenhub.models.Sensors;
import com.hmatalonga.greenhub.receivers.NotificationReceiver;
import com.hmatalonga.greenhub.tasks.DeleteSessionsTask;
import com.hmatalonga.greenhub.tasks.DeleteUsagesTask;
import com.hmatalonga.greenhub.util.LogUtils;
import com.hmatalonga.greenhub.util.SettingsUtils;

import java.util.List;
import io.realm.Realm;
import io.realm.RealmConfiguration;

import static com.hmatalonga.greenhub.util.LogUtils.logI;
import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * GreenHubApp Application class.
 */
public class GreenHubApp extends Application implements SensorEventListener {

    private static final String TAG = makeLogTag(GreenHubApp.class);

    private AlarmManager mAlarmManager;

    private PendingIntent mNotificationIntent;

    private SensorManager mSensorManager;

    private List<Sensor> mSensorList;

    @Override
    public void onCreate() {
        super.onCreate();

        // If running debug mode, enable logs
        if (BuildConfig.DEBUG) {
            LogUtils.LOGGING_ENABLED = true;
        }

        logI(TAG, "onCreate() called");

        // Init crash reports
        //FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        // Database init
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(Config.DATABASE_VERSION)
                .migration(new GreenHubDbMigration())
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        logI(TAG, "Estimator new instance");


        Context context = getApplicationContext();

        if (SettingsUtils.isTosAccepted(context)) {
            // Start GreenHub Service
            logI(TAG, "startGreenHubService() called");
            startGreenHubService();

            // Delete old data history
            final int interval = SettingsUtils.fetchDataHistoryInterval(context);
            new DeleteUsagesTask().execute(interval);
            new DeleteSessionsTask().execute(interval);

            if (SettingsUtils.isPowerIndicatorShown(context)) {
                startStatusBarUpdater();
            }
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        startSensorListeners(SensorManager.SENSOR_DELAY_NORMAL);
    }
    public void startSensorListeners(int sensorDelay) {
        for (Sensor sensor: mSensorList) {
            mSensorManager.registerListener(this, sensor, sensorDelay);
        }
        //Reset sensors information
        Sensors.resetSensorsMap(getBaseContext());
    }

    private void stopSensorListeners() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        Sensors.onSensorChanged(event);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    public void startGreenHubService() {
        if (!BatteryService.isServiceRunning) {
            logI(TAG, "GreenHubService starting...");

            final Context context = getApplicationContext();

            new Thread() {

                public void run() {
                    Intent service = new Intent(context, BatteryService.class);
                    try {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(service);
                        } else {
                            context.startService(service);
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }

                }
            }.start();
        } else {
            logI(TAG, "GreenHubService is already running...");
        }
    }

    public void stopGreenHubService() {
        Intent service = new Intent(getApplicationContext(), BatteryService.class);
        stopService(service);
    }

    public DataEstimator getEstimator() {
        return BatteryService.estimator;
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
