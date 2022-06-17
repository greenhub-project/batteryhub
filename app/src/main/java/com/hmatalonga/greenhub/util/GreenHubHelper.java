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

package com.hmatalonga.greenhub.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

/**
 * App Helper class.
 *
 * Created by hugo on 24-03-2016.
 */
public class GreenHubHelper {

    /**
     * Return a Drawable that contains an app icon for the named app. If not
     * found, return the Drawable for the GreenHub icon.
     *
     * @param appName
     *            the application name
     * @return the Drawable for the application's icon
     */
    public static Drawable iconForApp(final Context context, String appName) {
        try {
            return context.getPackageManager().getApplicationIcon(appName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon);
        }
    }

    /**
     * Return a human readable application label for the named app. If not
     * found, return appName.
     *
     * @param appName the application name
     * @return the human readable application label
     */
    public static String labelForApp(final Context context, String appName) {
        if (appName == null) return "Unknown";
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(appName, 0);
            if (info != null) {
                return context.getPackageManager().getApplicationLabel(info).toString();
            } else {
                return appName;
            }
        } catch (PackageManager.NameNotFoundException exception) {
            return appName;
        }
    }
}
