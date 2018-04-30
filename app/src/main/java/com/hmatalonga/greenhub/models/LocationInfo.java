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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;

import java.util.LinkedList;
import java.util.List;

import com.hmatalonga.greenhub.Config;
import com.hmatalonga.greenhub.models.data.LocationProvider;
import com.hmatalonga.greenhub.util.LogUtils;

import static com.hmatalonga.greenhub.util.LogUtils.logD;
import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * LocationInfo properties model.
 */
public class LocationInfo {

    private static final String TAG = makeLogTag(LocationInfo.class);

    private static Location lastKnownLocation = null;

    /**
     * Return distance between <code>lastKnownLocation</code> and a newly
     * obtained location from any available provider.
     *
     * @param context from Intent or Application.
     * @return
     */
    public static double getDistance(Context context) {
        Location location = getLastKnownLocation(context);
        double distance = 0.0;
        if (lastKnownLocation != null && location != null) {
            distance = lastKnownLocation.distanceTo(location);
        }
        lastKnownLocation = location;
        return distance;
    }

    private static Location getLastKnownLocation(Context context) {
        String provider = getBestProvider(context);
        // FIXME: Some buggy device is giving GPS to us, even though we cannot
        // use it.
        if (provider != null && !provider.equals("gps")) {
            return getLastKnownLocation(context, provider);
        }
        return null;
    }

    private static Location getLastKnownLocation(Context context, String provider) {
        try {
            LocationManager manager = (LocationManager)
                    context.getSystemService(Context.LOCATION_SERVICE);
            return manager.getLastKnownLocation(provider);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Get the distance users between two locations */
    public static double getDistance(double startLatitude,
                                     double startLongitude,
                                     double endLatitude,
                                     double endLongitude) {
        float[] results = new float[1];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
        return results[0];
    }

    /**
     * Get location as coarse latitude and longitude.
     * IMPORTANT: Use only with user's consent!
     *
     * @param context Application context
     * @return Comma-separated latitude and longitude
     */
    public static String getCoarseLocation(Context context) {
        String provider = getBestProvider(context);
        double latitude, longitude;
        LocationManager manager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);

        if (provider != null && !provider.equals("gps")) {
            try {
                Location location = manager.getLastKnownLocation(provider);
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                return String.valueOf(latitude) + "," + String.valueOf(longitude);
            } catch (SecurityException e) {
                if (Config.DEBUG) {
                    logD("SamplingLibrary", "Failed getting coarse location!");
                    e.printStackTrace();
                }
            }
        }

        return "Unknown";
    }

    /**
     * Return a list of enabled LocationProviders, such as GPS, Network, etc.
     *
     * @param context from onReceive or app.
     * @return
     */
    public static List<LocationProvider> getEnabledLocationProviders(Context context) {
        LocationManager manager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);

        List<String> providers = manager.getProviders(true);
        List<LocationProvider> locationProviders = new LinkedList<>();

        for (String provider : providers) {
            locationProviders.add(new LocationProvider(provider));
        }

        return locationProviders;
    }

    public static String getBestProvider(Context context) {
        LocationManager manager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        return manager.getBestProvider(criteria, true);
    }

    /* Check the maximum number of satellites can be used in the satellite list */
    @Deprecated
    public static int getMaxNumSatellite(Context context) {
        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // TODO: GpsStatus is deprecated!
        // return locationManager.getGpsStatus(null).getMaxSatellites();
        return 0;
    }

    /* Get the current location of the device */
    public static CellLocation getDeviceLocation(Context context) {
        TelephonyManager manager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);

        return manager.getCellLocation();
    }

    /**
     * Returns a two letter ISO3166-1 alpha-2 standard country code
     * https://en.wikipedia.org/wiki/ISO_3166-1
     * GetCellLocation returns longitude and latitude
     *
     * @param context Application context
     * @return Two letter country code
     */
    public static String getCountryCode(Context context) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String cc;
        if (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
            /// Network location is most accurate
            cc = telephonyManager.getNetworkCountryIso();
            if (cc != null && cc.length() == 2) return cc;
            cc = getCountryCodeFromProperty(context, "gsm.operator.numeric");
            if (cc != null && cc.length() == 2) return cc;
            cc = telephonyManager.getSimCountryIso();
            if (cc != null && cc.length() == 2) return cc;
        } else {
            // Telephony manager is unreliable with CDMA
            cc = getCountryCodeFromProperty(context, "ro.cdma.home.operator.numeric");
            if (cc != null && cc.length() == 2) return cc;
        }
        return "Unknown";
    }

    /**
     * Retrieves a two-letter country code using an undocumented system properties call.
     * WARNING: Uses reflection, data might not always be available.
     *
     * @param context  Application context
     * @param property Property name
     * @return Two-letter country code
     */
    private static String getCountryCodeFromProperty(Context context, String property) {
        try {
            String operator = Specifications.getSystemProperty(context, property);
            if (operator != null && operator.length() >= 5) {
                int mcc = Integer.parseInt(operator.substring(0, 3));
                return Phone.getCountryCodeForMcc(context, mcc);
            }
        } catch (Exception e) {
            if (Config.DEBUG && e != null && e.getLocalizedMessage() != null) {
                logD(TAG, "Failed getting network location: " + e.getLocalizedMessage());
            }
        }
        return null;
    }
}
