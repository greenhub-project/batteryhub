/*
 * Copyright (c) 2011-2016, AMP Lab and University of Helsinki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 *
 * -------------------------------------------------------------------------------
 *
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

package com.hmatalonga.greenhub.managers.sampling;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hmatalonga.greenhub.Config;
import com.hmatalonga.greenhub.events.StatusEvent;
import com.hmatalonga.greenhub.models.Application;
import com.hmatalonga.greenhub.models.Battery;
import com.hmatalonga.greenhub.models.Bluetooth;
import com.hmatalonga.greenhub.models.Cpu;
import com.hmatalonga.greenhub.models.Gps;
import com.hmatalonga.greenhub.models.LocationInfo;
import com.hmatalonga.greenhub.models.Memory;
import com.hmatalonga.greenhub.models.Network;
import com.hmatalonga.greenhub.models.Package;
import com.hmatalonga.greenhub.models.Phone;
import com.hmatalonga.greenhub.models.Process;
import com.hmatalonga.greenhub.models.Screen;
import com.hmatalonga.greenhub.models.SettingsInfo;
import com.hmatalonga.greenhub.models.SimCard;
import com.hmatalonga.greenhub.models.Specifications;
import com.hmatalonga.greenhub.models.Storage;
import com.hmatalonga.greenhub.models.Wifi;
import com.hmatalonga.greenhub.models.data.AppPermission;
import com.hmatalonga.greenhub.models.data.BatteryDetails;
import com.hmatalonga.greenhub.models.data.BatterySession;
import com.hmatalonga.greenhub.models.data.BatteryUsage;
import com.hmatalonga.greenhub.models.data.CpuStatus;
import com.hmatalonga.greenhub.models.data.Feature;
import com.hmatalonga.greenhub.models.data.NetworkDetails;
import com.hmatalonga.greenhub.models.data.ProcessInfo;
import com.hmatalonga.greenhub.models.data.Sample;
import com.hmatalonga.greenhub.models.data.Settings;
import com.hmatalonga.greenhub.util.SettingsUtils;
import io.realm.RealmList;

import static com.hmatalonga.greenhub.util.LogUtils.LOGI;
import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Inspector instrumentation class.
 */
public final class Inspector {

    private static final String TAG = makeLogTag(Inspector.class);

    private static final String INSTALLED = "installed:";

    private static final String REPLACED = "replaced:";

    private static final String UNINSTALLED = "uninstalled:";

    // Disabled or turned off applications will be scheduled for reporting using this prefix
    private static final String DISABLED = "disabled:";

    private static double sLastBatteryLevel = 0;

    private static double sCurrentBatteryLevel = 0;

    // we might not be able to read the current battery level at the first run
    // of GreenHub.
    // so it might be zero until we get the non-zero value from the intent
    // (BatteryManager.EXTRA_LEVEL & BatteryManager.EXTRA_SCALE)

    /**
     * Library class, prevent instantiation
     */
    private Inspector() {}

    public static double readLastBatteryLevel() {
        return sLastBatteryLevel;
    }

    public static void setLastBatteryLevel(double level) {
        sLastBatteryLevel = level;
    }

    public static double getLastBatteryLevel() {
        return sLastBatteryLevel;
    }

    public static double getCurrentBatteryLevel() {
        return sCurrentBatteryLevel;
    }

    public static void setCurrentBatteryLevel(double level) {
        sCurrentBatteryLevel = level;
    }

