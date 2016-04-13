package hmatalonga.greenhub.protocol;

import android.content.SharedPreferences;

import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

import java.util.ArrayList;

import hmatalonga.greenhub.GreenHub;
import hmatalonga.greenhub.utils.GHLogger;

/**
 * Send collected data to the server and receives responses
 * Created by hugo on 25-03-2016.
 */
public class CommunicationManager {
    private static final String TAG = "CommunicationManager";

    private GreenHub app = null;
    private SharedPreferences preferences = null;

    public CommunicationManager(GreenHub app) {
        this.app = app;
    }
}
