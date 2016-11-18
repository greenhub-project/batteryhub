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

package hmatalonga.greenhub.models;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Network.
 */
public class Network {

    private static final String TAG = "Network";

    // Network status constants
    public static final String NETWORKSTATUS_DISCONNECTED = "disconnected";
    public static final String NETWORKSTATUS_DISCONNECTING = "disconnecting";
    public static final String NETWORKSTATUS_CONNECTED = "connected";
    public static final String NETWORKSTATUS_CONNECTING = "connecting";
    // Network type constants
    public static final String TYPE_UNKNOWN = "unknown";

    /**
     * Get the network status, one of connected, disconnected, connecting, or disconnecting.
     *
     * @param context the Context.
     * @return the network status, one of connected, disconnected, connecting, or disconnecting.
     */
    public static String getStatus(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) return NETWORKSTATUS_DISCONNECTED;

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo == null) return NETWORKSTATUS_DISCONNECTED;

        NetworkInfo.State state = networkInfo.getState();

        switch (state) {
            case CONNECTED:
                return NETWORKSTATUS_CONNECTED;
            case DISCONNECTED:
                return NETWORKSTATUS_DISCONNECTED;
            case CONNECTING:
                return NETWORKSTATUS_CONNECTING;
            case DISCONNECTING:
                return NETWORKSTATUS_DISCONNECTING;
            default:
                return NETWORKSTATUS_DISCONNECTING;
        }
    }

    /**
     * Get the network type, for example Wifi, mobile, wimax, or none.
     */
    public static String getType(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) return TYPE_UNKNOWN;

        NetworkInfo info = manager.getActiveNetworkInfo();

        return (info == null) ? TYPE_UNKNOWN : info.getTypeName();
    }

    /**
     * Returns true if the Internet is reachable.
     *
     * @param context the Context
     * @return true if the Internet is reachable.
     */
    public static boolean isAvailable(Context context) {
        String network = getStatus(context);
        return network.equals(NETWORKSTATUS_CONNECTED);
    }
}
