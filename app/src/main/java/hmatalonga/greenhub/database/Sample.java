package hmatalonga.greenhub.database;

import java.util.List;

/**
 * Created by hugo on 09-04-2016.
 */
public class Sample {
    private String uuId; // required
    private double timestamp; // optional
    private List<ProcessInfo> piList; // optional
    private String batteryState; // optional
    private double batteryLevel; // optional
    private int memoryWired; // optional
    private int memoryActive; // optional
    private int memoryInactive; // optional
    private int memoryFree; // optional
    private int memoryUser; // optional
    private String triggeredBy; // optional
    private String networkStatus; // optional
    private double distanceTraveled; // optional
    private int screenBrightness; // optional
    private NetworkDetails networkDetails; // optional
    private BatteryDetails batteryDetails; // optional
    private CpuStatus cpuStatus; // optional
    private List<String> locationProviders; // optional
    private CallInfo callInfo; // optional
    private int screenOn; // optional
    private String timeZone; // optional
    private int unknownSources; // optional
    private int developerMode; // optional
    private List<Feature> extra; // optional

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public List<ProcessInfo> getPiList() {
        return piList;
    }

    public void setPiList(List<ProcessInfo> piList) {
        this.piList = piList;
    }

    public String getBatteryState() {
        return batteryState;
    }

    public void setBatteryState(String batteryState) {
        this.batteryState = batteryState;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public int getMemoryWired() {
        return memoryWired;
    }

    public void setMemoryWired(int memoryWired) {
        this.memoryWired = memoryWired;
    }

    public int getMemoryActive() {
        return memoryActive;
    }

    public void setMemoryActive(int memoryActive) {
        this.memoryActive = memoryActive;
    }

    public int getMemoryInactive() {
        return memoryInactive;
    }

    public void setMemoryInactive(int memoryInactive) {
        this.memoryInactive = memoryInactive;
    }

    public int getMemoryFree() {
        return memoryFree;
    }

    public void setMemoryFree(int memoryFree) {
        this.memoryFree = memoryFree;
    }

    public int getMemoryUser() {
        return memoryUser;
    }

    public void setMemoryUser(int memoryUser) {
        this.memoryUser = memoryUser;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public String getNetworkStatus() {
        return networkStatus;
    }

    public void setNetworkStatus(String networkStatus) {
        this.networkStatus = networkStatus;
    }

    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    public void setDistanceTraveled(double distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
    }

    public int getScreenBrightness() {
        return screenBrightness;
    }

    public void setScreenBrightness(int screenBrightness) {
        this.screenBrightness = screenBrightness;
    }

    public NetworkDetails getNetworkDetails() {
        return networkDetails;
    }

    public void setNetworkDetails(NetworkDetails networkDetails) {
        this.networkDetails = networkDetails;
    }

    public BatteryDetails getBatteryDetails() {
        return batteryDetails;
    }

    public void setBatteryDetails(BatteryDetails batteryDetails) {
        this.batteryDetails = batteryDetails;
    }

    public CpuStatus getCpuStatus() {
        return cpuStatus;
    }

    public void setCpuStatus(CpuStatus cpuStatus) {
        this.cpuStatus = cpuStatus;
    }

    public List<String> getLocationProviders() {
        return locationProviders;
    }

    public void setLocationProviders(List<String> locationProviders) {
        this.locationProviders = locationProviders;
    }

    public CallInfo getCallInfo() {
        return callInfo;
    }

    public void setCallInfo(CallInfo callInfo) {
        this.callInfo = callInfo;
    }

    public int getScreenOn() {
        return screenOn;
    }

    public void setScreenOn(int screenOn) {
        this.screenOn = screenOn;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public int getUnknownSources() {
        return unknownSources;
    }

    public void setUnknownSources(int unknownSources) {
        this.unknownSources = unknownSources;
    }

    public int getDeveloperMode() {
        return developerMode;
    }

    public void setDeveloperMode(int developerMode) {
        this.developerMode = developerMode;
    }

    public List<Feature> getExtra() {
        return extra;
    }

    public void setExtra(List<Feature> extra) {
        this.extra = extra;
    }

    public int getPiListSize() {
        return (this.piList == null) ? 0 : this.piList.size();
    }
}
