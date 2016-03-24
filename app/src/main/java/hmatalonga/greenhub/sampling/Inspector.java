package hmatalonga.greenhub.sampling;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.util.Enumeration;
import java.util.Properties;

import hmatalonga.greenhub.Constants;
import hmatalonga.greenhub.utils.GHLogger;

/**
 * Created by hugo on 06-03-2016.
 */
public final class Inspector {
    public static String TYPE_UNKNOWN = "unknown";

    private Inspector() {}

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
        return System.getProperty("ro.serial", TYPE_UNKNOWN);
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
                GHLogger.debug("PROPS", k + "=" + v);
        }
    }
}
