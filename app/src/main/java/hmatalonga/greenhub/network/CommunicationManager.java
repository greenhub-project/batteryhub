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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import hmatalonga.greenhub.GreenHubHelper;
import hmatalonga.greenhub.fragments.HomeFragment;
import hmatalonga.greenhub.managers.storage.GreenHubDb;
import hmatalonga.greenhub.models.data.Sample;
import hmatalonga.greenhub.util.NetworkWatcher;

/**
 * Send collected data to the server and receives responses.
 *
 * Created by hugo on 25-03-2016.
 */
public class CommunicationManager {
    private static final String TAG = "CommunicationManager";

    private static RequestQueue sQueue = Volley.newRequestQueue(GreenHubHelper.getContext());
    private static Map<String, String> sParams = new HashMap<>();

    private GreenHubHelper mApp;
    private Gson mGson;
    private Context mContext;
    private SortedMap<Long, Sample> map;
    private GreenHubDb db;
    private ArrayList<Sample> samples;
    private int sTimeout = 5000; // 5s default for socket timeout
    private int done, samplesCount;

    public CommunicationManager(GreenHubHelper app) {
        this.mApp = app;
        this.mContext = GreenHubHelper.getContext();
        this.mGson = new Gson();
        db = GreenHubDb.getInstance(mContext);
    }

    public CommunicationManager(GreenHubHelper app, int timeout) {
        this.mApp = app;
        this.mContext = GreenHubHelper.getContext();
        this.mGson = new Gson();
        this.sTimeout = timeout;
        db = GreenHubDb.getInstance(mContext);
    }

    public void sendSamples() {
        boolean connected = NetworkWatcher.hasInternet(mContext);

        if (!connected) {
            HomeFragment.setStatus("Not connected");
            return;
        }
        samplesCount = db.countSamples();
        done = 0;

        HomeFragment.setStatus("Samples sent " + done + "/" + samplesCount);

        map = db.queryOldestSamples(samplesCount); // Config.COMMS_MAX_UPLOAD_BATCH

        if (map.size() > 0)
            uploadSamples(map.values());
        else {
            Log.w(TAG, "No samples to send.");
            HomeFragment.setStatus("No samples to send.");
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
        String url = mApp.serverURL + "/samples";
        sParams.clear();
        sParams.put("data", mGson.toJson(sample));

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, response);
                        handleResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }){
            @Override
            protected Map<String, String> getParams() {
                return sParams;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(sTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        sQueue.add(stringRequest);
    }

    private void handleResponse(String response) {
        if (response.equals("OK")) {
            done++;

            // status
            HomeFragment.setStatus("Samples sent " + done + "/" + samplesCount);

            if (done == samplesCount) {
                // status finished
                SortedSet<Long> uploaded = new TreeSet<Long>();
                int i = 0;
                for (Long s : map.keySet()) {
                    if (i < done)
                        uploaded.add(s);
                    i += 1;
                }
                db.deleteSamples(uploaded);
            }
            else
                uploadSample(samples.get(done));
        }
        else
            HomeFragment.setStatus("Error sending samples. Try again later");
    }
}
