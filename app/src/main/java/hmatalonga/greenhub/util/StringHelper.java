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

package hmatalonga.greenhub.util;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.util.SparseArray;

import java.util.List;

import hmatalonga.greenhub.R;

import static hmatalonga.greenhub.util.LogUtils.LOGE;

/**
 * StringHelper.
 */
public class StringHelper {
    // Used to map importances to human readable strings for sending samples to
    // the server, and showing them in the process list.
    private static final SparseArray<String> importanceToString;

    static {
        importanceToString = new SparseArray<>();
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_EMPTY, "Not running");
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_BACKGROUND, "Background process");
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_SERVICE, "Service");
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_VISIBLE, "Visible task");
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_FOREGROUND, "Foreground app");
    }

    /**
     * Converts <code>importance</code> to a human readable string.
     *
     * @param importance the importance from Android process info.
     * @return a human readable String describing the importance.
     */
    public static String importanceString(int importance) {
        String s = importanceToString.get(importance);
        if (s == null || s.length() == 0) {
            LOGE("Importance not found: ", "" + importance);
            s = "Unknown";
        }
        return s;
    }

    public static String translatedPriority(final Context context, String importanceString) {
        if (importanceString == null) {
            return context.getString(R.string.priorityDefault);
        }

        switch (importanceString) {
            case "Not running":
                return context.getString(R.string.prioritynotrunning);
            case "Background process":
                return context.getString(R.string.prioritybackground);
            case "Service":
                return context.getString(R.string.priorityservice);
            case "Visible task":
                return context.getString(R.string.priorityvisible);
            case "Foreground app":
                return context.getString(R.string.priorityforeground);
            case "Perceptible task":
                return context.getString(R.string.priorityperceptible);
            case "Suggestion":
                return context.getString(R.string.prioritysuggestion);
            default:
                return context.getString(R.string.priorityDefault);
        }
    }

    public static String formatProcessName(String processName) {
        // TODO: Is this the right thing to do? Remove part after ":" in process names
        int indexOf = processName.lastIndexOf(':');
        if (indexOf <= 0) indexOf = processName.length();
        return processName.substring(0, indexOf);
    }

    public static String[] trimArray(String[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }

    public static String convertToHex(byte[] data) {
        StringBuilder builder = new StringBuilder();
        for (byte b : data) {
            int halfByte = (b >>> 4) & 0x0F;
            int twoHalfs = 0;
            do {
                builder.append(
                        (0 <= halfByte) && (halfByte <= 9) ?
                                (char) ('0' + halfByte) : (char) ('a' + (halfByte - 10))
                );
                halfByte = b & 0x0F;
            } while (twoHalfs++ < 1);
        }
        return builder.toString();
    }

    public static String convertToString(Object obj) {
        if (obj instanceof List<?>) {
            String s = String.valueOf(obj);
            // remove '[' and ']' chars from List.toString()
            return s.substring(1, s.length() - 1);
        }
        return String.valueOf(obj);
    }
}
