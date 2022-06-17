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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.SharedPreferences;

import com.hmatalonga.greenhub.Config;
import com.hmatalonga.greenhub.models.data.AppSignature;
import com.hmatalonga.greenhub.models.data.ProcessInfo;
import com.hmatalonga.greenhub.util.StringHelper;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

/**
 * Process properties model.
 */
public class Process {

    private static WeakReference<List<RunningAppProcessInfo>> runningAppInfo = null;

    /**
     * Populate running process info into the runningAppInfo WeakReference list
     * and return its value.
     *
     * @param context the Context
     * @return the value of the runningAppInfo WeakReference list after setting it.
     */
    public static List<RunningAppProcessInfo> getRunningProcessInfo(Context context) {
        if (runningAppInfo != null && runningAppInfo.get() != null) {
            return runningAppInfo.get();
        }

        ActivityManager manager =
                (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();

        final ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> recentTasks = Objects.requireNonNull(activityManager).getRunningTasks(Integer.MAX_VALUE);
        for (int i = 0; i < recentTasks.size(); i++) {
            System.out.println("Application executed: " + recentTasks.get(i).baseActivity.toShortString() + "\n\n ID: " + recentTasks.get(i).id + "");
        }
        for (RunningAppProcessInfo info : runningAppProcesses) {
            if (info == null || info.processName == null) continue;
            info.processName = StringHelper.formatProcessName(info.processName);
        }

        runningAppInfo = new WeakReference<>(runningAppProcesses);
        return runningAppProcesses;
    }

    /**
     * Helper to set application to the uninstalled state in the GreenHub sample.
     *
     * @param pName the package that was uninstalled.
     * @param pref The preference that stored the uninstallation directive.
     *             This preference will be deleted to
     *             ensure uninstallations are not sent multiple times.
     * @param e the Editor (passed and not created here for efficiency)
     * @return a new ProcessInfo entry describing the uninstalled item.
     */
    public static ProcessInfo uninstalledItem(String pName, String pref,
                                              SharedPreferences.Editor e) {
        ProcessInfo item = new ProcessInfo();
        item.name = pName;
        item.appSignatures.add(new AppSignature("uninstalled"));
        item.processId = -1;
        item.importance = Config.IMPORTANCE_UNINSTALLED;
        // Remember to remove it so we do not send
        // multiple uninstall events
        e.remove(pref);
        return item;
    }

    /**
     * Helper to set application to the disabled state in the GreenHub sample.
     *
     * @param pName the package that was disabled.
     * @param pref The preference that stored the disabled directive.
     *             This preference will be deleted to ensure
     *             disabled apps are not sent multiple times.
     * @param e the Editor (passed and not created here for efficiency)
     * @return a new ProcessInfo entry describing the uninstalled item.
     */
    public static ProcessInfo disabledItem(String pName, String pref, SharedPreferences.Editor e) {
        ProcessInfo item = new ProcessInfo();
        item.name = pName;
        item.processId = -1;
        item.importance = Config.IMPORTANCE_DISABLED;
        // Remember to remove it so we do not send
        // multiple uninstall events
        e.remove(pref);
        return item;
    }

    public static void clear() {
        runningAppInfo = null;
    }

}
