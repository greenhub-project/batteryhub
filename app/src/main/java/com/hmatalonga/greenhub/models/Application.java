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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hmatalonga.greenhub.models.data.ProcessInfo;
import com.hmatalonga.greenhub.util.ProcessUtils;
import com.hmatalonga.greenhub.util.StringHelper;

import static com.hmatalonga.greenhub.util.LogUtils.LOGE;

/**
 * Application properties model.
 */
public class Application {

    public static ArrayList<ProcessInfo> getRunningAppInfoLegacy(final Context context) {
        ArrayList<ProcessInfo> processInfoList = new ArrayList<>();
        List<String> psOutput = ProcessUtils.getCommandOutputAsList("ps");
        psOutput.remove(0); // the first line contains the headers of the `ps` command,
                              // so we just discard that

        for (String line : psOutput) {
            String[] properties = line.split("[ \\t]+");
            if (properties.length >= 9) {
                String cmdUser = properties[0];
                String cmdPid = properties[1];
                String cmdName = properties[8];
                String description = "";

                if (cmdUser.matches("u[0-9]+_.+")) {
                    if (cmdName.matches("(\\w+(\\.\\w+)+)$")) {  // this is a normal app
                        description = "user";
                    }else if (cmdName.matches("(\\w+(\\.\\w+)+:\\w+)$")) {  // this is a service
                        description = "user-service";
                    }else{
                        description = "unknown";
                    }
                }else{
                    description = "system";
                }

                ProcessInfo item = new ProcessInfo();
                item.importance = StringHelper.importanceStringLegacy(description);
                try {
                    int pid = Integer.parseInt(cmdPid);
                    item.processId = pid;
                }catch (NumberFormatException e) {
                    LOGE("Wrong PID format: ", "" + cmdPid);
                    item.processId = -1;  // FIXME: what should the PID be in an error situation?
                }
                item.name = cmdName;  // FIXME:
                processInfoList.add(item);

            }
        }

        return null;
    }

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
