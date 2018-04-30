/*
 * Copyright (c) 2016 Hugo Matalonga & João Paulo Fernandes
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

package com.hmatalonga.greenhub.models;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import com.hmatalonga.greenhub.R;

/**
 * Bluetooth.
 */
public class Bluetooth {

    private static final String TAG = "Bluetooth";

    /**
     * Checks if bluetooth is enabled on the device
     *
     * @return true if bluetooth is enabled
     */
    public static boolean isEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    @SuppressLint("HardwareIds")
    public static String getAddress(final Context context) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return (adapter != null) ? adapter.getAddress() : context.getString(R.string.not_available);
    }
}
