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
 * Network Details data definition.
 */
public class NetworkDetails {

    private static final int FIELD_NUM = 14;

    // wifi, mobile or unknown
    private String networkType;

    // GPRS, EDGE, UMTS, etc.
    private String mobileNetworkType;

    // connecting, connected, disconnected, suspended
    private String mobileDataStatus;

    // none, in, out, inout, dormant
    private String mobileDataActivity;

    // True if currently roaming in a foreign mobile network
    private boolean roamingEnabled;

    // disabled, disabling, enabled, enabling, unknown
    private String wifiStatus;

    // As given by getRssi()
    private int wifiSignalStrength;

    // Link speed in Mbps
    private int wifiLinkSpeed;

    // Sent and received data
    private NetworkStatistics networkStatistics;

    // Wifi access point status: disabled, disabling, enabled, enabling, unknown
    private String wifiApStatus;

    // Network infrastructure provider, unbound
    private String networkOperator;

    // Service provider, bound to sim
    private String simOperator;

    // Numeric country code
    private String mcc;

    // Numeric network code
    private String mnc;

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

    public NetworkStatistics getNetworkStatistics() {
        return networkStatistics;
    }

    public void setNetworkStatistics(NetworkStatistics networkStatistics) {
        this.networkStatistics = networkStatistics;
    }

    public String getWifiApStatus() {
        return wifiApStatus;
    }

    public void setWifiApStatus(String wifiApStatus) {
        this.wifiApStatus = wifiApStatus;
    }

    public String getNetworkOperator() {
        return networkOperator;
    }

    public void setNetworkOperator(String networkOperator) {
        this.networkOperator = networkOperator;
    }

    public String getSimOperator() {
        return simOperator;
    }

    public void setSimOperator(String simOperator) {
        this.simOperator = simOperator;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getMnc() {
        return mnc;
    }

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    public void parseString(String s) {
        String[] values = StringHelper.trimArray(s.split(";"));
        if (values.length == FIELD_NUM) {
            try {
                networkType = values[0];
                mobileNetworkType = values[1];
                mobileDataStatus = values[2];
                mobileDataActivity = values[3];
                roamingEnabled = Boolean.parseBoolean(values[4]);
                wifiStatus = values[5];
                wifiSignalStrength = Integer.parseInt(values[6]);
                wifiLinkSpeed = Integer.parseInt(values[7]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return getNetworkType() + ";" + getMobileNetworkType() + ";"  + getMobileDataActivity() + ";" +
                String.valueOf(isRoamingEnabled()) + ";" + getWifiStatus() + ";" +
                String.valueOf(getWifiSignalStrength()) + ";" + String.valueOf(getWifiLinkSpeed());
    }
}
