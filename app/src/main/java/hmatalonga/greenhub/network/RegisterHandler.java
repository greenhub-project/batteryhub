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

import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.GreenHubHelper;
import hmatalonga.greenhub.fragments.HomeFragment;
import hmatalonga.greenhub.managers.sampling.Inspector;
import hmatalonga.greenhub.models.Specifications;
import hmatalonga.greenhub.models.data.Device;

/**
 * Registers devices on server for first-run, connects device to server and provides uuid.
 *
 * Created by hugo on 25-03-2016.
 */
public class RegisterHandler {
    private static final String TAG = "RegisterHandler";

    private static RequestQueue sQueue = Volley.newRequestQueue(GreenHubHelper.getContext());
    private static Map<String, String> sParams = new HashMap<>();
    
    private GreenHubHelper sApp = null;
    private int sTimeout = 5000; // 5s default for socket timeout

    public RegisterHandler(GreenHubHelper app) {
        this.sApp = app;
    }

    public RegisterHandler(GreenHubHelper app, int timeout) {
        this.sApp = app;
        this.sTimeout = timeout;
    }

    public Device registerClient() {
        Device device = new Device(Specifications.getAndroidId(GreenHubHelper.getContext()));
        device.setTimestamp(System.currentTimeMillis() / 1000.0);
        device.setModel(Specifications.getModel());
        device.setManufacturer(Specifications.getManufacturer());
        device.setBrand(Specifications.getBrand());
        device.setProduct(Specifications.getProductName());
        device.setOsVersion(Specifications.getOsVersion());
        device.setKernelVersion(Specifications.getKernelVersion());
        device.setSerialNumber(Specifications.getBuildSerial());

        postRegistration(device);

        return device;
    }

    private void postRegistration(final Device device) {
        String url = sApp.serverURL + "/devices";
        sParams.clear();
        sParams.put("uuid", device.getUuId());
        sParams.put("model", device.getModel());
        sParams.put("manufacturer", device.getManufacturer());
        sParams.put("brand", device.getBrand());
        sParams.put("os", device.getOsVersion());
        sParams.put("product", device.getProduct());
        sParams.put("kernel", device.getKernelVersion());
        sParams.put("serialnum", device.getSerialNumber());

        Log.i(TAG, "Performing request.");
        
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // FIXME: Better response message handling, work on server-side
                        response = response.replaceAll("\n", "");
                        if (!response.equals("Device was registered"))
                            response = "Device is already registered";
                        HomeFragment.setStatus(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        HomeFragment.setStatus("Error on registering!");
                    }
        }){
            @Override
            protected Map<String, String> getParams() {
                return sParams;
            }
        };
        // Set Retry Policy for socket timeout
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(sTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Add the request to the RequestQueue.
        sQueue.add(stringRequest);
    }

    private void testRegistration() {
        String url = Config.LOCAL_SERVER_URL + "/devices";

        Log.i(TAG, "Test request!");

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(GreenHubHelper.getContext(), response.replaceAll("\n", ""),
                                Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(GreenHubHelper.getContext(), "Test failed",
                                Toast.LENGTH_LONG).show();
                    }
                });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(sTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        sQueue.add(stringRequest);
    }
}
