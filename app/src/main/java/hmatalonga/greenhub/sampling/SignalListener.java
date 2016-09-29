package hmatalonga.greenhub.sampling;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;

/**
 * Created by hugo on 13-04-2016.
 */
public class SignalListener extends PhoneStateListener {

    private int gsmSignal = 0;
    private int evdoDbm = 0;
    private int cdmaDbm = 0;

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        // TODO Auto-generated method stub
        super.onSignalStrengthsChanged(signalStrength);
        gsmSignal = signalStrength.getGsmSignalStrength();
        cdmaDbm  = signalStrength.getCdmaDbm();
        evdoDbm = signalStrength.getEvdoDbm();
    }

    public int getGsmSignal() {
        return gsmSignal;
    }

    public int getEvdoDbm() {
        return evdoDbm;
    }

    public int getCdmaDbm() {
        return cdmaDbm;
    }
}