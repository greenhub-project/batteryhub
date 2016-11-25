/*
 * Copyright (c) 2016 Hugo Matalonga & João Paulo Fernandes
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

package hmatalonga.greenhub.models.data;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;

/**
 * Sample data definition.
 */
@Table(name = "Samples")
public class Sample extends Model {

    // ID for the current device
    @Column(name = "UuId")
    public String uuId;

    // Timestamp of sample created time
    @Column(name = "Timestamp")
    public double timestamp;

    // State of the battery. ie. charging, discharging, etc.
    @Column(name = "BatteryState")
    public String batteryState;

    // Level of the battery (0 - 1.0) translates to percentage
    @Column(name = "BatteryLevel")
    public double batteryLevel;

    // Total wired memory
    @Column(name = "MemoryWired")
    public int memoryWired;

    // Total active memory
    @Column(name = "MemoryActive")
    public int memoryActive;

    // Total inactive memory
    @Column(name = "MemoryInactive")
    public int memoryInactive;

    // Total free memory
    @Column(name = "MemoryFree")
    public int memoryFree;

    // Total user memory
    @Column(name = "MemoryUser")
    public int memoryUser;

    // Trigger reason
    @Column(name = "TriggeredBy")
    public String triggeredBy;

    // Reachability status
    @Column(name = "NetworkStatus")
    public String networkStatus;

    // If locationchange triggers, then this will have a value
    @Column(name = "DistanceTraveled")
    public double distanceTraveled;

    // Brightness value, 0-255
    @Column(name = "ScreenBrightness")
    public int screenBrightness;

    // Network status struct, with info on the active network, mobile,  and wifi
    @Column(name = "NetworkDetails")
    public NetworkDetails networkDetails;

    @Column(name = "BatteryDetails")
    // Battery status struct, with battery health, charger, voltage, temperature, etc.
    public BatteryDetails batteryDetails;

    // CPU information, such as cpu usage percentage
    @Column(name = "CpuStatus")
    public CpuStatus cpuStatus;

    // Call ratios and information
    @Column(name = "CallInfo")
    public CallInfo callInfo;

    // If screen is on == 1, off == 0
    @Column(name = "ScreenOn")
    public int screenOn;

    // Device timezone abbreviation
    @Column(name = "TimeZone")
    public String timeZone;

    // Current set of SettingsInfo
    @Column(name = "Settings")
    public Settings settings;

    // Current storage details of device
    @Column(name = "StorageDetails")
    public StorageDetails storageDetails;

    // Two-letter country code from network or SIM
    @Column(name = "CountryCode")
    public String countryCode;
    
    public Sample() {
        super();
    }

    // List of processes running
    public List<ProcessInfo> processInfos() {
        return getMany(ProcessInfo.class, "Sample");
    }

    // Extra features for extensibility
    public List<Feature> features() {
        return getMany(Feature.class, "Sample");
    }

    // Enabled location providers
    public List<LocationProvider> locationProviders() {
        return getMany(LocationProvider.class, "Sample");
    }
}
