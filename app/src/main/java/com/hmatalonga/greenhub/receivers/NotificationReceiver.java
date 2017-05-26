package com.hmatalonga.greenhub.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hmatalonga.greenhub.util.LogUtils;
import com.hmatalonga.greenhub.util.Notifier;

import static com.hmatalonga.greenhub.util.LogUtils.LOGI;


public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = LogUtils.makeLogTag(NotificationReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        LOGI(TAG, "onReceive called!");
        Notifier.updateStatusBar(context);
    }
}