    /**
     * Take in currentLevel and scale as doubles to avoid loss of precision issues.
     * Note that GreenHub stores battery level as a value between 0 and 1, e.g. 0.45 for 45%.
     *
     * @param currentLevel Current battery level, usually in percent.
     * @param scale        Battery scale, usually 100.0.
     */
    public static void setCurrentBatteryLevel(double currentLevel, double scale) {
        /* we should multiply the result of the division below by 100.0 to get the battery level
		 * in the scale of 0-100, but since the previous samples in our server's dataset are in the scale of 0.00-1.00,
		 * we omit the multiplication. */
        double level = currentLevel / scale;
		/*
		 * whenever we get these two arguments (extras from the intent:
		 * EXTRA_LEVEL & EXTRA_SCALE), it doesn't necessarily mean that a battery
		 * percentage change has happened. Check the comments in the
		 * broadcast receiver (sampler).
		 */
        if (level != sCurrentBatteryLevel) sCurrentBatteryLevel = level;
    }

    /**
     * Safely terminate (kill) the given app.
     *
     * @param context the Application Context
     * @param packageName
     * @return
     */
    public static boolean killApp(final Context context, String packageName) {
        ActivityManager manager =
                (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        if (manager != null) {
            try {
                manager.killBackgroundProcesses(packageName);
                return true;
            } catch (Throwable th) {
                Log.e(TAG, "Could not kill process: " + packageName, th);
            }
        }
        return false;
    }

    /**
     * Returns a List of ProcessInfo objects, helper for getSample.
     *
     * @param context the Application Context.
     * @return a List of ProcessInfo objects, helper for getSample.
     */
    private static List<ProcessInfo> getRunningProcessInfoForSample(final Context context) {
        // Reset list for each sample
        Process.clear();

        List<ProcessInfo> list = Application.getRunningAppInfo(context);
        List<ProcessInfo> result = new ArrayList<>();

        PackageManager pm = context.getPackageManager();
        // Collected in the same loop to save computation.
        int[] procMem = new int[list.size()];

        Set<String> procs = new HashSet<>();

        boolean included = SettingsUtils.isInstalledPackagesIncluded(context);

        Map<String, ProcessInfo> processInfoMap =
                (included) ? Package.getInstalledPackages(context, false) : null;

        for (ProcessInfo pi : list) {
            String pName = pi.name;
            if (processInfoMap != null && processInfoMap.containsKey(pName)) {
                processInfoMap.remove(pName);
            }

            procs.add(pName);

            ProcessInfo item = new ProcessInfo();
            item.appPermissions = new RealmList<>();
            item.appSignatures = new RealmList<>();

            PackageInfo packageInfo = Package.getPackageInfo(context, pName);

            if (packageInfo != null) {
                item.versionName = packageInfo.versionName;
                item.versionCode = packageInfo.versionCode;
                ApplicationInfo info = packageInfo.applicationInfo;

                // Human readable label (if any)
                String label = pm.getApplicationLabel(info).toString();
                if (label.length() > 0) {
                    item.applicationLabel = label;
                }
                int flags = packageInfo.applicationInfo.flags;
                // Check if it is a system app
                boolean isSystemApp = (flags & ApplicationInfo.FLAG_SYSTEM) > 0;
                isSystemApp = isSystemApp || (flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0;
                item.isSystemApp = isSystemApp;

                // Get app permissions
                if (packageInfo.permissions != null) {
                    for (PermissionInfo permission : packageInfo.permissions) {
                        item.appPermissions.add(new AppPermission(permission.name));
                    }
                }

				/*
				 * boolean sigSent = p.getBoolean(SIG_SENT_256 + pname, false);
				 * if (collectSignatures && !sigSent && pak.signatures != null
				 * && pak.signatures.length > 0) { List<String> sigList =
				 * getSignatures(pak); boolean sigSentOld =
				 * p.getBoolean(SIG_SENT + pname, false); if (sigSentOld)
				 * p.edit().remove(SIG_SENT + pname);
				 * p.edit().putBoolean(SIG_SENT_256 + pname, true).commit();
				 * item.setAppSignatures(sigList); }
				 */
            }
            item.importance = pi.importance;
            item.processId = pi.processId;
            item.name = pi.name;

            String installationSource = null;
            if (!pi.isSystemApp) {
                try {
                    installationSource = pm.getInstallerPackageName(pName);
                } catch (IllegalArgumentException iae) {
                    Log.e(TAG, "Could not get installer for " + pName);
                }
            }
            if (installationSource == null) {
                installationSource = "null";
            }
            item.installationPkg = installationSource;

            // TODO: More fields will need to be added here, but ProcessInfo needs to change.
            // procMem[list.indexOf(pi)] = pi.getPId();
            // uid lru

            // add to result
            result.add(item);
        }

        // Send installed packages if we were to do so.
        if (processInfoMap != null && processInfoMap.size() > 0) {
            result.addAll(processInfoMap.values());
            SettingsUtils.markInstalledPackagesIncluded(context, false);
        }

        // Go through the preferences and look for UNINSTALL, INSTALL and
        // REPLACE keys set by InstallReceiver.
        updatePackagePreferences(context, result);

        // FIXME: These are not used yet.
        // ActivityManager pActivityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        // Debug.MemoryInfo[] memoryInfo = pActivityManager.getProcessMemoryInfo(procMem);
        // for (Debug.MemoryInfo info : memoryInfo) {
        // Decide which ones of info. we want, add to a new and improved ProcessInfo object
        // FIXME: Not used yet, Sample needs more fields
        // FIXME: Which memory fields to choose?
        // int memory = info.dalvikPrivateDirty;
        // }

        return result;
    }

    private static void updatePackagePreferences(final Context context, List<ProcessInfo> result) {
        // Go through the preferences and look for UNINSTALL, INSTALL and
        // REPLACE keys set by InstallReceiver.
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> ap = p.getAll().keySet();
        SharedPreferences.Editor e = p.edit();
        boolean edited = false;
        for (String pref : ap) {
            if (pref.startsWith(INSTALLED)) {
                String pname = pref.substring(INSTALLED.length());
                boolean installed = p.getBoolean(pref, false);
                if (installed) {
                    Log.i(TAG, "Installed:" + pname);
                    ProcessInfo i = Package.getInstalledPackage(context, pname);
                    if (i != null) {
                        i.importance = Config.IMPORTANCE_INSTALLED;
                        result.add(i);
                        e.remove(pref);
                        edited = true;
                    }
                }
            } else if (pref.startsWith(REPLACED)) {
                String pname = pref.substring(REPLACED.length());
                boolean replaced = p.getBoolean(pref, false);
                if (replaced) {
                    Log.i(TAG, "Replaced:" + pname);
                    ProcessInfo i = Package.getInstalledPackage(context, pname);
                    if (i != null) {
                        i.importance = Config.IMPORTANCE_REPLACED;
                        result.add(i);
                        e.remove(pref);
                        edited = true;
                    }
                }
            } else if (pref.startsWith(UNINSTALLED)) {
                String pname = pref.substring(UNINSTALLED.length());
                boolean uninstalled = p.getBoolean(pref, false);
                if (uninstalled) {
                    Log.i(TAG, "Uninstalled:" + pname);
                    result.add(Process.uninstalledItem(pname, pref, e));
                    edited = true;
                }
            } else if (pref.startsWith(DISABLED)) {
                String pname = pref.substring(DISABLED.length());
                boolean disabled = p.getBoolean(pref, false);
                if (disabled) {
                    Log.i(TAG, "Disabled app:" + pname);
                    result.add(Process.disabledItem(pname, pref, e));
                    edited = true;
                }
            }
        }
        if (edited) e.apply();
    }

    /**
     * Helper method to collect all the extra information we wish to add to the sample into
     * the Extra Feature list.
     *
     * @return a List<Feature> populated with extra items to collect outside of the protocol spec.
     */
    private static List<Feature> getExtras() {
        LinkedList<Feature> res = new LinkedList<>();
        res.add(Specifications.getVmVersion());
        return res;
    }

    static Sample getSample(final Context context, Intent intent, String lastBatteryState) {
        // Construct sample and return it in the end
        Sample newSample = new Sample();
        NetworkDetails networkDetails = new NetworkDetails();
        BatteryDetails batteryDetails = new BatteryDetails();
        CpuStatus cpuStatus = new CpuStatus();
        Settings settings = new Settings();

        newSample.processInfos = new RealmList<>();
        newSample.locationProviders = new RealmList<>();
        newSample.features = new RealmList<>();


        if (Config.DEBUG) LOGI(TAG, "getSample() was invoked.");

        String action = intent.getAction();
        if (Config.DEBUG) LOGI(TAG, "action = " + action);

        newSample.uuId = Specifications.getAndroidId(context);
        newSample.triggeredBy = action;
        newSample.timestamp = System.currentTimeMillis();
        newSample.id = String.valueOf(newSample.timestamp).hashCode();

        // Record first data point for CPU usage
        long[] idleAndCpu1 = Cpu.readUsagePoint();

        // If the sampler is running because of the SCREEN_ON or SCREEN_OFF
        // event/action,
        // we want to get the info of all installed apps/packages, not only
        // those running.
        // This is because we need the traffic info of all apps, some might not
        // be running when
        // those events (screen on / screen off) occur

        EventBus.getDefault().post(new StatusEvent("Getting processes list..."));
        newSample.processInfos.addAll(getRunningProcessInfoForSample(context));

        newSample.screenBrightness = Screen.getBrightness(context);
        boolean autoScreenBrightness = Screen.isAutoBrightness(context);
        if (autoScreenBrightness) {
            // Auto
            newSample.screenBrightness = -1;
        }

        // Location providers
        newSample.locationProviders.addAll(LocationInfo.getEnabledLocationProviders(context));

        // TODO: not in Sample yet
        // int maxNumSatellite = SamplingLibrary.getMaxNumSatellite(context);

        String network = Network.getStatus(context);
        String networkType = Network.getType(context);
        String mobileNetworkType = Network.getMobileNetworkType(context);

        if (network.equals(Network.NETWORKSTATUS_CONNECTED)) {
            if (networkType.equals("WIFI")) {
                newSample.networkStatus = networkType;
            } else {
                newSample.networkStatus = mobileNetworkType;
            }
        } else {
            newSample.networkStatus = network;
        }

        // Network Details
        networkDetails.networkType = networkType;
        networkDetails.mobileNetworkType = mobileNetworkType;
        networkDetails.roamingEnabled = Network.getRoamingStatus(context);
        networkDetails.mobileDataStatus = Network.getDataState(context);
        networkDetails.mobileDataActivity = Network.getDataActivity(context);
        networkDetails.simOperator = SimCard.getSIMOperator(context);
        networkDetails.networkOperator = Phone.getNetworkOperator(context);
        networkDetails.mcc = Phone.getMcc(context);
        networkDetails.mnc = Phone.getMnc(context);

        // Wifi stuff
        networkDetails.wifiStatus = Wifi.getState(context);
        networkDetails.wifiSignalStrength = Wifi.getSignalStrength(context);
        networkDetails.wifiLinkSpeed = Wifi.getLinkSpeed(context);
        networkDetails.wifiApStatus = Wifi.getHotspotState(context);

        // No easy way to check this as API keeps changing
        // Possible by using reflection and checking build version
        // NetworkStatistics ns = new NetworkStatistics();

        // Add NetworkDetails substruct to Sample
        newSample.networkDetails = networkDetails;

        // For now calling info is not included so is null the object
        newSample.callInfo = null;

		/* Calling Information */
        // List<String> callInfo;
        // callInfo=SamplingLibrary.getCallInfo(context);
		/* Total call time */
        // long totalCallTime=0;
        // totalCallTime=SamplingLibrary.getTotalCallDur(context);

		/*
		 * long[] incomingOutgoingIdle = getCalltimesSinceBoot(context);
		 * Log.d(TAG, "Call time since boot: Incoming=" +
		 * incomingOutgoingIdle[0] + " Outgoing=" + incomingOutgoingIdle[1] +
		 * " idle=" + incomingOutgoingIdle[2]);
		 *
		 * // Summary Call info CallInfo ci = new CallInfo(); String callState =
		 * SamplingLibrary.getCallState(context); ci.setCallStatus(callState);
		 * ci.setIncomingCallTime(incomingOutgoingIdle[0]);
		 * ci.setOutgoingCallTime(incomingOutgoingIdle[1]);
		 * ci.setNonCallTime(incomingOutgoingIdle[2]);
		 *
		 * mySample.setCallInfo(ci);
		 */

        // Bundle b = intent.getExtras();

        // Battery details
        int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
        // This is really an int.
        // FIXED: Not used yet, Sample needs more fields

        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        String batteryTechnology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);

        // FIXED: Not used yet, Sample needs more fields
        String batteryHealth = "Unknown";
        String batteryStatus;

        switch (health) {
            case BatteryManager.BATTERY_HEALTH_DEAD:
                batteryHealth = "Dead";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                batteryHealth = "Good";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                batteryHealth = "Over voltage";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                batteryHealth = "Overheat";
                break;
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                batteryHealth = "Unknown";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                batteryHealth = "Unspecified failure";
                break;
        }

        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                batteryStatus = "Charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                batteryStatus = "Discharging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                batteryStatus = "Full";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                batteryStatus = "Not charging";
                break;
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                batteryStatus = "Unknown";
                break;
            default:
                batteryStatus = lastBatteryState != null ? lastBatteryState : "Unknown";
        }

        // FIXED: Not used yet, Sample needs more fields
        String batteryCharger = "unplugged";
        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                batteryCharger = "ac";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                batteryCharger = "usb";
                break;
        }


