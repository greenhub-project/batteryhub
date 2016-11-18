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

package hmatalonga.greenhub.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import hmatalonga.greenhub.GreenHubHelper;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.managers.sampling.BatteryEstimator;
import hmatalonga.greenhub.models.ui.DeviceResourceCard;
import hmatalonga.greenhub.ui.adapters.RVAdapter;

/**
 * Home Fragment.
 */
public class HomeFragment extends Fragment {

    private static TextView sStatusText = null;

    private static String status = "";

    private static GreenHubHelper sApp;

    private Context mContext;

    private BatteryEstimator mEstimator;

    // private String mJson;

    private ArrayList<DeviceResourceCard> mDeviceResourceCards;

    private RecyclerView mRecyclerView;

    private TextView mBatteryText;

    private ProgressBar mProgressBar;

    private String mValue;

    private Thread mLocalThread;

    private int mCurrentBatteryValue;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        mEstimator = BatteryEstimator.getInstance();
        mContext = GreenHubHelper.getContext();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv);
        assert mRecyclerView != null;

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fabSendSample);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sApp != null)
                    sApp.communicationManager.sendSamples();
            }
        });

        GridLayoutManager layout = new GridLayoutManager(mContext, 1, GridLayoutManager.VERTICAL,
                false);
        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.setHasFixedSize(true);

        sStatusText = (TextView) view.findViewById(R.id.status);
        mBatteryText = (TextView) view.findViewById(R.id.batteryCurrentValue);
        mProgressBar = (ProgressBar) view.findViewById(R.id.batteryProgressbar);

        // populateView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

//        mBatteryText.setText(String.valueOf(mCurrentBatteryValue));
//        mProgressBar.setProgress(mCurrentBatteryValue);
//        sStatusText.setText(status);
    }

    /**
     *
     * @return
     */
    public static GreenHubHelper getApp() {
        return sApp;
    }

    /**
     *
     * @param app
     */
    public static void setApp(GreenHubHelper app) {
        HomeFragment.sApp = app;
    }

    public static void setStatus(String message) {
        status = message;
        if (sStatusText != null)
            sStatusText.setText(message);
    }

    /**
     * Set all values for layout view elements
     */
    private void populateView() {
        mCurrentBatteryValue = (int) mEstimator.currentBatteryLevel();
        mBatteryText.setText(String.valueOf(mCurrentBatteryValue));
        mProgressBar.setProgress(mCurrentBatteryValue);
        sStatusText.setText(status);

        loadData(mContext, mEstimator);
        setAdapter();
    }

    /**
     * Creates an array to feed data to the recyclerView
     *
     * @param context Application context
     * @param estimator Provider of mobile status
     */
    private void loadData(final Context context, final BatteryEstimator estimator) {
        // FIXME: Consider another way to load device data...
        mLocalThread = new Thread(new Runnable() {
            public void run() {
                estimator.getCurrentStatus(context);
                mDeviceResourceCards = new ArrayList<>();
                // Temperature
                mValue = String.valueOf(estimator.getTemperature() + " ºC");
                mDeviceResourceCards.add(new DeviceResourceCard("Temperature", mValue));
                // Voltage
                mValue = String.valueOf(estimator.getVoltage() + " V");
                mDeviceResourceCards.add(new DeviceResourceCard("Voltage", mValue));
                // Health
                mDeviceResourceCards.add(new DeviceResourceCard("Health",
                        estimator.getHealthStatus()));
                // Memory
//                double memUsed = Math.round((Inspector.readMemory(context)[1] / 1024) * 100.0) / 100.0;
//                mValue = String.valueOf(memUsed) + " MB";
//                mDeviceResourceCards.add(new DeviceResourceCard(getString(R.string.device_summary_memory_label), mValue));
            }
        });

        mLocalThread.start();
    }

    /**
     *
     */
    private void setAdapter(){
        try {
            mLocalThread.join();
            RVAdapter adapter = new RVAdapter(mDeviceResourceCards);
            mRecyclerView.setAdapter(adapter);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
