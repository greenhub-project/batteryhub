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

package com.hmatalonga.greenhub.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.hmatalonga.greenhub.Config;
import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.events.RefreshEvent;
import com.hmatalonga.greenhub.models.Bluetooth;
import com.hmatalonga.greenhub.models.Memory;
import com.hmatalonga.greenhub.models.Network;
import com.hmatalonga.greenhub.models.Phone;
import com.hmatalonga.greenhub.models.Specifications;
import com.hmatalonga.greenhub.models.Storage;
import com.hmatalonga.greenhub.models.Wifi;
import com.hmatalonga.greenhub.models.data.StorageDetails;
import com.hmatalonga.greenhub.ui.TaskListActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Device Fragment.
 */
public class DeviceFragment extends Fragment {
    private Context mContext = null;

    private View mParentView = null;

    private Handler mHandler;

    private ProgressBar mMemoryBar;

    private TextView mMemoryUsed;

    private TextView mMemoryFree;

    private ProgressBar mStorageBar;

    private TextView mStorageUsed;

    private TextView mStorageFree;

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
        textView = view.findViewById(R.id.androidVersion);
        textView.setText(Specifications.getOsVersion());
        textView = view.findViewById(R.id.androidModel);
        textView.setText(model);
        textView = view.findViewById(R.id.androidImei);
        value = Phone.getDeviceId(mContext);
        textView.setText(value == null ? getString(R.string.not_available) : value);
        textView = view.findViewById(R.id.androidRoot);
        textView.setText(Specifications.isRooted() ?
                getString(R.string.yes) : getString(R.string.no));

        // Network
        updateWifiData(view, Wifi.isEnabled(mContext));
        updateBluetoothData(view, Bluetooth.isEnabled());
        updateMobileData(view, Network.isMobileDataEnabled(mContext));

        // Memory
        mMemoryBar = view.findViewById(R.id.memoryBar);
        mMemoryUsed = view.findViewById(R.id.memoryUsed);
        mMemoryFree = view.findViewById(R.id.memoryFree);
        Button btViewMore = view.findViewById(R.id.buttonViewMore);
        btViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName("Enters Task Manager")
                        .putContentType("Page visit")
                        .putContentId("page-task-manager"));
                startActivity(new Intent(view.getContext(), TaskListActivity.class));
            }
        });

        // Storage
        mStorageBar = view.findViewById(R.id.storageBar);
        mStorageUsed = view.findViewById(R.id.storageUsed);
        mStorageFree = view.findViewById(R.id.storageFree);

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
            default:
                break;
        }
    }

    private void updateWifiData(final View view, boolean value) {
        TextView textView;

        textView = view.findViewById(R.id.wifi);
        textView.setText(value ? getString(R.string.yes) : getString(R.string.no));

        // Display/Hide Additional Wifi fields
        textView = view.findViewById(R.id.ipAddressLabel);
        textView.setVisibility(value ? View.VISIBLE : View.GONE);

        // IP Address
        textView = view.findViewById(R.id.ipAddress);
        if (value) {
            textView.setText(Wifi.getIpAddress(mContext));
        }
        textView.setVisibility(value ? View.VISIBLE : View.GONE);

        textView = view.findViewById(R.id.macAddressLabel);
        textView.setVisibility(value ? View.VISIBLE : View.GONE);

        // MAC Address
        textView = view.findViewById(R.id.macAddress);
        if (value) {
            textView.setText(Wifi.getMacAddress(mContext));
        }
        textView.setVisibility(value ? View.VISIBLE : View.GONE);

        textView = view.findViewById(R.id.ssidLabel);
        textView.setVisibility(value ? View.VISIBLE : View.GONE);

        // SSID Network
        textView = view.findViewById(R.id.ssid);
        if (value) {
            textView.setText(Wifi.getInfo(mContext).getSSID());
        }
        textView.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private void updateBluetoothData(final View view, boolean value) {
        TextView textView;

        textView = view.findViewById(R.id.bluetooth);
        textView.setText(value ? getString(R.string.yes) : getString(R.string.no));

        // Bluetooth Address
        textView = view.findViewById(R.id.bluetoothAddress);
        if (value) {
            textView.setText(Bluetooth.getAddress());
        }
        textView.setVisibility(value ? View.VISIBLE : View.GONE);

        textView = view.findViewById(R.id.bluetoothAddressLabel);
        textView.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private void updateMobileData(final View view, boolean value) {
        TextView textView;

        textView = view.findViewById(R.id.mobileData);
        textView.setText(value ? getString(R.string.yes) : getString(R.string.no));

        // Bluetooth Address
        textView = view.findViewById(R.id.networkType);
        if (value) {
            textView.setText(Network.getMobileNetworkType(mContext));
        }
        textView.setVisibility(value ? View.VISIBLE : View.GONE);

        textView = view.findViewById(R.id.networkTypeLabel);
        textView.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // 1 total, 2 active
            int[] memory = Memory.readMemoryInfo();
            StorageDetails storageDetails = Storage.getStorageDetails();
            String value;

            mMemoryBar.setMax(memory[1]);
            mMemoryBar.setProgress(memory[1] - memory[2]);

            value = (memory[1] - memory[2]) / 1024 + " MB";
            mMemoryUsed.setText(value);

            value = memory[2] / 1024 + " MB";
            mMemoryFree.setText(value);

            mStorageBar.setMax(storageDetails.total);
            mStorageBar.setProgress(storageDetails.total - storageDetails.free);

            value = (storageDetails.total - storageDetails.free) + " MB";
            mStorageUsed.setText(value);

            value = storageDetails.free + " MB";
            mStorageFree.setText(value);

            mHandler.postDelayed(this, Config.REFRESH_MEMORY_INTERVAL);
        }
    };
}
