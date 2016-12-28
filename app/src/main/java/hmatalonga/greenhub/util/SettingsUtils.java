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
     * Boolean indicating whether to send installed packages with the samples.
     */
    public static final String PREF_SEND_INSTALLED_PACKAGES = "pref_send_installed";

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

    public static void markInstalledPackagesIncluded(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_SEND_INSTALLED_PACKAGES, newValue).apply();
    }

    public static boolean isInstalledPackagesIncluded(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SEND_INSTALLED_PACKAGES, false);
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
