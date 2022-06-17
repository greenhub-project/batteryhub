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

package com.hmatalonga.greenhub.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hmatalonga.greenhub.util.LogUtils;
import com.hmatalonga.greenhub.util.Notifier;
import com.hmatalonga.greenhub.util.SettingsUtils;

import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * BootReceiver.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = makeLogTag(BootReceiver.class);

    /**
     * Used to start Service on reboot even when GreenHub is not started.
     *
     * @param context the context
     * @param intent the intent (should be ACTION_BOOT_COMPLETED)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.logI(TAG, "BOOT_COMPLETED onReceive()");
//        if (SettingsUtils.isTosAccepted(context) && SettingsUtils.isPowerIndicatorShown(context)) {
//            // Display Status bar
//            Notifier.startStatusBar(context);
//        }
    }
}
