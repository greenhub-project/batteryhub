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

import android.os.SystemClock;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Cpu properties model.
 */
public class Cpu {

    private static final String TAG = makeLogTag(Cpu.class);

    // FIXME: Describe this. Why are there so many fields? Why is it divided by
    // 100?
	/*
	 * The value of HZ varies across kernel versions and hardware platforms. On
	 * i386 the situation is as follows: on kernels up to and including 2.4.x,
	 * HZ was 100, giving a jiffy value of 0.01 seconds; starting with 2.6.0, HZ
	 * was raised to 1000, giving a jiffy of 0.001 seconds. Since kernel 2.6.13,
	 * the HZ value is a kernel configuration parameter and can be 100, 250 (the
	 * default) or 1000, yielding a jiffies value of, respectively, 0.01, 0.004,
	 * or 0.001 seconds. Since kernel 2.6.20, a further frequency is available:
	 * 300, a number that divides evenly for the common video frame rates (PAL,
	 * 25 HZ; NTSC, 30 HZ).
	 *
	 * I will leave the unit of cpu time as the jiffy and we can discuss later.
	 *
	 * 0 name of cpu 1 space 2 user time 3 nice time 4 sys time 5 idle time(it
	 * is not include in the cpu total time) 6 iowait time 7 irg time 8 softirg
	 * time
	 *
	 * the idleTotal[5] is the idle time which always changes. There are two
	 * spaces between cpu and user time.That is a tricky thing and messed up
	 * splitting.:)
	 */

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
    public static long getUptime() {
        return TimeUnit.MILLISECONDS.toSeconds(SystemClock.elapsedRealtime());
    }

    /**
     * CPU sleep time in seconds
     */
    public static long getSleepTime() {
        long sleep = SystemClock.elapsedRealtime() - SystemClock.uptimeMillis();
        return (sleep < 0) ? 0 : TimeUnit.MILLISECONDS.toSeconds(sleep);
    }
}
