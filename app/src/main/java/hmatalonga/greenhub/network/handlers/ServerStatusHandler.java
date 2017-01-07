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

package hmatalonga.greenhub.network.handlers;

import android.content.Context;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.models.ServerStatus;
import hmatalonga.greenhub.network.services.GreenHubStatusService;
import hmatalonga.greenhub.tasks.RegisterDeviceTask;
import hmatalonga.greenhub.util.SettingsUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static hmatalonga.greenhub.util.LogUtils.LOGI;
import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * ServerStatusHandler.
 */
public class ServerStatusHandler {
    private static final String TAG = makeLogTag(ServerStatusHandler.class);

    private GreenHubStatusService mService;

    public ServerStatusHandler() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.SERVER_STATUS_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mService = retrofit.create(GreenHubStatusService.class);
    }

    public void callGetStatus(final Context context) {
        Call<ServerStatus> call = mService.getStatus();
        call.enqueue(new Callback<ServerStatus>() {
            @Override
            public void onResponse(Call<ServerStatus> call, Response<ServerStatus> response) {
                LOGI(TAG, "Server Status: { server: " + response.body().server + ", version: " + response.body().version + " }");

                // Server url has changed so it is necessary to register device again
                if (!SettingsUtils.fetchServerUrl(context).equals(response.body().server)) {
                    SettingsUtils.markDeviceAccepted(context, false);
                }

                // Save new server url
                SettingsUtils.saveServerUrl(context, response.body().server);

                // Register device on the web server
                if (!SettingsUtils.isDeviceRegistered(context)) {
                    new RegisterDeviceTask().execute(context);
                }
            }

            @Override
            public void onFailure(Call<ServerStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
