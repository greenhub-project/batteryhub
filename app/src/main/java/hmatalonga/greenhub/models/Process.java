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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.List;

import hmatalonga.greenhub.util.StringHelper;

import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Process properties model.
 */
public class Process {

    private static final String TAG = makeLogTag(Process.class);

    private static WeakReference<List<RunningAppProcessInfo>> runningAppInfo = null;

    /**
     * Populate running process info into the runningAppInfo WeakReference list, and return its value.
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

        for (RunningAppProcessInfo info : runningAppProcesses) {
            if (info == null || info.processName == null) continue;
            info.processName = StringHelper.formatProcessName(info.processName);
        }

        runningAppInfo = new WeakReference<>(runningAppProcesses);
        return runningAppProcesses;
    }

    public static void clear() {
        runningAppInfo = null;
    }

}
