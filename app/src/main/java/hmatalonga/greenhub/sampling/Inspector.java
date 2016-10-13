

package hmatalonga.greenhub.sampling;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import hmatalonga.greenhub.Constants;
import hmatalonga.greenhub.GreenHub;
import hmatalonga.greenhub.database.BatteryDetails;
import hmatalonga.greenhub.database.CallMonth;
import hmatalonga.greenhub.database.CellInfo;
import hmatalonga.greenhub.database.CpuStatus;
import hmatalonga.greenhub.database.Feature;
import hmatalonga.greenhub.database.NetworkDetails;
import hmatalonga.greenhub.database.ProcessInfo;
import hmatalonga.greenhub.database.Sample;

/**
 * Inspector
 */
public final class Inspector {
    private static final boolean collectSignatures = true;
    public static final String SIG_SENT = "sig-sent:";
    public static final String SIG_SENT_256 = "sigs-sent:";
    public static final String INSTALLED = "installed:";
    public static final String REPLACED = "replaced:";
    public static final String UNINSTALLED = "uninstalled:";
    // Disabled or turned off applications will be scheduled for reporting using this prefix
    public static final String DISABLED = "disabled:";

    private static final int READ_BUFFER_SIZE = 2 * 1024;
    // Network status constants
    public static String NETWORKSTATUS_DISCONNECTED = "disconnected";
    public static String NETWORKSTATUS_DISCONNECTING = "disconnecting";
    public static String NETWORKSTATUS_CONNECTED = "connected";
    public static String NETWORKSTATUS_CONNECTING = "connecting";
    // Network type constants
    public static String TYPE_UNKNOWN = "unknown";
    // Data State constants
    public static String DATA_DISCONNECTED = NETWORKSTATUS_DISCONNECTED;
    public static String DATA_CONNECTING = NETWORKSTATUS_CONNECTING;
    public static String DATA_CONNECTED = NETWORKSTATUS_CONNECTED;
    public static String DATA_SUSPENDED = "suspended";
    // Data Activity constants
    public static String DATA_ACTIVITY_NONE = "none";
    public static String DATA_ACTIVITY_IN = "in";
    public static String DATA_ACTIVITY_OUT = "out";
    public static String DATA_ACTIVITY_INOUT = "inout";
    public static String DATA_ACTIVITY_DORMANT = "dormant";
    // Wifi State constants
    public static String WIFI_STATE_DISABLING = "disabling";
    public static String WIFI_STATE_DISABLED = "disabled";
    public static String WIFI_STATE_ENABLING = "enabling";
    public static String WIFI_STATE_ENABLED = "enabled";
    public static String WIFI_STATE_UNKNOWN = "unknown";
    // Call state constants
    public static String CALL_STATE_IDLE = "idle";
    public static String CALL_STATE_OFFHOOK = "offhook";
    public static String CALL_STATE_RINGING = "ringing";

    // Mobile network constants
    public static String NETWORK_TYPE_UNKNOWN = "unknown";
    public static String NETWORK_TYPE_GPRS = "gprs";
    public static String NETWORK_TYPE_EDGE = "edge";
    public static String NETWORK_TYPE_UMTS = "utms";
    public static String NETWORK_TYPE_CDMA = "cdma";
    public static String NETWORK_TYPE_EVDO_0 = "evdo_0";
    public static String NETWORK_TYPE_EVDO_A = "evdo_a";
    public static String NETWORK_TYPE_EVDO_B = "evdo_b";
    public static String NETWORK_TYPE_1xRTT = "1xrtt";
    public static String NETWORK_TYPE_HSDPA = "hsdpa";
    public static String NETWORK_TYPE_HSUPA = "hsupa";
    public static String NETWORK_TYPE_HSPA = "hspa";
    public static String NETWORK_TYPE_IDEN = "iden";
    public static String NETWORK_TYPE_LTE = "lte";
    public static String NETWORK_TYPE_EHRPD = "ehrpd";
    public static String NETWORK_TYPE_HSPAP = "hspap";

    private static final int EVDO_B = 12;
    private static final int LTE = 13;
    private static final int EHRPD = 14;
    private static final int HSPAP = 15;

    // Phone type constants
    public static String PHONE_TYPE_CDMA = "cdma";
    public static String PHONE_TYPE_GSM = "gsm";
    // public static String PHONE_TYPE_SIP="sip";
    public static String PHONE_TYPE_NONE = "none";

    public static double startLatitude = 0;
    public static double startLongitude = 0;
    public static double distance = 0;

    private static final String STAG = "getSample";
    // private static final String TAG="FeaturesPowerConsumption";

    public static final int UUID_LENGTH = 16;
    private static final String TAG = "Sampling";

    private static double lastBatteryLevel;
    private static double currentBatteryLevel;

    // we might not be able to read the current battery level at the first run
    // of Carat.
    // so it might be zero until we get the non-zero value from the intent
    // (BatteryManager.EXTRA_LEVEL & BatteryManager.EXTRA_SCALE)

    /** Library class, prevent instantiation */
    private Inspector() {
    }

    /**
     * Returns a randomly generated unique identifier that stays constant for
     * the lifetime of the device. (May change if wiped). This is probably our
     * best choice for a UUID across the Android landscape, since it is present
     * on both phones and non-phones.
     *
     * @return a String that uniquely identifies this device.
     */
    public static String getAndroidId(Context c) {
        return Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getUuid(Context c) {
        return getTimeBasedUuid(c, false);
    }

    public static String getTimeBasedUuid(Context c) {
        return getTimeBasedUuid(c, true);
    }

    public static double readLastBatteryLevel() {
        return lastBatteryLevel;
    }

    public static void setLastBatteryLevel(double level) {
        Inspector.lastBatteryLevel = level;
    }

    public static double getLastBatteryLevel(Context context) {
        return lastBatteryLevel;
    }

    public static double getCurrentBatteryLevel() {
        return currentBatteryLevel;
    }

    public static void setCurrentBatteryLevel(double level) {
        Inspector.currentBatteryLevel = level;
    }

    /**
     *
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
        if (level != getCurrentBatteryLevel())
            setCurrentBatteryLevel(level);
    }

    /**
     * Generate a time-based, random identifier.
     *
     * @param c
     *            the app's Context
     * @return a time-based, random identifier.
     */
    public static String getTimeBasedUuid(Context c, boolean includeTimestamp) {
        String aID = getAndroidId(c);
        String wifiMac = getWifiMacAddress(c);
        String devid = getDeviceId(c);
        String concat = "";
        if (aID != null)
            concat = aID;
        else
            concat = "0000000000000000";
        if (wifiMac != null)
            concat += wifiMac;
        else
            concat += "00:00:00:00:00:00";

        // IMEI is 15 characters, decimal, while MEID is 14 characters, hex. Add
        // a space if length is less than 15:
        if (devid != null) {
            concat += devid;
            if (devid.length() < 15)
                concat += " ";
        } else
            concat += "000000000000000";
        if (includeTimestamp) {
            long timestamp = System.currentTimeMillis();
            concat += timestamp;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(concat.getBytes());
            byte[] mdbytes = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < mdbytes.length; i++) {
                String hx = Integer.toHexString(0xFF & mdbytes[i]);
                if (hx.equals("0"))
                    hexString.append("00");
                else
                    hexString.append(hx);
            }
            String uuid = hexString.toString().substring(0, UUID_LENGTH);
            return uuid;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return aID;
        }
    }

    /**
     * Returns the model of the device running Carat, for example "sdk" for the
     * emulator, Galaxy Nexus for Samsung Galaxy Nexus.
     *
     * @return the model of the device running Carat, for example "sdk" for the
     *         emulator, Galaxy Nexus for Samsung Galaxy Nexus.
     */
    public static String getModel() {
        return android.os.Build.MODEL;
    }

    /**
     * Returns the manufacturer of the device running Carat, for example
     * "google" or "samsung".
     *
     * @return the manufacturer of the device running Carat, for example
     *         "google" or "samsung".
     */
    public static String getManufacturer() {
        return android.os.Build.MANUFACTURER;
    }

    /**
     * Returns the OS version of the device running Carat, for example 2.3.3 or
     * 4.0.2.
     *
     * @return the OS version of the device running Carat, for example 2.3.3 or
     *         4.0.2.
     */
    public static String getOsVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * Returns the product name.
     *
     * @return the product name.
     */
    public static String getProductName() {
        return android.os.Build.PRODUCT;
    }

    /**
     * Returns the kernel version, e.g. 3.4-1101.
     *
     * @return the kernel version, e.g. 3.4-1101.
     */
    public static String getKernelVersion() {
        return System.getProperty("os.version", TYPE_UNKNOWN);
    }

    /**
     * Returns the build serial number. May only work for 2.3 and up.
     *
     * @return the build serial number.
     */
    public static String getBuildSerial() {
        // return android.os.Build.Serial;
        return System.getProperty("ro.serial", TYPE_UNKNOWN);
    }

    /**
     * Print all system properties for debugging.
     *
     */
    public static void printAllProperties() {
        Properties list = System.getProperties();
        Enumeration<Object> keys = list.keys();
        while (keys.hasMoreElements()) {
            String k = (String) keys.nextElement();
            String v = list.getProperty(k);
            if (Constants.DEBUG)
                Log.d("PROPS", k + "=" + v);
        }
    }

