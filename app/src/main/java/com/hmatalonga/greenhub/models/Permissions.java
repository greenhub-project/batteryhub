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

import com.hmatalonga.greenhub.Config;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Permissions.
 */
public class Permissions {

    private static final String TAG = "Permissions";

    private static final ArrayList<String> PERMISSION_LIST = new ArrayList<>();

    public static byte[] getPermissionBytes(String[] perms) {
        if (perms == null) {
            return null;
        }
        if (PERMISSION_LIST.size() == 0) {
            populatePermList();
        }

        byte[] bytes = new byte[PERMISSION_LIST.size() / 8 + 1];

        for (String p : perms) {
            int idx = PERMISSION_LIST.indexOf(p);
            if (idx > 0) {
                int i = idx / 8;
                idx = (int) Math.pow(2, idx - i * 8);
                bytes[i] = (byte) (bytes[i] | idx);
            }
        }
        return bytes;
    }

    private static void populatePermList() {
        Collections.addAll(PERMISSION_LIST, Config.PERMISSIONS_ARRAY);
    }
}
