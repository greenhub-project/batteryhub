package hmatalonga.greenhub.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import hmatalonga.greenhub.GreenHub;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.model.DeviceResourceCard;
import hmatalonga.greenhub.sampling.BatteryEstimator;
import hmatalonga.greenhub.sampling.Inspector;
import hmatalonga.greenhub.utils.NetworkWatcher;

/**
 * Created by hugo on 27-03-2016.
 */
public class HomeFragment extends Fragment {
    private static GreenHub app;
    private Context context;
    private BatteryEstimator estimator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        estimator = new BatteryEstimator();
        context = GreenHub.getContext();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fabSendSample);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                assert app != null;
//                if (NetworkWatcher.hasInternet(context))
//                    app.device = app.registerHandler.registerClient();
            }
        });

//        try {
//            // Checking for connection on startup of home tab
//            if (!app.networkStartFlag) {
//                if (!NetworkWatcher.hasInternet(context))
//                    Snackbar.make(view, "No connection", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                app.networkStartFlag = !app.networkStartFlag;
//            }
//        }
//        catch (NullPointerException e) {
//            e.printStackTrace();
//        }

        // loadSummary(context, view);

        return view;
    }

    public GreenHub getApp() {
        return app;
    }

    public void setApp(GreenHub app) {
        this.app = app;
    }

    public void loadSummary(Context context, View view) {
        TextView textView = (TextView) view.findViewById(R.id.battery_current_value);
        textView.setText(String.valueOf((int)estimator.currentBatteryLevel(context)));
    }
}
