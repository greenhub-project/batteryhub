package hmatalonga.greenhub.database;

/**
 * Created by hugo on 09-04-2016.
 */
public class NetworkDetails {
    private String networkType; // optional
    private String mobileNetworkType; // optional
    private String mobileDataStatus; // optional
    private String mobileDataActivity; // optional
    private boolean roamingEnabled; // optional
    private String wifiStatus; // optional
    private int wifiSignalStrength; // optional
    private int wifiLinkSpeed; // optional

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getMobileNetworkType() {
        return mobileNetworkType;
    }

    public void setMobileNetworkType(String mobileNetworkType) {
        this.mobileNetworkType = mobileNetworkType;
    }

    public String getMobileDataStatus() {
        return mobileDataStatus;
    }

    public void setMobileDataStatus(String mobileDataStatus) {
        this.mobileDataStatus = mobileDataStatus;
    }

    public String getMobileDataActivity() {
        return mobileDataActivity;
    }

    public void setMobileDataActivity(String mobileDataActivity) {
        this.mobileDataActivity = mobileDataActivity;
    }

    public boolean isRoamingEnabled() {
        return roamingEnabled;
    }

    public void setRoamingEnabled(boolean roamingEnabled) {
        this.roamingEnabled = roamingEnabled;
    }

    public String getWifiStatus() {
        return wifiStatus;
    }

    public void setWifiStatus(String wifiStatus) {
        this.wifiStatus = wifiStatus;
    }

    public int getWifiSignalStrength() {
        return wifiSignalStrength;
    }

    public void setWifiSignalStrength(int wifiSignalStrength) {
        this.wifiSignalStrength = wifiSignalStrength;
    }

    public int getWifiLinkSpeed() {
        return wifiLinkSpeed;
    }

    public void setWifiLinkSpeed(int wifiLinkSpeed) {
        this.wifiLinkSpeed = wifiLinkSpeed;
    }
}
