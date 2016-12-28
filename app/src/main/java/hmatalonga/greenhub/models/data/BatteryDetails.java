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
 * Battery Details data definition.
 */
public class BatteryDetails extends RealmObject {

    // Currently ac, usb, or unplugged
    public String batteryCharger;

    // Currently Unknown, Unspecified failure, Dead, Cold, Overheat, Over voltage or Good
    public String batteryHealth;

    // Voltage in Volts
    public double batteryVoltage;

    // Temperature in Celsius
    public double batteryTemperature;

    // Battery technology
    public String batteryTechnology;

    // Capacity in mAh
    public int batteryCapacity;

    // Battery capacity in microAmpere-hours
    public int batteryChargeCounter;

    // Average battery current in microAmperes
    public int batteryCurrentAverage;

    // Instantaneous battery current in microAmperes
    public int batteryCurrentNow;

    // Battery remaining energy in nanoWatt-hours
    public long batteryEnergyCounter;

    // age factor ...
}
