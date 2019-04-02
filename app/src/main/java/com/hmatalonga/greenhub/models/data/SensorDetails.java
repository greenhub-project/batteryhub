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
public class SensorDetails extends RealmObject {

    public int	fifoMaxEventCount;

    public int	fifoReservedEventCount;

    //Get the highest supported direct report mode rate level of the sensor.
    public int	highestDirectReportRateLevel;

    public int	id;
    //This value is defined only for continuous and on-change sensors.
    public int	maxDelay;

    public float maximumRange;

    public int	minDelay;

    public String name;

    public float power;

    //Each sensor has exactly one reporting mode associated with it.
    public int reportingMode;

    public float resolution;

    public String	stringType;

    public int	codeType;

    public String vendor;

    public int	version;

    //Returns true if the sensor supports sensor additional information API
    public boolean	isAdditionalInfoSupported;

    //Returns true if the sensor is a dynamic sensor.
    public boolean	isDynamicSensor;

    //https://developer.android.com/reference/android/hardware/Sensor.html#isWakeUpSensor()
    public boolean	isWakeUpSensor;
}
