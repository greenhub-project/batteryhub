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
 * Battery Details data definition.
 */
@Table(name = "BatteryDetails")
public class BatteryDetails extends Model {

    // Currently ac, usb, or unplugged
    @Column(name = "BatteryCharger")
    public String batteryCharger;

    // Currently Unknown, Unspecified failure, Dead, Cold, Overheat, Over voltage or Good
    @Column(name = "BatteryHealth")
    public String batteryHealth;

    // Voltage in Volts
    @Column(name = "BatteryVoltage")
    public double batteryVoltage;

    // Temperature in Celsius
    @Column(name = "BatteryTemperature")
    public double batteryTemperature;

    // Battery technology
    @Column(name = "BatteryTechnology")
    public String batteryTechnology;

    // Capacity in mAh
    @Column(name = "BatteryCapacity")
    public double batteryCapacity;

    // age factor ...

    public BatteryDetails() {
        super();
    }
}
