package hmatalonga.greenhub.protocol;

import android.content.SharedPreferences;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import hmatalonga.greenhub.Constants;
import hmatalonga.greenhub.GreenHub;
import hmatalonga.greenhub.sampling.Inspector;
import hmatalonga.greenhub.storage.Device;
import hmatalonga.greenhub.utils.GHLogger;

/**
 * Registers devices on server for first-run, connects device to server and provides uuid
 * Created by hugo on 25-03-2016.
 */
public class RegisterHandler {
    private static final String TAG = "RegisterHandler";

    private GreenHub app = null;
    private SharedPreferences preferences = null;

    public RegisterHandler(GreenHub app) {
        this.app = app;
    }

    public void registerClient(){
        Device device = new Device(Inspector.getAndroidId(GreenHub.getContext()));
        device.setTimestamp(System.currentTimeMillis() / 1000.0);
        device.setModel(Inspector.getModel());
        device.setManufacturer(Inspector.getManufacturer());
        device.setBrand(Inspector.getBrand());
        device.setProduct(Inspector.getProductName());
        device.setOsVersion(Inspector.getOsVersion());
        device.setKernelVersion(Inspector.getKernelVersion());
        device.setSerialNumber(Inspector.getBuildSerial());

        requestRegistration(device);
    }

    private void requestRegistration(Device device) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(GreenHub.getContext());
        String url = Constants.SERVER_URL;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Toast.makeText(GreenHub.getContext(), "It Works!", Toast.LENGTH_LONG).show();
                        GHLogger.debug(TAG, "Response is: " + response.substring(0, 500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(GreenHub.getContext(), "That didn't work!", Toast.LENGTH_LONG).show();
                GHLogger.debug(TAG, "That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
