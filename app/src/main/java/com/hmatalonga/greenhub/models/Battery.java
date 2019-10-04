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
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import com.hmatalonga.greenhub.Config;
import com.hmatalonga.greenhub.managers.storage.GreenHubDb;
import com.hmatalonga.greenhub.models.data.BatteryUsage;
import com.hmatalonga.greenhub.util.LogUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.ArrayList;

import io.realm.RealmResults;

import static com.hmatalonga.greenhub.util.LogUtils.logI;
import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;
import static java.lang.Math.abs;
import static java.lang.Math.max;

/**
 * Battery.R
 */
public class Battery {
    private static final String TAG = makeLogTag(Battery.class);

    /**
     * Obtains the current battery voltage value.
     *
     * @param context Application context
     * @return Returns the battery voltage
     */
    public static double getBatteryVoltage(final Context context) {
        Intent receiver = context.registerReceiver(
                null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        );

        if (receiver == null) return 0;

        double voltage = receiver.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);

        return (voltage == 0) ? voltage : voltage / 1000;
    }

    /**
     * Get the battery capacity at the moment (in %, from 0-100)
     *
     * @param context Application context
     * @return Battery capacity (in %, from 0-100)
     */
    public static int getBatteryCapacity(final Context context) {
        int value = 0;

        BatteryManager manager = (BatteryManager)
                context.getSystemService(Context.BATTERY_SERVICE);
        if (manager != null) {
            value = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }

        if (value != 0 && value != Integer.MIN_VALUE) {
            return value;
        }

        return 0;
    }

    public static int getBatteryDesignCapacity(final Context context) {
        Object mPowerProfile;
        double batteryCapacity = 0;
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class)
                    .newInstance(context);

            batteryCapacity = (double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getBatteryCapacity")
                    .invoke(mPowerProfile);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return (int) batteryCapacity;
    }

    /**
     * Get the battery full capacity (charge counter) in mAh.
     * Since Power (W) = (Current (A) * Voltage (V)) <=> Power (Wh) = (Current (Ah) * Voltage (Vh)).
     * Therefore, Current (mA) = Power (mW) / Voltage (mV)
     *
     * @param context Application context
     * @return Battery full capacity (in mAh)
     */
    public static int getBatteryChargeCounter(final Context context) {
        int value = 0;

        BatteryManager manager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (manager != null) {
            value = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
        }

        return value;

//        if (value <= 0) {
//            value = getBatteryPropertyLegacy(Config.BATTERY_CHARGE_FULL);
//        }
//
//        if (value != 0 && value != Integer.MIN_VALUE) {
//            return value;
//        } else {
//            // in uAh
//            int chargeFullDesign =
//                    getBatteryPropertyLegacy(Config.BATTERY_ENERGY_FULL_DESIGN) / 1000000;
//            int chargeFull = chargeFullDesign != 0 ?
//                    chargeFullDesign :
//                    getBatteryPropertyLegacy(Config.BATTERY_ENERGY_FULL) / 1000000;
//
//            // in mAh
//            return (chargeFull != 0) ? chargeFull : -1;
//        }
    }

    public static int getBatteryCurrentAverage(final Context context) {
        int value = 0;

        BatteryManager manager = (BatteryManager)
                context.getSystemService(Context.BATTERY_SERVICE);
        if (manager != null) {
            value = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
        }

        return (value != 0 && value != Integer.MIN_VALUE) ? value : 0;
    }

    /**
     * Get the Battery current at the moment (in mA)
     *
     * @param context Application context
     * @return battery current now (in mA)
     */
    public static int getBatteryCurrentNow(final Context context) {
        int value = 0;

        BatteryManager manager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (manager != null) {
            value = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        }

        return (value != 0 && value != Integer.MIN_VALUE) ? value : 0;
    }

    /**
     * Get the Battery current at the moment (in mA)
     *
     * @param context Application context
     * @return battery current now (in mA)
     */
    public static double getBatteryCurrentNowInAmperes(final Context context) {
        int value = 0;

        BatteryManager manager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (manager != null) {
            value = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        }

        value = (value != 0 && value != Integer.MIN_VALUE) ? value : 0;

        return (double) value / 1000000;
    }

    /**
     * Get the battery energy counter capacity (in mWh)
     *
     * @param context Application context
     * @return battery energy counter (in mWh)
     */
    public static long getBatteryEnergyCounter(final Context context) {
        long value = 0;

        BatteryManager manager = (BatteryManager)
                context.getSystemService(Context.BATTERY_SERVICE);
        if (manager != null) {
            value = manager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER);
        }

