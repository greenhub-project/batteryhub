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

package com.hmatalonga.greenhub.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hmatalonga.greenhub.BuildConfig;
import com.hmatalonga.greenhub.Config;
import com.hmatalonga.greenhub.ui.TaskListActivity;
import com.hmatalonga.greenhub.ui.WelcomeActivity;

/**
 * SettingsUtils.
 */
public class SettingsUtils {

    private static final String TAG = "SettingsUtils";

    // region Preferences Declarations

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
     * Boolean indicating whether to display battery charging/level alerts.
     */
    public static final String PREF_CHARGE_ALERTS = "pref_charge_alerts";
    /**
     * Boolean indicating whether to display battery temperature alerts.
     */
    public static final String PREF_TEMPERATURE_ALERTS = "pref_temperature_alerts";
    /**
     * Integer indicating which temperature interval rate to use.
     */
    public static final String PREF_TEMPERATURE_RATE = "pref_temperature_rate";
    /**
     * Long integer indicating when was send the last battery temperature alert.
     */
    public static final String PREF_LAST_TEMPERATURE_ALERT = "pref_last_temperature_alert";
    /**
     * Integer indicating which temperature warning value in celsius degrees to use.
     */
    public static final String PREF_TEMPERATURE_WARNING = "pref_temperature_warning";
    /**
     * Integer indicating which temperature critical value in celsius degrees to use.
     */
    public static final String PREF_TEMPERATURE_HIGH = "pref_temperature_high";

    /**
     * Boolean indicating whether to display battery alerts.
     */
    public static final String PREF_MESSAGE_ALERTS = "pref_message_alerts";
    /**
     * String indicating the app version.
     */
    public static final String PREF_APP_VERSION = "pref_app_version";
    /**
     * Integer indicating the last message id received.
     */
    public static final String PREF_MESSAGE_LAST_ID = "pref_message_last";
    /**
     * Boolean indicating whether to hide system apps or not.
     */
    public static final String PREF_HIDE_SYSTEM_APPS = "pref_system_apps";
    /**
     * Boolean indicating whether to use the old measurement or not.
     */
    public static final String PREF_USE_OLD_MEASUREMENT = "pref_old_measurement";

    // endregion

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
        return sp.getBoolean(PREF_POWER_INDICATOR, false);
    }

    /**
     * Return true if battery alerts are to be shown, false if hidden.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isBatteryAlertsOn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_BATTERY_ALERTS, true);
    }

    /**
     * Return true if charge related alerts are to be shown, false if hidden.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isChargeAlertsOn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_CHARGE_ALERTS, true);
    }

    /**
     * Return true if temperature related alerts are to be shown, false if hidden.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isTemperatureAlertsOn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_TEMPERATURE_ALERTS, true);
    }

    public static int fetchTemperatureAlertsRate(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(
                sp.getString(PREF_TEMPERATURE_RATE, Config.NOTIFICATION_DEFAULT_TEMPERATURE_RATE)
        );
    }

    public static int fetchTemperatureWarning(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(
                sp.getString(
                        PREF_TEMPERATURE_WARNING,
                        Config.NOTIFICATION_DEFAULT_TEMPERATURE_WARNING
                ).replaceFirst("^0+(?!$)", "")
        );
    }

    public static int fetchTemperatureHigh(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(
                sp.getString(
                        PREF_TEMPERATURE_HIGH,
                        Config.NOTIFICATION_DEFAULT_TEMPERATURE_HIGH
                ).replaceFirst("^0+(?!$)", "")
        );
    }

    /**
     * Save the new last time of battery temperature alert.
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param time New value that will be set.
     */
    public static void saveLastTemperatureAlertDate(final Context context, long time) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_LAST_TEMPERATURE_ALERT, time).apply();
    }

    /**
     * Return the time in millis of the last battery temperature alert
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static long fetchLastTemperatureAlertDate(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_TEMPERATURE_ALERT, 0);
    }

    /**
     * Return true if message alerts are to be shown, false if hidden.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isMessageAlertsOn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_MESSAGE_ALERTS, true);
    }

    /**
     * Save the message {@code id}.
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param id New value that will be set.
     */
    public static void saveLastMessageId(final Context context, int id) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREF_MESSAGE_LAST_ID, id).apply();
    }

    /**
     * Fetch the last message {@code id}.
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static int fetchLastMessageId(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_MESSAGE_LAST_ID, Config.STARTER_MESSAGE_ID);
    }

    /**
     * Return true if system apps are hidden
     * {@link TaskListActivity Setting}, false if they aren't (yet).
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isSystemAppsHidden(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_HIDE_SYSTEM_APPS, true);
    }
    /**
     * Mark {@code newValue whether} system apps to be hidden from list.
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void markSystemAppsHidden(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_HIDE_SYSTEM_APPS, newValue).apply();
    }

    /**
     * Return true if old measurement method is being used, false if it isn't.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isOldMeasurementUsed(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_USE_OLD_MEASUREMENT, false);
    }
    /**
     * Mark {@code newValue whether} old measurement method to be used.
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void markOldMeasurementUsed(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_USE_OLD_MEASUREMENT, newValue).apply();
    }

    /**
     * Save the most recent app version {@code version}.
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param version New value that will be set.
     */
    public static void saveAppVersion(final Context context, int version) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREF_APP_VERSION, version).apply();
    }

    /**
     * Fetch the most recent app version {@code version}.
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static int fetchAppVersion(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_APP_VERSION, BuildConfig.VERSION_CODE);
    }

    // region Listeners

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

    // endregion
}
