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
import android.util.Log;


import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import hmatalonga.greenhub.events.StatusEvent;
import hmatalonga.greenhub.managers.storage.GreenHubDb;
import hmatalonga.greenhub.models.data.Sample;
import hmatalonga.greenhub.util.GreenHubHelper;
import hmatalonga.greenhub.util.NetworkWatcher;

/**
 * Send collected data to the server and receives responses.
 *
 * Created by hugo on 25-03-2016.
 */
public class CommunicationManager {
    private static final String TAG = "CommunicationManager";

    private static Map<String, String> sParams = new HashMap<>();

    private GreenHubHelper mApp;
    private Gson mGson;
    private Context mContext;
    private SortedMap<Long, Sample> map;
    private GreenHubDb database;
    private ArrayList<Sample> samples;
    private int sTimeout = 5000; // 5s default for socket timeout
    private int done;
    private long samplesCount;

    public CommunicationManager(GreenHubHelper app) {
        this.mApp = app;
        this.mContext = null;
        this.mGson = new Gson();
        database = new GreenHubDb();
    }

    public CommunicationManager(GreenHubHelper app, int timeout) {
        this.mApp = app;
        this.mContext = null;
        this.mGson = new Gson();
        this.sTimeout = timeout;
        database = new GreenHubDb();
    }

    public void sendSamples() {
        boolean connected = NetworkWatcher.hasInternet(mContext);

        if (!connected) {
            // HomeFragment.setStatus("Not connected");
            return;
        }
        samplesCount = database.count(Sample.class);
        done = 0;

        // HomeFragment.setStatus("Samples sent " + done + "/" + samplesCount);

        // map = database.queryOldestSamples(samplesCount); // Config.COMMS_MAX_UPLOAD_BATCH

        if (map.size() > 0)
            uploadSamples(null);
        else {
            Log.w(TAG, "No samples to send.");
            EventBus.getDefault().post(new StatusEvent("No samples to send."));
        }
    }

    private void uploadSamples(Collection<Sample> collection) {
        samples = new ArrayList<>();
        samples.addAll(collection);

        try {
            uploadSample(samples.get(done));
        } catch (Throwable th) {
            Log.e(TAG, "Error refreshing main reports.", th);
        }
    }


    /**
     * Uploads a single Sample object, sending a HTTP request to the server
     * @param sample object to upload
     * @return if uploaded successfully returns true, otherwise returns false
     */
    private void uploadSample(final Sample sample) {
    }

    private void handleResponse(String response) {
        if (response.equals("OK")) {
            done++;

            // status
            // HomeFragment.setStatus("Samples sent " + done + "/" + samplesCount);

            if (done == samplesCount) {
                // status finished
                SortedSet<Long> uploaded = new TreeSet<>();
                int i = 0;
                for (Long s : map.keySet()) {
                    if (i < done)
                        uploaded.add(s);
                    i += 1;
                }
                // database.deleteSamples(uploaded);
            }
            else
                uploadSample(samples.get(done));
        }
        else;
             //HomeFragment.setStatus("Error sending samples. Try again later");
    }
}
