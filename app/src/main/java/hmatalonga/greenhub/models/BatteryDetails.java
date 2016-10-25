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
 * Battery Details data definition
 *
 * Created by hugo on 09-04-2016.
 */
public class BatteryDetails {
    private static final int fieldNum = 6;
    private String batteryCharger; // optional
    private String batteryHealth; // optional
    private double batteryVoltage; // optional
    private double batteryTemperature; // optional
    private String batteryTechnology; // optional
    private double batteryCapacity; // optional

    public String getBatteryCharger() {
        return batteryCharger;
    }

    public void setBatteryCharger(String batteryCharger) {
        this.batteryCharger = batteryCharger;
    }

    public String getBatteryHealth() {
        return batteryHealth;
    }

    public void setBatteryHealth(String batteryHealth) {
        this.batteryHealth = batteryHealth;
    }

    public double getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(double batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public double getBatteryTemperature() {
        return batteryTemperature;
    }

    public void setBatteryTemperature(double batteryTemperature) {
        this.batteryTemperature = batteryTemperature;
    }

    public String getBatteryTechnology() {
        return batteryTechnology;
    }

    public void setBatteryTechnology(String batteryTechnology) {
        this.batteryTechnology = batteryTechnology;
    }

    public double getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(double batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    public void parseString(String s) {
        String[] values = StringHelper.trimArray(s.split(";"));
        if (values.length == fieldNum) {
            try {
                setBatteryCharger(values[0]);
                setBatteryHealth(values[1]);
                setBatteryVoltage(Double.parseDouble(values[2]));
                setBatteryTemperature(Double.parseDouble(values[3]));
                setBatteryTechnology(values[4]);
                setBatteryCapacity(Double.parseDouble(values[5]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return batteryCharger + ";" + batteryHealth + ";" + String.valueOf(batteryVoltage) + ";" +
                String.valueOf(batteryTemperature) + ";" + batteryTechnology + ";" +
                String.valueOf(batteryCapacity);
    }
}
