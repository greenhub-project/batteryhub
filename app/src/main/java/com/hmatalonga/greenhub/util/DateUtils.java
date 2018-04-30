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

package com.hmatalonga.greenhub.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * DateUtils.
 */
public class DateUtils {

    public static final int INTERVAL_24H = 1;

    public static final int INTERVAL_3DAYS = 2;

    public static final int INTERVAL_5DAYS = 3;

    public static final int INTERVAL_10DAYS = 4;

    public static final int INTERVAL_15DAYS = 5;

    private static final String DATE_FORMAT = "dd-MM HH:mm";

    private static SimpleDateFormat sSimpleDateFormat =
            new SimpleDateFormat(DATE_FORMAT, Locale.UK);

    public static String convertMilliSecondsToFormattedDate(Long milliSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);

        return sSimpleDateFormat.format(calendar.getTime());
    }

    public static long getMilliSecondsInterval(int interval) {
        long now = System.currentTimeMillis();

        if (interval == INTERVAL_24H) {
            return now - (1000 * 60 * 60 * 24);
        } else if (interval == INTERVAL_3DAYS) {
            return now - (1000 * 60 * 60 * 24 * 3);
        } else if (interval == INTERVAL_5DAYS) {
            return now - (1000 * 60 * 60 * 24 * 5);
        } else if (interval == INTERVAL_10DAYS) {
            return now - (1000 * 60 * 60 * 24 * 10);
        } else if (interval == INTERVAL_15DAYS) {
            return now - (1000 * 60 * 60 * 24 * 15);
        }

        return now;
    }
}
