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
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.managers.sampling.BatteryEstimator;
import hmatalonga.greenhub.managers.storage.GreenHubDb;
import hmatalonga.greenhub.ui.MainActivity;

/**
 * Created by hugo on 06-03-2016.
 */
public class Notifier {
    public static void toOpenApp(Context context) {
        long now = System.currentTimeMillis();
        long lastNotify = BatteryEstimator.getInstance().getLastNotify();

        // Do not notify if it is less than 2 days from last notification
        if (lastNotify + Config.FRESHNESS_TIMEOUT_QUICKHOGS > now)
            return;

        int samples = GreenHubDb.getInstance(context).countSamples();
        if (samples >= BatteryEstimator.MAX_SAMPLES){
            BatteryEstimator.getInstance().setLastNotify(now);
            PendingIntent launchCarat = PendingIntent.getActivity(context, 0,
                    new Intent(context, MainActivity.class), 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    context)
                    .setSmallIcon(R.drawable.ic_information_white_24dp)
                    .setContentTitle("Please open GreenHubHelper")
                    .setContentText("Please open GreenHubHelper. Samples to send:")
                    .setNumber(samples);
            mBuilder.setContentIntent(launchCarat);
            //mBuilder.setSound(null);
            mBuilder.setAutoCancel(true);
            NotificationManager mNotificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());
        }

    }
}
