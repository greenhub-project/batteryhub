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

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

import com.hmatalonga.greenhub.models.data.ProcessInfo;
import com.hmatalonga.greenhub.util.StringHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Application properties model.
 */
public class Application {
    public static ArrayList<ProcessInfo> getRunningAppInfo(final Context context) {
        List<RunningAppProcessInfo> runningProcessInfo = Process.getRunningProcessInfo(context);
        List<RunningServiceInfo> runningServices = Service.getRunningServiceInfo(context);

        Set<String> packages = new HashSet<>();
        ArrayList<ProcessInfo> processInfoList = new ArrayList<>();

        if (runningProcessInfo != null) {
            for (RunningAppProcessInfo pi : runningProcessInfo) {
                if (pi == null) continue;
                if (packages.contains(pi.processName)) continue;
                packages.add(pi.processName);
                ProcessInfo item = new ProcessInfo();
                item.importance = StringHelper.importanceString(pi.importance);
                item.processId = pi.pid;
                item.name = pi.processName;
                processInfoList.add(item);
            }
        }

        if (runningServices != null) {
            for (RunningServiceInfo pi : runningServices) {
                if (pi == null) continue;
                if (packages.contains(pi.process)) continue;
                packages.add(pi.process);
                ProcessInfo item = new ProcessInfo();
                item.importance = pi.foreground ? "Foreground app" : "Service";
                item.processId = pi.pid;
                // Debug this field
                item.applicationLabel = pi.service.flattenToString();
                item.name = pi.process;

                processInfoList.add(item);
            }
        }

        return processInfoList;
    }

    /**
     * Helper to query whether an application is currently running
     * and its code has not been evicted from memory.
     *
     * @param context Application's context
     * @param appName The package name or process name of the application.
     * @return true if the application is running, false otherwise.
     */
    public static boolean isRunning(final Context context, String appName) {
        List<RunningAppProcessInfo> runningProcessInfo = Process.getRunningProcessInfo(context);
        List<RunningServiceInfo> services = Service.getRunningServiceInfo(context);

        for (RunningAppProcessInfo info : runningProcessInfo) {
            if (info.processName.equals(appName) &&
                    info.importance != RunningAppProcessInfo.IMPORTANCE_EMPTY) {
                return true;
            }
        }

        for (RunningServiceInfo service: services) {
            String name = StringHelper.formatProcessName(service.process);
            if (name.equals(appName)) return true;
        }

        return false;
    }

    /**
     * Get highest priority process for the package.
     *
     * @param context
     * @param packageName
     * @return
     */
    public static String getAppPriority(Context context, String packageName){
        List<RunningAppProcessInfo> processInfos = Process.getRunningProcessInfo(context);
        List<RunningServiceInfo> serviceInfos = Service.getRunningServiceInfo(context);
        int highestPriority = Integer.MAX_VALUE;

        // Check if there are running services for the package
        for(RunningServiceInfo si : serviceInfos) {
            if(si.service.getPackageName().equals(packageName)){
                highestPriority = RunningAppProcessInfo.IMPORTANCE_SERVICE;
            }

        }
        // Check if there are running processes for the package
        for(RunningAppProcessInfo pi : processInfos){
            if(Arrays.asList(pi.pkgList).contains(packageName)) {
                if(pi.importance < highestPriority){
                    highestPriority = pi.importance;
                }
            }
        }

        return StringHelper.translatedPriority(
                context,
                StringHelper.importanceString(highestPriority)
        );
    }
}
