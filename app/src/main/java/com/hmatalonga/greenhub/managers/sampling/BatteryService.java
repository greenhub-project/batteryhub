package com.hmatalonga.greenhub.managers.sampling;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.hmatalonga.greenhub.util.SettingsUtils;

import static com.hmatalonga.greenhub.util.LogUtils.logE;
import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Created by marco on 06-03-2019.
 */

public class BatteryService extends Service {

    private static final String TAG = makeLogTag(BatteryService.class);
    public static boolean isServiceRunning = false;

    private IntentFilter mIntentFilter;

    public static DataEstimator estimator = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        isServiceRunning = true;

        estimator = new DataEstimator();

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        Context context = getApplicationContext();

        registerReceiver(estimator, mIntentFilter);

        if (SettingsUtils.isSamplingScreenOn(context)) {
            mIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
            registerReceiver(estimator, mIntentFilter);
            mIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(estimator, mIntentFilter);
        }

    }

    @Override
    public void onDestroy() {
        isServiceRunning = false;
        try {
            unregisterReceiver(estimator);
        } catch (IllegalArgumentException e) {
            logE(TAG, "Estimator receiver is not registered!");
            e.printStackTrace();
        }
        estimator = null;
    }

}
