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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.ui.MainActivity;

/**
 * Created by hugo on 06-03-2016.
 */
public class Notifier {
    public static void toOpenApp(Context context) {
//        long now = System.currentTimeMillis();
//        long lastNotify = DataEstimator.getGson().getLastNotify();
//
//        // Do not notify if it is less than 2 days from last notification
//        if (lastNotify + Config.FRESHNESS_TIMEOUT_QUICKHOGS > now)
//            return;
//
//        int samples = GreenHubDb.getGson(context).countSamples();
//        if (samples >= DataEstimator.MAX_SAMPLES){
//            DataEstimator.getGson().setLastNotify(now);
//            PendingIntent launchCarat = PendingIntent.getActivity(context, 0,
//                    new Intent(context, MainActivity.class), 0);
//
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                    context)
//                    .setSmallIcon(R.drawable.ic_information_white_24dp)
//                    .setContentTitle("Please open GreenHubHelper")
//                    .setContentText("Please open GreenHubHelper. Samples to send:")
//                    .setNumber(samples);
//            mBuilder.setContentIntent(launchCarat);
//            //mBuilder.setSound(null);
//            mBuilder.setAutoCancel(true);
//            NotificationManager mNotificationManager = (NotificationManager) context
//                    .getSystemService(Context.NOTIFICATION_SERVICE);
//            mNotificationManager.notify(1, mBuilder.build());
//        }

    }

    public static void testNotification(final Context context) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_information_white_24dp)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
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
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(Config.NOTIFICATION_BATTERY_STATUS, mBuilder.build());
    }
}
