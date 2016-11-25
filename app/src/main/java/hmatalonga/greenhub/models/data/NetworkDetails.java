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

/**
 * Network Details data definition.
 */
@Table(name = "NetworkDetails")
public class NetworkDetails extends Model {

    // wifi, mobile or unknown
    @Column(name = "NetworkType")
    public String networkType;

    // GPRS, EDGE, UMTS, etc.
    @Column(name = "MobileNetworkType")
    public String mobileNetworkType;

    // connecting, connected, disconnected, suspended
    @Column(name = "MobileDataStatus")
    public String mobileDataStatus;

    // none, in, out, inout, dormant
    @Column(name = "MobileDataActivity")
    public String mobileDataActivity;

    // 1 if currently roaming in a foreign mobile network, 0 otherwise
    @Column(name = "RoamingEnabled")
    public int roamingEnabled;

    // disabled, disabling, enabled, enabling, unknown
    @Column(name = "WifiStatus")
    public String wifiStatus;

    // As given by getRssi()
    @Column(name = "WifiSignalStrength")
    public int wifiSignalStrength;

    // Link speed in Mbps
    @Column(name = "WifiLinkSpeed")
    public int wifiLinkSpeed;

    // Sent and received data
    @Column(name = "NetworkStatistics")
    public NetworkStatistics networkStatistics;

    // Wifi access point status: disabled, disabling, enabled, enabling, unknown
    @Column(name = "WifiApStatus")
    public String wifiApStatus;

    // Network infrastructure provider, unbound
    @Column(name = "NetworkOperator")
    public String networkOperator;

    // Service provider, bound to sim
    @Column(name = "SimOperator")
    public String simOperator;

    // Numeric country code
    @Column(name = "Mcc")
    public String mcc;

    // Numeric network code
    @Column(name = "Mnc")
    public String mnc;
    
    public NetworkDetails() {
        super();
    }
}
