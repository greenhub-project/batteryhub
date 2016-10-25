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
 * Verifies connection to the Internet
 * Created by hugo on 06-03-2016.
 */
public class NetworkWatcher {
    private static final String TAG = "NetworkWatcher";
    private static boolean mobileDataAllowed = false;
    private static String urlTest = "google.com";
    private static boolean response = false;

    public NetworkWatcher() {}

    public static boolean isMobileDataAllowed() {
        return mobileDataAllowed;
    }

    public static void setMobileDataAllowed(boolean mobileDataAllowed) {
        NetworkWatcher.mobileDataAllowed = mobileDataAllowed;
    }

    public static String getUrlTest() {
        return urlTest;
    }

    public static void setUrlTest(String urlTest) {
        NetworkWatcher.urlTest = urlTest;
    }

    private static boolean isInternetAvailable() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress ipAddr = InetAddress.getByName(getUrlTest());
                    response = !ipAddr.equals("");

                } catch (Exception e) {
                    response = false;
                }
            }
        });

        t.start();
        try {
            t.join();
        } catch (Exception e) {
            response = false;
        }

        return response;
    }

    public static boolean hasInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return isInternetAvailable();
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                return isMobileDataAllowed() && isInternetAvailable();
            }
        }
        return false;
    }
}
