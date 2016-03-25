package hmatalonga.greenhub.protocol;

import android.content.SharedPreferences;

import hmatalonga.greenhub.GreenHub;

/**
 *
 * Created by hugo on 25-03-2016.
 */
public class CommunicationManager {
    private static final String TAG = "CommunicationManager";

    private GreenHub app = null;
    private SharedPreferences preferences = null;

    public CommunicationManager(GreenHub app) {
        this.app = app;
    }
}
