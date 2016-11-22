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

package hmatalonga.greenhub.managers.sampling;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.models.Application;
import hmatalonga.greenhub.models.Cpu;
import hmatalonga.greenhub.models.LocationInfo;
import hmatalonga.greenhub.models.Memory;
import hmatalonga.greenhub.models.Network;
import hmatalonga.greenhub.models.Package;
import hmatalonga.greenhub.models.Phone;
import hmatalonga.greenhub.models.Process;
import hmatalonga.greenhub.models.Screen;
import hmatalonga.greenhub.models.SettingsInfo;
import hmatalonga.greenhub.models.SimCard;
import hmatalonga.greenhub.models.Specifications;
import hmatalonga.greenhub.models.Storage;
import hmatalonga.greenhub.models.Wifi;
import hmatalonga.greenhub.models.data.BatteryDetails;
import hmatalonga.greenhub.models.data.CpuStatus;
import hmatalonga.greenhub.models.data.Feature;
import hmatalonga.greenhub.models.data.NetworkDetails;
import hmatalonga.greenhub.models.data.ProcessInfo;
import hmatalonga.greenhub.models.data.Sample;
import hmatalonga.greenhub.models.data.Settings;
import hmatalonga.greenhub.util.SettingsUtils;

import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Inspector instrumentation class.
 */
public final class Inspector {

    private static final String TAG = makeLogTag("Inspector");

    public static final String INSTALLED = "installed:";
    public static final String REPLACED = "replaced:";
    public static final String UNINSTALLED = "uninstalled:";
    // Disabled or turned off applications will be scheduled for reporting using this prefix
    public static final String DISABLED = "disabled:";

    private static final String STAG = "getSample";

    private static double lastBatteryLevel;
    private static double currentBatteryLevel;

    // we might not be able to read the current battery level at the first run
    // of Carat.
    // so it might be zero until we get the non-zero value from the intent
    // (BatteryManager.EXTRA_LEVEL & BatteryManager.EXTRA_SCALE)

    /** Library class, prevent instantiation */
    private Inspector() {}

    public static double readLastBatteryLevel() {
        return lastBatteryLevel;
    }

    public static void setLastBatteryLevel(double level) {
        lastBatteryLevel = level;
    }

    public static double getLastBatteryLevel() {
        return lastBatteryLevel;
    }

    public static double getCurrentBatteryLevel() {
        return currentBatteryLevel;
    }

    public static void setCurrentBatteryLevel(double level) {
        currentBatteryLevel = level;
    }

    /**
     * Take in currentLevel and scale as doubles to avoid loss of precision issues.
     * Note that Carat stores battery level as a value between 0 and 1, e.g. 0.45 for 45%.
     * @param currentLevel Current battery level, usually in percent.
     * @param scale Battery scale, usually 100.0.
     */
    public static void setCurrentBatteryLevel(double currentLevel, double scale) {
		/* we should multiply the result of the division below by 100.0 to get the battery level
		 * in the scale of 0-100, but since the previous samples in our server's dataset are in the scale of 0.00-1.00,
		 * we omit the multiplication. */
        double level = currentLevel / scale;
		/*
		 * whenever we get these two arguments (extras from the intent:
		 * EXTRA_LEVEL & EXTRA_SCALE), it doens't necessarily mean that a battery
		 * percentage change has happened. Check the comments in the
		 * broadcast receiver (sampler).
		 */
        if (level != currentBatteryLevel) currentBatteryLevel = level;
    }

