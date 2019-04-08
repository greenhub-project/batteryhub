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

package com.hmatalonga.greenhub.network;

import android.content.Context;
import android.os.Handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hmatalonga.greenhub.BuildConfig;
import com.hmatalonga.greenhub.Config;
import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.events.StatusEvent;
import com.hmatalonga.greenhub.managers.storage.GreenHubDb;
import com.hmatalonga.greenhub.models.data.AppPermission;
import com.hmatalonga.greenhub.models.data.AppSignature;
import com.hmatalonga.greenhub.models.data.Feature;
import com.hmatalonga.greenhub.models.data.LocationProvider;
import com.hmatalonga.greenhub.models.data.ProcessInfo;
import com.hmatalonga.greenhub.models.data.Sample;
import com.hmatalonga.greenhub.models.data.SensorDetails;
import com.hmatalonga.greenhub.models.data.Upload;
import com.hmatalonga.greenhub.network.services.GreenHubAPIService;
import com.hmatalonga.greenhub.tasks.DeleteSampleTask;
import com.hmatalonga.greenhub.util.LogUtils;
import com.hmatalonga.greenhub.util.NetworkWatcher;
import com.hmatalonga.greenhub.util.SettingsUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Iterator;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.hmatalonga.greenhub.util.LogUtils.logI;
import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Send collected data to the server and receives responses.
 * <p>
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

    private Iterator<Integer> mCollection;

    public CommunicationManager(final Context context, boolean background) {
        mContext = context;
        String url = SettingsUtils.fetchServerUrl(context);

        if (BuildConfig.DEBUG) {
            url = Config.SERVER_URL_DEVELOPMENT;
        }

        LogUtils.logI(TAG, "new CommunicationManager background:" + background);
        LogUtils.logI(TAG, "Server url => " + url);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mService = retrofit.create(GreenHubAPIService.class);
    }

    public void sendSamples() {
        boolean isConnected = NetworkWatcher.hasInternet(
                mContext,
                NetworkWatcher.COMMUNICATION_MANAGER
        );

        if (!isConnected) {
            // Schedule upload next time connectivity changes
            EventBus.getDefault().post(
                    new StatusEvent(mContext.getString(R.string.event_no_connectivity))
            );
            isUploading = false;
            isQueued = true;
            return;
        }

        GreenHubDb database = new GreenHubDb();
        long count = database.count(Sample.class);
        mCollection = database.allSamplesIds();
        database.close();

        LogUtils.logI(TAG, count + " samples to upload...");

        if (!mCollection.hasNext()) {
            EventBus.getDefault().post(
                    new StatusEvent(mContext.getString(R.string.event_no_samples))
            );
            isUploading = false;
            refreshStatus();
            return;
        }

        EventBus.getDefault().post(new StatusEvent(this.makeUploadingMessage(count)));
        uploadSample(mCollection.next());
    }

    /**
     * Uploads a single Sample object, sending a HTTP request to the server
     *
     * @param id Id of sample to upload
     */
    private void uploadSample(final int id) {
        isUploading = true;

        LogUtils.logI(TAG, "Uploading sample => " + id);

        Realm realm = Realm.getDefaultInstance();
        final Sample sample = realm.where(Sample.class).equalTo("id", id).findFirst();
        final Upload upload = sample == null ? null : new Upload(bundleSample(sample));
        realm.close();

        LogUtils.logI(TAG, "Sample found => " + String.valueOf(sample != null));

        if (sample == null && mCollection.hasNext()) {
            uploadSample(mCollection.next());
        } else if (sample == null) {
            new DeleteSampleTask().execute(id);
            isUploading = false;
            isQueued = false;
            uploadAttempts = 0;
            return;
        }

        Call<Integer> call = mService.createSample(upload);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response == null) {
                    EventBus.getDefault().post(
                            new StatusEvent(
                                    mContext.getString(R.string.event_server_response_failed)
                            )
                    );
                    return;
                }
                if (response.body() != null) {
                    handleResponse(response.body(), id);
                } else {
                    handleResponse(RESPONSE_ERROR, -1);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                EventBus.getDefault().post(
                        new StatusEvent(mContext.getString(R.string.event_server_not_responding))
                );
                uploadAttempts++;
                isUploading = false;
                isQueued = true;

                logI(TAG, "HTTP call onFailure uploadAttempts:" + uploadAttempts);

                // Clean up mDatabase
                // new DeleteOldSamplesTask().execute();

                refreshStatus();
            }
        });
    }

    private void handleResponse(int response, int id) {
        if (response == RESPONSE_OKAY) {
            String message =
                    "Sample => " + id + " uploaded successfully! Deleting uploaded sample...";
            logI(TAG, message);
            // delete uploaded sample and upload next one
            new DeleteSampleTask().execute(id);

            if (!mCollection.hasNext()) {
                EventBus.getDefault().post(
                        new StatusEvent(mContext.getString(R.string.event_upload_finished))
                );
                uploadAttempts = 0;
                isUploading = false;
                isQueued = false;

                LogUtils.logI(TAG, "All samples were uploaded!");

                refreshStatus();
            } else {
                uploadSample(mCollection.next());
            }
        } else if (response == RESPONSE_ERROR) {
            EventBus.getDefault().post(
                    new StatusEvent(mContext.getString(R.string.event_error_uploading_sample))
            );
            uploadAttempts++;
            isUploading = false;

            String message =
                    "Sample: " + id + " HTTP response error uploadAttempts:" + uploadAttempts;
            LogUtils.logI(TAG, message);

            // Clean up mDatabase
            // First try-force upload or ask user through notification?
            // new DeleteOldSamplesTask().execute();

            refreshStatus();
        }
    }

    private JsonObject bundleSample(final Sample sample) {
        /*
         * This is a manual approach, not ideal.
         * In the future create a gson builder with proper type adapters.
         */
        JsonObject root = new JsonObject();
        JsonObject child, subChild;
        JsonArray list, subList;

        root.addProperty("uuId", sample.uuId);
        root.addProperty("timestamp", sample.timestamp);
        root.addProperty("version", sample.version);
        root.addProperty("mDatabase", sample.database);
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
            subChild.addProperty(
                    "wifiReceived",
                    sample.networkDetails.networkStatistics.wifiReceived
            );
            subChild.addProperty(
                    "wifiSent",
                    sample.networkDetails.networkStatistics.wifiSent
            );
            subChild.addProperty(
                    "mobileReceived",
                    sample.networkDetails.networkStatistics.mobileReceived
            );
            subChild.addProperty(
                    "mobileSent",
                    sample.networkDetails.networkStatistics.mobileSent
            );
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

        // SensorDetails list
        if (sample.sensorDetailsList != null && !sample.sensorDetailsList.isEmpty()) {
            list = new JsonArray();
            for (SensorDetails el : sample.sensorDetailsList) {
                child = new JsonObject();
                child.addProperty("codeType", el.codeType);
                child.addProperty("fifoMaxEventCount", el.fifoMaxEventCount);
                child.addProperty("fifoReservedEventCount", el.fifoReservedEventCount);
                child.addProperty("highestDirectReportRateLevel", el.highestDirectReportRateLevel);
                child.addProperty("id", el.id);
                child.addProperty("isAdditionalInfoSupported", el.isAdditionalInfoSupported);
                child.addProperty("isDynamicSensor", el.isDynamicSensor);
                child.addProperty("isWakeUpSensor", el.isWakeUpSensor);
                child.addProperty("maxDelay", el.maxDelay);
                child.addProperty("maximumRange", el.maximumRange);
                child.addProperty("minDelay", el.minDelay);
                child.addProperty("name", el.name);
                child.addProperty("power", el.power);
                child.addProperty("reportingMode", el.reportingMode);
                child.addProperty("resolution", el.resolution);
                child.addProperty("stringType", el.stringType);
                child.addProperty("vendor", el.vendor);
                child.addProperty("version", el.version);

                list.add(child);
            }
            root.add("sensorDetailsList", list);
        }

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
                EventBus.getDefault().post(
                        new StatusEvent(mContext.getString(R.string.event_idle))
                );
            }
        }, Config.REFRESH_STATUS_ERROR);
    }

    private String makeUploadingMessage(long count) {
        return mContext.getString(R.string.event_uploading) + " " + count +
                " " + mContext.getString(R.string.event_samples);
    }
}
