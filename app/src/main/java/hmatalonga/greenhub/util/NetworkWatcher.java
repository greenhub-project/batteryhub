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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.InetAddress;

/**
 * Verifies connection to the Internet.
 *
 * Created by hugo on 06-03-2016.
 */
public class NetworkWatcher {

    private static final String TAG = "NetworkWatcher";

    public static final int BACKGROUND_TASKS = 1;

    public static final int COMMUNICATION_MANAGER = 2;

    public NetworkWatcher() {}

    /**
     * Checks for Internet connection.
     *
     * @param context Application context
     * @param mode which module is requesting Internet access
     * @return Whether or not it is connected
     */
    public static boolean hasInternet(Context context, int mode) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return activeNetwork.isConnected();
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // if communication manager is calling check for settings on mobile data
                if (mode == BACKGROUND_TASKS) {
                    return activeNetwork.isConnected();
                } else if (mode == COMMUNICATION_MANAGER) {
                    return SettingsUtils.isMobileDataAllowed(context) && activeNetwork.isConnected();
                }
            }
        }
        return false;
    }
}
