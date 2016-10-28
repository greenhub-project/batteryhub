/*
 * Copyright (C) 2016 Hugo Matalonga & João Paulo Fernandes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hmatalonga.greenhub.models;

import com.google.gson.Gson;

import java.util.List;

/**
 * Sample data definition.
 */
public class Sample {
    private static final Gson PARSER = new Gson();

    // ID for the current device
    private String uuId;

    // Timestamp of sample created time
    private double timestamp;

    // List of processes running
    private List<ProcessInfo> piList;

    // State of the battery. ie. charging, discharging, etc.
    private String batteryState;

    // Level of the battery (0 - 1.0) translates to percentage
    private double batteryLevel;

    // Total wired memory
    private int memoryWired;

    // Total active memory
    private int memoryActive;

    // Total inactive memory
    private int memoryInactive;

    // Total free memory
    private int memoryFree;

    // Total user memory
    private int memoryUser;

    // Trigger reason
    private String triggeredBy;

    // Reachability status
    private String networkStatus;

    // If locationchange triggers, then this will have a value
    private double distanceTraveled;

    // Brightness value, 0-255
    private int screenBrightness;

    // Network status struct, with info on the active network, mobile,  and wifi
    private NetworkDetails networkDetails;

    // Battery status struct, with battery health, charger, voltage, temperature, etc.
    private BatteryDetails batteryDetails;

    // CPU information, such as cpu usage percentage
    private CpuStatus cpuStatus;

    // Enabled location providers
    private List<String> locationProviders;

    // Call ratios and information
    private CallInfo callInfo;

    // If screen is on == 1, off == 0
    private int screenOn;

    // Device timezone abbreviation
    private String timeZone;

    // Unknown source app installation on == 1, off == 0
    private int unknownSources;

    // Developer mode on == 1, off == 0
    private int developerMode;

    // Extra features for extensibility
    private List<Feature> extra;

    // Current set of Settings
    private Settings settings;

    // Current storage details of device
    private StorageDetails storageDetails;

    // Two-letter country code from network or SIM
    private String countryCode;

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
