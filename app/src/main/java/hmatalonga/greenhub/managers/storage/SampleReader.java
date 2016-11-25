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

package hmatalonga.greenhub.managers.storage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import hmatalonga.greenhub.models.data.BatteryDetails;
import hmatalonga.greenhub.models.data.CpuStatus;
import hmatalonga.greenhub.models.data.Feature;
import hmatalonga.greenhub.models.data.NetworkDetails;
import hmatalonga.greenhub.models.data.ProcessInfo;
import hmatalonga.greenhub.models.data.Sample;
import hmatalonga.greenhub.models.data.Settings;
import hmatalonga.greenhub.util.StringHelper;

import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * SampleReader
 */
public class SampleReader {
    private static final String TAG = makeLogTag(SampleReader.class);

    public static HashMap<String, String> writeSample(final Sample sample) {
        HashMap<String, String> map = new HashMap<>();

        map.put("uuId", sample.getUuId());
        map.put("timestamp", StringHelper.convertToString(sample.getTimestamp()));
        map.put("piList", StringHelper.convertToString(sample.getPiList())); // List
        map.put("batteryState", sample.getBatteryState());
        map.put("batteryLevel", StringHelper.convertToString(sample.getBatteryLevel()));
        map.put("memoryWired", StringHelper.convertToString(sample.getMemoryWired()));
        map.put("memoryActive", StringHelper.convertToString(sample.getMemoryActive()));
        map.put("memoryInactive", StringHelper.convertToString(sample.getMemoryInactive()));
        map.put("memoryFree", StringHelper.convertToString(sample.getMemoryFree()));
        map.put("memoryUser", StringHelper.convertToString(sample.getMemoryUser()));
        map.put("triggeredBy", sample.getTriggeredBy());
        map.put("networkStatus", sample.getNetworkStatus());
        map.put("distanceTraveled", StringHelper.convertToString(sample.getDistanceTraveled()));
        map.put("screenBrightness", StringHelper.convertToString(sample.getScreenBrightness()));
        map.put("networkDetails", StringHelper.convertToString(sample.getNetworkDetails())); // obj
        map.put("batteryDetails", StringHelper.convertToString(sample.getBatteryDetails())); // obj
        map.put("cpuStatus", StringHelper.convertToString(sample.getCpuStatus())); // obj
        map.put("locationProviders", StringHelper.convertToString(sample.getLocationProviders())); // List
        map.put("callInfo", StringHelper.convertToString(sample.getCallInfo())); // obj
        map.put("screenOn", StringHelper.convertToString(sample.getScreenOn()));
        map.put("timeZone", sample.getTimeZone());
        map.put("unknownSources", StringHelper.convertToString(sample.getUnknownSources()));
        map.put("developerMode", StringHelper.convertToString(sample.getDeveloperMode()));
        map.put("extra", StringHelper.convertToString(sample.getExtra())); // List

        return map;
    }

    public static Sample readSample(Object data) {
        if (data == null || !(data instanceof HashMap<?, ?>)) {
            return null;
        }

        @SuppressWarnings("unchecked")
        HashMap<String, String> map = (HashMap<String, String>) data;

        // Prepare new Sample object to fill in
        Sample sample = new Sample();
        NetworkDetails networkDetails = new NetworkDetails();
        BatteryDetails batteryDetails = new BatteryDetails();
        CpuStatus cpuStatus = new CpuStatus();
        Settings settings = new Settings();

        for (String key : map.keySet()) {
            switch (key) {
                case "uuId":
                    sample.setUuId(map.get(key));
                    break;
                case "timestamp":
                    try {
                        sample.setTimestamp(Double.parseDouble(map.get(key)));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    break;
                case "piList":
                    List<ProcessInfo> piList = new ArrayList<>();
                    ProcessInfo item;
                    String[] processes = map.get(key).split(",");
                    for (String process : processes) {
                        item = new ProcessInfo();
                        item.parseString(process);
                        piList.add(item);
                    }
                    sample.setPiList(piList);
                    break;
                case "batteryState":
                    sample.setBatteryState(map.get(key));
                    break;
                case "batteryLevel":
                    try {
                        sample.setBatteryLevel(Double.parseDouble(map.get(key)));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    break;
                case "memoryWired":
                    try {
                        sample.setMemoryWired(Integer.parseInt(map.get(key)));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    break;
                case "memoryActive":
                    try {
                        sample.setMemoryActive(Integer.parseInt(map.get(key)));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    break;
                case "memoryInactive":
                    try {
                        sample.setMemoryInactive(Integer.parseInt(map.get(key)));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    break;
                case "memoryFree":
                    try {
                        sample.setMemoryFree(Integer.parseInt(map.get(key)));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    break;
                case "memoryUser":
                    try {
                        sample.setMemoryUser(Integer.parseInt(map.get(key)));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    break;
                case "triggeredBy":
                    sample.setTriggeredBy(map.get(key));
                    break;
                case "networkStatus":
                    sample.setNetworkStatus(map.get(key));
                    break;
                case "distanceTraveled":
                    try {
                        sample.setDistanceTraveled(Double.parseDouble(map.get(key)));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    break;
                case "screenBrightness":
                    try {
                        sample.setScreenBrightness(Integer.parseInt(map.get(key)));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    break;
                case "networkDetails":
                    networkDetails.parseString(map.get(key));
                    sample.setNetworkDetails(networkDetails);
                    break;
                case "batteryDetails":
                    batteryDetails.parseString(map.get(key));
                    sample.setBatteryDetails(batteryDetails);
                    break;
                case "cpuStatus":
                    cpuStatus.parseString(map.get(key));
                    sample.setCpuStatus(cpuStatus);
                    break;
                case "locationProviders":
                    List<String> locationProviders = new ArrayList<>();
                    String[] providers = StringHelper.trimArray(map.get(key).split(","));
                    Collections.addAll(locationProviders, providers);
                    sample.setLocationProviders(locationProviders);
                    break;
                case "callInfo":
                    break;
                case "screenOn":
                    try {
                        sample.setScreenOn(Integer.parseInt(map.get(key)));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    break;
                case "timeZone":
                    sample.setTimeZone(map.get(key));
                    break;
                case "unknownSources":
                    try {
                        sample.setUnknownSources(Integer.parseInt(map.get(key)));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    break;
                case "developerMode":
                    try {
                        sample.setDeveloperMode(Integer.parseInt(map.get(key)));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    break;
                case "extra":
                    List<Feature> extra = new ArrayList<>();
                    Feature feature;
                    String[] features = map.get(key).split(",");
                    for (String f : features) {
                        feature = new Feature();
                        feature.parseString(f);
                        extra.add(feature);
                    }
                    sample.setExtra(extra);
                    break;
                case "settings":
                    settings.parseString(map.get(key));
                    sample.setSettings(settings);
            }
        }
        return sample;
    }
}
