package hmatalonga.greenhub.sampling;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.List;

import hmatalonga.greenhub.Constants;

/**
 * Created by hugo on 09-04-2016.
 */
public class BatteryEstimator extends WakefulBroadcastReceiver implements LocationListener {
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
            List<String> providers = Inspector.getEnabledLocationProviders(context);
            if (providers != null) {
                for (String provider : providers)
                    lm.requestLocationUpdates(provider, Constants.FRESHNESS_TIMEOUT, 0, this);
            }
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        present = intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
        status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
        technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
        temperature = (float) (intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10);
        voltage = (float) (intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000);

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
                lastKnownLocation = Inspector.getLastKnownLocation(context);

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

        health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        present = batteryStatus.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
        status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
        technology = batteryStatus.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
        temperature = (float) (batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10);
        voltage = (float) (batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000);
    }

    public double currentBatteryLevel(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        assert batteryStatus != null;

        level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

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
