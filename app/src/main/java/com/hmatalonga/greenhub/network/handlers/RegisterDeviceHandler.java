/*
 * Copyright (c) 2017 Hugo Matalonga & João Paulo Fernandes
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

package com.hmatalonga.greenhub.network.handlers;

import android.content.Context;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import com.hmatalonga.greenhub.BuildConfig;
import com.hmatalonga.greenhub.Config;
import com.hmatalonga.greenhub.events.StatusEvent;
import com.hmatalonga.greenhub.models.Specifications;
import com.hmatalonga.greenhub.models.data.Device;
import com.hmatalonga.greenhub.network.services.GreenHubAPIService;
import com.hmatalonga.greenhub.util.GsonRealmBuilder;
import com.hmatalonga.greenhub.util.SettingsUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.hmatalonga.greenhub.util.LogUtils.LOGI;
import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Registers devices on server for first-run, connects device to server and provides uuid.
 *
 * Created by hugo on 25-03-2016.
 */
public class RegisterDeviceHandler {
    private static final String TAG = makeLogTag(RegisterDeviceHandler.class);

    private GreenHubAPIService mService;

    private Context mContext;

    public RegisterDeviceHandler(final Context context) {
        mContext = context;
        Gson gson = GsonRealmBuilder.get();
        String url = SettingsUtils.fetchServerUrl(context);

        if (BuildConfig.DEBUG) {
            url = Config.SERVER_URL_DEVELOPMENT;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        mService = retrofit.create(GreenHubAPIService.class);
    }

    public Device registerClient() {
        Device device = new Device();
        device.uuId = Specifications.getAndroidId(mContext);
        device.model = Specifications.getModel();
        device.manufacturer = Specifications.getManufacturer();
        device.brand = Specifications.getBrand();
        device.product = Specifications.getProductName();
        device.osVersion = Specifications.getOsVersion();
        device.kernelVersion = Specifications.getKernelVersion();
        device.serialNumber = Specifications.getBuildSerial();
        device.isRoot = Specifications.isRooted() ? 1 : 0;

        callRegistration(device);

        return device;
    }

    private void callRegistration(Device device) {
        LOGI(TAG, "callRegistration()");
        Call<Integer> call = mService.createDevice(device);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response == null || response.body() == null) {
                    if (response == null) {
                        LOGI(TAG, "response is null");
                    } else {
                        LOGI(TAG, "response body is null");
                    }
                    SettingsUtils.markDeviceAccepted(mContext, false);
                    EventBus.getDefault().post(new StatusEvent("Device registration failed!"));
                    return;
                }
                if (response.body() > 0) {
                    EventBus.getDefault().post(new StatusEvent("Device registered successfully!"));
                } else if (response.body() == 0){
                    EventBus.getDefault().post(new StatusEvent("Device is already registered"));
                }
                SettingsUtils.markDeviceAccepted(mContext, true);
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                SettingsUtils.markDeviceAccepted(mContext, false);
                EventBus.getDefault().post(new StatusEvent("Device registration failed!"));
            }
        });
    }
}
