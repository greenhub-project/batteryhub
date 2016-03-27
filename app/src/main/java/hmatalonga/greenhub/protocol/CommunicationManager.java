package hmatalonga.greenhub.protocol;

import android.content.SharedPreferences;

import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

import java.util.ArrayList;

import hmatalonga.greenhub.GreenHub;
import hmatalonga.greenhub.thrift.GreenHubService;
import hmatalonga.greenhub.thrift.Sample;
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

    public boolean uploadSamples(ArrayList<Sample> samples) {
        GreenHubService.Client instance = null;
        int succeeded = 0;
        ArrayList<Sample> samplesLeft = new ArrayList<Sample>();
        // registerLocal();
        try {
            instance = ProtocolClient.open(GreenHub.getContext());
            // registerOnFirstRun(instance);

            for (Sample s : samples) {
                boolean success = false;
                try {
                    success = instance.uploadSample(s);
                } catch (Throwable th) {
                    GHLogger.debug(TAG, "Error uploading sample.");
                }
                if (success)
                    succeeded++;
                else
                    samplesLeft.add(s);
            }

            safeClose(instance);
        } catch (Throwable th) {
            GHLogger.debug(TAG, "Error refreshing main reports.");
            safeClose(instance);
        }

        return false;
    }

    public static void safeClose(GreenHubService.Client c) {
        if (c == null)
            return;
        TProtocol i = c.getInputProtocol();
        TProtocol o = c.getOutputProtocol();
        if (i != null) {
            TTransport it = i.getTransport();
            if (it != null)
                it.close();
        }
        if (o != null) {
            TTransport it = o.getTransport();
            if (it != null)
                it.close();
        }
    }
}
