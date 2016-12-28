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

package hmatalonga.greenhub.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.util.GreenHubHelper;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.managers.storage.GreenHubDb;
import hmatalonga.greenhub.models.Network;
import hmatalonga.greenhub.models.data.Sample;
import hmatalonga.greenhub.util.NetworkWatcher;

import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Sample Sender
 */
public class SampleSender {

    private static final String TAG = makeLogTag(SampleSender.class);

    private static final Object SEND_LOCK = new Object();

    public static void sendSamples(final Context context) {
        synchronized(SEND_LOCK){
            boolean connected = NetworkWatcher.hasInternet(context);
        }
    }
}
