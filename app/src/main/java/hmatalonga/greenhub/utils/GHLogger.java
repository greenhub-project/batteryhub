package hmatalonga.greenhub.utils;

import android.util.Log;

/**
 * Created by hugo on 24-03-2016.
 */
public class GHLogger {
    public GHLogger() {}

    public static void debug(String tag, String msg) {
        Log.d(tag, msg);
    }
}
