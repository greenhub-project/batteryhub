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

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.events.BatteryLevelEvent;
import hmatalonga.greenhub.events.PowerSourceEvent;
import hmatalonga.greenhub.events.StatusEvent;
import hmatalonga.greenhub.managers.sampling.DataEstimator;
import hmatalonga.greenhub.managers.sampling.Inspector;
import hmatalonga.greenhub.models.Battery;
import hmatalonga.greenhub.models.ui.BatteryCard;
import hmatalonga.greenhub.ui.MainActivity;
import hmatalonga.greenhub.ui.adapters.BatteryRVAdapter;

import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Home Fragment.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = makeLogTag("HomeFragment");

    private Context mContext;

    private MainActivity mActivity;

    private TextView mBatteryPercentage;

    private TextView mBatteryCurrentNow;

    private TextView mBatteryCurrentMin;

    private TextView mBatteryCurrentMax;

    private ImageView mPowerDischarging;

    private ImageView mPowerAc;

    private ImageView mPowerUsb;

    private ImageView mPowerWireless;

    private TextView mStatus;

    private ProgressBar mBatteryCircleBar;

    private RecyclerView mRecyclerView;

    private BatteryRVAdapter mAdapter;

    private ArrayList<BatteryCard> mBatteryCards;

    private Thread mLocalThread;

    private Handler mHandler;

    private int mMin;

    private int mMax;

    private String mActivePower;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        mContext = view.getContext();
        mActivity = (MainActivity) getActivity();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv);
        mAdapter = null;

        LinearLayoutManager layout = new LinearLayoutManager(mContext) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.setHasFixedSize(true);

        mBatteryPercentage = (TextView) view.findViewById(R.id.batteryCurrentValue);
        mBatteryCircleBar = (ProgressBar) view.findViewById(R.id.batteryProgressbar);
        mStatus = (TextView) view.findViewById(R.id.status);

        mBatteryCurrentNow = (TextView) view.findViewById(R.id.batteryCurrentNow);
        mBatteryCurrentMin = (TextView) view.findViewById(R.id.batteryCurrentMin);
        mBatteryCurrentMax = (TextView) view.findViewById(R.id.batteryCurrentMax);

        mPowerDischarging = (ImageView) view.findViewById(R.id.imgPowerDischarging);
        mPowerAc = (ImageView) view.findViewById(R.id.imgPowerAc);
        mPowerUsb = (ImageView) view.findViewById(R.id.imgPowerUsb);
        mPowerWireless = (ImageView) view.findViewById(R.id.imgPowerWireless);
        mActivePower = "";

        mMin = Integer.MAX_VALUE;
        mMax = 0;
        mHandler = new Handler();
        mHandler.postDelayed(runnable, Config.REFRESH_CURRENT_INTERVAL);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mActivity.getEstimator() != null) {
            String level = Integer.toString(mActivity.getEstimator().getLevel());
            mBatteryPercentage.setText(level);
            mBatteryCircleBar.setProgress(mActivity.getEstimator().getLevel());
            loadData(mActivity.getEstimator());
            loadPluggedState("home");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateBatteryLevelUI(BatteryLevelEvent event) {
        String text = "" + event.level;
        mBatteryPercentage.setText(text);
        mBatteryCircleBar.setProgress(event.level);

        // Reload battery cards data from estimator
        loadData(mActivity.getEstimator());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateStatus(StatusEvent event) {
        mStatus.setText(event.status);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updatePowerSource(PowerSourceEvent event) {
        loadPluggedState(event.status);
        if (mActivePower.equals("unplugged")) {
            resetBatteryCurrent();
        }
    }

    /**
     * Creates an array to feed data to the recyclerView
     *
     * @param estimator Provider of mobile status
     */
    private void loadData(final DataEstimator estimator) {
        mLocalThread = new Thread(new Runnable() {
            public void run() {
                mBatteryCards = new ArrayList<>();
                String value;
                int color;

                // Temperature
                value = String.valueOf(estimator.getTemperature() + " ºC");
                mBatteryCards.add(
                        new BatteryCard(R.drawable.ic_thermometer_black_18dp, "Temperature", value)
                );

                // Voltage
                value = String.valueOf(estimator.getVoltage() + " V");
                mBatteryCards.add(
                        new BatteryCard(R.drawable.ic_flash_black_18dp, "Voltage", value)
                );

                // Health
                color = estimator.getHealthStatus().equals("Good") ? Color.GREEN : Color.RED;
                mBatteryCards.add(
                        new BatteryCard(R.drawable.ic_heart_black_18dp, "Health", estimator.getHealthStatus(), color)
                );

                // Technology
                color = estimator.getTechnology().equals("Li-ion") ? Color.GRAY : Color.GREEN;
                mBatteryCards.add(
                        new BatteryCard(R.drawable.ic_wrench_black_18dp, "Technology", estimator.getTechnology(), color)
                );
            }
        });

        mLocalThread.start();
        setAdapter();
    }

    private void setAdapter(){
        try {
            mLocalThread.join();
            if (mAdapter == null) {
                mAdapter = new BatteryRVAdapter(mBatteryCards);
                mRecyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.swap(mBatteryCards);
            }
            mRecyclerView.invalidate();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void loadPluggedState(String status) {
        mMin = Integer.MAX_VALUE;
        mMax = Integer.MIN_VALUE;
        String batteryCharger = "unplugged";

        if (status.equals("home")) {
            if (mActivity.getEstimator() == null) return;

            switch (mActivity.getEstimator().getPlugged()) {
                case BatteryManager.BATTERY_PLUGGED_AC:
                    batteryCharger = "ac";
                    break;
                case BatteryManager.BATTERY_PLUGGED_USB:
                    batteryCharger = "usb";
                    break;
            }
        } else {
            batteryCharger = status;
        }

        switch (mActivePower) {
            case "unplugged":
                mPowerDischarging.setImageResource(R.drawable.ic_battery_50_grey600_24dp);
                break;
            case "ac":
                mPowerAc.setImageResource(R.drawable.ic_power_plug_grey600_24dp);
                break;
            case "usb":
                mPowerUsb.setImageResource(R.drawable.ic_usb_grey600_24dp);
                break;
            case "wireless":
                mPowerWireless.setImageResource(R.drawable.ic_access_point_grey600_24dp);
                break;
        }

        if (batteryCharger.equals("unplugged")) {
            mPowerDischarging.setImageResource(R.drawable.ic_battery_50_white_24dp);
        } else if (batteryCharger.equals("ac")) {
            mPowerAc.setImageResource(R.drawable.ic_power_plug_white_24dp);
        } else if (batteryCharger.equals("usb")) {
            mPowerUsb.setImageResource(R.drawable.ic_usb_white_24dp);
        } else if (batteryCharger.equals("wireless")) {
            mPowerWireless.setImageResource(R.drawable.ic_access_point_white_24dp);
        }

        mActivePower = batteryCharger;
    }

    private void resetBatteryCurrent() {
        mMin = Integer.MAX_VALUE;
        mMax = 0;
        String value = "min: --";
        mBatteryCurrentMin.setText(value);
        value = "max: --";
        mBatteryCurrentMax.setText(value);
        value = "Measuring";
        mBatteryCurrentNow.setText(value);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int now = Battery.getBatteryCurrentNow(mContext);
            double level = Inspector.getCurrentBatteryLevel();
            String value;

            // If is charging and full battery stop runnable
            if (!mActivePower.equals("unplugged") && level == 1.0) {
                value = "min: --";
                mBatteryCurrentMin.setText(value);
                value = "max: --";
                mBatteryCurrentMax.setText(value);
                value = "Full";
                mBatteryCurrentNow.setText(value);
                mHandler.postDelayed(this, Config.REFRESH_CURRENT_INTERVAL);
                return;
            }

            if (Math.abs(now) < Math.abs(mMin)) {
                mMin = now;
                value = "min: " + mMin + " mA";
                mBatteryCurrentMin.setText(value);
            }

            if (Math.abs(now) > Math.abs(mMax)) {
                mMax = now;
                value = "max: " + mMax + " mA";
                mBatteryCurrentMax.setText(value);
            }

            value = now + " mA";
            mBatteryCurrentNow.setText(value);
            mHandler.postDelayed(this, Config.REFRESH_CURRENT_INTERVAL);
        }
    };
}
