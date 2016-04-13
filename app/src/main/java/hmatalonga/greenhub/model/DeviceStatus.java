package hmatalonga.greenhub.model;

/**
 * Created by hugo on 09-04-2016.
 */
public class DeviceStatus {
    private String greenHubID;
    private String batteryLife;

    public DeviceStatus() {}


    public String getGreenHubID() {
        return greenHubID;
    }

    public void setGreenHubID(String greenHubID) {
        this.greenHubID = greenHubID;
    }

    public String getBatteryLife() {
        return batteryLife;
    }

    public void setBatteryLife(String batteryLife) {
        this.batteryLife = batteryLife;
    }
}
