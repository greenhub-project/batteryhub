package com.android.internal.os;

import android.content.Context;
import android.util.Log;

/**
 * Created by hugo on 06-03-2016.
 */
public class PowerProfileHelper {
    public static PowerProfile pp = null;

    public static double getAverageWifiPower(Context context){
        if (pp == null)
            pp = new PowerProfile(context);

        //double wifiScanCost=powCal.getAveragePower(PowerProfile.POWER_WIFI_SCAN);
        double wifiOnCost=pp.getAveragePower(PowerProfile.POWER_WIFI_ON);
        Log.i("wifiOnCost", "Wifi on cost is:" + wifiOnCost);
        double wifiActiveCost=pp.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE);
        Log.i("wifiActiveCost", "Wifi active cost is:" + wifiActiveCost);

        double alpha = 0.5;
        double wifiPowerCost=wifiOnCost*alpha+wifiActiveCost*(1-alpha);
        Log.i("wifiPowerConsumption", "Wifi power consumption is:"+wifiPowerCost);
        return wifiPowerCost;
    }

    public static double [] getAverageCpuPower(Context context){
        double result[] = new double[3];
        if (pp == null)
            pp=new PowerProfile(context);
        double cpuActiveCost=pp.getAveragePower(PowerProfile.POWER_CPU_ACTIVE);
        double cpuIdleCost=pp.getAveragePower(PowerProfile.POWER_CPU_IDLE);
        double cpuAwakeCost=pp.getAveragePower(PowerProfile.POWER_CPU_AWAKE);

        result[0]=cpuActiveCost;
        result[1]=cpuIdleCost;
        result[2]=cpuAwakeCost;
        Log.i("cpuPowerConsumption", "When cpu is active:\n"+cpuActiveCost);
        Log.i("cpuPowerConsumption", "When cpu is idle:\n"+cpuIdleCost);
        Log.i("cpuPowerConsumption", "When cpu is awake:\n"+cpuAwakeCost);
        return result;
    }
}
