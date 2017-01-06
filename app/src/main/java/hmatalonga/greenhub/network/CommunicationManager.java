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

package hmatalonga.greenhub.network;

import android.content.Context;
import android.os.Handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.events.StatusEvent;
import hmatalonga.greenhub.managers.storage.GreenHubDb;
import hmatalonga.greenhub.models.data.Sample;
import hmatalonga.greenhub.network.services.GreenHubAPIService;
import hmatalonga.greenhub.util.GsonRealmBuilder;
import hmatalonga.greenhub.util.NetworkWatcher;
import hmatalonga.greenhub.util.SettingsUtils;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static hmatalonga.greenhub.util.LogUtils.LOGI;
import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Send collected data to the server and receives responses.
 *
 * Created by hugo on 25-03-2016.
 */
public class CommunicationManager {

    private static final String TAG = makeLogTag(CommunicationManager.class);

    private static final int RESPONSE_OKAY = 1;
    private static final int RESPONSE_ERROR = 0;

    public static boolean isUploading = false;
    public static boolean isQueued = false;

    private Context mContext;

    private GreenHubAPIService mService;

    private GreenHubDb mDatabase;

    private RealmResults<Sample> mCollection;

    public CommunicationManager(final Context context) {
        mContext = context;
        mDatabase = new GreenHubDb();
        Gson gson = GsonRealmBuilder.get();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SettingsUtils.fetchServerUrl(context))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        mService = retrofit.create(GreenHubAPIService.class);
    }

    public void sendSamples() {
        boolean connected = NetworkWatcher.hasInternet(mContext, NetworkWatcher.COMMUNICATION_MANAGER);

        mCollection = mDatabase.allSamples();

        if (mCollection.isEmpty()) {
            EventBus.getDefault().post(new StatusEvent("No samples to send..."));
            isUploading = false;
            return;
        }

        if (connected) {
            EventBus.getDefault().post(new StatusEvent("Uploading samples..."));
            isUploading = true;
            uploadSample(mCollection.first());
        }

    }

    /**
     * Uploads a single Sample object, sending a HTTP request to the server
     * @param sample object to sendSamples
     * @return if uploaded successfully returns true, otherwise returns false
     */
    private void uploadSample(final Sample sample) {
        LOGI(TAG, "Uploading Sample => " + sample.id);
        final int id = sample.id;
        Call<Integer> call = mService.createSample(prepareSample(sample));
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response == null) {
                    EventBus.getDefault().post(new StatusEvent("Server response has failed..."));
                    return;
                }

                LOGI(TAG, "Sample => " + id + " uploaded successfully!");
                handleResponse(response.body());
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                t.printStackTrace();
                EventBus.getDefault().post(new StatusEvent("Server is not responding..."));
                isUploading = false;
                refreshStatus();
            }
        });
    }

    private void handleResponse(int response) {
        if (response == RESPONSE_OKAY) {
            if (mCollection.isEmpty()) {
                EventBus.getDefault().post(new StatusEvent("Upload finished!"));
                isUploading = false;
                refreshStatus();
                return;
            }

            LOGI(TAG, "Deleting uploaded sample...");

            // delete uploaded sample and upload next one
//            if (mCollection.deleteFirstFromRealm()) {
//                uploadSample(mCollection.first());
//                return;
//            }

            EventBus.getDefault().post(new StatusEvent("Not all samples were sent..."));
        } else if (response == RESPONSE_ERROR){
            EventBus.getDefault().post(new StatusEvent("Server error uploading samples."));
        }
        isUploading = false;
        refreshStatus();
    }

    private JsonObject prepareSample(final Sample sample) {
        /**
         * This is a manual approach, not ideal.
         * In the future create a gson builder with proper type adapters.
         */
        JsonObject object = new JsonObject();
        object.addProperty("uuId", sample.uuId);
        object.addProperty("timestamp", sample.timestamp);
        object.addProperty("batteryState", sample.batteryState);
        object.addProperty("batteryLevel", sample.batteryState);
        return object;
    }

    private void refreshStatus() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new StatusEvent(Config.STATUS_IDLE));
            }
        }, Config.REFRESH_STATUS_ERROR);
    }
}
