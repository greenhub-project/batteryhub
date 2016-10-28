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
 * CPU Status data definition.
 */
public class CpuStatus {

    private static final int FIELD_NUM = 3;

    // CPU usage fraction (0-1)
    private double cpuUsage;

    // Uptime in seconds
    private double uptime;

    // Experimental sleep time
    private double sleeptime;

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getUptime() {
        return uptime;
    }

    public void setUptime(double uptime) {
        this.uptime = uptime;
    }

    public double getSleeptime() {
        return sleeptime;
    }

    public void setSleeptime(double sleeptime) {
        this.sleeptime = sleeptime;
    }

    public void parseString(String s) {
        String[] values = StringHelper.trimArray(s.split(";"));
        if (values.length == FIELD_NUM) {
            try {
                setCpuUsage(Double.parseDouble(values[0]));
                setUptime(Double.parseDouble(values[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return String.valueOf(cpuUsage) + ";" + String.valueOf(uptime);
    }
}
