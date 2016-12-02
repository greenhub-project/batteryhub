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

import io.realm.RealmObject;

/**
 * Network Details data definition.
 */
public class NetworkDetails extends RealmObject {

    // wifi, mobile or unknown
    public String networkType;

    // GPRS, EDGE, UMTS, etc.
    public String mobileNetworkType;

    // connecting, connected, disconnected, suspended
    public String mobileDataStatus;

    // none, in, out, inout, dormant
    public String mobileDataActivity;

    // 1 if currently roaming in a foreign mobile network, 0 otherwise
    public int roamingEnabled;

    // disabled, disabling, enabled, enabling, unknown
    public String wifiStatus;

    // As given by getRssi()
    public int wifiSignalStrength;

    // Link speed in Mbps
    public int wifiLinkSpeed;

    // Sent and received data
    public NetworkStatistics networkStatistics;

    // Wifi access point status: disabled, disabling, enabled, enabling, unknown
    public String wifiApStatus;

    // Network infrastructure provider, unbound
    public String networkOperator;

    // Service provider, bound to sim
    public String simOperator;

    // Numeric country code
    public String mcc;

    // Numeric network code
    public String mnc;
}
