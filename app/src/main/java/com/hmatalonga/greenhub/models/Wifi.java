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

package com.hmatalonga.greenhub.models;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.format.Formatter;

import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

/**
 * Wifi properties model.
 */
public class Wifi {
    // Wifi State constants
    private static final String WIFI_STATE_DISABLING = "disabling";
    private static final String WIFI_STATE_DISABLED = "disabled";
    private static final String WIFI_STATE_ENABLING = "enabling";
    private static final String WIFI_STATE_ENABLED = "enabled";
    private static final String WIFI_STATE_UNKNOWN = "unknown";

    // Wifi AP State constants
    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;

    /**
     * Get current WiFi signal Strength.
     *
     * @param context The Context
     * @return Signal strength
     */
    public static int getSignalStrength(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = manager.getConnectionInfo();
        return wifiInfo.getRssi();
    }

    /**
     * Get Wifi MAC ADDR. Hashed and used in UUID calculation.
     */
    @SuppressLint("HardwareIds")
    public static String getMacAddress(final Context context) {
        if (Build.VERSION.SDK_INT >= 23) return getMacAddressMarshmallow();

        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (manager == null) return null;

        WifiInfo wifiInfo = manager.getConnectionInfo();
        return (wifiInfo == null) ? null : wifiInfo.getMacAddress();
    }

    @SuppressWarnings("deprecation")
    public static String getIpAddress(final Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (manager == null) return null;

        WifiInfo wifiInfo = manager.getConnectionInfo();

        // TODO: Formatter.formatIpAddress() is deprecated! Replace it...
        return (wifiInfo == null) ? null : Formatter.formatIpAddress(wifiInfo.getIpAddress());
    }

    /**
     * @return
     */
    @TargetApi(23)
    private static String getMacAddressMarshmallow() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) return "";

                StringBuilder builder = new StringBuilder();
                for (byte b : macBytes) {
                    builder.append(String.format("%02X:", b));
                }

                if (builder.length() > 0) {
                    builder.deleteCharAt(builder.length() - 1);
                }
                return builder.toString();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    /**
     * Get current WiFi link speed.
     *
     * @param context The Context
     * @return Link speed of wifi connection
     */
    public static int getLinkSpeed(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = manager.getConnectionInfo();
        return wifiInfo.getLinkSpeed();
    }

    /**
     * Check whether WiFi is enabled.
     *
     * @param context The Context
     * @return true if enabled, false otherwise
     */
    public static boolean isEnabled(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return manager.isWifiEnabled();
    }

    /**
     * Get Wifi state.
     *
     * @param context The Context
     * @return Wifi state constant, an int value
     */
    public static String getState(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int wifiState = manager.getWifiState();

        switch (wifiState) {
            case WifiManager.WIFI_STATE_DISABLED:
                return WIFI_STATE_DISABLED;
            case WifiManager.WIFI_STATE_DISABLING:
                return WIFI_STATE_DISABLING;
            case WifiManager.WIFI_STATE_ENABLED:
                return WIFI_STATE_ENABLED;
            case WifiManager.WIFI_STATE_ENABLING:
                return WIFI_STATE_ENABLING;
            case WifiManager.WIFI_STATE_UNKNOWN:
                return WIFI_STATE_UNKNOWN;
            default:
                return WIFI_STATE_UNKNOWN;
        }
    }

    public static WifiInfo getInfo(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return manager.getConnectionInfo();
    }

    /**
     * @param context Application context
     * @return Wifi access point state
     */
    public static String getHotspotState(Context context) {
        try {
            int state = getWifiApState(context);
            switch (state) {
                case WIFI_AP_STATE_DISABLED:
                    return "disabled";
                case WIFI_AP_STATE_DISABLING:
                    return "disabling";
                case WIFI_AP_STATE_ENABLED:
                    return "enabled";
                case WIFI_AP_STATE_ENABLING:
                    return "enabling";
                case WIFI_AP_STATE_FAILED:
                    return "failed";
                default:
                    return "unknown";
            }
        } catch (Exception e) {
            return "unknown";
        }

    }

    /**
     * Undocumented call to check if wifi access point is enabled.
     * WARNING: Uses reflection, data might not always be available.
     *
     * @param context Application context
     * @return State integer, see WIFI_AP statics
     * @throws Exception
     */
    private static int getWifiApState(Context context) throws Exception {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        Method getWifiApState = wifiManager.getClass().getDeclaredMethod("getWifiApState");
        getWifiApState.setAccessible(true);
        return (int) getWifiApState.invoke(wifiManager);
    }
}
