/*
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

package hmatalonga.greenhub;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;

import hmatalonga.greenhub.managers.sampling.BatteryEstimator;
import hmatalonga.greenhub.models.Device;
import hmatalonga.greenhub.network.CommunicationManager;
import hmatalonga.greenhub.network.RegisterHandler;
import hmatalonga.greenhub.ui.MainActivity;

/**
 * App class
 * Created by hugo on 24-03-2016.
 */
public class GreenHub {
    private static final String TAG = "GreenHub";
    private static GreenHub instance;

    public static Context context = null;
    public static SharedPreferences preferences = null;

    // Used to map importances to human readable strings for sending samples to
    // the server, and showing them in the process list.
    private static final SparseArray<String> importanceToString;

    static {
        importanceToString = new SparseArray<>();
    }

    {
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_EMPTY, "Not running");
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_BACKGROUND, "Background process");
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_SERVICE, "Service");
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_VISIBLE, "Visible task");
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_FOREGROUND, "Foreground app");

        importanceToString.put(Config.IMPORTANCE_PERCEPTIBLE, "Perceptible task");
        importanceToString.put(Config.IMPORTANCE_SUGGESTION, "Suggestion");

        instance = this;
    }

    public static Context getContext() {
        return context;
    }

    public static MainActivity main = null;
    public static Device device = null;
    private static BatteryEstimator estimator = null;

    public final String serverURL;

    // GreenHub app Modules
    public CommunicationManager communicationManager = null;
    public RegisterHandler registerHandler = null;

    public GreenHub(Context c) {
        context = c;
        if (Config.PRODUCTION)
            serverURL = Config.PUBLIC_SERVER_URL;
        else
            serverURL = Config.LOCAL_SERVER_URL;
    }

    public void initModules() {
        new Thread() {
            public void run() {
                // FIXME: Change for file
                preferences = PreferenceManager.getDefaultSharedPreferences(context);
            }
        }.start();

        // set Storage ??

        new Thread() {
            private IntentFilter intentFilter;

            public void run() {
                // Let sampling happen on battery change
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
                // Take samples when screen changes too
                intentFilter.addAction(Intent.ACTION_SCREEN_ON);
                intentFilter.addAction(Intent.ACTION_SCREEN_OFF);

                /* For future monitoring
                intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
                intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
                intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
                intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
                */

                estimator = BatteryEstimator.getInstance();

                // Unregister, since GreenHub may have been started multiple times, since reboot
//                try {
//                    main.unregisterReceiver(estimator);
//                } catch (IllegalArgumentException e) {
//                    e.printStackTrace();
//                }

                main.registerReceiver(estimator, intentFilter);
            }
        }.start();

        new Thread() {
            public void run() {
                registerHandler = new RegisterHandler(GreenHub.this);
                communicationManager = new CommunicationManager(GreenHub.this);
            }
        }.start();
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
            else if (importanceString.equals("Not running")) {
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
        } catch (PackageManager.NameNotFoundException e) {
            return context.getResources().getDrawable(R.mipmap.ic_launcher);
        }
    }

    /**
     * Return a human readable application label for the named app. If not
     * found, return appName.
     *
     * @param appName the application name
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
        } catch (PackageManager.NameNotFoundException e) {
            return appName;
        }
    }

    public static String getRegisteredUuid() {
        return Config.REGISTERED_UUID;
    }

    public static void setMain(MainActivity mainActivity) {
        main = mainActivity;
    }

    public void startReceivers() {
        new Thread() {
            private IntentFilter intentFilter;

            public void run() {
                // Let sampling happen on battery change
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
                // Take samples when screen changes too
                intentFilter.addAction(Intent.ACTION_SCREEN_ON);
                intentFilter.addAction(Intent.ACTION_SCREEN_OFF);

                estimator = BatteryEstimator.getInstance();
                main.registerReceiver(estimator, intentFilter);
            }
        }.start();
    }

    public void stopReceivers() {
        try {
            main.unregisterReceiver(estimator);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
