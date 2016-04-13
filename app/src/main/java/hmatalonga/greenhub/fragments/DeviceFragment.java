package hmatalonga.greenhub.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import hmatalonga.greenhub.GreenHub;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.adapters.RVAdapter;
import hmatalonga.greenhub.sampling.BatteryEstimator;
import hmatalonga.greenhub.sampling.Inspector;
import hmatalonga.greenhub.model.DeviceResourceCard;

/**
 * Created by hugo on 28-03-2016.
 */
public class DeviceFragment extends Fragment {
    private Context context;
    private BatteryEstimator estimator;
    private List<DeviceResourceCard> deviceResourceCards;
    private RecyclerView rv;
    private SwipeRefreshLayout swipeRefresh;
    private String value;
    private Thread t;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);

        context = GreenHub.context;
        estimator = new BatteryEstimator();
        int numberColumns = 1;

        rv = (RecyclerView) view.findViewById(R.id.rv);

        if(context.getResources().getConfiguration().orientation == context.getResources().getConfiguration().ORIENTATION_LANDSCAPE)
            numberColumns = 2;

        GridLayoutManager layout = new GridLayoutManager(context, numberColumns, GridLayoutManager.VERTICAL, false);
        rv.setLayoutManager(layout);
        rv.setHasFixedSize(true);

        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefresh.setColorSchemeColors(R.color.colorAccent);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });

//        loadData(context, estimator);
//        setAdapter();

        return view;
    }

    private void loadData(final Context context, final BatteryEstimator estimator) {
        // FIXME: Consider another way to load device data...
        t = new Thread(new Runnable() {
            public void run() {
                estimator.getCurrentStatus(context);
                deviceResourceCards = new ArrayList<>();

                // Temperature
                value = String.valueOf(estimator.getTemperature() + " ºC");
                deviceResourceCards.add(new DeviceResourceCard("Temperature", value));
                // Voltage
                value = String.valueOf(estimator.getVoltage() + " V");
                deviceResourceCards.add(new DeviceResourceCard("Voltage", value));
                // Health
                deviceResourceCards.add(new DeviceResourceCard("Health", estimator.getHealthStatus()));
                // Memory
                double memUsed = Math.round((Inspector.readMemory(context)[1] / 1024) * 100.0) / 100.0;
                value = String.valueOf(memUsed) + " MB";
                deviceResourceCards.add(new DeviceResourceCard(getString(R.string.device_summary_memory_label), value));
            }
        });

        t.start();
    }

    private void setAdapter(){
        try {
            t.join();
            RVAdapter adapter = new RVAdapter(deviceResourceCards);
            rv.setAdapter(adapter);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void refreshItems() {
        loadData(context, estimator);
        onItemsLoadComplete();
    }

    private void onItemsLoadComplete() {
        setAdapter();
        swipeRefresh.setRefreshing(false);
    }
}
