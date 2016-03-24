package hmatalonga.greenhub;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.SparseArray;

/**
 * Created by hugo on 24-03-2016.
 */
public class GreenHub {
    private static GreenHub mInstance;
    // Used for logging
    private static final String TAG = "GreenHub";

    public static Context mAppContext = null;
    public static SharedPreferences mPrefs = null;

    // NOTE: This needs to be initialized before CommunicationManager.
    private static CaratDataStorage storage = null;
    // NOTE: The CommunicationManager requires a working instance of
    // CaratDataStorage.
    public CommunicationManager commManager = null;

    // Activity pointers so that all activity UIs can be updated with a callback
    // to CaratApplication
    static MainActivity main = null;
    private static SuggestionsFragment actionList = null;
    // The Sampler samples the battery level when it changes.
    private static Sampler sampler = null;

    public static MyDeviceData myDeviceData = new MyDeviceData();

    // used to check if Internet is available
    private static ConnectivityManager mConnectivityManager = null;

    public GreenHub() {}

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

        mInstance = this;
    }

    public static Context getContext() {
        return mInstance;
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

    public static String translatedPriority(String importanceString) {
        if (main != null) {
            if (importanceString == null)
                return main.getString(R.string.priorityDefault);
            if (importanceString.equals("Not running")) {
                return main.getString(R.string.prioritynotrunning);
            } else if (importanceString.equals("Background process")) {
                return main.getString(R.string.prioritybackground);
            } else if (importanceString.equals("Service")) {
                return main.getString(R.string.priorityservice);
            } else if (importanceString.equals("Visible task")) {
                return main.getString(R.string.priorityvisible);
            } else if (importanceString.equals("Foreground app")) {
                return main.getString(R.string.priorityforeground);
            } else if (importanceString.equals("Perceptible task")) {
                return main.getString(R.string.priorityperceptible);
            } else if (importanceString.equals("Suggestion")) {
                return main.getString(R.string.prioritysuggestion);
            } else
                return main.getString(R.string.priorityDefault);
        } else
            return importanceString;
    }

    /**
     * Return a Drawable that contains an app icon for the named app. If not
     * found, return the Drawable for the Carat icon.
     *
     * @param appName
     *            the application name
     * @return the Drawable for the application's icon
     */
    public static Drawable iconForApp(Context context, String appName) {
        try {
            return context.getPackageManager().getApplicationIcon(appName);
        } catch (NameNotFoundException e) {
            return context.getResources().getDrawable(R.drawable.ic_launcher);
        }
    }

    /**
     * Return a human readable application label for the named app. If not
     * found, return appName.
     *
     * @param appName
     *            the application name
     * @return the human readable application label
     */
    public static String labelForApp(Context context, String appName) {
        if (appName == null)
            return "Unknown";
        try {
            ApplicationInfo i = context.getPackageManager().getApplicationInfo(appName, 0);
            if (i != null)
                return context.getPackageManager().getApplicationLabel(i).toString();
            else
                return appName;
        } catch (NameNotFoundException e) {
            return appName;
        }
    }


    public static int getJscore() {
        final Reports reports = getStorage().getReports();
        int jscore = 0;
        if (reports != null) {
            jscore = ((int) (reports.getJScore() * 100));
        }
        return jscore;
    }

    /**
     * Return titles from the drawer items array.
     * @return titles from the drawer items array.
     */
    public static String[] getTitles() {
        Resources res = getContext().getResources();
        return res.getStringArray(R.array.drawer_items);
    }

    public static void setActionInProgress() {
        if (main != null) {
            main.runOnUiThread(new Runnable() {
                public void run() {
                    // Updating done
                    main.setTitleUpdating(getTitles()[2]);
                    main.setProgress(0);
                    main.setProgressBarVisibility(true);
                    main.setProgressBarIndeterminateVisibility(true);
                }
            });
        }
    }

    public static void refreshActions() {
        if (actionList != null) {
            main.runOnUiThread(new Runnable() {
                public void run() {
                    actionList.refresh();
                }
            });
        }
    }

    public static void setActionProgress(final int progress, final String what, final boolean fail) {
        if (main != null) {
            main.runOnUiThread(new Runnable() {
                public void run() {
                    if (fail)
                        main.setTitleUpdatingFailed(what);
                    else
                        main.setTitleUpdating(what);
                    main.setProgress(progress * 100);
                }
            });
        }
    }

    public static void setActionFinished() {
        if (main != null) {
            main.runOnUiThread(new Runnable() {
                public void run() {
                    // Updating done
                    main.setTitleNormal();
                    main.setProgress(100);
                    main.setProgressBarVisibility(false);
                    main.setProgressBarIndeterminateVisibility(false);
                }
            });
        }
    }

    protected static void setMain(MainActivity mainActivity) {
        main = mainActivity;
    }

    public static void setActionList(SuggestionsFragment suggestionsFragment) {
        actionList = suggestionsFragment;
    }

    public static String getRegisteredUuid() {
        return Constants.REGISTERED_UUID;
    }


    public void refreshUi() {
        boolean connecting = false;
        Context co = getApplicationContext();
        // TODO: using a shared preferences object might cause problem in different OS versions. replace with a private one. see MainActivity.AsyncTask.doInBackground().
        final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(co);
        final boolean useWifiOnly = p.getBoolean(getString(R.string.wifi_only_key), false);
        if (Constants.DEBUG)
            Log.d(TAG, "Wi-Fi only: "+ useWifiOnly);
        String networkStatus = SamplingLibrary.getNetworkStatus(getApplicationContext());
        String networkType = SamplingLibrary.getNetworkType(co);

        boolean connected = (!useWifiOnly && networkStatus == SamplingLibrary.NETWORKSTATUS_CONNECTED)
                || networkType.equals("WIFI");

        if (connected && commManager != null) {
            // Show we are updating...
            CaratApplication.setActionInProgress();
            try {
                commManager.refreshAllReports();
                // Log.d(TAG, "Reports refreshed.");
            } catch (Throwable th) {
                // Any sort of malformed response, too short string,
                // etc...
                Log.w(TAG, "Failed to refresh reports: " + th + Constants.MSG_TRY_AGAIN);
                th.printStackTrace();
            }
            connecting = false;

        } else if (networkStatus.equals(SamplingLibrary.NETWORKSTATUS_CONNECTING)) {
            Log.w(TAG, "Network status: " + networkStatus + ", trying again in 10s.");
            connecting = true;
        }

        // do this regardless
        setReportData();

        CaratApplication.setActionProgress(90, getString(R.string.finishing), false);

        if (!connecting)
            CaratApplication.setActionFinished();

        if (connecting) {
            // wait for WiFi to come up
            try {
                Thread.sleep(Constants.COMMS_WIFI_WAIT);
            } catch (InterruptedException e1) {
                // ignore
            }
            connecting = false;

            // Show we are updating...
            CaratApplication.setActionInProgress();
            try {
                commManager.refreshAllReports();
                // Log.d(TAG, "Reports refreshed.");
            } catch (Throwable th) {
                // Any sort of malformed response, too short string,
                // etc...
                Log.w(TAG, "Failed to refresh reports: " + th + Constants.MSG_TRY_AGAIN);
                th.printStackTrace();
            }
            connecting = false;

            // do this regardless
            setReportData();

            setActionProgress(90, getString(R.string.finishing), false);
        }
        CaratApplication.setActionFinished();
        SampleSender.sendSamples(CaratApplication.this);
        CaratApplication.setActionFinished();
    }

    public static boolean isInternetAvailable2() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
            if (ipAddr.equals("")) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks whether WiFi or mobile data is enabled
     * @return true of false
     */
    public static boolean isInternetAvailable() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;


        NetworkInfo[] netInfo = mConnectivityManager.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }


    public static void setReportData() {
        final Reports r = getStorage().getReports();
        if (Constants.DEBUG)
            Log.d(TAG, "Got reports.");
        long freshness = CaratApplication.getStorage().getFreshness();
        long l = System.currentTimeMillis() - freshness;
        final long h = l / 3600000;
        final long min = (l - h * 3600000) / 60000;
        double bl = 0;
        double error = 0;

        if (r != null) {
            if (Constants.DEBUG)
                Log.d(TAG, "r (reports) not null.");
            // Try exact battery life
            if (r.jScoreWith != null) {
                // Log.d(TAG, "jscoreWith not null.");
                double exp = r.jScoreWith.expectedValue;
                if (exp > 0.0) {
                    bl = 100 / exp;
                    error = 100 / (exp + r.jScoreWith.error);
                } else if (r.getModel() != null) {
                    exp = r.getModel().expectedValue;
                    if (Constants.DEBUG)
                        Log.d(TAG, "Model expected value: " + exp);
                    if (exp > 0.0) {
                        bl = 100 / exp;
                        error = 100 / (exp + r.getModel().error);
                    }
                }
                // If not possible, try model battery life
            }
        }


        // Only take the error part
        error = bl - error;

        int blh = (int) (bl / 3600);
        bl -= blh * 3600;
        int blmin = (int) (bl / 60);

        int errorH = 0;
        int errorMin = 0;
        if (error > 7200) {
            errorH = (int) (error / 3600);
            error -= errorH * 3600;
        }

        errorMin = (int) (error / 60);

        final String blS = blh + "h " + blmin + "m \u00B1 " + (errorH > 0 ? errorH + "h " : "") + errorMin + " m";

		/*
		 * we removed direct manipulation of MyDevice fragment,
		 * and moved the data pertaining to this fragment to a class field, called myDeviceData.
		 * In the onResume() method of MyDeviceFragment, we fetch this data and show (see setViewData())
		 * The reason for this movement is that we migrated from tabs to fragments.
		 * We cannot change a fragment's view while it's not in the foreground
		 * (fragments get replaced by a fragment transaction:
		 * the parent activity which hosts a frame-layout
		 * (a placeholder for fragment's layout), replaces the frame-layout with
		 * the new fragment's layout)
		 */

        SharedPreferences p = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        String caratId = p.getString(Constants.REGISTERED_UUID, "0");

        myDeviceData.setAllFields(freshness, h, min, caratId, blS);
    }

    /**
     * @return the storage
     */
    public static CaratDataStorage getStorage() {
        if (storage == null)
            storage = new CaratDataStorage(CaratApplication.getContext());
        return storage;
    }

    /**
     * @param storage the storage to set
     */
    public static void setStorage(CaratDataStorage storage) {
        CaratApplication.storage = storage;
    }
}
