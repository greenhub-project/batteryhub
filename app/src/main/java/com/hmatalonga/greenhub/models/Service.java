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
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Service properties model.
 */
public class Service {

    private static final String TAG = makeLogTag(Service.class);

    private static WeakReference<List<RunningServiceInfo>> runningServiceInfo = null;

    /**
     * Returns a list of currently running Services.
     *
     * @param context the Context.
     * @return Returns a list of currently running Services.
     */
    public static List<RunningServiceInfo> getRunningServiceInfo(Context context) {
        if (runningServiceInfo != null && runningServiceInfo.get() != null) {
            return runningServiceInfo.get();
        }

        ActivityManager manager =
                (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        List<RunningServiceInfo> runningServices = manager.getRunningServices(255);
        runningServiceInfo = new WeakReference<>(runningServices);

        return runningServices;
    }

    public static void clear() {
        runningServiceInfo = null;
    }
}
