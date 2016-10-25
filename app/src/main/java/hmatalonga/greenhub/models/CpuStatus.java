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
 * Created by hugo on 09-04-2016.
 */
public class CpuStatus {
    private static final int fieldNum = 2;
    private double cpuUsage; // optional
    private double uptime; // optional

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

    public void parseString(String s) {
        String[] values = StringHelper.trimArray(s.split(";"));
        if (values.length == fieldNum) {
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
