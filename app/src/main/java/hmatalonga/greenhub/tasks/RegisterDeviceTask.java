package hmatalonga.greenhub.tasks;

import android.os.AsyncTask;
import android.util.Log;

import hmatalonga.greenhub.GreenHub;
import hmatalonga.greenhub.fragments.HomeFragment;
import hmatalonga.greenhub.utils.NetworkWatcher;

/**
 * Task to register devices on the web server
 * Created by hugo on 09-04-2016.
 */
public class RegisterDeviceTask extends AsyncTask<GreenHub, Void, Void> {
    @Override
    protected Void doInBackground(GreenHub... params) {
        GreenHub app = params[0];
        assert app != null;

        Log.i("RegisterDeviceTask", "Task called.");

        try {
            if (NetworkWatcher.hasInternet(GreenHub.getContext()))
                app.registerHandler.registerClient();
            else
                HomeFragment.setStatus("Not connected");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
