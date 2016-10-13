package hmatalonga.greenhub.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import hmatalonga.greenhub.Constants;
import hmatalonga.greenhub.ui.MainActivity;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.sampling.BatteryEstimator;
import hmatalonga.greenhub.storage.GreenHubDb;

/**
 * Created by hugo on 06-03-2016.
 */
public class Notifier {
    public static void toOpenApp(Context context) {
        long now = System.currentTimeMillis();
        long lastNotify = BatteryEstimator.getInstance().getLastNotify();

        // Do not notify if it is less than 2 days from last notification
        if (lastNotify + Constants.FRESHNESS_TIMEOUT_QUICKHOGS > now)
            return;

        int samples = GreenHubDb.getInstance(context).countSamples();
        if (samples >= BatteryEstimator.MAX_SAMPLES){
            BatteryEstimator.getInstance().setLastNotify(now);
            PendingIntent launchCarat = PendingIntent.getActivity(context, 0,
                    new Intent(context, MainActivity.class), 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    context)
                    .setSmallIcon(R.drawable.ic_information_white_24dp)
                    .setContentTitle("Please open GreenHub")
                    .setContentText("Please open GreenHub. Samples to send:")
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
