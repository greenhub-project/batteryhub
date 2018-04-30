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
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;

import com.hmatalonga.greenhub.util.LogUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Locale;

import static com.hmatalonga.greenhub.util.LogUtils.logI;
import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Memory properties model.
 */
public class Memory {

    private static final String TAG = makeLogTag(Memory.class);

    public static final int TOTAL = 0;
    public static final int FREE = 1;
    public static final int ACTIVE = 2;
    public static final int INACTIVE = 3;

    //TODO: Provide implementations for newer API levels due to SDK changes.

    /**
     * Read memory usage using the public Android API methods in ActivityManager,
     * such as MemoryInfo and getProcessMemoryInfo.
     *
     * @param context the Context from the running Activity.
     * @return array of int values with total and used memory, in KB.
     */
    @TargetApi(21)
    public static int[] readMemory(final Context context) {
        int[] pIds;
        int total;
        int used = 0;
        int counter = 0;
        int memoryUsed = 0;

        ActivityManager manager = (ActivityManager)
                context.getSystemService(Activity.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();

        manager.getMemoryInfo(memoryInfo);
        total = (int) memoryInfo.availMem;

        List<ActivityManager.RunningAppProcessInfo> processInfoList =
                manager.getRunningAppProcesses();
        List<ActivityManager.RunningServiceInfo> serviceInfoList =
                manager.getRunningServices(Integer.MAX_VALUE);

        if (processInfoList == null || serviceInfoList == null) {
            return new int[] {total, used};
        }

        pIds = new int[processInfoList.size() + serviceInfoList.size()];

        for (ActivityManager.RunningAppProcessInfo processInfo : processInfoList) {
            pIds[counter] = processInfo.pid;
            counter++;
        }

        for (ActivityManager.RunningServiceInfo serviceInfo : serviceInfoList) {
            pIds[counter] = serviceInfo.pid;
            counter++;
        }

        // Two approaches one for Debug and other for Production
        // TODO: Do further testing to check Debug.MemoryInfo vs ActivityManager.MemoryInfo...

        // 1) ActivityManager.MemoryInfo
        manager.getMemoryInfo(memoryInfo);

        String message =
                String.format(Locale.US, "%d availMem, %b lowMemory, %d threshold, %d total",
                        memoryInfo.availMem, memoryInfo.lowMemory,
                        memoryInfo.threshold, memoryInfo.totalMem);

        LogUtils.logI(TAG, message);

        // 2) Debug.MemoryInfo
        Debug.MemoryInfo[] memoryInfosArray = manager.getProcessMemoryInfo(pIds);

        for (Debug.MemoryInfo memory : memoryInfosArray) {
            memoryUsed += memory.getTotalPss();
        }

        return new int[] {total, used};
    }

    /**
     * Reads the current memory usage by accessing the system file /proc/meminfo
     *
     * @return array of int values containing free, total, active and inactive memory in KB.
     */
    public static synchronized int[] readMemoryInfo() {
        // Local variables need to initialized before use, because of static
        int total = 0;
        int free = 0;
        int active = 0;
        int inactive = 0;

        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/meminfo", "r");

            String load = reader.readLine();
            String[] tokens = load.split("\\s+");
            total = Integer.parseInt(tokens[1]);
            load = reader.readLine();
            tokens = load.split("\\s+");
            free = Integer.parseInt(tokens[1]);
            for (int i = 0; i < 4; i++) {
                load = reader.readLine();
            }
            tokens = load.split("\\s+");
            active = Integer.parseInt(tokens[1]);
            load = reader.readLine();
            tokens = load.split("\\s+");
            inactive = Integer.parseInt(tokens[1]);

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new int[] {free, total, active, inactive};
    }
}