    /**
     * Returns the brand for which the device is customized, e.g. Verizon.
     *
     * @return the brand for which the device is customized, e.g. Verizon.
     */
    public static String getBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * Return misc system details that we might want to use later. Currently
     * does nothing.
     *
     * @return
     */
    public static Map<String, String> getSystemDetails() {
        Map<String, String> results = new HashMap<>();
        // TODO: Some of this should be added to registration to identify the
        // device and OS.
        // Cyanogenmod and others may have different kernels etc that affect
        // performance.

		/*
		 * Log.d("SetModel", "board:" + android.os.Build.BOARD);
		 * Log.d("SetModel", "bootloader:" + android.os.Build.BOOTLOADER);
		 * Log.d("SetModel", "brand:" + android.os.Build.BRAND);
		 * Log.d("SetModel", "CPU_ABI 1 and 2:" + android.os.Build.CPU_ABI +
		 * ", " + android.os.Build.CPU_ABI2); Log.d("SetModel", "dev:" +
		 * android.os.Build.DEVICE); Log.d("SetModel", "disp:" +
		 * android.os.Build.DISPLAY); Log.d("SetModel", "FP:" +
		 * android.os.Build.FINGERPRINT); Log.d("SetModel", "HW:" +
		 * android.os.Build.HARDWARE); Log.d("SetModel", "host:" +
		 * android.os.Build.HOST); Log.d("SetModel", "ID:" +
		 * android.os.Build.ID); Log.d("SetModel", "manufacturer:" +
		 * android.os.Build.MANUFACTURER); Log.d("SetModel", "prod:" +
		 * android.os.Build.PRODUCT); Log.d("SetModel", "radio:" +
		 * android.os.Build.RADIO); // FIXME: SERIAL not available on 2.2 //
		 * Log.d("SetModel", "ser:" + android.os.Build.SERIAL);
		 * Log.d("SetModel", "tags:" + android.os.Build.TAGS); Log.d("SetModel",
		 * "time:" + android.os.Build.TIME); Log.d("SetModel", "type:" +
		 * android.os.Build.TYPE); Log.d("SetModel", "unknown:" +
		 * android.os.Build.UNKNOWN); Log.d("SetModel", "user:" +
		 * android.os.Build.USER); Log.d("SetModel", "model:" +
		 * android.os.Build.MODEL); Log.d("SetModel", "codename:" +
		 * android.os.Build.VERSION.CODENAME); Log.d("SetModel", "release:" +
		 * android.os.Build.VERSION.RELEASE);
		 */

        return results;
    }

    /**
     * Read memory information from /proc/meminfo. Return used, free, inactive,
     * and active memory.
     *
     * @return an int[] with used, free, inactive, and active memory, in kB, in
     *         that order.
     */
    public static int[] readMeminfo() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/meminfo", "r");
            String load = reader.readLine();

            String[] toks = load.split("\\s+");
            // Log.v("meminfo", "Load: " + load + " 1:" + toks[1]);
            int total = Integer.parseInt(toks[1]);
            load = reader.readLine();
            toks = load.split("\\s+");
            // Log.v("meminfo", "Load: " + load + " 1:" + toks[1]);
            int free = Integer.parseInt(toks[1]);
            load = reader.readLine();
            load = reader.readLine();
            load = reader.readLine();
            load = reader.readLine();
            toks = load.split("\\s+");
            // Log.v("meminfo", "Load: " + load + " 1:" + toks[1]);
            int act = Integer.parseInt(toks[1]);
            load = reader.readLine();
            toks = load.split("\\s+");
            // Log.v("meminfo", "Load: " + load + " 1:" + toks[1]);
            int inact = Integer.parseInt(toks[1]);
            reader.close();

