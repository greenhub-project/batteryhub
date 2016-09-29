package hmatalonga.greenhub.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.net.InetAddress;

/**
 * Verifies connection to the Internet
 * Created by hugo on 06-03-2016.
 */
public class NetworkWatcher {
    private static final String TAG = "NetworkWatcher";
    private static boolean mobileDataAllowed = false;
    private static String urlTest = "google.com";
    private static boolean response = false;

    public NetworkWatcher() {}

    public static boolean isMobileDataAllowed() {
        return mobileDataAllowed;
    }

    public static void setMobileDataAllowed(boolean mobileDataAllowed) {
        NetworkWatcher.mobileDataAllowed = mobileDataAllowed;
    }

    public static String getUrlTest() {
        return urlTest;
    }

    public static void setUrlTest(String urlTest) {
        NetworkWatcher.urlTest = urlTest;
    }

    private static boolean isInternetAvailable() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress ipAddr = InetAddress.getByName(getUrlTest());
                    response = !ipAddr.equals("");

                } catch (Exception e) {
                    response = false;
                }
            }
        });

        t.start();
        try {
            t.join();
        } catch (Exception e) {
            response = false;
        }

        return response;
    }

    public static boolean hasInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return isInternetAvailable();
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                return isMobileDataAllowed() && isInternetAvailable();
            }
        }
        return false;
    }
}
