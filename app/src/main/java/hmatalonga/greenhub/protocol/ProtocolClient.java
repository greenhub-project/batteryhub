package hmatalonga.greenhub.protocol;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import android.content.Context;
import android.util.Log;
import hmatalonga.greenhub.Constants;
import hmatalonga.greenhub.thrift.GreenHubService;

/**
 * Created by hugo on 25-03-2016.
 */
public class ProtocolClient {
    public static final String TAG = "ProtocolClient";
    public static final String SERVER_PROPERTIES = "caratserver.properties";
    public static int SERVER_PORT = 0;
    public static String SERVER_ADDRESS = null;

    /**
     * FIXME: this needs to come from a factory, so that connections are not
     * kept open unnecessarily, and that they do not become stale, and that we
     * handle disconnections gracefully.
     *
     * @param c
     * @return
     * @throws NumberFormatException
     * @throws TTransportException
     */
    public static GreenHubService.Client getInstance(Context c) throws NumberFormatException, TTransportException {
        if (SERVER_ADDRESS == null) {
            Properties properties = new Properties();
            try {
                InputStream raw = c.getAssets().open(SERVER_PROPERTIES);
                if (raw != null) {
                    properties.load(raw);
                    if (properties.containsKey("PORT"))
                        SERVER_PORT = Integer.parseInt(properties.getProperty(
                                "PORT", "8080"));
                    if (properties.containsKey("ADDRESS"))
                        SERVER_ADDRESS = properties.getProperty("ADDRESS",
                                "server.caratproject.com");
                    if (Constants.DEBUG)
                        Log.d(TAG, "Set address=" + SERVER_ADDRESS + " port="
                                + SERVER_PORT);
                } else
                    Log.e(TAG, "Could not open server property file!");
            } catch (IOException e) {
                Log.e(TAG,
                        "Could not open server property file: " + e.toString());
            }
        }
        if (SERVER_ADDRESS == null || SERVER_PORT == 0)
            return null;

        TSocket soc = new TSocket(SERVER_ADDRESS, SERVER_PORT, 60000);
        TProtocol p = new TBinaryProtocol(soc, true, true);
        GreenHubService.Client instance = new GreenHubService.Client(p);

        if (soc != null && !soc.isOpen())
            soc.open();

        return instance;
    }

    public static GreenHubService.Client open(Context c) throws NumberFormatException, TTransportException {
        if (Constants.DEBUG)
            Log.d("ProtocolClient", "trying to get an instance of CaratProtocol.");
        return getInstance(c);
    }

}
