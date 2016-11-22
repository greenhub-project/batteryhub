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
        final String[] permArray = { "android.permission.ACCESS_CHECKIN_PROPERTIES",
                "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS", "android.permission.ACCESS_MOCK_LOCATION",
                "android.permission.ACCESS_NETWORK_STATE", "android.permission.ACCESS_SURFACE_FLINGER",
                "android.permission.ACCESS_WIFI_STATE", "android.permission.ACCOUNT_MANAGER",
                "android.permission.AUTHENTICATE_ACCOUNTS", "android.permission.BATTERY_STATS",
                "android.permission.BIND_APPWIDGET", "android.permission.BIND_DEVICE_ADMIN",
                "android.permission.BIND_INPUT_METHOD", "android.permission.BIND_WALLPAPER",
                "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN", "android.permission.BRICK",
                "android.permission.BROADCAST_PACKAGE_REMOVED", "android.permission.BROADCAST_SMS",
                "android.permission.BROADCAST_STICKY", "android.permission.BROADCAST_WAP_PUSH",
                "android.permission.CALL_PHONE", "android.permission.CALL_PRIVILEGED", "android.permission.CAMERA",
                "android.permission.CHANGE_COMPONENT_ENABLED_STATE", "android.permission.CHANGE_CONFIGURATION",
                "android.permission.CHANGE_NETWORK_STATE", "android.permission.CHANGE_WIFI_MULTICAST_STATE",
                "android.permission.CHANGE_WIFI_STATE", "android.permission.CLEAR_APP_CACHE",
                "android.permission.CLEAR_APP_USER_DATA", "android.permission.CONTROL_LOCATION_UPDATES",
                "android.permission.DELETE_CACHE_FILES", "android.permission.DELETE_PACKAGES",
                "android.permission.DEVICE_POWER", "android.permission.DIAGNOSTIC",
                "android.permission.DISABLE_KEYGUARD", "android.permission.DUMP",
                "android.permission.EXPAND_STATUS_BAR", "android.permission.FACTORY_TEST",
                "android.permission.FLASHLIGHT", "android.permission.FORCE_BACK", "android.permission.GET_ACCOUNTS",
                "android.permission.GET_PACKAGE_SIZE", "android.permission.GET_TASKS",
                "android.permission.GLOBAL_SEARCH", "android.permission.HARDWARE_TEST",
                "android.permission.INJECT_EVENTS", "android.permission.INSTALL_LOCATION_PROVIDER",
                "android.permission.INSTALL_PACKAGES", "android.permission.INTERNAL_SYSTEM_WINDOW",
                "android.permission.INTERNET", "android.permission.KILL_BACKGROUND_PROCESSES",
                "android.permission.MANAGE_ACCOUNTS", "android.permission.MANAGE_APP_TOKENS",
                "android.permission.MASTER_CLEAR", "android.permission.MODIFY_AUDIO_SETTINGS",
                "android.permission.MODIFY_PHONE_STATE", "android.permission.MOUNT_FORMAT_FILESYSTEMS",
                "android.permission.MOUNT_UNMOUNT_FILESYSTEMS", "android.permission.PERSISTENT_ACTIVITY",
                "android.permission.PROCESS_OUTGOING_CALLS", "android.permission.READ_CALENDAR",
                "android.permission.READ_CONTACTS", "android.permission.READ_FRAME_BUFFER",
                "com.android.browser.permission.READ_HISTORY_BOOKMARKS", "android.permission.READ_INPUT_STATE",
                "android.permission.READ_LOGS", "android.permission.READ_OWNER_DATA",
                "android.permission.READ_PHONE_STATE", "android.permission.READ_SMS",
                "android.permission.READ_SYNC_SETTINGS", "android.permission.READ_SYNC_STATS",
                "android.permission.REBOOT", "android.permission.RECEIVE_BOOT_COMPLETED",
                "android.permission.RECEIVE_MMS", "android.permission.RECEIVE_SMS",
                "android.permission.RECEIVE_WAP_PUSH", "android.permission.RECORD_AUDIO",
                "android.permission.REORDER_TASKS", "android.permission.RESTART_PACKAGES",
                "android.permission.SEND_SMS", "android.permission.SET_ACTIVITY_WATCHER",
                "android.permission.SET_ALWAYS_FINISH", "android.permission.SET_ANIMATION_SCALE",
                "android.permission.SET_DEBUG_APP", "android.permission.SET_ORIENTATION",
                "android.permission.SET_PREFERRED_APPLICATIONS", "android.permission.SET_PROCESS_LIMIT",
                "android.permission.SET_TIME", "android.permission.SET_TIME_ZONE", "android.permission.SET_WALLPAPER",
                "android.permission.SET_WALLPAPER_HINTS", "android.permission.SIGNAL_PERSISTENT_PROCESSES",
                "android.permission.STATUS_BAR", "android.permission.SUBSCRIBED_FEEDS_READ",
                "android.permission.SUBSCRIBED_FEEDS_WRITE", "android.permission.SYSTEM_ALERT_WINDOW",
                "android.permission.UPDATE_DEVICE_STATS", "android.permission.USE_CREDENTIALS",
                "android.permission.VIBRATE", "android.permission.WAKE_LOCK", "android.permission.WRITE_APN_SETTINGS",
                "android.permission.WRITE_CALENDAR", "android.permission.WRITE_CONTACTS",
                "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.WRITE_GSERVICES",
                "com.android.browser.permission.WRITE_HISTORY_BOOKMARKS", "android.permission.WRITE_OWNER_DATA",
                "android.permission.WRITE_SECURE_SETTINGS", "android.permission.WRITE_SETTINGS",
                "android.permission.WRITE_SMS", "android.permission.WRITE_SYNC_SETTINGS" };

        Collections.addAll(PERMISSION_LIST, permArray);
    }
}
