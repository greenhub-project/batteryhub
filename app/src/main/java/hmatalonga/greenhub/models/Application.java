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

package hmatalonga.greenhub.models;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hmatalonga.greenhub.models.data.ProcessInfo;
import hmatalonga.greenhub.util.StringHelper;

import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Application properties model.
 */
public class Application {

    private static final String TAG = makeLogTag(Application.class);

    public static List<ProcessInfo> getRunningAppInfo(Context context) {
        List<RunningAppProcessInfo> runningProcessInfo = Process.getRunningProcessInfo(context);
        List<RunningServiceInfo> runningServices = Service.getRunningServiceInfo(context);

        Set<String> packages = new HashSet<>();
        List<ProcessInfo> processInfoList = new ArrayList<>();

        if (runningProcessInfo != null) {
            for (RunningAppProcessInfo pi : runningProcessInfo) {
                if (pi == null) continue;
                if (packages.contains(pi.processName)) continue;
                packages.add(pi.processName);
                ProcessInfo item = new ProcessInfo();
                item.setImportance(StringHelper.importanceString(pi.importance));
                item.setpId(pi.pid);
                item.setName(pi.processName);
                processInfoList.add(item);
            }
        }

        if (runningServices != null) {
            for (RunningServiceInfo pi : runningServices) {
                if (pi == null) continue;
                if (packages.contains(pi.process)) continue;
                packages.add(pi.process);
                ProcessInfo item = new ProcessInfo();
                item.setImportance(pi.foreground ? "Foreground app" : "Service");
                item.setpId(pi.pid);
                // item.setApplicationLabel(pi.service.flattenToString());
                item.setName(pi.process);

                processInfoList.add(item);
            }
        }

        return processInfoList;
    }

    /**
     * Helper to query whether an application is currently running and its code has not been evicted from memory.
     *
     * @param context the Context
     * @param appName the package name or process name of the application.
     * @return true if the application is running, false otherwise.
     */
    public static boolean isRunning(Context context, String appName) {
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
}