        // otherInfo.setCPUIdleTime(totalIdleTime);

        // IMPORTANT: All of the battery details fields were never set (=always
        // zero), like the last battery level.
        // Now all must have been fixed.

        // current battery temperature in degrees Centigrade (the unit of the
        // temperature value
        // (returned by BatteryManager) is not Centigrade, it should be divided
        // by 10)
        batteryDetails.temperature =
                ((float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10;
        // otherInfo.setBatteryTemperature(temperature);

        // current battery voltage in VOLTS (the unit of the returned value by
        // BatteryManager is millivolts)
        batteryDetails.voltage =
                ((float) intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)) / 1000;
        // otherInfo.setBatteryVoltage(voltage);

        batteryDetails.charger = batteryCharger;
        batteryDetails.health = batteryHealth;
        batteryDetails.technology = batteryTechnology;

        // Battery other values with API level limitations
        batteryDetails.capacity = Battery.getBatteryCapacity(context);
        batteryDetails.chargeCounter = Battery.getBatteryChargeCounter(context);
        batteryDetails.currentAverage = Battery.getBatteryCurrentAverage(context);
        batteryDetails.currentNow = Battery.getBatteryCurrentNow(context);
        batteryDetails.energyCounter = Battery.getBatteryEnergyCounter(context);

        newSample.batteryDetails = batteryDetails;
        newSample.batteryLevel = sCurrentBatteryLevel;
        newSample.batteryState = batteryStatus;

        // Memory statistics
        int[] usedFreeActiveInactive = Memory.readMemoryInfo();
        if (usedFreeActiveInactive != null && usedFreeActiveInactive.length == 4) {
            newSample.memoryUser = usedFreeActiveInactive[0];
            newSample.memoryFree = usedFreeActiveInactive[1];
            newSample.memoryActive = usedFreeActiveInactive[2];
            newSample.memoryInactive = usedFreeActiveInactive[3];
        }

        // Record second data point for cpu/idle time
        long[] idleAndCpu2 = Cpu.readUsagePoint();

        // CPU status
        long uptime = Cpu.getUptime();
        long sleep = Cpu.getSleepTime();

        cpuStatus.cpuUsage = Cpu.getUsage(idleAndCpu1, idleAndCpu2);
        cpuStatus.upTime = uptime;
        cpuStatus.sleepTime = sleep;
        newSample.cpuStatus = cpuStatus;

        // Storage details
        newSample.storageDetails = Storage.getStorageDetails();

        // System settings
        settings.bluetoothEnabled = Bluetooth.isEnabled();
        settings.locationEnabled = Gps.isEnabled(context);
        settings.powersaverEnabled = SettingsInfo.isPowerSaveEnabled(context);
        settings.flashlightEnabled = false;
        settings.nfcEnabled = SettingsInfo.isNfcEnabled(context);
        settings.developerMode = SettingsInfo.isDeveloperModeOn(context);
        settings.unknownSources = SettingsInfo.allowUnknownSources(context);
        newSample.settings = settings;

        // Other fields
        newSample.screenOn = Screen.isOn(context);
        newSample.timeZone = SettingsInfo.getTimeZone();
        newSample.countryCode = LocationInfo.getCountryCode(context);

        // If there are extra fields, include them into the sample.
        List<Feature> extras = getExtras();
        if (extras != null && extras.size() > 0) {
            newSample.features.addAll(extras);
        }

        return newSample;
    }

