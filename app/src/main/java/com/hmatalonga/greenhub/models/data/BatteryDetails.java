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

import io.realm.RealmObject;

/**
 * Battery Details data definition.
 */
public class BatteryDetails extends RealmObject {

    // Currently ac, usb, or unplugged
    public String charger;

    // Currently Unknown, Unspecified failure, Dead, Cold, Overheat, Over voltage or Good
    public String health;

    // Voltage in Volts
    public double voltage;

    // Temperature in Celsius
    public double temperature;

    // Battery technology
    public String technology;

    // Capacity in mAh
    public int capacity;

    // Battery capacity in microAmpere-hours
    public int chargeCounter;

    // Average battery current in microAmperes
    public int currentAverage;

    // Instantaneous battery current in milliAmperes
    public int currentNow;

    // Battery remaining energy in nanoWatt-hours
    public long energyCounter;

    // age factor ...
}
