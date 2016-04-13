package hmatalonga.greenhub.tasks;

import android.os.AsyncTask;

import hmatalonga.greenhub.GreenHub;
import hmatalonga.greenhub.utils.NetworkWatcher;

/**
 * Created by hugo on 09-04-2016.
 */
public class RegisterDeviceTask extends AsyncTask<GreenHub, Void, Void> {
    @Override
    protected Void doInBackground(GreenHub... params) {
        GreenHub app = params[0];
        assert app != null;

        if (NetworkWatcher.hasInternet(GreenHub.context))
            app.registerHandler.registerClient();

        return null;
    }
}