//        if (Build.VERSION.SDK_INT < 21 || value == Long.MIN_VALUE) {
//            value = getBatteryPropertyLegacy(Config.BATTERY_ENERGY_NOW);
//        }

        return value;  // in mWh
    }

    /**
     * Calculate Average Power
     * Average Power = (Average Voltage * Average Current) / 1e9
     *
     * @param context Context of application
     * @return Average power in integer
     */
    public static int getBatteryAveragePower(final Context context) {
        int voltage;
        int current = 0;

        Intent receiver =
                context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        if (receiver == null) return -1;

        voltage = receiver.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);

        BatteryManager manager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (manager != null) {
            current = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
        }

        return (voltage * current) / 1000000000;
    }

    /**
     * Calculate Battery Capacity Consumed
     * Battery Capacity Consumed = (Average Current * Workload Duration) / 1e3
     *
     * @param workload Workload duration (in hours)
     * @param context  Context of application
     * @return Average power in integer
     */
    public static double getBatteryCapacityConsumed(final double workload, final Context context) {
        int current = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager manager = (BatteryManager)
                    context.getSystemService(Context.BATTERY_SERVICE);
            current = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
        }

        return (current * workload) / 1000;
    }

    /**
     * Calculates the battery's remaining energy capacity
     *
     * @param context the context of application
     * @return the battery remaining capacity, in mAh, as Integer
     */
    public static int getBatteryRemainingCapacity(final Context context) {
        double remainingCapacity;
        long capacity = getBatteryCapacity(context);  // in %
        if (capacity <= -1) {
            capacity = 0;
        }

        long chargeCounter = getBatteryChargeCounter(context);
        if (chargeCounter <= -1) {
            chargeCounter = abs(getBatteryDesignCapacity(context));  // in mAh
        }

        if (capacity > 0 && chargeCounter > 0) {
            remainingCapacity = (double) ((chargeCounter * capacity) / 100);
        } else {
            double voltageNow = max(1, getBatteryVoltage(context));
            long energyCounter = getBatteryEnergyCounter(context);
            if (energyCounter <= -1) {
                energyCounter = 0;
            }
            return (int) (energyCounter / voltageNow);
        }

        return (int) remainingCapacity;
    }

    /**
     * Calculate Remaining Battery Time (in hours) - An Estimate
     * Remaining Battery Life [h] ->
     * Battery Remaining Capacity [mAh/mWh] / Battery Present Drain Rate [mA/mW]
     *
     * @param context Context of application
     * @return Remaining Time (in hours)
     */
    @Deprecated
    public static double getRemainingBatteryTimeEstimate(final Context context) {
        double remainingCapacity = getBatteryRemainingCapacity(context);
        //in mA
        double currentNow = getBatteryCurrentNow(context) != -1 ?
                abs(getBatteryCurrentNow(context)) : 0;

        if (remainingCapacity > 0 && currentNow > 0) {
            return (remainingCapacity / currentNow);
        }

        return -1;
    }

    /**
     * Calculates an estimate, in seconds, for the remaining battery time,
     * or for the remaining time to fully charge the battery.
     *
     * @param context  Context of application
     * @param charging If true, the method returns the expected time until full charge
     * @return remaining battery time in seconds
     */
    public static long getRemainingBatteryTime(final Context context, boolean charging,
                                               String charger) {
        double remainingCapacity;
        double chargingSignal;
        int defaultDischargeRate = Config.DEFAULT_DISCHARGE_RATE;

        if (!charging) {
            chargingSignal = -1;
            remainingCapacity = getBatteryRemainingCapacity(context);
            logI("WOW", "[B] RemCap: " + remainingCapacity);
        } else {
            chargingSignal = 1;
            switch (charger) {
                case "usb":
                    defaultDischargeRate = Config.DEFAULT_USB_CHARGE_RATE;
                    break;
                case "ac":
                    defaultDischargeRate = Config.DEFAULT_AC_CHARGE_RATE;
                    break;
                case "wireless":
                    defaultDischargeRate = Config.DEFAULT_WIRELESS_CHARGE_RATE;
                    break;
                default:
                    break;
            }

            int fullCapacity = getBatteryChargeCounter(context) != -1 ?
                    getBatteryChargeCounter(context) : getBatteryDesignCapacity(context);
            remainingCapacity = fullCapacity - getBatteryRemainingCapacity(context);
        }

        GreenHubDb database = new GreenHubDb();

        RealmResults<BatteryUsage> allUsages = database.getUsages();
        int limit = Math.min(Config.BATTERY_CAPACITY_SAMPLES_SIZE, allUsages.size());
        if (limit <= 1) {
            // no samples collected yet
            // consider a naive value
            LogUtils.logI(TAG, "Not enough samples yet in the DB." +
                    "Assuming a blind estimation of battery remaining time.");
            return ((int) ((remainingCapacity * (60 * 60)) / defaultDischargeRate));
        }

        LogUtils.logI(TAG, "Estimating battery remaining time using " + limit + " samples");
        ArrayList<BatteryUsage> lastUsages = new ArrayList<>(allUsages.subList(0, limit));
        ArrayList<Double> dischargeSamples = new ArrayList<>();
        BatteryUsage previousUsage = null;

        for (BatteryUsage currentUsage : lastUsages) {
            if (previousUsage == null) {
                previousUsage = currentUsage;
                continue;
            }
            int currentCapacity = currentUsage.details.remainingCapacity;
            int previousCapacity = previousUsage.details.remainingCapacity;
            double discharge = chargingSignal * (previousCapacity - currentCapacity);

            // prevent division by zero OR a negative charge/discharge:
            // i.e., only positive differences are considered
            if (discharge <= 0.0) {
                previousUsage = currentUsage;
                continue;
            }
            // in seconds
            long elapsedTime = abs(
                    (currentUsage.timestamp - previousUsage.timestamp) / 1000
            );
            dischargeSamples.add(elapsedTime / discharge);

            previousUsage = currentUsage;
        }
        database.close();

        if (dischargeSamples.size() == 0) {
            return ((int) ((remainingCapacity * (60 * 60)) / defaultDischargeRate));
        }

        double sumDischarges = 0;
        for (Double discharge : dischargeSamples) {
            sumDischarges += discharge;
        }
        double dischargeRatio = sumDischarges / dischargeSamples.size();
        return (long) (remainingCapacity * dischargeRatio);

    }

    private static int getBatteryCurrentNowLegacy() {
        int value = 0;

        try {
            RandomAccessFile reader = new RandomAccessFile(Config.BATTERY_SOURCE_DEFAULT, "r");
            String average = reader.readLine();
            value = Integer.parseInt(average);
            reader.close();
        } catch (IOException e) {
            // Device has no current_avg file available
            logI(TAG, "Device has no current_avg file available");
        }
        return value;
    }

    private static int getBatteryPropertyLegacy(String property) {
        int value = 0;
        BufferedReader reader;
        File file = new File(Config.BATTERY_STATS_SOURCE_DEFAULT);
        if (file.exists()) {
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line = reader.readLine();
                while (line != null && value == 0) {
                    if (line.matches(property + ".*")) {
                        String[] splittedLine = line.split("=");
                        if (splittedLine.length == 2) {
                            value = Integer.parseInt(splittedLine[1]);
                        }
                    }
                    line = reader.readLine();
                }
            } catch (IOException e) {
                LogUtils.logI(TAG, "Could not read from standard battery stats file");
            }
        } else {
            LogUtils.logI(TAG, "Standard battery stats file does not exist or is not accessible");
        }
        return value;
    }

    public static void logAllBatteryValues(final Context context) {
        BatteryManager manager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);

        LogUtils.logI("Battery Voltage", "v: " + getBatteryVoltage(context));
        if (Build.VERSION.SDK_INT >= 21) {
            LogUtils.logI("[API] Battery Capacity", "v: " +
                    manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
            LogUtils.logI("[API] Battery Charge Counter", "v: " +
                    manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER));
            LogUtils.logI("[API] Battery Energy Counter", "v: " +
                    manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER));
            LogUtils.logI("[API] Battery Current Average", "v: " +
                    manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE));
            LogUtils.logI("[API] Battery Current Now", "v: " +
                    manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW));
        }

        LogUtils.logI("Battery Capacity", "v: " +
                getBatteryPropertyLegacy(Config.BATTERY_CAPACITY));
        LogUtils.logI("Battery Charge Counter", "v: " +
                getBatteryPropertyLegacy(Config.BATTERY_CHARGE_FULL));
        LogUtils.logI("Battery Charge Counter (Design)", "v: " +
                getBatteryPropertyLegacy(Config.BATTERY_CHARGE_FULL_DESIGN));
        LogUtils.logI("Battery Energy Counter", "v: " +
                getBatteryPropertyLegacy(Config.BATTERY_ENERGY_FULL));
        LogUtils.logI("Battery Energy Counter (Design)", "v: " +
                getBatteryPropertyLegacy(Config.BATTERY_ENERGY_FULL_DESIGN));
        LogUtils.logI("Battery Current Now", "v: " +
                getBatteryPropertyLegacy(Config.BATTERY_CURRENT_NOW));
        LogUtils.logI("Battery Current Now (2)", "v: " +
                getBatteryCurrentNowLegacy());
        LogUtils.logI("Battery Energy Now", "v: " +
                getBatteryPropertyLegacy(Config.BATTERY_ENERGY_NOW));

        // Reflections
        LogUtils.logI("Actual Battery Capacity", "v: " + getBatteryDesignCapacity(context));
    }
}
