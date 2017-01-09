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

package hmatalonga.greenhub.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.managers.sampling.DataEstimator;
import hmatalonga.greenhub.managers.sampling.Inspector;
import hmatalonga.greenhub.models.Battery;
import hmatalonga.greenhub.ui.MainActivity;

/**
 * Notifier
 */
public class Notifier {
    private static boolean isStatusBarShown = false;

    private static NotificationCompat.Builder sBuilder = null;

    private static NotificationManager sNotificationManager = null;

    public static void startStatusBar(final Context context) {
        if (isStatusBarShown) return;

        // At this moment Inspector still doesn't have a current level assigned
        DataEstimator estimator = new DataEstimator();
        estimator.getCurrentStatus(context);

        int now = Battery.getBatteryCurrentNow(context);
        int level = estimator.getLevel();
        String title = "Now: " + now + " mA";
        String text = "GreenHub is running";

        sBuilder = new NotificationCompat.Builder(context)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setPriority(NotificationCompat.PRIORITY_LOW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        if (level < 100) {
            sBuilder.setSmallIcon(R.drawable.ic_stat_00_pct_charged + level);
            if (level <= 15) {
                sBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
            }
        } else {
            sBuilder.setSmallIcon(R.drawable.ic_stat_z100_pct_charged);
        }

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        sBuilder.setContentIntent(resultPendingIntent);
        sNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        sNotificationManager.notify(Config.NOTIFICATION_BATTERY_STATUS, sBuilder.build());
        isStatusBarShown = true;
    }

    public static void updateStatusBar(final Context context) {
        // In case status bar is not started yet call start method
        if (!isStatusBarShown) {
            startStatusBar(context);
            return;
        }

        int now = Battery.getBatteryCurrentNow(context);
        int level = (int) (Inspector.getCurrentBatteryLevel() * 100);
        String title = "Now: " + now + " mA";
        String text = "GreenHub is running";

        sBuilder.setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(false)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        if (level < 100) {
            sBuilder.setSmallIcon(R.drawable.ic_stat_00_pct_charged + level);
            if (level <= 15) {
                sBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
            }
        } else {
            sBuilder.setSmallIcon(R.drawable.ic_stat_z100_pct_charged);
        }

        // Because the ID remains unchanged, the existing notification is updated.
        sNotificationManager.notify(Config.NOTIFICATION_BATTERY_STATUS, sBuilder.build());
    }

    public static void batteryFullAlert(final Context context) {
        if (sNotificationManager == null) {
            sNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_information_white_24dp)
                .setContentTitle("Battery is full")
                .setContentText("Remove your phone from the charger")
                .setAutoCancel(true)
                .setOngoing(false)
                .setLights(Color.GREEN, 500, 2000)
                .setVibrate(new long[] {0, 400, 1000})
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        // Because the ID remains unchanged, the existing notification is updated.
        sNotificationManager.notify(Config.NOTIFICATION_BATTERY_FULL, mBuilder.build());
    }

    public static void batteryLowAlert(final Context context) {
        if (sNotificationManager == null) {
            sNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_alert_circle_white_24dp)
                .setContentTitle("Battery is low")
                .setContentText("Please connect your phone to a power source")
                .setAutoCancel(true)
                .setOngoing(false)
                .setLights(Color.RED, 500, 2000)
                .setVibrate(new long[] {0, 400, 1000})
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        // Because the ID remains unchanged, the existing notification is updated.
        sNotificationManager.notify(Config.NOTIFICATION_BATTERY_LOW, mBuilder.build());
    }
}
