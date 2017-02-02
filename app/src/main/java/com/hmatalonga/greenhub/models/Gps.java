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
import android.location.LocationManager;

/**
 * Gps.
 */
public class Gps {

    private static final String TAG = "Gps";

    /**
     * Check whether GPS are enabled.
     *
     * @param context The Context
     * @return whether or not GPS is enabled
     */
    public static boolean isEnabled(final Context context) {
        LocationManager manager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
