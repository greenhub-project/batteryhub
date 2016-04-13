package hmatalonga.greenhub.protocol;

import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import hmatalonga.greenhub.Constants;
import hmatalonga.greenhub.GreenHub;
import hmatalonga.greenhub.sampling.Inspector;
import hmatalonga.greenhub.model.Device;

/**
 * Registers devices on server for first-run, connects device to server and provides uuid
 * Created by hugo on 25-03-2016.
 */
public class RegisterHandler {
    private static final String TAG = "RegisterHandler";

    private GreenHub app = null;

    public RegisterHandler(GreenHub app) {
        this.app = app;
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
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(GreenHub.getContext());
        String url = app.serverURL + "/device";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // FIXME: Better response message handling, work on server-side
                        response = response.replaceAll("\n", "");
                        if (!response.equals("Device was registered!"))
                            response = "Device is already registered...";
                        // Toast.makeText(GreenHub.context, response, Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        // Toast.makeText(GreenHub.context, "Server has returned an error...", Toast.LENGTH_LONG).show();
                    }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("uuid", device.getUuId());
                params.put("model", device.getModel());
                params.put("manufacturer", device.getManufacturer());
                params.put("brand", device.getBrand());
                params.put("os", device.getOsVersion());
                params.put("product", device.getProduct());
                params.put("kernel", device.getKernelVersion());
                params.put("serialnum", device.getSerialNumber());

                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void testRegistration() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(GreenHub.getContext());
        String url = Constants.LOCAL_SERVER_URL + "/device";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(GreenHub.getContext(), response.replaceAll("\n", ""), Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(GreenHub.getContext(), "Test failed", Toast.LENGTH_LONG).show();
                    }
                });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
