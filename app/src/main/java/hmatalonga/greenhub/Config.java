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

    public static final String STATUS_URL = "https://hmatalonga.github.io/";
    public static final String SERVER_URL_DEFAULT = "none";

    // Report Freshness timeout. Default: 15 minutes
    public static final long FRESHNESS_TIMEOUT = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    public static final String PREFERENCE_FIRST_RUN = "greenhub.first.run";
    static final String REGISTERED_UUID = "greenhub.registered.uuid";
    public static final String REGISTERED_OS = "greenhub.registered.os";
    public static final String REGISTERED_MODEL = "greenhub.registered.model";

    // Alarm event for sampling when battery has not changed for
    // SAMPLE_INTERVAL_MS. Currently not used.
    public static final String ACTION_GREENHUB_SAMPLE = "hmatalonga.greenhub.ACTION_SAMPLE";

    public static final String GREENHUB_PACKAGE = "hmatalonga.greenhub";

    public static final String IMPORTANCE_NOT_RUNNING = "Not Running";
    public static final String IMPORTANCE_UNINSTALLED = "uninstalled";
    public static final String IMPORTANCE_DISABLED = "disabled";
    public static final String IMPORTANCE_INSTALLED = "installed";
    public static final String IMPORTANCE_REPLACED = "replaced";

    public static final boolean EXTRA_SCREEN_ACTIONS = false;

    public static final int SAMPLE_MAX_BATCHES = 50;
    public static final int REFRESH_CURRENT_INTERVAL = 5000;
    public static final int REFRESH_STATUS_BAR_INTERVAL = REFRESH_CURRENT_INTERVAL * 6;

    public static final String STATUS_IDLE = "Idle";

    public static final int NOTIFICATION_BATTERY_STATUS = 1001;
}
