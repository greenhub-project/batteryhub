/*
 * Copyright (c) 2016 Hugo Matalonga & João Paulo Fernandes
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

package com.hmatalonga.greenhub.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import com.hmatalonga.greenhub.events.BatteryTimeEvent;
import com.hmatalonga.greenhub.events.PowerSourceEvent;
import com.hmatalonga.greenhub.managers.sampling.Inspector;
import com.hmatalonga.greenhub.managers.storage.GreenHubDb;
import com.hmatalonga.greenhub.models.Battery;
import com.hmatalonga.greenhub.util.LogUtils;
import com.hmatalonga.greenhub.util.Notifier;

import org.greenrobot.eventbus.EventBus;

import io.realm.exceptions.RealmMigrationNeededException;

import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

public class PowerConnectionReceiver extends BroadcastReceiver {

    private static final String TAG = makeLogTag(PowerConnectionReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action == null) return;

        boolean isCharging = false;
        String batteryCharger = "";
        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            isCharging = true;

            final Intent mIntent = context.getApplicationContext()
                    .registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

            if (mIntent == null) return;

            int chargePlug = mIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            boolean wirelessCharge = false;

            if (Build.VERSION.SDK_INT >= 21) {
                wirelessCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS;
            }

            if (acCharge) {
                batteryCharger = "ac";
                EventBus.getDefault().post(new PowerSourceEvent("ac"));
            } else if (usbCharge) {
                batteryCharger = "usb";
                EventBus.getDefault().post(new PowerSourceEvent("usb"));
            } else if (wirelessCharge) {
                batteryCharger = "wireless";
                EventBus.getDefault().post(new PowerSourceEvent("wireless"));
            }
        } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
            isCharging = false;
            EventBus.getDefault().post(new PowerSourceEvent("unplugged"));
        }
        // Post to subscribers & update notification
        int batteryRemaining =
                (int) (Battery.getRemainingBatteryTime(context, isCharging, batteryCharger) / 60);
        int batteryRemainingHours = batteryRemaining / 60;
        int batteryRemainingMinutes = batteryRemaining % 60;

        EventBus.getDefault().post(
                new BatteryTimeEvent(batteryRemainingHours, batteryRemainingMinutes, isCharging)
        );
//        Notifier.remainingBatteryTimeAlert(
//                context,
//                batteryRemainingHours + "h " + batteryRemainingMinutes + "m", isCharging
//        );

        try {
            // Save a new Battery Session to the mDatabase
            GreenHubDb database = new GreenHubDb();
            LogUtils.logI(TAG, "Getting new session");
            database.saveSession(Inspector.getBatterySession(context, intent));
            database.close();
        } catch (IllegalStateException | RealmMigrationNeededException e) {
            LogUtils.logE(TAG, "No session was created");
            e.printStackTrace();
        }
    }
}