    static BatteryUsage getBatteryUsage(final Context context, Intent intent) {
        BatteryUsage usage = new BatteryUsage();
        BatteryDetails details = new BatteryDetails();

        // Battery details
        int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        String batteryTechnology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
        String batteryHealth = "Unknown";
        String batteryCharger = "unplugged";
        String batteryStatus;

        usage.timestamp = System.currentTimeMillis();
        usage.id = String.valueOf(usage.timestamp).hashCode();

        switch (health) {
            case BatteryManager.BATTERY_HEALTH_DEAD:
                batteryHealth = "Dead";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                batteryHealth = "Good";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                batteryHealth = "Over voltage";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                batteryHealth = "Overheat";
                break;
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                batteryHealth = "Unknown";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                batteryHealth = "Unspecified failure";
                break;
        }

        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                batteryStatus = "Charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                batteryStatus = "Discharging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                batteryStatus = "Full";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                batteryStatus = "Not charging";
                break;
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                batteryStatus = "Unknown";
                break;
            default:
                batteryStatus = "Unknown";
        }

        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                batteryCharger = "ac";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                batteryCharger = "usb";
                break;
        }

        details.temperature = ((float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10;

        // current battery voltage in VOLTS (the unit of the returned value by BatteryManager is millivolts)
        details.voltage = ((float) intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)) / 1000;

        details.charger = batteryCharger;
        details.health = batteryHealth;
        details.technology = batteryTechnology;

        // Battery other values with API level limitations
        details.capacity = Battery.getBatteryCapacity(context);
        details.chargeCounter = Battery.getBatteryChargeCounter(context);
        details.currentAverage = Battery.getBatteryCurrentAverage(context);
        details.currentNow = Battery.getBatteryCurrentNow(context);
        details.energyCounter = Battery.getBatteryEnergyCounter(context);

        usage.level = (float) sCurrentBatteryLevel;
        usage.state = batteryStatus;
        usage.screenOn = Screen.isOn(context);
        usage.triggeredBy = intent.getAction();
        usage.details = details;

        return usage;
    }

    public static BatterySession getBatterySession(final Context context, Intent intent) {
        BatterySession session = new BatterySession();

        session.timestamp = System.currentTimeMillis();
        session.id = String.valueOf(session.timestamp).hashCode();
        session.level = (float) sCurrentBatteryLevel;
        session.screenOn = Screen.isOn(context);
        session.triggeredBy = intent.getAction();

        return session;
    }
}
