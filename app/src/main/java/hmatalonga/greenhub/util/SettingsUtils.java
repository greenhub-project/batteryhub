/*
 * Copyright (c) 2015 Google Inc. All rights reserved.
 * Copyright (C) 2016 Hugo Matalonga & João Paulo Fernandes
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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.ui.WelcomeActivity;

/**
 * SettingsUtils.
 */
public class SettingsUtils {

    private static final String TAG = "SettingsUtils";

    /**
     * Boolean indicating whether ToS has been accepted.
     */
    public static final String PREF_TOS_ACCEPTED = "pref_tos_accepted";
    /**
     * Boolean indicating whether ToS has been accepted.
     */
    public static final String PREF_DEVICE_REGISTERED = "pref_device_registered";
    /**
     * String containing Web Server url.
     */
    public static final String PREF_SERVER_URL = "pref_server_url";
    /**
     * Boolean indicating whether to send installed packages with the samples.
     */
    public static final String PREF_SEND_INSTALLED_PACKAGES = "pref_send_installed";
    /**
     * Boolean indicating whether to save data on screen on/off broadcasts.
     */
    public static final String PREF_SAMPLING_SCREEN = "pref_sampling_screen";
    /**
     * Integer indicating which data history days range to keep.
     */
    public static final String PREF_DATA_HISTORY = "pref_data_history";
    /**
     * Boolean indicating whether to allow uploads using mobile data.
     */
    public static final String PREF_MOBILE_DATA = "pref_mobile_data";
    /**
     * Boolean indicating whether to allow uploads using mobile data.
     */
    public static final String PREF_AUTO_UPLOAD = "pref_auto_upload";
    /**
     * Integer indicating which upload interval rate to use.
     */
    public static final String PREF_UPLOAD_RATE = "pref_upload_rate";
    /**
     * Integer indicating which upload interval rate to use.
     */
    public static final String PREF_NOTIFICATIONS_PRIORITY = "pref_notifications_priority";
    /**
     * Boolean indicating whether to display the power indicator.
     */
    public static final String PREF_POWER_INDICATOR = "pref_power_indicator";
    /**
     * Boolean indicating whether to display battery alerts.
     */
    public static final String PREF_BATTERY_ALERTS = "pref_battery_alerts";
    /**
     * String indicating the app version.
     */
    public static final String PREF_APP_VERSION = "pref_app_version";

    /**
     * Return true if user has accepted the
     * {@link WelcomeActivity Tos}, false if they haven't (yet).
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isTosAccepted(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_TOS_ACCEPTED, false);
    }
    /**
     * Mark {@code newValue whether} the user has accepted the TOS so the app doesn't ask again.
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void markTosAccepted(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_TOS_ACCEPTED, newValue).apply();
    }

    /**
     * Mark {@code newValue whether} the device has registered in the web server
     * so the app doesn't register again.
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void markDeviceAccepted(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DEVICE_REGISTERED, newValue).apply();
    }

    /**
     * Return true if device has been registered in the web server,
     * false if they haven't (yet).
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isDeviceRegistered(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DEVICE_REGISTERED, false);
    }

    /**
     * Save {@code url} of the web server.
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param url New value that will be set.
     */
    public static void saveServerUrl(final Context context, String url) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_SERVER_URL, url).apply();
    }

    /**
     * Fetch stored {@code url} of the web server.
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static String fetchServerUrl(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_SERVER_URL, Config.SERVER_URL_DEFAULT);
    }

    public static boolean isServerUrlPresent(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return !sp.getString(PREF_SERVER_URL, Config.SERVER_URL_DEFAULT).equals(Config.SERVER_URL_DEFAULT);
    }

    public static void markInstalledPackagesIncluded(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_SEND_INSTALLED_PACKAGES, newValue).apply();
    }

    public static boolean isInstalledPackagesIncluded(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SEND_INSTALLED_PACKAGES, false);
    }

    /**
     * Return true if saving data for screen on/off is allowed, false if it is not.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isSamplingScreenOn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SAMPLING_SCREEN, false);
    }

    public static int fetchDataHistoryInterval(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(
                sp.getString(PREF_DATA_HISTORY, Config.DATA_HISTORY_DEFAULT)
        );
    }

    /**
     * Return true if mobile data is allowed to upload samples,
     * false if it is not.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isMobileDataAllowed(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_MOBILE_DATA, false);
    }

    /**
     * Return true if automatic uploading is allowed,
     * false if it is not.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isAutomaticUploadingAllowed(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_AUTO_UPLOAD, true);
    }

    public static int fetchUploadRate(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(
                sp.getString(PREF_UPLOAD_RATE, Config.UPLOAD_DEFAULT_RATE)
        );
    }


    public static int fetchNotificationsPriority(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(
                sp.getString(PREF_NOTIFICATIONS_PRIORITY, Config.NOTIFICATION_DEFAULT_PRIORITY)
        );
    }

    /**
     * Return true if power indicator is to be shown, false if it is hidden.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isPowerIndicatorShown(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_POWER_INDICATOR, true);
    }

    /**
     * Return true if power indicator is to be shown, false if it is hidden.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isBatteryAlertsOn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_BATTERY_ALERTS, true);
    }

    /**
     * Helper method to register a settings_prefs listener. This method does not automatically handle
     * {@code unregisterOnSharedPreferenceChangeListener() un-registering} the listener at the end
     * of the {@code context} lifecycle.
     *
     * @param context  Context to be used to lookup the {@link android.content.SharedPreferences}.
     * @param listener Listener to register.
     */
    public static void registerOnSharedPreferenceChangeListener(final Context context,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Helper method to un-register a settings_prefs listener typically registered with
     * {@code registerOnSharedPreferenceChangeListener()}
     *
     * @param context  Context to be used to lookup the {@link android.content.SharedPreferences}.
     * @param listener Listener to un-register.
     */
    public static void unregisterOnSharedPreferenceChangeListener(final Context context,
                                                                  SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
