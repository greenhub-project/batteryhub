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

import hmatalonga.greenhub.util.StringHelper;

/**
 * Created by hugo on 09-04-2016.
 */
public class NetworkDetails {
    private static final int fieldNum = 8;
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

    public void parseString(String s) {
        String[] values = StringHelper.trimArray(s.split(";"));
        if (values.length == fieldNum) {
            try {
                setNetworkType(values[0]);
                setMobileNetworkType(values[1]);
                setMobileDataStatus(values[2]);
                setMobileDataActivity(values[3]);
                setRoamingEnabled(Boolean.parseBoolean(values[4]));
                setWifiStatus(values[5]);
                setWifiSignalStrength(Integer.parseInt(values[5]));
                setWifiLinkSpeed(Integer.parseInt(values[6]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return networkType + ";" + mobileNetworkType + ";"  + mobileDataActivity + ";" +
                String.valueOf(roamingEnabled) + ";" + wifiStatus + ";" +
                String.valueOf(wifiSignalStrength) + ";" + String.valueOf(wifiLinkSpeed);
    }
}
