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

import java.util.List;

/**
 * Created by hugo on 17-04-2016.
 */
public class StringHelper {
    public static String[] trimArray(String[] arr) {
        for (int i = 0; i < arr.length; i++)
            arr[i] = arr[i].trim();

        return arr;
    }

    public static String convertToString(Object o) {
        if (o instanceof List<?>) {
            String s = String.valueOf(o);
            // remove '[' and ']' chars from List.toString()
            return s.substring(1, s.length() - 1);
        }
        return String.valueOf(o);
    }
}
