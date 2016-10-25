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

/**
 * Device Status class model.
 *
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
