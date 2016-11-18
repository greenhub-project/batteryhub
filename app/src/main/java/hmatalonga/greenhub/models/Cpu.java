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

import android.os.SystemClock;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Cpu properties model.
 */
public class Cpu {

    private static final String TAG = makeLogTag(Cpu.class);

    public static synchronized long[] readUsagePoint() {
        long idle = 0;
        long cpu = 0;

        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");

            String load = reader.readLine();
            String[] tokens = load.split(" ");
            for (int i = 2; i <= 8; i++) {
                // 5 index has idle value
                if (i == 5) {
                    idle = Long.parseLong(tokens[i]);
                    continue;
                }
                cpu += Long.parseLong(tokens[i]);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new long[]{idle, cpu};
    }

    /**
     * Calculate CPU usage between the cpu and idle time given at two time
     * points.
     *
     * @param then
     * @param now
     * @return
     */
    public static double getUsage(long[] then, long[] now) {
        if (then == null || now == null || then.length < 2 || now.length < 2) return 0.0;
        double idleAndCpuDiff = (now[0] + now[1]) - (then[0] + then[1]);
        return (now[1] - then[1]) / idleAndCpuDiff;
    }

    /**
     * Real time in seconds since last boot
     */
    public static double getUptime() {
        return TimeUnit.MILLISECONDS.toSeconds(SystemClock.elapsedRealtime());
    }

    /**
     * CPU sleep time in seconds
     */
    public static double getSleepTime() {
        long sleep = SystemClock.elapsedRealtime() - SystemClock.uptimeMillis();
        return (sleep < 0) ? 0 : TimeUnit.MILLISECONDS.toSeconds(sleep);
    }
}
