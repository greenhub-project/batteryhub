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
import android.content.ReceiverCallNotAllowedException;
import android.os.BatteryManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.events.BatteryLevelEvent;
import com.hmatalonga.greenhub.util.LogUtils;
import com.hmatalonga.greenhub.util.Notifier;
import com.hmatalonga.greenhub.util.SettingsUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

import static com.hmatalonga.greenhub.util.LogUtils.logE;
import static com.hmatalonga.greenhub.util.LogUtils.logI;
import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Provides current Device data readings.
 * <p>
 * Created by hugo on 09-04-2016.
 */
public class DataEstimator extends WakefulBroadcastReceiver {

    private static final String TAG = makeLogTag(DataEstimator.class);

    private long mLastNotify;

    private int mHealth;
    private int mLevel;
    private int mPlugged;
    private boolean mPresent;
    private int mScale;
    private int mStatus;
    private String mTechnology;
    private float mTemperature;
    private float mVoltage;

    @Override
    public void onReceive(Context context, Intent intent) {
        //region null conditionals validations
        if (context == null) {
            LogUtils.logE(TAG, "Error, context is null");
            return;
        }

        if (intent == null) {
            LogUtils.logE(TAG, "Received intent is null");
            return;
        }

        String action = intent.getAction();

        if (action == null) {
            LogUtils.logE(TAG, "Intent has no action");
            return;
        }
        //endregion

        LogUtils.logI(TAG, "ENTRY onReceive => " + action);

        if (!action.equals(Intent.ACTION_BATTERY_CHANGED)) return;

        // Fetch Intent extras related to the battery state
        try {
            mLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            mScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            mHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
            mPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            mPresent = intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
            mStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
            mTechnology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
            mTemperature = ((float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10;
            mVoltage = ((float) intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)) / 1000;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        /* We don't send battery mLevel alerts here because we need to check if the mLevel changed
           So we verify that inside the DataEstimator Service */

        if (mTemperature > SettingsUtils.fetchTemperatureWarning(context)) {
            if (SettingsUtils.isBatteryAlertsOn(context) &&
                    SettingsUtils.isTemperatureAlertsOn(context)) {

                // Check mTemperature limit rate
                Calendar lastAlert = Calendar.getInstance();
                long lastSavedTime = SettingsUtils.fetchLastTemperatureAlertDate(context);

                // Set last alert time with saved preferences
                if (lastSavedTime != 0) {
                    lastAlert.setTimeInMillis(lastSavedTime);
                }
                int minutes = SettingsUtils.fetchTemperatureAlertsRate(context);

                lastAlert.add(Calendar.MINUTE, minutes);

                // If last saved time isn't default and now is after limit rate then notify
                if (lastSavedTime == 0 || Calendar.getInstance().after(lastAlert)) {
                    // Notify for mTemperature alerts...
                    if (mTemperature > SettingsUtils.fetchTemperatureHigh(context)) {
                        Notifier.batteryHighTemperature(context);
                        SettingsUtils.saveLastTemperatureAlertDate(
                                context,
                                System.currentTimeMillis()
                        );
                    } else if (mTemperature <= SettingsUtils.fetchTemperatureHigh(context) &&
                            mTemperature > SettingsUtils.fetchTemperatureWarning(context)) {
                        Notifier.batteryWarningTemperature(context);
                        SettingsUtils.saveLastTemperatureAlertDate(
                                context,
                                System.currentTimeMillis()
                        );
                    }
                }
            }
        }

        if (SettingsUtils.isPowerIndicatorShown(context)) {
            LogUtils.logI(TAG, "Updating notification mStatus bar");
            Notifier.updateStatusBar(context);
        }

        // On some phones, mScale is always 0.
        if (mScale == 0) mScale = 100;

        if (mLevel > 0) {
            Inspector.setCurrentBatteryLevel(mLevel, mScale);

            Intent service = new Intent(context, DataEstimatorService.class);
            service.putExtra("OriginalAction", action);
            service.fillIn(intent, 0);

            EventBus.getDefault().post(new BatteryLevelEvent(mLevel));

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

        try {
            Intent batteryStatus = context.registerReceiver(null, ifilter);

            if (batteryStatus != null) {
                mLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                mScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                mHealth = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
                mPlugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                mPresent = batteryStatus.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
                mStatus = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
                mTechnology = batteryStatus.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
                mTemperature =
                        (float) (batteryStatus.getIntExtra(
                                BatteryManager.EXTRA_TEMPERATURE, 0
                        ) / 10);
                mVoltage =
                        (float) (batteryStatus.getIntExtra(
                            BatteryManager.EXTRA_VOLTAGE, 0
                        ) / 1000);
            }
        } catch (ReceiverCallNotAllowedException e) {
            LogUtils.logE(TAG, "ReceiverCallNotAllowedException from Notification Receiver?");
            e.printStackTrace();
        }
    }

    public String getHealthStatus() {
        String status = "";
        switch (mHealth) {
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

    public String getHealthStatus(final Context context) {
        String status = "";
        switch (mHealth) {
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                status = context.getString(R.string.battery_health_unknown);
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                status = context.getString(R.string.battery_health_good);
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                status = context.getString(R.string.battery_health_overheat);
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                status = context.getString(R.string.battery_health_dead);
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                status = context.getString(R.string.battery_health_over_voltage);
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                status = context.getString(R.string.battery_health_failure);
                break;
        }

        return status;
    }

    public long getmLastNotify() {
        return mLastNotify;
    }

    public void setmLastNotify(long now) {
        this.mLastNotify = now;
    }

    public int getHealth() {
        return mHealth;
    }

    public int getLevel() {
        return mLevel;
    }

    public int getPlugged() {
        return mPlugged;
    }

    public boolean isPresent() {
        return mPresent;
    }

    public int getScale() {
        return mScale;
    }

    public int getStatus() {
        return mStatus;
    }

    public String getTechnology() {
        return mTechnology;
    }

    public float getTemperature() {
        return mTemperature;
    }

    public float getVoltage() {
        return mVoltage;
    }
}
