package hmatalonga.greenhub.utils;

import android.util.Log;

/**
 * Custom Logger
 * Created by hugo on 24-03-2016.
 */
public class GreenHubLog {
    public static void info(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void debug(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void error(String tag, String msg) {
        Log.e(tag, msg);
    }
}
