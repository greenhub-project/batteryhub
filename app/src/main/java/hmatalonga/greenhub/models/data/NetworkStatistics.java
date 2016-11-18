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

/**
 * Network Statistics data definition.
 */
public class NetworkStatistics {

    public static final int FIELD_NUM = 4;

    // Amount of wifi data received
    private double wifiReceived;

    // Amount of wifi data sent
    private double wifiSent;

    // Amount of mobile data received
    private double mobileReceived;

    // Amount of mobile data sent
    private double mobileSent;

    public double getWifiReceived() {
        return wifiReceived;
    }

    public void setWifiReceived(double wifiReceived) {
        this.wifiReceived = wifiReceived;
    }

    public double getWifiSent() {
        return wifiSent;
    }

    public void setWifiSent(double wifiSent) {
        this.wifiSent = wifiSent;
    }

    public double getMobileReceived() {
        return mobileReceived;
    }

    public void setMobileReceived(double mobileReceived) {
        this.mobileReceived = mobileReceived;
    }

    public double getMobileSent() {
        return mobileSent;
    }

    public void setMobileSent(double mobileSent) {
        this.mobileSent = mobileSent;
    }
}
