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

package com.hmatalonga.greenhub.models;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;

import com.hmatalonga.greenhub.models.data.SensorDetails;

import java.util.ArrayList;
import java.util.List;

import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Sensor attributes getters. See https://developer.android.com/reference/android/hardware/Sensor.html
 */
public class Sensors {
    private static final String TAG = makeLogTag(Sensors.class);
    private static final int SDK_VERSION = Build.VERSION.SDK_INT;

    /**
     * Obtains the current Fifo Max Event Count.
     *
     * @param context Application's context
     * @return Returns the battery voltage
     */
    public static List<SensorDetails> getSensorDetailsList(final Context context) {
        List<SensorDetails> list = new ArrayList<>();
        SensorManager manager = (SensorManager)
                context.getSystemService(Context.SENSOR_SERVICE);
        assert manager != null;
        List<Sensor> values = manager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor: values) {
            SensorDetails details = new SensorDetails();
            list.add(details);
            details.codeType = sensor.getType();
            details.fifoMaxEventCount = sensor.getFifoMaxEventCount();
            details.fifoReservedEventCount = sensor.getFifoReservedEventCount();
            getAttributesNewVersion(sensor, details);
            details.isWakeUpSensor = sensor.isWakeUpSensor();
            details.maxDelay = sensor.getMaxDelay();
            details.maximumRange = sensor.getMaximumRange();
            details.minDelay = sensor.getMinDelay();
            details.name = sensor.getName();
            details.power = sensor.getPower();
            details.reportingMode = sensor.getReportingMode();
            details.resolution = sensor.getResolution();
            details.stringType = sensor.getStringType();
            details.vendor = sensor.getVendor();
            details.version = sensor.getVersion();
        }
        return list;
    }

    @TargetApi(23)
    private static void getAttributesNewVersion(Sensor sensor, SensorDetails details) {
        if (SDK_VERSION > 23) {
            details.id = sensor.getId();
            details.isAdditionalInfoSupported = sensor.isAdditionalInfoSupported();
            details.isDynamicSensor = sensor.isDynamicSensor();
        }
        if (SDK_VERSION > 25) {
            details.highestDirectReportRateLevel = sensor.getHighestDirectReportRateLevel();
        }
    }
}
