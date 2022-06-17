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

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

/**
 * Screen properties model.
 */
public class Screen {
    /**
     * Get Current Screen Brightness Value.
     *
     * @param context The Context
     * @return
     */
    public static int getBrightness(Context context) {
        int screenBrightnessValue = 0;

        try {
            screenBrightnessValue = Settings.System.getInt(
                    context.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS
            );
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return screenBrightnessValue;
    }

    /**
     * @param context
     * @return
     */
    public static boolean isAutoBrightness(Context context) {
        boolean autoBrightness = false;

        try {
            int brightnessMode = Settings.System.getInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE
            );
            autoBrightness = (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return autoBrightness;
    }

    /**
     * Get whether the screen is on or off.
     *
     * @return true if the screen is on.
     */
    public static int isOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        if (powerManager == null) return 0;

        if (Build.VERSION.SDK_INT >= 20) {
            return (powerManager.isInteractive()) ? 1 : 0;
        } else {
            //noinspection deprecation
            return (powerManager.isScreenOn()) ? 1 : 0;
        }
    }
}
