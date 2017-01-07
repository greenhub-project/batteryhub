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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;

import hmatalonga.greenhub.BuildConfig;
import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.events.StatusEvent;
import hmatalonga.greenhub.managers.storage.GreenHubDb;
import hmatalonga.greenhub.models.data.AppPermission;
import hmatalonga.greenhub.models.data.AppSignature;
import hmatalonga.greenhub.models.data.Feature;
import hmatalonga.greenhub.models.data.LocationProvider;
import hmatalonga.greenhub.models.data.ProcessInfo;
import hmatalonga.greenhub.models.data.Sample;
import hmatalonga.greenhub.models.data.Upload;
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

    public static int uploadAttempts = 0;

    private Context mContext;

    private GreenHubAPIService mService;

    private GreenHubDb mDatabase;

    private RealmResults<Sample> mCollection;

    private boolean mOnBackground;

    public CommunicationManager(final Context context, boolean background) {
        mContext = context;
        mDatabase = new GreenHubDb();
        mOnBackground = background;
        String url = SettingsUtils.fetchServerUrl(context);

        if (BuildConfig.DEBUG) {
            url = Config.SERVER_URL_DEVELOPMENT;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mService = retrofit.create(GreenHubAPIService.class);
    }

    public void sendSamples() {
        boolean isConnected = NetworkWatcher.hasInternet(mContext, NetworkWatcher.COMMUNICATION_MANAGER);

        mCollection = mDatabase.allSamples();

        if (mCollection.isEmpty()) {
            EventBus.getDefault().post(new StatusEvent("No samples to send..."));
            isUploading = false;
            refreshStatus();
            return;
        }

        if (isConnected) {
            EventBus.getDefault().post(new StatusEvent("Uploading samples..."));
            isUploading = true;
            uploadSample(mCollection.first());
        } else {
            // Schedule upload next time connectivity changes
            CommunicationManager.isQueued = true;
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
        final Upload upload = new Upload(bundleSample(sample));

        Call<Integer> call = mService.createSample(upload);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response == null) {
                    EventBus.getDefault().post(new StatusEvent("Server response has failed..."));
                    return;
                }

                LOGI(TAG, "Sample => " + id + " uploaded successfully!");

                if (response.body() != null) {
                    handleResponse(response.body());
                } else {
                    handleResponse(RESPONSE_ERROR);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                t.printStackTrace();
                EventBus.getDefault().post(new StatusEvent("Server is not responding..."));
                // If was called from service count uploadAttempts attempts
                if (mOnBackground) {
                    uploadAttempts++;
                }
                isUploading = false;
                refreshStatus();
            }
        });
    }

    private void handleResponse(int response) {
        if (response == RESPONSE_OKAY) {
            LOGI(TAG, "Deleting uploaded sample...");

            // delete uploaded sample and upload next one
            mCollection = mDatabase.deleteSampleFromQuery(mCollection);

            if (mCollection.isEmpty()) {
                EventBus.getDefault().post(new StatusEvent("Upload finished!"));
            } else {
                uploadSample(mCollection.first());
            }

        } else if (response == RESPONSE_ERROR){
            EventBus.getDefault().post(new StatusEvent("Server error uploading samples."));
        }

        // If was called from service reset uploadAttempts counter
        if (mOnBackground) {
            uploadAttempts = 0;
        }
        isUploading = false;
        refreshStatus();
    }

    private JsonObject bundleSample(final Sample sample) {
        /**
         * This is a manual approach, not ideal.
         * In the future create a gson builder with proper type adapters.
         */
        JsonObject root = new JsonObject();
        JsonObject child, subChild;
        JsonArray list, subList;

        root.addProperty("uuId", sample.uuId);
        root.addProperty("timestamp", sample.timestamp);
        root.addProperty("batteryState", sample.batteryState);
        root.addProperty("batteryLevel", sample.batteryLevel);
        root.addProperty("memoryWired", sample.memoryWired);
        root.addProperty("memoryActive", sample.memoryActive);
        root.addProperty("memoryInactive", sample.memoryInactive);
        root.addProperty("memoryFree", sample.memoryFree);
        root.addProperty("memoryUser", sample.memoryUser);
        root.addProperty("triggeredBy", sample.triggeredBy);
        root.addProperty("networkStatus", sample.networkStatus);
        root.addProperty("distanceTraveled", sample.distanceTraveled);
        root.addProperty("screenBrightness", sample.screenBrightness);

        // NetworkDetails
        child = new JsonObject();
        child.addProperty("networkType", sample.networkDetails.networkType);
        child.addProperty("mobileNetworkType", sample.networkDetails.mobileNetworkType);
        child.addProperty("mobileDataStatus", sample.networkDetails.mobileDataStatus);
        child.addProperty("mobileDataActivity", sample.networkDetails.mobileDataActivity);
        child.addProperty("roamingEnabled", sample.networkDetails.roamingEnabled);
        child.addProperty("wifiStatus", sample.networkDetails.wifiStatus);
        child.addProperty("wifiSignalStrength", sample.networkDetails.wifiSignalStrength);
        child.addProperty("wifiLinkSpeed", sample.networkDetails.wifiLinkSpeed);
        child.addProperty("wifiApStatus", sample.networkDetails.wifiApStatus);
        child.addProperty("networkOperator", sample.networkDetails.networkOperator);
        child.addProperty("simOperator", sample.networkDetails.simOperator);
        child.addProperty("mcc", sample.networkDetails.mcc);
        child.addProperty("mnc", sample.networkDetails.mnc);

        // NetworkDetails->NetworkStatistics
        if (sample.networkDetails.networkStatistics != null) {
            subChild = new JsonObject();
            subChild.addProperty("wifiReceived", sample.networkDetails.networkStatistics.wifiReceived);
            subChild.addProperty("wifiSent", sample.networkDetails.networkStatistics.wifiSent);
            subChild.addProperty("mobileReceived", sample.networkDetails.networkStatistics.mobileReceived);
            subChild.addProperty("mobileSent", sample.networkDetails.networkStatistics.mobileSent);
            child.add("networkStatistics", subChild);
        }

        root.add("networkDetails", child);

        // Battery Details
        child = new JsonObject();
        child.addProperty("charger", sample.batteryDetails.charger);
        child.addProperty("health", sample.batteryDetails.health);
        child.addProperty("voltage", sample.batteryDetails.voltage);
        child.addProperty("temperature", sample.batteryDetails.temperature);
        child.addProperty("technology", sample.batteryDetails.technology);
        child.addProperty("capacity", sample.batteryDetails.capacity);
        child.addProperty("chargeCounter", sample.batteryDetails.chargeCounter);
        child.addProperty("currentAverage", sample.batteryDetails.currentAverage);
        child.addProperty("currentNow", sample.batteryDetails.currentNow);
        child.addProperty("energyCounter", sample.batteryDetails.energyCounter);
        root.add("batteryDetails", child);

        // CpuStatus
        child = new JsonObject();
        child.addProperty("cpuUsage", sample.cpuStatus.cpuUsage);
        child.addProperty("upTime", sample.cpuStatus.upTime);
        child.addProperty("sleepTime", sample.cpuStatus.sleepTime);
        root.add("cpuStatus", child);

        root.addProperty("screenOn", sample.screenOn);
        root.addProperty("timeZone", sample.timeZone);

        // Settings
        child = new JsonObject();
        child.addProperty("bluetoothEnabled", sample.settings.bluetoothEnabled);
        child.addProperty("locationEnabled", sample.settings.locationEnabled);
        child.addProperty("powersaverEnabled", sample.settings.powersaverEnabled);
        child.addProperty("flashlightEnabled", sample.settings.flashlightEnabled);
        child.addProperty("nfcEnabled", sample.settings.nfcEnabled);
        child.addProperty("unknownSources", sample.settings.unknownSources);
        child.addProperty("developerMode", sample.settings.developerMode);
        root.add("settings", child);

        // StorageDetails
        child = new JsonObject();
        child.addProperty("free", sample.storageDetails.free);
        child.addProperty("total", sample.storageDetails.total);
        child.addProperty("freeExternal", sample.storageDetails.freeExternal);
        child.addProperty("totalExternal", sample.storageDetails.totalExternal);
        child.addProperty("freeSystem", sample.storageDetails.freeSystem);
        child.addProperty("totalSystem", sample.storageDetails.totalSystem);
        child.addProperty("freeSecondary", sample.storageDetails.freeSecondary);
        child.addProperty("totalSecondary", sample.storageDetails.totalSecondary);
        root.add("storageDetails", child);

        root.addProperty("countryCode", sample.countryCode);

        // LocationProviders
        if (sample.locationProviders != null && !sample.locationProviders.isEmpty()) {
            list = new JsonArray();
            for (LocationProvider el : sample.locationProviders) {
                child = new JsonObject();
                child.addProperty("provider", el.provider);
                list.add(child);
            }
            root.add("locationProviders", list);
        }

        // Features
        if (sample.features != null && !sample.features.isEmpty()) {
            list = new JsonArray();
            for (Feature el : sample.features) {
                child = new JsonObject();
                child.addProperty("key", el.key);
                child.addProperty("value", el.value);
                list.add(child);
            }
            root.add("features", list);
        }

        // ProcessInfos
        if (sample.processInfos != null && !sample.processInfos.isEmpty()) {
            list = new JsonArray();
            for (ProcessInfo el : sample.processInfos) {
                child = new JsonObject();
                child.addProperty("processId", el.processId);
                child.addProperty("name", el.name);
                child.addProperty(
                        "applicationLabel",
                        el.applicationLabel == null ? "" : el.applicationLabel
                );
                child.addProperty("isSystemApp", el.isSystemApp);
                child.addProperty("importance", el.importance);
                child.addProperty(
                        "versionName",
                        el.versionName == null ? "" : el.versionName
                );
                child.addProperty("versionCode", el.versionCode);
                child.addProperty(
                        "installationPkg",
                        el.installationPkg == null ? "" : el.installationPkg
                );

                // AppPermissions
                if (el.appPermissions != null && !el.appPermissions.isEmpty()) {
                    subList = new JsonArray();
                    for (AppPermission x : el.appPermissions) {
                        subChild = new JsonObject();
                        subChild.addProperty("permission", x.permission);
                        subList.add(subChild);
                    }
                    child.add("appPermissions", subList);
                }

                // AppSignatures
                if (el.appSignatures != null && !el.appSignatures.isEmpty()) {
                    subList = new JsonArray();
                    for (AppSignature x : el.appSignatures) {
                        subChild = new JsonObject();
                        subChild.addProperty("signature", x.signature);
                        subList.add(subChild);
                    }
                    child.add("appSignatures", subList);
                }

                // Add current process to array list
                list.add(child);
            }
            root.add("processInfos", list);
        }

        return root;
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
