package hmatalonga.greenhub.protocol;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import hmatalonga.greenhub.Constants;
import hmatalonga.greenhub.GreenHub;
import hmatalonga.greenhub.fragments.HomeFragment;
import hmatalonga.greenhub.sampling.Inspector;
import hmatalonga.greenhub.model.Device;
import hmatalonga.greenhub.utils.NetworkWatcher;

/**
 * Registers devices on server for first-run, connects device to server and provides uuid
 * Created by hugo on 25-03-2016.
 */
public class RegisterHandler {
    private static final String TAG = "RegisterHandler";

    private static RequestQueue sQueue = Volley.newRequestQueue(GreenHub.getContext());
    private static Map<String, String> sParams = new HashMap<>();
    
    private GreenHub sApp = null;
    private int sTimeout = 5000; // 5s default for socket timeout

    public RegisterHandler(GreenHub app) {
        this.sApp = app;
    }

    public RegisterHandler(GreenHub app, int timeout) {
        this.sApp = app;
        this.sTimeout = timeout;
    }

    public Device registerClient() {
        Device device = new Device(Inspector.getAndroidId(GreenHub.getContext()));
        device.setTimestamp(System.currentTimeMillis() / 1000.0);
        device.setModel(Inspector.getModel());
        device.setManufacturer(Inspector.getManufacturer());
        device.setBrand(Inspector.getBrand());
        device.setProduct(Inspector.getProductName());
        device.setOsVersion(Inspector.getOsVersion());
        device.setKernelVersion(Inspector.getKernelVersion());
        device.setSerialNumber(Inspector.getBuildSerial());

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
        String url = Constants.LOCAL_SERVER_URL + "/devices";

        Log.i(TAG, "Test request!");

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(GreenHub.getContext(), response.replaceAll("\n", ""),
                                Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(GreenHub.getContext(), "Test failed",
                                Toast.LENGTH_LONG).show();
                    }
                });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(sTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        sQueue.add(stringRequest);
    }
}
