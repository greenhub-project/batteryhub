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

package com.hmatalonga.greenhub.models.data;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Sample data definition.
 */
public class Sample extends RealmObject {

    @PrimaryKey
    public int id;

    // ID for the current device
    public String uuId;

    // Timestamp of sample created time
    @Index
    public long timestamp;

    // Mobile app version of sample
    public int version;

    // State of the battery. ie. charging, discharging, etc.
    public String batteryState;

    // Level of the battery (0 - 1.0) translates to percentage
    public double batteryLevel;

    // Total wired memory
    public int memoryWired;

    // Total active memory
    public int memoryActive;

    // Total inactive memory
    public int memoryInactive;

    // Total free memory
    public int memoryFree;

    // Total user memory
    public int memoryUser;

    // Trigger reason
    public String triggeredBy;

    // Reachability status
    public String networkStatus;

    // If locationchange triggers, then this will have a value
    public double distanceTraveled;

    // Brightness value, 0-255
    public int screenBrightness;

    // Network status struct, with info on the active network, mobile,  and wifi
    public NetworkDetails networkDetails;

    // Battery status struct, with battery health, charger, voltage, temperature, etc.
    public BatteryDetails batteryDetails;

    // CPU information, such as cpu usage percentage
    public CpuStatus cpuStatus;

    // Call ratios and information
    public CallInfo callInfo;

    // If screen is on == 1, off == 0
    public int screenOn;

    // Device timezone abbreviation
    public String timeZone;

    // Current set of SettingsInfo
    public Settings settings;

    // Current storage details of device
    public StorageDetails storageDetails;

    // Two-letter country code from network or SIM
    public String countryCode;

    // Enabled location providers
    public RealmList<LocationProvider> locationProviders;

    // List of processes running
    public RealmList<ProcessInfo> processInfos;

    // Extra features for extensibility
    public RealmList<Feature> features;
}