            return new int[] { total - free, free, inact, act };
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return new int[] { 0, 0, 0, 0 };
    }

    /**
     * Read memory usage using the public Android API methods in
     * ActivityManager, such as MemoryInfo and getProcessMemoryInfo.
     *
     * @param c
     *            the Context from the running Activity.
     * @return int[] with total and used memory, in kB, in that order.
     */
    public static int[] readMemory(Context c) {
        ActivityManager man = (ActivityManager) c.getSystemService(Activity.ACTIVITY_SERVICE);
		/* Get available (free) memory */
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        man.getMemoryInfo(info);
        int totalMem = (int) info.availMem;

		/* Get memory used by all running processes. */

		/* Step 1: gather pids */
        List<ActivityManager.RunningAppProcessInfo> procs = man.getRunningAppProcesses();
        List<ActivityManager.RunningServiceInfo> servs = man.getRunningServices(Integer.MAX_VALUE);
        int[] pids = new int[procs.size() + servs.size()];
        int i = 0;
        for (ActivityManager.RunningAppProcessInfo pinfo : procs) {
            pids[i] = pinfo.pid;
            i++;
        }
        for (ActivityManager.RunningServiceInfo pinfo : servs) {
            pids[i] = pinfo.pid;
            i++;
        }

		/*
		 * Step 2: Sum up Pss values (weighted memory usage, taking into account
		 * shared page usage)
		 */
        android.os.Debug.MemoryInfo[] mems = man.getProcessMemoryInfo(pids);
        int memUsed = 0;
        for (android.os.Debug.MemoryInfo mem : mems) {
            memUsed += mem.getTotalPss();
        }
        Log.v("Mem", "Total mem:" + totalMem);
        Log.v("Mem", "Mem Used:" + memUsed);
        return new int[] { totalMem, memUsed };
    }

    // FIXME: Describe this. Why are there so many fields? Why is it divided by
    // 100?
	/*
	 * The value of HZ varies across kernel versions and hardware platforms. On
	 * i386 the situation is as follows: on kernels up to and including 2.4.x,
	 * HZ was 100, giving a jiffy value of 0.01 seconds; starting with 2.6.0, HZ
	 * was raised to 1000, giving a jiffy of 0.001 seconds. Since kernel 2.6.13,
	 * the HZ value is a kernel configuration parameter and can be 100, 250 (the
	 * default) or 1000, yielding a jiffies value of, respectively, 0.01, 0.004,
	 * or 0.001 seconds. Since kernel 2.6.20, a further frequency is available:
	 * 300, a number that divides evenly for the common video frame rates (PAL,
	 * 25 HZ; NTSC, 30 HZ).
	 *
	 * I will leave the unit of cpu time as the jiffy and we can discuss later.
	 *
	 * 0 name of cpu 1 space 2 user time 3 nice time 4 sys time 5 idle time(it
	 * is not include in the cpu total time) 6 iowait time 7 irg time 8 softirg
	 * time
	 *
	 * the idleTotal[5] is the idle time which always changes. There are two
	 * spaces between cpu and user time.That is a tricky thing and messed up
	 * splitting.:)
	 */

    /**
     * Read CPU usage from /proc/stat, return a fraction of
     * usage/(usage+idletime)
     *
     * @return a fraction of usage/(usage+idletime)
     */
    public static long[] readUsagePoint() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" ");

            long idle1 = Long.parseLong(toks[5]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            reader.close();
            return new long[] { idle1, cpu1 };
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Calculate CPU usage between the cpu and idle time given at two time
     * points.
     *
     * @param then
     * @param now
     * @return
     */
    public static double getUsage(long[] then, long[] now) {
        if (then == null || now == null || then.length < 2 || now.length < 2)
            return 0.0;
        double idleAndCpuDiff = (now[0] + now[1]) - (then[0] + then[1]);

        return (now[1] - then[1]) / idleAndCpuDiff;
    }

    private static WeakReference<List<RunningAppProcessInfo>> runningAppInfo = null;

    public static List<ProcessInfo> getRunningAppInfo(Context c) {
        List<RunningAppProcessInfo> runningProcs = getRunningProcessInfo(c);
        List<RunningServiceInfo> runningServices = getRunningServiceInfo(c);

        Set<String> packages = new HashSet<>();
        ArrayList<ProcessInfo> l = new ArrayList<>();

        if (runningProcs != null) {
            for (RunningAppProcessInfo pi : runningProcs) {
                if (pi == null)
                    continue;
                if (packages.contains(pi.processName))
                    continue;
                packages.add(pi.processName);
                ProcessInfo item = new ProcessInfo();
                item.setImportance(GreenHub.importanceString(pi.importance));
                item.setpId(pi.pid);
                item.setpName(pi.processName);
                l.add(item);
            }
        }

        if (runningServices != null) {
            for (RunningServiceInfo pi : runningServices) {
                if (pi == null)
                    continue;
                if (packages.contains(pi.process))
                    continue;
                packages.add(pi.process);
                ProcessInfo item = new ProcessInfo();
                item.setImportance(pi.foreground ? "Foreground app" : "Service");
                item.setpId(pi.pid);
                //item.setApplicationLabel(pi.service.flattenToString());
                item.setpName(pi.process);

                l.add(item);
            }
        }

        return l;
    }

    /**
     * Populate running process info into the runningAppInfo WeakReference list, and return its value.
     * @param context the Context
     * @return the value of the runningAppInfo WeakReference list after setting it.
     */
    private static List<RunningAppProcessInfo> getRunningProcessInfo(Context context) {
        if (runningAppInfo == null || runningAppInfo.get() == null) {
            ActivityManager pActivityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
            List<RunningAppProcessInfo> runningProcs = pActivityManager.getRunningAppProcesses();
			/*
			 * TODO: Is this the right thing to do? Remove part after ":" in
			 * process names
			 */
            for (RunningAppProcessInfo i : runningProcs) {
                if (i != null && i.processName != null) {
                    int idx = i.processName.lastIndexOf(':');
                    if (idx <= 0)
                        idx = i.processName.length();
                    i.processName = i.processName.substring(0, idx);
                }
            }

            runningAppInfo = new WeakReference<>(runningProcs);

            return runningProcs;
        } else
            return runningAppInfo.get();
    }

    /**
     * Returns a list of currently running Services.
     * @param c the Context.
     * @return Returns a list of currently running Services.
     */
    public static List<RunningServiceInfo> getRunningServiceInfo(Context c) {
        ActivityManager pActivityManager = (ActivityManager) c.getSystemService(Activity.ACTIVITY_SERVICE);
        return pActivityManager.getRunningServices(255);
    }

    /**
     * Helper to query whether an application is currently running and its code has not been evicted from memory.
     * @param context the Context
     * @param appName the package name or process name of the application.
     * @return true if the application is running, false otherwise.
     */
    public static boolean isRunning(Context context, String appName) {
        List<RunningAppProcessInfo> runningProcs = getRunningProcessInfo(context);
        for (RunningAppProcessInfo i : runningProcs) {
            // Log.d(TAG, "Matching process: "+i.processName +" with app: "+ appName);
            if (i.processName.equals(appName) && i.importance != RunningAppProcessInfo.IMPORTANCE_EMPTY)
                return true;
        }

        List<RunningServiceInfo> services = getRunningServiceInfo(context);
        for (RunningServiceInfo service: services){
            //  Log.d(TAG, "Matching service: "+service.process +" with app: "+ appName + " service pkg: " + service.clientPackage + " label: " + service.clientLabel);
            String pname = service.process;
            int idx = pname.indexOf(":");
            if (idx > 0)
                pname = pname.substring(0, idx);
            if (pname.equals(appName))
                return true;
        }

        return false;
    }

    public static boolean isSettingsSuggestion(Context context, String appName) {
        // TODO: fill in (if everything is a suggestion, and no need for checking at the client side, then remove this method)
        return true;
    }

    /**
     * Used to clear the runningAppInfo WeakReference list when Carat is paused, so that it is refreshed when the process list view is shown.
     */
    public static void resetRunningProcessInfo() {
        runningAppInfo = null;
    }

    /**
     * package name to packageInfo map for quick querying.
     */
    static WeakReference<Map<String, PackageInfo>> packages = null;

//    /**
//     * Returns true if an application should be hidden in the UI. Uses the blacklist downloaded from Carat servers.
//     * @param c the Context
//     * @param processName the process name
//     * @return true if the process should be hidden from the user, usually because it is an unkillable application that belongs to the system.
//     */
//    public static boolean isHidden(Context c, String processName) {
//        boolean isSystem = isSystem(c, processName);
//        boolean blocked = isDisabled(c, processName) || (isSystem && !isWhiteListed(c, processName));
//        return blocked || isBlacklisted(c, processName);
//    }
//
//    /**
//     * We currently do not employ a whitelist, so this returns true iff isBlacklisted(c, processName) returns false and vice versa.
//     *
//     * @param c the Context.
//     * @param processName the process name.
//     * @return true iff isBlacklisted(c, processName) returns false and vice versa.
//     */
//    private static boolean isWhiteListed(Context c, String processName) {
//        return !isBlacklisted(c, processName);
//    }
//
//    /**
//     * Returns true if the processName matches an intem on the blacklist downloaded from Carat servers.
//     *
//     * @param c the Context.
//     * @param processName the process name.
//     * @return true if the processName matches an intem on the blacklist downloaded from Carat servers.
//     */
//    private static boolean isBlacklisted(Context c, String processName) {
//		/*
//		 * Whitelist: Messaging, Voice Search, Bluetooth Share
//		 *
//		 * Blacklist: Key chain, google partner set up, package installer,
//		 * package access helper
//		 */
//        if (GreenHub.getStorage() != null) {
//            List<String> blacklist = GreenHub.getStorage().getBlacklist();
//            if (blacklist != null && blacklist.size() > 0 && processName != null && blacklist.contains(processName)) {
//                return true;
//            }
//
//            blacklist = GreenHub.getStorage().getGloblist();
//            if (blacklist != null && blacklist.size() > 0 && processName != null) {
//                for (String glob : blacklist) {
//                    if (glob == null)
//                        continue;
//                    // something*
//                    if (glob.endsWith("*") && processName.startsWith(glob.substring(0, glob.length() - 1)))
//                        return true;
//                    // *something
//                    if (glob.startsWith("*") && processName.endsWith(glob.substring(1)))
//                        return true;
//                }
//            }
//        }
//        String label = GreenHub.labelForApp(c, processName);
//
//        if (processName != null && label != null && label.equals(processName)) {
//            // Log.v("Hiding uninstalled", processName);
//            return true;
//        }
//
//        // FlurryAgent.logEvent("Whitelisted "+processName + " \""+ label+"\"");
//        return false;
//    }
//
    /**
     * Returns true if the application is preinstalled on the device.
     * This usually means it is a system application, e.g. Key chain, google partner set up, package installer, package access helper.
     * We currently do not filter these out, because some of them are killable by the user, and not part of the core system, even if they are preinstalled on the device.
     * @param context the Context.
     * @param processName the process name.
     * @return true if the application is preinstalled on the device.
     */
    private static boolean isSystem(Context context, String processName) {
        PackageInfo pak = getPackageInfo(context, processName);
        if (pak != null) {
            ApplicationInfo i = pak.applicationInfo;
            int flags = i.flags;
            boolean isSystemApp = (flags & ApplicationInfo.FLAG_SYSTEM) > 0;
            isSystemApp = isSystemApp || (flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0;
            // Log.v(STAG, processName + " is System app? " + isSystemApp);
            return isSystemApp;
        }
        return false;
    }
//
//    public static boolean isDisabled(Context c, String processName) {
//        PackageManager pm = c.getPackageManager();
//        if (pm == null)
//            return false;
//        try {
//            ApplicationInfo info = pm.getApplicationInfo(processName, 0);
//            boolean disabled = !info.enabled;
//            /* If an app is disabled, schedule it for sending with the next sample.
//             * This is triggered in the UI, so the amount of times that an app being
//             * disabled is sent is limited to the number of times the user refreshes Carat
//             * between two analysis runs. Disabled applications will then be recorded by
//             * the analysis, and not sent to the client when they ask for hogs/bugs after that.
//             * Over time, Carat then follows users' Hogs and Bugs better, knowing which apps are
//             * disabled.
//             */
//            if (disabled) {
//                if (Constants.DEBUG)
//                    Log.i(STAG, "DISABLED: " + processName);
//                SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext()).edit();
//                e.putBoolean(Inspector.DISABLED + processName, true).commit();
//            }
//            return disabled;
//        } catch (PackageManager.NameNotFoundException e) {
//            if (Constants.DEBUG)
//                Log.d(STAG, "Could not find app info for: "+processName);
//        }
//        return false;
//    }

    /**
     * Helper to ensure the WeakReferenced `packages` is populated.
     *
     * @param context
     * @return The content of `packages` or null in case of failure.
     */
    private static Map<String, PackageInfo> getPackages(Context context) {
        List<android.content.pm.PackageInfo> packagelist = null;

        if (packages == null || packages.get() == null || packages.get().size() == 0) {
            Map<String, PackageInfo> mp = new HashMap<>();
            PackageManager pm = context.getPackageManager();
            if (pm == null)
                return null;

            try {
                if (collectSignatures)
                    packagelist = pm.getInstalledPackages(PackageManager.GET_SIGNATURES | PackageManager.GET_PERMISSIONS);
                else
                    packagelist = pm.getInstalledPackages(0);
            } catch (Throwable th) {
                // Forget about it...
            }
            if (packagelist == null)
                return null;
            for (PackageInfo pak : packagelist) {
                if (pak == null || pak.applicationInfo == null || pak.applicationInfo.processName == null)
                    continue;
                mp.put(pak.applicationInfo.processName, pak);
            }

            packages = new WeakReference<>(mp);

            if (mp.size() == 0) // mp == null
                return null;

            return mp;
        } else {
            if (packages == null)
                return null;
            Map<String, PackageInfo> p = packages.get();
            if (p == null || p.size() == 0)
                return null;

            return p;
        }
    }

    /**
     * Get info for a single package from the WeakReferenced packagelist.
     *
     * @param context
     * @param processName
     *            The package to get info for.
     * @return info for a single package from the WeakReferenced packagelist.
     */
    public static PackageInfo getPackageInfo(Context context, String processName) {
        Map<String, PackageInfo> mp = getPackages(context);
        if ((mp == null) || !mp.containsKey(processName))
            return null;

        return mp.get(processName);
    }

    /**
     * Returns a list of installed packages on the device. Will be called for
     * the first Carat sample on a phone, to get signatures for the malware
     * detection project. Later on, single package information is got by
     * receiving the package installed intent.
     *
     * @param context
     * @param filterSystem
     *            if true, exclude system packages.
     * @return a list of installed packages on the device.
     */
    public static Map<String, ProcessInfo> getInstalledPackages(Context context, boolean filterSystem) {
        Map<String, PackageInfo> packageMap = getPackages(context);
        PackageManager pm = context.getPackageManager();
        if (pm == null)
            return null;

        Map<String, ProcessInfo> result = new HashMap<>();

        assert packageMap != null;
        for (Map.Entry<String, PackageInfo> pentry : packageMap.entrySet()) {
            try {
                String pkg = pentry.getKey();
                PackageInfo pak = pentry.getValue();
                if (pak != null) {
                    int vc = pak.versionCode;
                    ApplicationInfo appInfo = pak.applicationInfo;
                    String label = pm.getApplicationLabel(appInfo).toString();
                    // we need application UID to be able to use Android's
                    // TrafficStat API
                    // in order to get the traffic info of a particular app:
                    int appUid = appInfo.uid;
                    // get the amount of transmitted and received bytes by an
                    // app
                    // TODO: disabled for debugging
//					TrafficRecord trafficRecord = getAppTraffic(appUid);

                    int flags = pak.applicationInfo.flags;
                    // Check if it is a system app
                    boolean isSystemApp = (flags & ApplicationInfo.FLAG_SYSTEM) > 0;
                    isSystemApp = isSystemApp || (flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0;
                    if (filterSystem & isSystemApp)
                        continue;
                    if (pak.signatures.length > 0) {
                        List<String> sigList = getSignatures(pak);
                        ProcessInfo pi = new ProcessInfo();
                        pi.setpName(pkg);
                        pi.setApplicationLabel(label);
                        pi.setVersionCode(vc);
                        pi.setpId(-1);
                        pi.setSystemApp(isSystemApp);
                        pi.setAppSignatures(sigList);
                        pi.setImportance(Constants.IMPORTANCE_NOT_RUNNING);
                        pi.setInstallationPkg(pm.getInstallerPackageName(pkg));
                        pi.setVersionName(pak.versionName);
                        //TODO: disbaled for debugging
//						pi.setTrafficRecord(trafficRecord);
                        result.put(pkg, pi);
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Returns info about an installed package. Will be called when receiving
     * the PACKAGE_ADDED or PACKAGE_REPLACED intent.
     *
     * @param context
     * @param pkg
     *            if true, exclude system packages.
     * @return a list of installed packages on the device.
     */
    public static ProcessInfo getInstalledPackage(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        if (pm == null)
            return null;
        PackageInfo pak;
        try {
            pak = pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES | PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        if (pak == null)
            return null;

        ProcessInfo pi = new ProcessInfo();
        int vc = pak.versionCode;
        ApplicationInfo info = pak.applicationInfo;
        String label = pm.getApplicationLabel(info).toString();
        int flags = pak.applicationInfo.flags;
        // Check if it is a system app
        boolean isSystemApp = (flags & ApplicationInfo.FLAG_SYSTEM) > 0;
        isSystemApp = isSystemApp || (flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0;

        if (pak.signatures.length > 0) {
            List<String> sigList;
            sigList = getSignatures(pak);
            pi.setpName(pkg);
            pi.setApplicationLabel(label);
            pi.setVersionCode(vc);
            pi.setpId(-1);
            pi.setSystemApp(isSystemApp);
            pi.setAppSignatures(sigList);
            pi.setImportance(Constants.IMPORTANCE_NOT_RUNNING);
            pi.setInstallationPkg(pm.getInstallerPackageName(pkg));
            pi.setVersionName(pak.versionName);
        }
        return pi;
    }

    /**
     * Returns a List of ProcessInfo objects, helper for getSample.
     *
     * @param context the Context.
     * @return a List of ProcessInfo objects, helper for getSample.
     */
    private static List<ProcessInfo> getRunningProcessInfoForSample(Context context) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);

        // Reset list for each sample
        runningAppInfo = null;
        List<ProcessInfo> list;
        list = getRunningAppInfo(context);
        ArrayList<ProcessInfo> result = new ArrayList<>();

        PackageManager pm = context.getPackageManager();
        // Collected in the same loop to save computation.
        int[] procMem = new int[list.size()];

        Set<String> procs = new HashSet<>();

        boolean inst = p.getBoolean(Constants.PREFERENCE_SEND_INSTALLED_PACKAGES, true);

        Map<String, ProcessInfo> ipkg = null;
        if (inst)
            ipkg = getInstalledPackages(context, false);

        for (ProcessInfo pi : list) {
            String pname = pi.getpName();
            if (ipkg != null && ipkg.containsKey(pname))
                ipkg.remove(pname);
            procs.add(pname);
            ProcessInfo item = new ProcessInfo();
            PackageInfo pak = getPackageInfo(context, pname);
            if (pak != null) {
                String ver = pak.versionName;
                int vc = pak.versionCode;
                item.setVersionName(ver);
                item.setVersionCode(vc);
                ApplicationInfo info = pak.applicationInfo;

                // Human readable label (if any)
                String label = pm.getApplicationLabel(info).toString();
                if (label.length() > 0)
                    item.setApplicationLabel(label);
                int flags = pak.applicationInfo.flags;
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
            item.setpName(pname);

            String installationSource = null;
            if (!pi.isSystemApp()) {
                try {
                    // Log.w(STAG, "Calling getInstallerPackageName with: " +
                    // pname);
                    installationSource = pm.getInstallerPackageName(pname);
                } catch (IllegalArgumentException iae) {
                    Log.e(STAG, "Could not get installer for " + pname);
                }
            }
            if (installationSource == null)
                installationSource = "null";
            item.setInstallationPkg(installationSource);

            // procMem[list.indexOf(pi)] = pi.getPId();
            // FIXME: More fields will need to be added here, but ProcessInfo
            // needs to change.
			/*
			 * uid lru
			 */
            // add to result
            result.add(item);
        }

        // Send installed packages if we were to do so.
        if (ipkg != null && ipkg.size() > 0) {
            result.addAll(ipkg.values());
            p.edit().putBoolean(Constants.PREFERENCE_SEND_INSTALLED_PACKAGES, false).apply();
        }

        // Go through the preferences and look for UNINSTALL, INSTALL and
        // REPLACE keys set by InstallReceiver.
        Set<String> ap = p.getAll().keySet();
        SharedPreferences.Editor e = p.edit();
        boolean edited = false;
        for (String pref : ap) {
            if (pref.startsWith(INSTALLED)) {
                String pname = pref.substring(INSTALLED.length());
                boolean installed = p.getBoolean(pref, false);
                if (installed) {
                    Log.i(STAG, "Installed:" + pname);
                    ProcessInfo i = getInstalledPackage(context, pname);
                    if (i != null) {
                        i.setImportance(Constants.IMPORTANCE_INSTALLED);
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
                    ProcessInfo i = getInstalledPackage(context, pname);
                    if (i != null) {
                        i.setImportance(Constants.IMPORTANCE_REPLACED);
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
                    result.add(uninstalledItem(pname, pref, e));
                    edited = true;
                }
            } else if (pref.startsWith(DISABLED)) {
                String pname = pref.substring(DISABLED.length());
                boolean disabled = p.getBoolean(pref, false);
                if (disabled) {
                    Log.i(STAG, "Disabled app:" + pname);
                    result.add(disabledItem(pname, pref, e));
                    edited = true;
                }
            }
        }
        if (edited)
            e.apply();

        // FIXME: These are not used yet.
		/*
		 * ActivityManager pActivityManager = (ActivityManager) context
		 * .getSystemService(Activity.ACTIVITY_SERVICE); Debug.MemoryInfo[]
		 * memoryInfo = pActivityManager .getProcessMemoryInfo(procMem); for
		 * (Debug.MemoryInfo info : memoryInfo) { // Decide which ones of info.*
		 * we want, add to a new and improved // ProcessInfo object // FIXME:
		 * Not used yet, Sample needs more fields // FIXME: Which memory fields
		 * to choose? //int memory = info.dalvikPrivateDirty; }
		 */

        return result;
    }

    /**
     * Helper to set application to the uninstalled state in the Carat sample.
     * @param pname the package that was uninstalled.
     * @param pref The preference that stored the uninstallation directive. This preference will be deleted to ensure uninstallations are not sent multiple times.
     * @param e the Editor (passed and not created here for efficiency)
     * @return a new ProcessInfo entry describing the uninstalled item.
     */
    private static ProcessInfo uninstalledItem(String pname, String pref, SharedPreferences.Editor e) {
        ProcessInfo item = new ProcessInfo();
        item.setpName(pname);
        List<String> sigs = new LinkedList<>();
        sigs.add("uninstalled");
        item.setAppSignatures(sigs);
        item.setpId(-1);
        item.setImportance(Constants.IMPORTANCE_UNINSTALLED);
        // Remember to remove it so we do not send
        // multiple uninstall events
        e.remove(pref);
        return item;
    }

    /**
     * Helper to set application to the disabled state in the Carat sample.
     * @param pname the package that was disabled.
     * @param pref The preference that stored the disabled directive. This preference will be deleted to ensure disabled apps are not sent multiple times.
     * @param e the Editor (passed and not created here for efficiency)
     * @return a new ProcessInfo entry describing the uninstalled item.
     */
    private static ProcessInfo disabledItem(String pname, String pref, SharedPreferences.Editor e) {
        ProcessInfo item = new ProcessInfo();
        item.setpName(pname);
        item.setpId(-1);
        item.setImportance(Constants.IMPORTANCE_DISABLED);
        // Remember to remove it so we do not send
        // multiple uninstall events
        e.remove(pref);

        return item;
    }
//
    /**
     * Depratecated, use int[] meminfo = readMemInfo(); int totalMemory =
     * meminfo[0] + meminfo[1];
     */
    @Deprecated
    public static String getMemoryInfo() {
        String tmp = null;
        BufferedReader br = null;

        try {
            File file = new File("/proc/meminfo");
            FileInputStream in = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(in), READ_BUFFER_SIZE);

        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            tmp = (br != null) ? br.readLine() : null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        StringBuilder sMemory = new StringBuilder();
        sMemory.append(tmp);

        try {
            tmp = (br != null) ? br.readLine() : null;
            if (br != null)
                br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        sMemory.append("\n").append(tmp).append("\n");

        return  "Memory Status:\n" + sMemory;
    }

    /*
     * Deprecated, use readMemInfo()[1]
     */
    @Deprecated
    public static String getMemoryFree() {
        String tmp = null;
        BufferedReader br = null;

        try {
            File file = new File("/proc/meminfo");
            FileInputStream in = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(in), READ_BUFFER_SIZE);

        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            tmp = (br != null) ? br.readLine() : null;
            tmp = (br != null) ? br.readLine() : null;
            if (tmp != null) {
                // split by whitespace and take 2nd element, so that in:
                // MemoryFree: x kb
                // the x remains.
                String[] arr = tmp.split("\\s+");
                if (arr.length > 1)
                    tmp = arr[1];
            }
            if (br != null)
                br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return tmp;
    }

    /**
     * Return time in seconds since last boot.
     */
    public static double getUptime() {
        long uptime = SystemClock.elapsedRealtime();
		/*
		 * int seconds = (int) (uptime / 1000) % 60; int minutes = (int) (uptime
		 * / (1000 * 60) % 60); int hours = (int) (uptime / (1000 * 60 * 60) %
		 * 24); String tmp = "\nThe uptime is :" + hours + "hr:" + minutes +
		 * "mins:" + seconds + "sec.\n"; return tmp;
		 */
        Log.v("uptime", String.valueOf(uptime));

        return uptime / 1000.0;
    }

    /**
     * Get the network status, one of connected, disconnected, connecting, or disconnecting.
     * @param context the Context.
     * @return the network status, one of connected, disconnected, connecting, or disconnecting.
     */
    public static String getNetworkStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return NETWORKSTATUS_DISCONNECTED;
        NetworkInfo i = cm.getActiveNetworkInfo();
        if (i == null)
            return NETWORKSTATUS_DISCONNECTED;
        NetworkInfo.State s = i.getState();
        if (s == NetworkInfo.State.CONNECTED)
            return NETWORKSTATUS_CONNECTED;
        if (s == NetworkInfo.State.DISCONNECTED)
            return NETWORKSTATUS_DISCONNECTED;
        if (s == NetworkInfo.State.CONNECTING)
            return NETWORKSTATUS_CONNECTING;
        if (s == NetworkInfo.State.DISCONNECTING)
            return NETWORKSTATUS_DISCONNECTING;
        else
            return NETWORKSTATUS_DISCONNECTED;
    }


    /**
     * Get the network type, for example Wifi, mobile, wimax, or none.
     *
     * @param context
     * @return
     */
    public static String getNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return TYPE_UNKNOWN;
        NetworkInfo i = cm.getActiveNetworkInfo();
        if (i == null)
            return TYPE_UNKNOWN;

        return i.getTypeName();
    }

    /**
     * Returns true if the Internet is reachable.
     * @param c the Context
     * @return true if the Internet is reachable.
     */
    public static boolean networkAvailable(Context c) {
        return getNetworkStatus(c).equals(NETWORKSTATUS_CONNECTED);
    }

    /* Get current WiFi signal Strength */
    public static int getWifiSignalStrength(Context context) {
        WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
        // Log.v("WifiRssi", "Rssi:" + wifiRssi);

        return myWifiInfo.getRssi();

    }

    /**
     * Get Wifi MAC ADDR. Hashed and used in UUID calculation.
     */
    private static String getWifiMacAddress(Context context) {
        WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (myWifiManager == null)
            return null;
        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
        if (myWifiInfo == null)
            return null;
        return myWifiInfo.getMacAddress();
    }

    /* Get current WiFi link speed */
    public static int getWifiLinkSpeed(Context context) {
        WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
        // Log.v("linkSpeed", "Link speed:" + linkSpeed);

        return myWifiInfo.getLinkSpeed();
    }

    /* Check whether WiFi is enabled */
    public static boolean getWifiEnabled(Context context) {
        WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // Log.v("WifiEnabled", "Wifi is enabled:" + wifiEnabled);

        return myWifiManager.isWifiEnabled();
    }

    /* Get Wifi state: */
    public static String getWifiState(Context context) {
        WifiManager myWifiManager;
        myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int wifiState = myWifiManager.getWifiState();
        switch (wifiState) {
            case WifiManager.WIFI_STATE_DISABLED:
                return WIFI_STATE_DISABLED;
            case WifiManager.WIFI_STATE_DISABLING:
                return WIFI_STATE_DISABLING;
            case WifiManager.WIFI_STATE_ENABLED:
                return WIFI_STATE_ENABLED;
            case WifiManager.WIFI_STATE_ENABLING:
                return WIFI_STATE_ENABLING;
            default:
                return WIFI_STATE_UNKNOWN;
        }
    }

    public static WifiInfo getWifiInfo(Context context) {
        WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        return myWifiManager.getConnectionInfo();
    }

    /*
     * This method is deprecated. As of ICE_CREAM_SANDWICH, availability of
     * background data depends on several combined factors, and this method will
     * always return true. Instead, when background data is unavailable,
     * getActiveNetworkInfo() will now appear disconnected.
     */
	/* Check whether background data are enabled */
    @Deprecated
    public static boolean getBackgroundDataEnabled(Context context) {
        boolean bacDataEnabled = false;
        try {
            if (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.BACKGROUND_DATA) == 1) {
                bacDataEnabled = true;
            }
        } catch (Settings.SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Log.v("BackgroundDataEnabled", "Background data enabled? " +
        // bacDataEnabled);
        // return bacDataEnabled;
        return true;
    }

    /* Get Current Screen Brightness Value */
    public static int getScreenBrightness(Context context) {

        int screenBrightnessValue = 0;
        try {
            screenBrightnessValue = android.provider.Settings.System.getInt(context.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Log.v("ScreenBrightness", "Screen brightness value:" +
        // screenBrightnessValue);
        return screenBrightnessValue;
    }

    public static boolean isAutoBrightness(Context context) {
        boolean autoBrightness = false;
        try {
            autoBrightness = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        // Log.v("AutoScreenBrightness",
        // "Automatic Screen brightness mode is enabled:" + autoBrightness);
        return autoBrightness;
    }

    /* Check whether GPS are enabled */
    public static boolean getGpsEnabled(Context context) {
        boolean gpsEnabled = false;
        LocationManager myLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        gpsEnabled = myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // Log.v("GPS", "GPS is :" + gpsEnabled);
        return gpsEnabled;
    }

    /* check the GSM cell information */
    public static CellInfo getCellInfo(Context context) {
        CellInfo curCell = new CellInfo();

        TelephonyManager myTelManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String netOperator = myTelManager.getNetworkOperator();

        // Fix crash when not connected to network (airplane mode, underground,
        // etc)
        if (netOperator == null || netOperator.length() < 3) {
            return curCell;
        }

		/*
		 * FIXME: Actually check for mobile network status == connected before
		 * doing this stuff.
		 */

        if (Inspector.getPhoneType(context) == PHONE_TYPE_CDMA) {
            CdmaCellLocation cdmaLocation = (CdmaCellLocation) myTelManager.getCellLocation();
            if (cdmaLocation == null) {
                // Log.v("cdmaLocation", "CDMA Location:" + cdmaLocation);
            } else {
                int cid = cdmaLocation.getBaseStationId();
                int lac = cdmaLocation.getNetworkId();
                int mnc = cdmaLocation.getSystemId();
                int mcc = Integer.parseInt(netOperator.substring(0, 3));

                curCell.CID = cid;
                curCell.LAC = lac;
                curCell.MNC = mnc;
                curCell.MCC = mcc;
                curCell.radioType = Inspector.getMobileNetworkType(context);

                // Log.v("MCC", "MCC is:" + mcc);
                // Log.v("MNC", "MNC is:" + mnc);
                // Log.v("CID", "CID is:" + cid);
                // Log.v("LAC", "LAC is:" + lac);
            }

        } else if (Inspector.getPhoneType(context) == PHONE_TYPE_GSM) {
            GsmCellLocation gsmLocation = (GsmCellLocation) myTelManager.getCellLocation();

            if (gsmLocation == null) {
                // Log.v("gsmLocation", "GSM Location:" + gsmLocation);
            } else {
                int cid = gsmLocation.getCid();
                int lac = gsmLocation.getLac();
                int mcc = Integer.parseInt(netOperator.substring(0, 3));
                int mnc = Integer.parseInt(netOperator.substring(3));

                curCell.MCC = mcc;
                curCell.MNC = mnc;
                curCell.LAC = lac;
                curCell.CID = cid;
                curCell.radioType = Inspector.getMobileNetworkType(context);

                // Log.v("MCC", "MCC is:" + mcc);
                // Log.v("MNC", "MNC is:" + mnc);
                // Log.v("CID", "CID is:" + cid);
                // Log.v("LAC", "LAC is:" + lac);
            }
        }
        return curCell;
    }

    /**
     * Return distance between <code>lastKnownLocation</code> and a newly
     * obtained location from any available provider.
     *
     * @param c
     *            from Intent or Application.
     * @return
     */
    public static double getDistance(Context c) {
        Location l = getLastKnownLocation(c);
        double distance = 0.0;
        if (lastKnownLocation != null && l != null) {
            distance = lastKnownLocation.distanceTo(l);
        }
        lastKnownLocation = l;
        return distance;
    }

    public static Location getLastKnownLocation(Context c) {
        String provider = getBestProvider(c);
        // FIXME: Some buggy device is giving GPS to us, even though we cannot
        // use it.
        if (provider != null && !provider.equals("gps")) {
            Location l = getLastKnownLocation(c, provider);
            return l;
        }
        return null;
    }

    private static Location getLastKnownLocation(Context context, String provider) {
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.getLastKnownLocation(provider);
        }
        catch (SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* Get the distance users between two locations */
    public static double getDistance(double startLatitude, double startLongitude, double endLatitude,
                                     double endLongitude) {
        float[] results = new float[1];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
        return results[0];
    }

    /**
     * Return a list of enabled LocationProviders, such as GPS, Network, etc.
     *
     * @param context
     *            from onReceive or app.
     * @return
     */
    public static List<String> getEnabledLocationProviders(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return lm.getProviders(true);
    }

    public static String getBestProvider(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_COARSE);
        c.setPowerRequirement(Criteria.POWER_LOW);
        String provider = lm.getBestProvider(c, true);
        return provider;
    }

    /* Check the maximum number of satellites can be used in the satellite list */
    public static int getMaxNumSatellite(Context context) {

        // LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        // int maxNumSatellite = gpsStatus.getMaxSatellites();

        // Log.v("maxNumStatellite", "Maximum number of satellites:" +
        // maxNumSatellite);
        return 0; // maxNumSatellite;
    }

    /* Get call status */
    public static String getCallState(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        int callState = telManager.getCallState();
        switch (callState) {
            case TelephonyManager.CALL_STATE_OFFHOOK:
                return CALL_STATE_OFFHOOK;
            case TelephonyManager.CALL_STATE_RINGING:
                return CALL_STATE_RINGING;
            default:
                return CALL_STATE_IDLE;
        }
    }

    private static String getDeviceId(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telManager == null)
            return null;
        return telManager.getDeviceId();
    }

    /* Get network type */
    public static String getMobileNetworkType(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        int netType = telManager.getNetworkType();
        switch (netType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return NETWORK_TYPE_1xRTT;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return NETWORK_TYPE_CDMA;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return NETWORK_TYPE_EDGE;
            case EHRPD:
                return NETWORK_TYPE_EHRPD;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return NETWORK_TYPE_EVDO_0;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return NETWORK_TYPE_EVDO_A;
            case EVDO_B:
                return NETWORK_TYPE_EVDO_B;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return NETWORK_TYPE_GPRS;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return NETWORK_TYPE_HSDPA;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return NETWORK_TYPE_HSPA;
            case HSPAP:
                return NETWORK_TYPE_HSPAP;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return NETWORK_TYPE_HSUPA;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_TYPE_IDEN;
            case LTE:
                return NETWORK_TYPE_LTE;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return NETWORK_TYPE_UMTS;
            default:
                // If we don't know the type, just return the number and let the
                // backend take care of it
                return netType + "";
        }
    }

    /* Get Phone Type */
    public static String getPhoneType(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        int phoneType = telManager.getPhoneType();
        switch (phoneType) {
            case TelephonyManager.PHONE_TYPE_CDMA:
                return PHONE_TYPE_CDMA;
            case TelephonyManager.PHONE_TYPE_GSM:
                return PHONE_TYPE_GSM;
            default:
                return PHONE_TYPE_NONE;
        }
    }

    /* Check is it network roaming */
    public static boolean getRoamingStatus(Context context) {
        boolean roamStatus = false;

        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        roamStatus = telManager.isNetworkRoaming();
        // Log.v("RoamingStatus", "Roaming status:" + roamStatus);
        return roamStatus;
    }

    /* Get data state */
    public static String getDataState(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        int dataState = telManager.getDataState();
        switch (dataState) {
            case TelephonyManager.DATA_CONNECTED:
                return DATA_CONNECTED;
            case TelephonyManager.DATA_CONNECTING:
                return DATA_CONNECTING;
            case TelephonyManager.DATA_DISCONNECTED:
                return DATA_DISCONNECTED;
            default:
                return DATA_SUSPENDED;
        }
    }

    /* Get data activity */
    public static String getDataActivity(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        int dataActivity = telManager.getDataActivity();
        switch (dataActivity) {
            case TelephonyManager.DATA_ACTIVITY_IN:
                return DATA_ACTIVITY_IN;
            case TelephonyManager.DATA_ACTIVITY_OUT:
                return DATA_ACTIVITY_OUT;
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                return DATA_ACTIVITY_INOUT;
            default:
                return DATA_ACTIVITY_NONE;
        }
    }

    /* Get the current location of the device */
    public static CellLocation getDeviceLocation(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        return telManager.getCellLocation();
    }

    /**
     * Return a long[3] with incoming call time, outgoing call time, and
     * non-call time in seconds since boot.
     *
     * @param context
     *            from onReceive or Activity
     * @return a long[3] with incoming call time, outgoing call time, and
     *         non-call time in seconds since boot.
     */
    public static long[] getCalltimesSinceBoot(Context context) {

        long[] result = new long[3];

        long callInSeconds = 0;
        long callOutSeconds = 0;
        int type;
        long dur;

        // ms since boot
        long uptime = SystemClock.elapsedRealtime();
        long now = System.currentTimeMillis();
        long bootTime = now - uptime;

        String[] queries = new String[] { android.provider.CallLog.Calls.TYPE, android.provider.CallLog.Calls.DATE,
                android.provider.CallLog.Calls.DURATION };

        Cursor cur = null;

        try {
            cur = context.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI, queries,
                    android.provider.CallLog.Calls.DATE + " > " + bootTime, null,
                    android.provider.CallLog.Calls.DATE + " ASC");
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }

        if (cur != null) {
            if (cur.moveToFirst()) {
                while (!cur.isAfterLast()) {
                    type = cur.getInt(0);
                    dur = cur.getLong(2);
                    switch (type) {
                        case android.provider.CallLog.Calls.INCOMING_TYPE:
                            callInSeconds += dur;
                            break;
                        case android.provider.CallLog.Calls.OUTGOING_TYPE:
                            callOutSeconds += dur;
                            break;
                        default:
                    }
                    cur.moveToNext();
                }
            } else {
                Log.w("CallDurFromBoot", "No calls listed");
            }
            cur.close();
        } else {
            Log.w("CallDurFromBoot", "Cursor is null");
        }

        // uptime is ms, so it needs to be divided by 1000
        long nonCallTime = uptime / 1000 - callInSeconds - callOutSeconds;
        result[0] = callInSeconds;
        result[1] = callOutSeconds;
        result[2] = nonCallTime;
        return result;
    }

    /* Get a monthly call duration record */
    public static Map<String, CallMonth> getMonthCallDur(Context context) {

        Map<String, CallMonth> callMonth = new HashMap<String, CallMonth>();
        Map<String, String> callInDur = new HashMap<String, String>();
        Map<String, String> callOutDur = new HashMap<String, String>();

        int callType;
        long callDur;
        Date callDate;
        String tmpTime = null;
        String time;
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM");
        CallMonth curMonth = null;

        String[] queryFields = new String[] { android.provider.CallLog.Calls.TYPE, android.provider.CallLog.Calls.DATE,
                android.provider.CallLog.Calls.DURATION };

        Cursor myCursor = null;

        try {
            myCursor = context.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI, queryFields,
                    null, null, android.provider.CallLog.Calls.DATE + " DESC");
            assert myCursor != null;

            if (myCursor.moveToFirst()) {
                for (int i = 0; i < myCursor.getColumnCount(); i++) {
                    myCursor.moveToPosition(i);
                    callType = myCursor.getInt(0);
                    callDate = new Date(myCursor.getLong(1));
                    callDur = myCursor.getLong(2);

                    time = dateformat.format(callDate);
                    if (tmpTime != null && !time.equals(tmpTime)) {
                        callMonth.put(tmpTime, curMonth);
                        callInDur.clear();
                        callOutDur.clear();
                        curMonth = new CallMonth();
                    }
                    tmpTime = time;

                    if (callType == 1) {
                        curMonth.tolCallInNum++;
                        curMonth.tolCallInDur += callDur;
                        callInDur.put("tolCallInNum", String.valueOf(curMonth.tolCallInNum));
                        callInDur.put("tolCallInDur", String.valueOf(curMonth.tolCallInDur));
                    }
                    if (callType == 2) {
                        curMonth.tolCallOutNum++;
                        curMonth.tolCallOutDur += callDur;
                        callOutDur.put("tolCallOutNum", String.valueOf(curMonth.tolCallOutNum));
                        callOutDur.put("tolCallOutDur", String.valueOf(curMonth.tolCallOutDur));
                    }
                    if (callType == 3) {
                        curMonth.tolMissedCallNum++;
                        callInDur.put("tolMissedCallNum", String.valueOf(curMonth.tolMissedCallNum));
                    }
                }
            }
        }
        catch (SecurityException | NullPointerException e) {
            e.printStackTrace();
        }

        return callMonth;
    }

    public static CallMonth getCallMonthinfo(Context context, String time) {

        Map<String, CallMonth> callInfo;
        callInfo = Inspector.getMonthCallDur(context);
        CallMonth call = new CallMonth();
        call = callInfo.get(time);
        return call;
    }

    private static Location lastKnownLocation = null;

    /**
     * Get whether the screen is on or off.
     *
     * @return true if the screen is on.
     */
    public static int isScreenOn(Context context) {
        android.os.PowerManager powerManager = (android.os.PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        if (powerManager != null)
            if (powerManager.isScreenOn())
                return 1;
        return 0;
    }

    /**
     * Get the current timezone of the device.
     */

    public static String getTimeZone(Context context) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        return tz.getID();
    }

    /**
     *
     * @param context
     * @return true when app installation from unknown sources is enabled.
     */
    public static int allowUnknownSources(Context context) {
        ContentResolver res = context.getContentResolver();
        int unknownSources = Settings.Secure.getInt(res, Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
        return unknownSources;
    }

    /**
     *
     * @param context
     * @return true when developer mode is enabled.
     */
    public static int isDeveloperModeOn(Context context) {
        ContentResolver res = context.getContentResolver();
        return Settings.Secure.getInt(res, Settings.Secure.ADB_ENABLED, 0);
        // In API level 17, this is Settings.Global.ADB_ENABLED.
    }

	/*
	 * TODO: Make the app running when the system reboots, and provide a stop
	 * button. CPU and Memory info per application CPU core/ frequency, CPU
	 * governors usage How to motivate user to upload more samples.
	 */

    /**
     * Safely terminate (kill) the given app.
     *
     * @param context
     * @param packageName
     * @param label
     * @return
     */
    public static boolean killApp(Context context, String packageName, String label) {
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        if (am != null) {
            try {
                PackageInfo p = getPackageInfo(context, packageName);
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

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte)
                        : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static Sample getSample(Context context, Intent intent, String lastBatteryState) {
        final String TAG = "Inspector.getSample";
        if (Constants.DEBUG)
            Log.d(TAG, "getSample() was invoked.");

        String action = intent.getAction();
        if (Constants.DEBUG)
            Log.d(TAG, "action = " + action);

        // Construct sample and return it in the end
        Sample mySample = new Sample();
        // SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        // p.getString(GreenHub.getRegisteredUuid(), null);
        String uuId = getUuid(context);
        mySample.setUuId(uuId);
        mySample.setTriggeredBy(action);
        // required always
        long now = System.currentTimeMillis();
        mySample.setTimestamp(now / 1000.0);

        // Record first data point for CPU usage
        long[] idleAndCpu1 = readUsagePoint();

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

        int screenBrightness = Inspector.getScreenBrightness(context);
        mySample.setScreenBrightness(screenBrightness);
        boolean autoScreenBrightness = Inspector.isAutoBrightness(context);
        if (autoScreenBrightness)
            mySample.setScreenBrightness(-1); // Auto
        // boolean gpsEnabled = Inspector.getGpsEnabled(context);
        // Location providers
        List<String> enabledLocationProviders = Inspector.getEnabledLocationProviders(context);
        mySample.setLocationProviders(enabledLocationProviders);

        // TODO: not in Sample yet
        // int maxNumSatellite = Inspector.getMaxNumSatellite(context);

        String network = Inspector.getNetworkStatus(context);
        String networkType = Inspector.getNetworkType(context);
        String mobileNetworkType = Inspector.getMobileNetworkType(context);

        // Required in new Carat protocol
        if (network.equals(NETWORKSTATUS_CONNECTED)) {
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
        boolean roamStatus = Inspector.getRoamingStatus(context);
        nd.setRoamingEnabled(roamStatus);
        String dataState = Inspector.getDataState(context);
        nd.setMobileDataStatus(dataState);
        String dataActivity = Inspector.getDataActivity(context);
        nd.setMobileDataActivity(dataActivity);

        // Wifi stuff
        String wifiState = Inspector.getWifiState(context);
        nd.setWifiStatus(wifiState);
        int wifiSignalStrength = Inspector.getWifiSignalStrength(context);
        nd.setWifiSignalStrength(wifiSignalStrength);
        int wifiLinkSpeed = Inspector.getWifiLinkSpeed(context);
        nd.setWifiLinkSpeed(wifiLinkSpeed);
        // Add NetworkDetails substruct to Sample
        mySample.setNetworkDetails(nd);

		/* Calling Information */
        // List<String> callInfo;
        // callInfo=Inspector.getCallInfo(context);
		/* Total call time */
        // long totalCallTime=0;
        // totalCallTime=Inspector.getTotalCallDur(context);

		/*
		 * long[] incomingOutgoingIdle = getCalltimesSinceBoot(context);
		 * Log.d(STAG, "Call time since boot: Incoming=" +
		 * incomingOutgoingIdle[0] + " Outgoing=" + incomingOutgoingIdle[1] +
		 * " idle=" + incomingOutgoingIdle[2]);
		 *
		 * // Summary Call info CallInfo ci = new CallInfo(); String callState =
		 * Inspector.getCallState(context); ci.setCallStatus(callState);
		 * ci.setIncomingCallTime(incomingOutgoingIdle[0]);
		 * ci.setOutgoingCallTime(incomingOutgoingIdle[1]);
		 * ci.setNonCallTime(incomingOutgoingIdle[2]);
		 *
		 * mySample.setCallInfo(ci);
		 */

        // Bundle b = intent.getExtras();

        int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
        // This is really an int.
        // FIXED: Not used yet, Sample needs more fields

        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        String batteryTechnology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);

        // FIXED: Not used yet, Sample needs more fields
        String batteryHealth = "Unknown";
        String batteryStatus = "Unknown";

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

        int[] usedFreeActiveInactive = Inspector.readMeminfo();
        if (usedFreeActiveInactive != null && usedFreeActiveInactive.length == 4) {
            mySample.setMemoryUser(usedFreeActiveInactive[0]);
            mySample.setMemoryFree(usedFreeActiveInactive[1]);
            mySample.setMemoryActive(usedFreeActiveInactive[2]);
            mySample.setMemoryInactive(usedFreeActiveInactive[3]);
        }
        // TODO: Memory Wired should have memory that is "unevictable", that
        // will always be used even when all apps are killed

        // Log.d(STAG, "serial=" + getBuildSerial());

        // Record second data point for cpu/idle time
        now = System.currentTimeMillis();
        long[] idleAndCpu2 = readUsagePoint();

        CpuStatus cs = new CpuStatus();

        cs.setCpuUsage(getUsage(idleAndCpu1, idleAndCpu2));
        cs.setUptime(getUptime());
        mySample.setCpuStatus(cs);

        mySample.setDeveloperMode(isDeveloperModeOn(context));
        mySample.setUnknownSources(allowUnknownSources(context));
        mySample.setScreenOn(isScreenOn(context));
        mySample.setTimeZone(getTimeZone(context));
        // printAverageFeaturePower(context);

        // If there are extra fields, include them into the sample.
        List<Feature> extras = getExtras(context);
        if (extras != null && extras.size() > 0)
            mySample.setExtra(extras);

        return mySample;
    }

    /**
     * Helper method to collect all the extra information we wish to add to the sample into the Extra Feature list.
     * @param context the Context
     * @return a List<Feature> populated with extra items to collect outside of the protocol spec.
     */
    private static List<Feature> getExtras(Context context) {
        LinkedList<Feature> res = new LinkedList<>();
        res.add(getVmVersion(context));
        return res;
    }

    //TODO: disabled for debugging
//	private static TrafficRecord getAppTraffic(Integer uid) {
//		TrafficRecord trafficRecord = new TrafficRecord();
//		trafficRecord.setTx(TrafficStats.getUidTxBytes(uid));
//		trafficRecord.setRx(TrafficStats.getUidRxBytes(uid));
//		return trafficRecord;
//	}

    /**
     * Get the java.vm.version system property as a Feature("vm", version).
     * @param context the Context.
     * @return a Feature instance with the key "vm" and value of the "java.vm.version" system property.
     */
    private static Feature getVmVersion(Context context) {
        String vm = System.getProperty("java.vm.version");
        if (vm == null)
            vm = "";
        Feature vmVersion = new Feature();
        vmVersion.setKey("vm");
        vmVersion.setValue(vm);
        return vmVersion;
    }

    public static List<String> getSignatures(PackageInfo pak) {
        List<String> sigList = new LinkedList<>();
        String[] pmInfos = pak.requestedPermissions;
        if (pmInfos != null) {
            byte[] bytes = getPermissionBytes(pmInfos);
            String hexB = convertToHex(bytes);
            sigList.add(hexB);
        }
        Signature[] sigs = pak.signatures;

        for (Signature s : sigs) {
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("SHA-1");
                md.update(s.toByteArray());
                byte[] dig = md.digest();
                // Add SHA-1
                sigList.add(convertToHex(dig));

                CertificateFactory fac = CertificateFactory.getInstance("X.509");
                if (fac == null)
                    continue;
                X509Certificate cert = (X509Certificate) fac.generateCertificate(new ByteArrayInputStream(s
                        .toByteArray()));
                if (cert == null)
                    continue;
                PublicKey pkPublic = cert.getPublicKey();
                if (pkPublic == null)
                    continue;
                String al = pkPublic.getAlgorithm();
                if (al.equals("RSA")) {
                    md = MessageDigest.getInstance("SHA-256");
                    RSAPublicKey rsa = (RSAPublicKey) pkPublic;
                    byte[] data = rsa.getModulus().toByteArray();
                    if (data[0] == 0) {
                        byte[] copy = new byte[data.length - 1];
                        System.arraycopy(data, 1, copy, 0, data.length - 1);
                        md.update(copy);
                    } else
                        md.update(data);
                    dig = md.digest();
                    // Add SHA-256 of modulus
                    sigList.add(convertToHex(dig));
                } else if (al.equals("DSA")) {
                    DSAPublicKey dsa = (DSAPublicKey) pkPublic;
                    md = MessageDigest.getInstance("SHA-256");
                    byte[] data = dsa.getY().toByteArray();
                    if (data[0] == 0) {
                        byte[] copy = new byte[data.length - 1];
                        System.arraycopy(data, 1, copy, 0, data.length - 1);
                        md.update(copy);
                    } else
                        md.update(data);
                    dig = md.digest();
                    // Add SHA-256 of public key (DSA)
                    sigList.add(convertToHex(dig));
                } else {
                    Log.e("Inspector", "Weird algorithm: " + al + " for " + pak.packageName);
                }
            } catch (NoSuchAlgorithmException | CertificateException e) {
                // Do nothing
            }

        }
        return sigList;
    }

    public static byte[] getPermissionBytes(String[] perms) {
        if (perms == null)
            return null;
        if (permList.size() == 0)
            populatePermList();
        // Log.i(STAG, "PermList Size: " + permList.size());
        byte[] bytes = new byte[permList.size() / 8 + 1];
        for (String p : perms) {
            int idx = permList.indexOf(p);
            if (idx > 0) {
                int i = idx / 8;
                idx = (int) Math.pow(2, idx - i * 8);
                bytes[i] = (byte) (bytes[i] | idx);
            }
        }
        return bytes;
    }

    private static final ArrayList<String> permList = new ArrayList<String>();

    private static void populatePermList() {
        final String[] permArray = { "android.permission.ACCESS_CHECKIN_PROPERTIES",
                "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS", "android.permission.ACCESS_MOCK_LOCATION",
                "android.permission.ACCESS_NETWORK_STATE", "android.permission.ACCESS_SURFACE_FLINGER",
                "android.permission.ACCESS_WIFI_STATE", "android.permission.ACCOUNT_MANAGER",
                "android.permission.AUTHENTICATE_ACCOUNTS", "android.permission.BATTERY_STATS",
                "android.permission.BIND_APPWIDGET", "android.permission.BIND_DEVICE_ADMIN",
                "android.permission.BIND_INPUT_METHOD", "android.permission.BIND_WALLPAPER",
                "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN", "android.permission.BRICK",
                "android.permission.BROADCAST_PACKAGE_REMOVED", "android.permission.BROADCAST_SMS",
                "android.permission.BROADCAST_STICKY", "android.permission.BROADCAST_WAP_PUSH",
                "android.permission.CALL_PHONE", "android.permission.CALL_PRIVILEGED", "android.permission.CAMERA",
                "android.permission.CHANGE_COMPONENT_ENABLED_STATE", "android.permission.CHANGE_CONFIGURATION",
                "android.permission.CHANGE_NETWORK_STATE", "android.permission.CHANGE_WIFI_MULTICAST_STATE",
                "android.permission.CHANGE_WIFI_STATE", "android.permission.CLEAR_APP_CACHE",
                "android.permission.CLEAR_APP_USER_DATA", "android.permission.CONTROL_LOCATION_UPDATES",
                "android.permission.DELETE_CACHE_FILES", "android.permission.DELETE_PACKAGES",
                "android.permission.DEVICE_POWER", "android.permission.DIAGNOSTIC",
                "android.permission.DISABLE_KEYGUARD", "android.permission.DUMP",
                "android.permission.EXPAND_STATUS_BAR", "android.permission.FACTORY_TEST",
                "android.permission.FLASHLIGHT", "android.permission.FORCE_BACK", "android.permission.GET_ACCOUNTS",
                "android.permission.GET_PACKAGE_SIZE", "android.permission.GET_TASKS",
                "android.permission.GLOBAL_SEARCH", "android.permission.HARDWARE_TEST",
                "android.permission.INJECT_EVENTS", "android.permission.INSTALL_LOCATION_PROVIDER",
                "android.permission.INSTALL_PACKAGES", "android.permission.INTERNAL_SYSTEM_WINDOW",
                "android.permission.INTERNET", "android.permission.KILL_BACKGROUND_PROCESSES",
                "android.permission.MANAGE_ACCOUNTS", "android.permission.MANAGE_APP_TOKENS",
                "android.permission.MASTER_CLEAR", "android.permission.MODIFY_AUDIO_SETTINGS",
                "android.permission.MODIFY_PHONE_STATE", "android.permission.MOUNT_FORMAT_FILESYSTEMS",
                "android.permission.MOUNT_UNMOUNT_FILESYSTEMS", "android.permission.PERSISTENT_ACTIVITY",
                "android.permission.PROCESS_OUTGOING_CALLS", "android.permission.READ_CALENDAR",
                "android.permission.READ_CONTACTS", "android.permission.READ_FRAME_BUFFER",
                "com.android.browser.permission.READ_HISTORY_BOOKMARKS", "android.permission.READ_INPUT_STATE",
                "android.permission.READ_LOGS", "android.permission.READ_OWNER_DATA",
                "android.permission.READ_PHONE_STATE", "android.permission.READ_SMS",
                "android.permission.READ_SYNC_SETTINGS", "android.permission.READ_SYNC_STATS",
                "android.permission.REBOOT", "android.permission.RECEIVE_BOOT_COMPLETED",
                "android.permission.RECEIVE_MMS", "android.permission.RECEIVE_SMS",
                "android.permission.RECEIVE_WAP_PUSH", "android.permission.RECORD_AUDIO",
                "android.permission.REORDER_TASKS", "android.permission.RESTART_PACKAGES",
                "android.permission.SEND_SMS", "android.permission.SET_ACTIVITY_WATCHER",
                "android.permission.SET_ALWAYS_FINISH", "android.permission.SET_ANIMATION_SCALE",
                "android.permission.SET_DEBUG_APP", "android.permission.SET_ORIENTATION",
                "android.permission.SET_PREFERRED_APPLICATIONS", "android.permission.SET_PROCESS_LIMIT",
                "android.permission.SET_TIME", "android.permission.SET_TIME_ZONE", "android.permission.SET_WALLPAPER",
                "android.permission.SET_WALLPAPER_HINTS", "android.permission.SIGNAL_PERSISTENT_PROCESSES",
                "android.permission.STATUS_BAR", "android.permission.SUBSCRIBED_FEEDS_READ",
                "android.permission.SUBSCRIBED_FEEDS_WRITE", "android.permission.SYSTEM_ALERT_WINDOW",
                "android.permission.UPDATE_DEVICE_STATS", "android.permission.USE_CREDENTIALS",
                "android.permission.VIBRATE", "android.permission.WAKE_LOCK", "android.permission.WRITE_APN_SETTINGS",
                "android.permission.WRITE_CALENDAR", "android.permission.WRITE_CONTACTS",
                "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.WRITE_GSERVICES",
                "com.android.browser.permission.WRITE_HISTORY_BOOKMARKS", "android.permission.WRITE_OWNER_DATA",
                "android.permission.WRITE_SECURE_SETTINGS", "android.permission.WRITE_SETTINGS",
                "android.permission.WRITE_SMS", "android.permission.WRITE_SYNC_SETTINGS" };

        for (String s : permArray)
            permList.add(s);
    }
}
