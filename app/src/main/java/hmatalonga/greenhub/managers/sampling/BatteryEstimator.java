/*
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

package hmatalonga.greenhub.managers.sampling;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.List;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.GreenHubHelper;
import hmatalonga.greenhub.models.LocationInfo;

/**
 * Provides current Mobile status
 * Created by hugo on 09-04-2016.
 */
public class BatteryEstimator extends WakefulBroadcastReceiver implements LocationListener {
    private static final String TAG = "Estimator";
    public static final int MAX_SAMPLES = 250;

    private static BatteryEstimator instance = null;
    private Context context = null;
    private Location lastKnownLocation = null;
    private double distance = 0.0;
    private long lastNotify;

    private int health;
    private int level;
    private int plugged;
    private boolean present;
    private int scale;
    private int status;
    private String technology;
    private float temperature;
    private float voltage;

    public static BatteryEstimator getInstance() {
        if (instance == null)
            BatteryEstimator.instance = new BatteryEstimator();
        return instance;
    }

    private void requestLocationUpdates() {
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            lm.removeUpdates(this);
            List<String> providers = LocationInfo.getEnabledLocationProviders(context);
            if (providers != null) {
                for (String provider : providers)
                    lm.requestLocationUpdates(provider, Config.FRESHNESS_TIMEOUT, 0, this);
            }
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
            plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            present = intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
            status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
            technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
            temperature = (float) (intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10);
            voltage = (float) (intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }

        /* On some phones, scale is always 0. */
        if (scale == 0)
            scale = 100;
        if (level > 0) {
            Inspector.setCurrentBatteryLevel(level, scale);

            if (this.context == null) {
                this.context = context;
                requestLocationUpdates();
            }

            // Update last known location...
            if (lastKnownLocation == null)
                lastKnownLocation = LocationInfo.getLastKnownLocation(context);

            Intent service = new Intent(context, BatteryEstimatorService.class);
            service.putExtra("OriginalAction", intent.getAction());
            service.fillIn(intent, 0);
            service.putExtra("distance", distance);
            startWakefulService(context, service);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (lastKnownLocation != null && location != null)
            distance = lastKnownLocation.distanceTo(location);
        lastKnownLocation = location;
    }

    @Override
    public void onProviderDisabled(String provider) {
        requestLocationUpdates();
    }

    @Override
    public void onProviderEnabled(String provider) {
        requestLocationUpdates();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        requestLocationUpdates();
    }

    public void getCurrentStatus(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        assert batteryStatus != null;

        level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        present = batteryStatus.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
        status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
        technology = batteryStatus.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
        temperature = (float) (batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10);
        voltage = (float) (batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000);

        /* On some phones, scale is always 0. */
        if (scale == 0)
            scale = 100;
        if (level > 0) {
            Inspector.setCurrentBatteryLevel(level, scale);

            if (this.context == null) {
                this.context = context;
                requestLocationUpdates();
            }

            // Update last known location...
            if (lastKnownLocation == null)
                lastKnownLocation = LocationInfo.getLastKnownLocation(context);

            Intent service = new Intent(context, BatteryEstimatorService.class);
            service.putExtra("distance", distance);
            startWakefulService(context, service);
        }
    }

    public double currentBatteryLevel() {
        Log.d(TAG, "currentBatteryLevel() called.");

        Thread t = new Thread() {
            public void run() {
                if (context == null)
                    context = GreenHubHelper.getContext();

                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = context.registerReceiver(null, ifilter);
                assert batteryStatus != null;

                level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            }
        };
        t.start();

        // Force thread to finish before calculating battery level
        // Otherwise it can return 0
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        double result = (getLevel() / (float) getScale()) * 100;
        return Math.round(result * 100.0) / 100.0;
    }

    public String getHealthStatus() {
        String status = "";
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                status = "Unknown";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                status = "Good";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                status = "Overheat";
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                status = "Dead";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                status = "Over Voltage";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                status = "Unspecified Failure";
                break;
        }

        return status;
    }

    public long getLastNotify() {
        return lastNotify;
    }

    public void setLastNotify(long now) {
        this.lastNotify = now;
    }

    public int getHealth() {
        return health;
    }

    public int getLevel() {
        return level;
    }

    public int getPlugged() {
        return plugged;
    }

    public boolean isPresent() {
        return present;
    }

    public int getScale() {
        return scale;
    }

    public int getStatus() {
        return status;
    }

    public String getTechnology() {
        return technology;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getVoltage() {
        return voltage;
    }
}
