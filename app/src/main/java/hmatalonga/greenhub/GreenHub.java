package hmatalonga.greenhub;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;

import hmatalonga.greenhub.protocol.CommunicationManager;
import hmatalonga.greenhub.protocol.RegisterHandler;
import hmatalonga.greenhub.storage.Device;

/**
 * App class
 * Created by hugo on 24-03-2016.
 */
public class GreenHub {
    // Logger class name tag
    private static final String TAG = "GreenHub";

    public static Context context = null;
    public static SharedPreferences preferences = null;

    public final String serverURL;
    public Device device = null;

    // GreenHub app Modules
    public CommunicationManager communicationManager = null;
    public RegisterHandler registerHandler = null;

    public GreenHub(Context c) {
        context = c;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (Constants.PRODUCTION)
            serverURL = Constants.PUBLIC_SERVER_URL;
        else
            serverURL = Constants.LOCAL_SERVER_URL;
    }

    public void initModules() {
        communicationManager = new CommunicationManager(this);
        registerHandler = new RegisterHandler(this);
    }

    // Used to map importances to human readable strings for sending samples to
    // the server, and showing them in the process list.
    private static final SparseArray<String> importanceToString = new SparseArray<String>();
    {
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_EMPTY, "Not running");
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_BACKGROUND, "Background process");
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_SERVICE, "Service");
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_VISIBLE, "Visible task");
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_FOREGROUND, "Foreground app");

        importanceToString.put(Constants.IMPORTANCE_PERCEPTIBLE, "Perceptible task");
        importanceToString.put(Constants.IMPORTANCE_SUGGESTION, "Suggestion");
    }

    public static Context getContext() {
        return context;
    }

    /**
     * Converts <code>importance</code> to a human readable string.
     *
     * @param importance
     *            the importance from Android process info.
     * @return a human readable String describing the importance.
     */
    public static String importanceString(int importance) {
        String s = importanceToString.get(importance);
        if (s == null || s.length() == 0) {
            Log.e("Importance not found:", "" + importance);
            s = "Unknown";
        }
        return s;
    }

    public static String getRegisteredUuid() {
        return Constants.REGISTERED_UUID;
    }
}
