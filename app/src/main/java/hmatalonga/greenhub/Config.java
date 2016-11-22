/*
 * Copyright (c) 2011-2016, AMP Lab and University of Helsinki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 *
 * -------------------------------------------------------------------------------
 *
 * Copyright (C) 2016 Hugo Matalonga & João Paulo Fernandes
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

package hmatalonga.greenhub;

import android.app.AlarmManager;

/**
 * Config fields.
 * Created by hugo on 24-03-2016.
 */
public final class Config {
    // Whether to output debug messages.
    public static final boolean DEBUG = true;
    public static final boolean PRODUCTION = false;

    public static final String PUBLIC_SERVER_URL = "http://greenhub-hmatalonga.rhcloud.com";
    public static final String LOCAL_SERVER_URL = "http://192.168.1.104";

    // Report Freshness timeout. Default: 15 minutes
    // public static final long FRESHNESS_TIMEOUT = 30 * 1000;
    public static final long FRESHNESS_TIMEOUT = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    // Blacklist freshness timeout. Default 24h.
    // public static final long FRESHNESS_TIMEOUT_BLACKLIST = 30 * 1000;
    public static final long FRESHNESS_TIMEOUT_BLACKLIST = 24 * 3600 * 1000;
    // Blacklist freshness timeout. Default 2 days.
    // public static final long FRESHNESS_TIMEOUT_QUICKHOGS = 30 * 1000;
    public static final long FRESHNESS_TIMEOUT_QUICKHOGS = 2 * 24 * 3600 * 1000;

    // If this preference is true, register this as a new device on the Carat
    // server.
    public static final String PREFERENCE_FIRST_RUN = "greenhub.first.run";
    static final String REGISTERED_UUID = "greenhub.registered.uuid";
    public static final String REGISTERED_OS = "greenhub.registered.os";
    public static final String REGISTERED_MODEL = "greenhub.registered.model";

    // if you change the preference key of any of our preference widgets (in
    // res/xml/preferences.xml),
    // update the following constants as well
    public static final String SHARE_PREFERENCE_KEY = "sharePrefKey";
    public static final String FEEDBACK_PREFERENCE_KEY = "feedbackPrefKey";

    // for caching summary statistics fetched from server
    public static final String PREFERENCE_FILE_NAME = "caratPrefs";
    public static final String STATS_WELLBEHAVED_COUNT_PREFERENCE_KEY = "wellbehavedPrefKey";
    public static final String STATS_HOGS_COUNT_PREFERENCE_KEY = "hogsPrefKey";
    public static final String STATS_BUGS_COUNT_PREFERENCE_KEY = "bugsPrefKey";

    public static final String PREFERENCE_NEW_UUID = "carat.new.uuid";
    public static final String PREFERENCE_TIME_BASED_UUID = "carat.uuid.timebased";

    // Check for and send new samples at most every 15 minutes, but only when
    // the user wakes up/starts Carat
    public static final long COMMS_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    // When waking up from screen off, wait 5 seconds for wifi etc to come up
    public static final long COMMS_WIFI_WAIT = 5 * 1000;
    // Send up to 10 samples at a time
    public static final int COMMS_MAX_UPLOAD_BATCH = 10;

    // Alarm event for sampling when battery has not changed for
    // SAMPLE_INTERVAL_MS. Currently not used.
    public static final String ACTION_GREENHUB_SAMPLE = "hmatalonga.greenhub.ACTION_SAMPLE";
    // If true, install Sampling events to occur at boot. Currently not used.
    public static final String PREFERENCE_SAMPLE_FIRST_RUN = "greenhub.sample.first.run";
    public static final String PREFERENCE_SEND_INSTALLED_PACKAGES = "greenhub.sample.send.installed";

    // default icon and Carat package name:
    public static final String CARAT_PACKAGE_NAME = "edu.berkeley.cs.amplab.carat.android";
    // Used to blacklist old Carat
    public static final String CARAT_OLD = "edu.berkeley.cs.amplab.carat";

    // Not in Android 2.2, but needed for app importances
    public static final int IMPORTANCE_PERCEPTIBLE = 130;
    // Used for non-app suggestions
    public static final int IMPORTANCE_SUGGESTION = 123456789;

    public static final String IMPORTANCE_NOT_RUNNING = "Not Running";
    public static final String IMPORTANCE_UNINSTALLED = "uninstalled";
    public static final String IMPORTANCE_DISABLED = "disabled";
    public static final String IMPORTANCE_INSTALLED = "installed";
    public static final String IMPORTANCE_REPLACED = "replaced";

    public static final int SAMPLE_MAX_BATCHES = 50;

    // used in the PrefetchData class and MainActivity
    // (to check whether the users statistics are fetched from the server)
    public static final String DATA_NOT_AVAIABLE = "not_available";

    public static final String MAIN_ACTIVITY_PREFERENCE_KEY = "Main_Activity_Shared_Preferences_Key";

    // keys for retrieving values from the shared preference
    public static final String WELL_BEHAVED_APPS_COUNT_PREF_KEY = "wellbehaved";
    public static final String HOGS_COUNT_PREF_KEY = "hogs";
    public static final String BUGS_COUNT_PREF_KEY = "bugs";

    // Used for messages in comms threads
    static final String MSG_TRY_AGAIN = " will try again in " + (FRESHNESS_TIMEOUT / 1000) + "s.";

    public static int VALUE_NOT_AVAILABLE = -1;
}
