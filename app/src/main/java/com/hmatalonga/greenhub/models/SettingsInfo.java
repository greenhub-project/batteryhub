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

import android.content.ContentResolver;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import java.util.Calendar;

/**
 * SettingsInfo.
 */
public class SettingsInfo {

    private static final String TAG = "SettingsInfo";

    /**
     * Get the current timezone of the device.
     */
    public static String getTimeZone() {
        return Calendar.getInstance().getTimeZone().getID();
    }

    /**
     *
     * @param context
     * @return true when app installation from unknown sources is enabled.
     */
    public static int allowUnknownSources(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.Secure.getInt(resolver, Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
    }

    /**
     * Check if Developer Mode is enabled on the device's settings
     *
     * @param context the Application Context
     * @return true when developer mode is enabled.
     */
    public static int isDeveloperModeOn(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        if (Build.VERSION.SDK_INT >= 17) {
            try {
                return Settings.Secure.getInt(resolver, Settings.Global.ADB_ENABLED);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }
        //noinspection deprecation
        return Settings.Secure.getInt(resolver, Settings.Secure.ADB_ENABLED, 0);
    }

    /**
     * Checks if NFC is enabled on the device
     *
     * @return True if NFC is enabled
     */
    public static boolean isNfcEnabled(final Context context) {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
        return adapter != null && adapter.isEnabled();
    }

    /**
     * Checks if PowerSave Mode is enabled on the device
     *
     * @return True if PowerSave Mode is enabled
     */
    public static boolean isPowerSaveEnabled(final Context context) {
        PowerManager manager = (PowerManager)
                context.getSystemService(Context.POWER_SERVICE);

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && manager.isPowerSaveMode();
    }
}
