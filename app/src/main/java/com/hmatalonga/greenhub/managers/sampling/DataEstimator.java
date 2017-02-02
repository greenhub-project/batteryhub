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

package com.hmatalonga.greenhub.managers.sampling;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.greenrobot.eventbus.EventBus;

import com.hmatalonga.greenhub.events.BatteryLevelEvent;
import com.hmatalonga.greenhub.util.Notifier;
import com.hmatalonga.greenhub.util.SettingsUtils;

import static com.hmatalonga.greenhub.util.LogUtils.LOGE;
import static com.hmatalonga.greenhub.util.LogUtils.LOGI;
import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Provides current Device data readings.
 *
 * Created by hugo on 09-04-2016.
 */
public class DataEstimator extends WakefulBroadcastReceiver {

    private static final String TAG = makeLogTag(DataEstimator.class);

    private double distance = 0.0;
    private long lastNotify;

    private int health;
    private int level;
    private int plugged;
    private boolean present;
    private int scale;
    private int status;
    private String technology;
    private float temperature;
    private float voltage;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            LOGE(TAG, "Error, context is null");
            return;
        }

        if (intent == null) {
            LOGE(TAG, "Data Estimator error, received intent is null");
            return;
        }

        LOGI(TAG, "onReceive action => " + intent.getAction());

        if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            try {
                level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
                plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                present = intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
                status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
                technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
                temperature = ((float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10;
                voltage = ((float) intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)) / 1000;
            }
            catch (RuntimeException e) {
                e.printStackTrace();
            }

            if (SettingsUtils.isBatteryAlertsOn(context)) {
                // Notify for temperature alerts...
                if (temperature > 45) {
                    Notifier.batteryHighTemperature(context);
                } else if (temperature <= 45 && temperature > 35) {
                    Notifier.batteryWarningTemperature(context);
                }
            }

            if (SettingsUtils.isPowerIndicatorShown(context)) {
                Notifier.updateStatusBar(context);
            }
        }

        // On some phones, scale is always 0.
        if (scale == 0) scale = 100;

        if (level > 0) {
            Inspector.setCurrentBatteryLevel(level, scale);

            // Location updates disabled for now
            // requestLocationUpdates();

            // Update last known location...
            // if (lastKnownLocation == null) {
            //    lastKnownLocation = LocationInfo.getLastKnownLocation(context);
            // }

            Intent service = new Intent(context, DataEstimatorService.class);
            service.putExtra("OriginalAction", intent.getAction());
            service.fillIn(intent, 0);
            service.putExtra("distance", distance);

            EventBus.getDefault().post(new BatteryLevelEvent(level));

            startWakefulService(context, service);
        }
    }

    public static Intent getBatteryChangedIntent(final Context context) {
        return context.registerReceiver(
                null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        );
    }

    // Getters & Setters
    public void getCurrentStatus(final Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        assert batteryStatus != null;

        level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        present = batteryStatus.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
        status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
        technology = batteryStatus.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
        temperature = (float) (batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10);
        voltage = (float) (batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000);
    }

    public String getHealthStatus() {
        String status = "";
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                status = "Unknown";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                status = "Good";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                status = "Overheat";
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                status = "Dead";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                status = "Over Voltage";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                status = "Unspecified Failure";
                break;
        }

        return status;
    }

    public long getLastNotify() {
        return lastNotify;
    }

    public void setLastNotify(long now) {
        this.lastNotify = now;
    }

    public int getHealth() {
        return health;
    }

    public int getLevel() {
        return level;
    }

    public int getPlugged() {
        return plugged;
    }

    public boolean isPresent() {
        return present;
    }

    public int getScale() {
        return scale;
    }

    public int getStatus() {
        return status;
    }

    public String getTechnology() {
        return technology;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getVoltage() {
        return voltage;
    }
}
