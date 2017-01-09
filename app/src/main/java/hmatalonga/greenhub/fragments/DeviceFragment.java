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
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.events.RefreshEvent;
import hmatalonga.greenhub.models.Bluetooth;
import hmatalonga.greenhub.models.Memory;
import hmatalonga.greenhub.models.Network;
import hmatalonga.greenhub.models.Phone;
import hmatalonga.greenhub.models.Specifications;
import hmatalonga.greenhub.models.Wifi;

import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Device Fragment.
 */
public class DeviceFragment extends Fragment {

    private static final String TAG = makeLogTag("DeviceFragment");

    private Context mContext = null;
    private View mParentView = null;

    private Handler mHandler;

    private ProgressBar mMemoryBar;
    private TextView mMemoryUsed;
    private TextView mMemoryFree;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_device, container, false);

        mParentView = view;
        mContext = view.getContext();
        mHandler = new Handler();

        loadComponents(view);

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

    public static DeviceFragment newInstance() {
        return new DeviceFragment();
    }

    // Private Helper Methods ----------------------------------------------------------------------

    /**
     * Helper method to load all UI views and set all values for layout view elements.
     *
     * @param view View to update
     */
    private void loadComponents(final View view) {
        StringBuilder model = new StringBuilder();
        TextView textView;
        String value;

        // Create model device string
        model.append(Specifications.getBrand());
        model.append(" ");
        model.append(Specifications.getModel());

        // Device
        textView = (TextView) view.findViewById(R.id.androidVersion);
        textView.setText(Specifications.getOsVersion());
        textView = (TextView) view.findViewById(R.id.androidImei);
        value = Phone.getDeviceId(mContext);
        textView.setText(value == null ? "not available" : value);
        textView = (TextView) view.findViewById(R.id.androidModel);
        textView.setText(model);
        textView = (TextView) view.findViewById(R.id.androidRoot);
        textView.setText(Specifications.isRooted() ? "Yes" : "No");

        // Network
        updateWifiData(view, Wifi.isEnabled(mContext));
        updateBluetoothData(view, Bluetooth.isEnabled());
        updateMobileData(view, Network.isMobileDataEnabled(mContext));

        // Memory
        mMemoryBar = (ProgressBar) view.findViewById(R.id.memoryBar);
        mMemoryBar.setIndeterminate(false);
        mMemoryUsed = (TextView) view.findViewById(R.id.memoryUsed);
        mMemoryFree = (TextView) view.findViewById(R.id.memoryFree);

        mHandler.post(runnable);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshData(RefreshEvent event) {
        if (mParentView == null) return;

        switch (event.field) {
            case "wifi":
                updateWifiData(mParentView, event.value);
                break;
            case "bluetooth":
                updateBluetoothData(mParentView, event.value);
                break;
            case "mobile":
                updateMobileData(mParentView, event.value);
                break;
        }
    }

    private void updateWifiData(final View view, boolean value) {
        TextView textView;

        textView = (TextView) view.findViewById(R.id.wifi);
        textView.setText(value ? "Yes" : "No");

        // Display/Hide Additional Wifi fields
        textView = (TextView) view.findViewById(R.id.ipAddressLabel);
        textView.setVisibility(value ? View.VISIBLE : View.GONE);

        // IP Address
        textView = (TextView) view.findViewById(R.id.ipAddress);
        if (value) {
            textView.setText(Wifi.getIpAddress(mContext));
        }
        textView.setVisibility(value ? View.VISIBLE : View.GONE);

        textView = (TextView) view.findViewById(R.id.macAddressLabel);
        textView.setVisibility(value ? View.VISIBLE : View.GONE);

        // MAC Address
        textView = (TextView) view.findViewById(R.id.macAddress);
        if (value) {
            textView.setText(Wifi.getMacAddress(mContext));
        }
        textView.setVisibility(value ? View.VISIBLE : View.GONE);

        textView = (TextView) view.findViewById(R.id.ssidLabel);
        textView.setVisibility(value ? View.VISIBLE : View.GONE);

        // SSID Network
        textView = (TextView) view.findViewById(R.id.ssid);
        if (value) {
            textView.setText(Wifi.getInfo(mContext).getSSID());
        }
        textView.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private void updateBluetoothData(final View view, boolean value) {
        TextView textView;

        textView = (TextView) view.findViewById(R.id.bluetooth);
        textView.setText(value ? "Yes" : "No");

        // Bluetooth Address
        textView = (TextView) view.findViewById(R.id.bluetoothAddress);
        if (value) {
            textView.setText(Bluetooth.getAddress());
        }
        textView.setVisibility(value ? View.VISIBLE : View.GONE);

        textView = (TextView) view.findViewById(R.id.bluetoothAddressLabel);
        textView.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private void updateMobileData(final View view, boolean value) {
        TextView textView;

        textView = (TextView) view.findViewById(R.id.mobileData);
        textView.setText(value ? "Yes" : "No");

        // Bluetooth Address
        textView = (TextView) view.findViewById(R.id.networkType);
        if (value) {
            textView.setText(Network.getMobileNetworkType(mContext));
        }
        textView.setVisibility(value ? View.VISIBLE : View.GONE);

        textView = (TextView) view.findViewById(R.id.networkTypeLabel);
        textView.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // 1 total, 2 active
            int[] memory = Memory.readMemoryInfo();
            String value;

            mMemoryBar.setMax(memory[1]);
            mMemoryBar.setProgress(memory[1] - memory[2]);

            value = (memory[1] - memory[2]) / 1024 + " MB";
            mMemoryUsed.setText(value);

            value = memory[2] / 1024 + " MB";
            mMemoryFree.setText(value);

            mHandler.postDelayed(this, Config.REFRESH_CURRENT_INTERVAL);
        }
    };

    /**
     * Cleans local variables preventing memory leaks.
     */
    private void clear() {
        mParentView = null;
        mContext = null;
        mHandler = null;
        mMemoryBar = null;
        mMemoryFree = null;
        mMemoryUsed = null;
    }
}