    /**
     * Returns a List of ProcessInfo objects, helper for getSample.
     *
     * @param context the Context.
     * @return a List of ProcessInfo objects, helper for getSample.
     */
    private static List<ProcessInfo> getRunningProcessInfoForSample(Context context) {
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
            String pName = pi.getName();
            if (processInfoMap != null && processInfoMap.containsKey(pName)) {
                processInfoMap.remove(pName);
            }

            procs.add(pName);
            ProcessInfo item = new ProcessInfo();
            PackageInfo packageInfo = Package.getPackageInfo(context, pName);

            if (packageInfo != null) {
                item.setVersionName(packageInfo.versionName);
                item.setVersionCode(packageInfo.versionCode);
                ApplicationInfo info = packageInfo.applicationInfo;

                // Human readable label (if any)
                String label = pm.getApplicationLabel(info).toString();
                if (label.length() > 0) {
                    item.setApplicationLabel(label);
                }
                int flags = packageInfo.applicationInfo.flags;
                // Check if it is a system app
                boolean isSystemApp = (flags & ApplicationInfo.FLAG_SYSTEM) > 0;
                isSystemApp = isSystemApp || (flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0;
                item.setSystemApp(isSystemApp);
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
            item.setImportance(pi.getImportance());
            item.setpId(pi.getpId());
            item.setName(pName);

            String installationSource = null;
            if (!pi.isSystemApp()) {
                try {
                    installationSource = pm.getInstallerPackageName(pName);
                } catch (IllegalArgumentException iae) {
                    Log.e(STAG, "Could not get installer for " + pName);
                }
            }
            if (installationSource == null) {
                installationSource = "null";
            }
            item.setInstallationPkg(installationSource);

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
                    Log.i(STAG, "Installed:" + pname);
                    ProcessInfo i = Package.getInstalledPackage(context, pname);
                    if (i != null) {
                        i.setImportance(Config.IMPORTANCE_INSTALLED);
                        result.add(i);
                        e.remove(pref);
                        edited = true;
                    }
                }
            } else if (pref.startsWith(REPLACED)) {
                String pname = pref.substring(REPLACED.length());
                boolean replaced = p.getBoolean(pref, false);
                if (replaced) {
                    Log.i(STAG, "Replaced:" + pname);
                    ProcessInfo i = Package.getInstalledPackage(context, pname);
                    if (i != null) {
                        i.setImportance(Config.IMPORTANCE_REPLACED);
                        result.add(i);
                        e.remove(pref);
                        edited = true;
                    }
                }
            } else if (pref.startsWith(UNINSTALLED)) {
                String pname = pref.substring(UNINSTALLED.length());
                boolean uninstalled = p.getBoolean(pref, false);
                if (uninstalled) {
                    Log.i(STAG, "Uninstalled:" + pname);
                    result.add(Process.uninstalledItem(pname, pref, e));
                    edited = true;
                }
            } else if (pref.startsWith(DISABLED)) {
                String pname = pref.substring(DISABLED.length());
                boolean disabled = p.getBoolean(pref, false);
                if (disabled) {
                    Log.i(STAG, "Disabled app:" + pname);
                    result.add(Process.disabledItem(pname, pref, e));
                    edited = true;
                }
            }
        }
        if (edited) e.apply();
    }

    /**
     * Safely terminate (kill) the given app.
     *
     * @param context
     * @param packageName
     * @param label
     * @return
     */
    public static boolean killApp(final Context context, String packageName, String label) {
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        if (am != null) {
            try {
                PackageInfo p = Package.getPackageInfo(context, packageName);
                // Log.v(STAG, "Trying to kill proc=" + packageName + " pak=" +
                // p.packageName);
//                FlurryAgent.logEvent("Killing app=" + (label == null ? "null" : label) + " proc=" + packageName
//                        + " pak=" + (p == null ? "null" : p.packageName));
                am.killBackgroundProcesses(packageName);

                return true;
            } catch (Throwable th) {
                Log.e(STAG, "Could not kill process: " + packageName, th);
            }
        }
        return false;
    }

    public static Sample getSample(final Context context, Intent intent, String lastBatteryState) {
        final String TAG = "SamplingLibrary.getSample";
        if (Config.DEBUG)
            Log.d(TAG, "getSample() was invoked.");

        String action = intent.getAction();
        if (Config.DEBUG)
            Log.d(TAG, "action = " + action);

        // Construct sample and return it in the end
        Sample mySample = new Sample();
        String uuId = Specifications.getAndroidId(context);
        mySample.setUuId(uuId);
        mySample.setTriggeredBy(action);
        // required always
        long now = System.currentTimeMillis();
        mySample.setTimestamp(now / 1000.0);

        // Record first data point for CPU usage
        long[] idleAndCpu1 = Cpu.readUsagePoint();

        // If the sampler is running because of the SCREEN_ON or SCREEN_OFF
        // event/action,
        // we want to get the info of all installed apps/packages, not only
        // those running.
        // This is because we need the traffic info of all apps, some might not
        // be running when
        // those events (screen on / screen off) occur

        // TODO: let's comment out these lines for debugging purpose

        // if (action.equals(Intent.ACTION_SCREEN_ON) ||
        // action.equals(Intent.ACTION_SCREEN_OFF)) {
        // Log.d(TAG,
        // "the action has been Intent.ACTION_SCREEN_ON or SCREEN_OFF. Taking sample of ALL INSTALLED packages (rather than running processes)");
        // Map<String, ProcessInfo> installedPackages =
        // getInstalledPackages(context, false);
        // List<ProcessInfo> processes = new ArrayList<ProcessInfo>();
        // processes.addAll(installedPackages.values());
        // } else {
        // Log.d(TAG,
        // "the action has NOT been Intent.ACTION_SCREEN_ON or SCREEN_OFF. Taking sample of running processes.");
        List<ProcessInfo> processes = getRunningProcessInfoForSample(context);
        mySample.setPiList(processes);
        // }

        int screenBrightness = Screen.getBrightness(context);
        mySample.setScreenBrightness(screenBrightness);
        boolean autoScreenBrightness = Screen.isAutoBrightness(context);
        if (autoScreenBrightness)
            mySample.setScreenBrightness(-1); // Auto
        // boolean gpsEnabled = SamplingLibrary.getGpsEnabled(context);
        // Location providers
        List<String> enabledLocationProviders = LocationInfo.getEnabledLocationProviders(context);
        mySample.setLocationProviders(enabledLocationProviders);

        // TODO: not in Sample yet
        // int maxNumSatellite = SamplingLibrary.getMaxNumSatellite(context);

        String network = Network.getStatus(context);
        String networkType = Network.getType(context);
        String mobileNetworkType = Network.getMobileNetworkType(context);

        // Required in new Carat protocol
        if (network.equals(Network.NETWORKSTATUS_CONNECTED)) {
            if (networkType.equals("WIFI"))
                mySample.setNetworkStatus(networkType);
            else
                mySample.setNetworkStatus(mobileNetworkType);
        } else
            mySample.setNetworkStatus(network);

        // String ns = mySample.getNetworkStatus();
        // Log.d(STAG, "Set networkStatus="+ns);

        // Network details
        NetworkDetails nd = new NetworkDetails();

        // Network type
        nd.setNetworkType(networkType);
        nd.setMobileNetworkType(mobileNetworkType);
        boolean roamStatus = Network.getRoamingStatus(context);
        nd.setRoamingEnabled(roamStatus);
        String dataState = Network.getDataState(context);
        nd.setMobileDataStatus(dataState);
        String dataActivity = Network.getDataActivity(context);
        nd.setMobileDataActivity(dataActivity);
        String simOperator = SimCard.getSIMOperator(context);
        nd.setSimOperator(simOperator);
        String networkOperator = Phone.getNetworkOperator(context);
        nd.setNetworkOperator(networkOperator);
        String mcc = Phone.getMcc(context);
        nd.setMcc(mcc);
        String mnc = Phone.getMnc(context);
        nd.setMnc(mnc);

        // Wifi stuff
        String wifiState = Wifi.getState(context);
        nd.setWifiStatus(wifiState);
        int wifiSignalStrength = Wifi.getSignalStrength(context);
        nd.setWifiSignalStrength(wifiSignalStrength);
        int wifiLinkSpeed = Wifi.getLinkSpeed(context);
        nd.setWifiLinkSpeed(wifiLinkSpeed);
        String wifiApStatus = Wifi.getHotspotState(context);
        nd.setWifiApStatus(wifiApStatus);

        // No easy way to check this as API keeps changing
        // Possible by using reflection and checking build version
        // NetworkStatistics ns = new NetworkStatistics();

        // Add NetworkDetails substruct to Sample
        mySample.setNetworkDetails(nd);

		/* Calling Information */
        // List<String> callInfo;
        // callInfo=SamplingLibrary.getCallInfo(context);
		/* Total call time */
        // long totalCallTime=0;
        // totalCallTime=SamplingLibrary.getTotalCallDur(context);

		/*
		 * long[] incomingOutgoingIdle = getCalltimesSinceBoot(context);
		 * Log.d(STAG, "Call time since boot: Incoming=" +
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

        BatteryDetails bd = new BatteryDetails();
        // otherInfo.setCPUIdleTime(totalIdleTime);

        // IMPORTANT: All of the battery details fields were never set (=always
        // zero), like the last battery level.
        // Now all must have been fixed.

        // current battery temperature in degrees Centigrade (the unit of the
        // temperature value
        // (returned by BatteryManager) is not Centigrade, it should be divided
        // by 10)
        int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10;
        bd.setBatteryTemperature(temperature);
        // otherInfo.setBatteryTemperature(temperature);

        // current battery voltage in VOLTS (the unit of the returned value by
        // BatteryManager is millivolts)
        double voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000;
        bd.setBatteryVoltage(voltage);
        // otherInfo.setBatteryVoltage(voltage);
        bd.setBatteryTechnology(batteryTechnology);
        bd.setBatteryCharger(batteryCharger);
        bd.setBatteryHealth(batteryHealth);
        mySample.setBatteryDetails(bd);
        mySample.setBatteryLevel(currentBatteryLevel);
        mySample.setBatteryState(batteryStatus);

        // Memory statistics
        int[] usedFreeActiveInactive = Memory.readMemoryInfo();
        if (usedFreeActiveInactive != null && usedFreeActiveInactive.length == 4) {
            mySample.setMemoryUser(usedFreeActiveInactive[0]);
            mySample.setMemoryFree(usedFreeActiveInactive[1]);
            mySample.setMemoryActive(usedFreeActiveInactive[2]);
            mySample.setMemoryInactive(usedFreeActiveInactive[3]);
        }

        // Record second data point for cpu/idle time
        long[] idleAndCpu2 = Cpu.readUsagePoint();

        // CPU status
        CpuStatus cs = new CpuStatus();
        double uptime = Cpu.getUptime();
        double sleep = Cpu.getSleepTime();
        cs.setCpuUsage(Cpu.getUsage(idleAndCpu1, idleAndCpu2));
        cs.setUptime(uptime);
        cs.setSleeptime(sleep);
        mySample.setCpuStatus(cs);

        // Storage details
        mySample.setStorageDetails(Storage.getStorageDetails());

        // System settings
        Settings settings = new Settings();
        settings.setBluetoothEnabled(SettingsInfo.isBluetoothEnabled());
        mySample.setSettings(settings);

        // Other fields
        mySample.setDeveloperMode(SettingsInfo.isDeveloperModeOn(context));
        mySample.setUnknownSources(SettingsInfo.allowUnknownSources(context));
        mySample.setScreenOn(Screen.isOn(context));
        mySample.setTimeZone(SettingsInfo.getTimeZone());
        mySample.setCountryCode(LocationInfo.getCountryCode(context));

        // If there are extra fields, include them into the sample.
        List<Feature> extras = getExtras();
        if (extras != null && extras.size() > 0)
            mySample.setExtra(extras);

        if(Config.DEBUG){
            // Need to split since output is over 1000 characters
            Log.d("debug", "Created the following sample:");
            String sampleString = mySample.toString();
            int limit = 1000;
            for(int i = 0; i <= sampleString.length() / limit; i++) {
                int start = i * limit;
                int end = (i+1) * limit;
                end = (end > sampleString.length()) ? sampleString.length() : end;
                Log.d("debug", sampleString.substring(start, end));
            }
        }

        return mySample;
    }

    /**
     * Helper method to collect all the extra information we wish to add to the sample into the Extra Feature list.
     *
     * @return a List<Feature> populated with extra items to collect outside of the protocol spec.
     */
    private static List<Feature> getExtras() {
        LinkedList<Feature> res = new LinkedList<>();
        res.add(Specifications.getVmVersion());
        return res;
    }
}
