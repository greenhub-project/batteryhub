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

package hmatalonga.greenhub.receivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.greenrobot.eventbus.EventBus;

import hmatalonga.greenhub.events.RefreshEvent;
import hmatalonga.greenhub.network.CommunicationManager;
import hmatalonga.greenhub.tasks.ServerStatusTask;
import hmatalonga.greenhub.util.SettingsUtils;

import static hmatalonga.greenhub.util.LogUtils.LOGI;
import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * ConnectivityReceiver.
 */
public class ConnectivityReceiver extends BroadcastReceiver {

    private static final String TAG = makeLogTag("ConnectivityReceiver");

    /**
     * Used to start update network status.
     *
     * @param context the context
     * @param intent the intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int state;

        switch (action) {
            case ConnectivityManager.CONNECTIVITY_ACTION:
                ConnectivityManager cm =
                        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();

                if (!isConnected) {
                    EventBus.getDefault().post(new RefreshEvent("wifi", false));
                    EventBus.getDefault().post(new RefreshEvent("mobile", false));
                    return;
                }

                // Reset upload uploadAttempts counter on network state change
                CommunicationManager.uploadAttempts = 0;

                // Check if Server url is stored
                if (!SettingsUtils.isServerUrlPresent(context)) {
                    new ServerStatusTask().execute(context);
                }

                if (CommunicationManager.isQueued) {
                    CommunicationManager manager = new CommunicationManager(context, true);
                    manager.sendSamples();
                    CommunicationManager.isQueued = false;
                }

                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    EventBus.getDefault().post(new RefreshEvent("wifi", true));
                }
                else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    EventBus.getDefault().post(new RefreshEvent("mobile", true));
                }
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_ON) {
                    EventBus.getDefault().post(new RefreshEvent("bluetooth", true));
                } else {
                    EventBus.getDefault().post(new RefreshEvent("bluetooth", false));
                }
                break;
        }
    }
}
