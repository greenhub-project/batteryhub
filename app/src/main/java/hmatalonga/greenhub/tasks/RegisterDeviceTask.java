/*
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

package hmatalonga.greenhub.tasks;

import android.content.Context;
import android.os.AsyncTask;

import hmatalonga.greenhub.network.RegisterHandler;
import hmatalonga.greenhub.util.GreenHubHelper;
import hmatalonga.greenhub.fragments.HomeFragment;
import hmatalonga.greenhub.util.NetworkWatcher;

/**
 * Task to register devices on the web server.
 */
public class RegisterDeviceTask extends AsyncTask<Context, Void, Void> {

    private static final String TAG = "RegisterDeviceTask";

    @Override
    protected Void doInBackground(Context... params) {
        RegisterHandler handler = new RegisterHandler();
        handler.registerClient(params[0]);
        return null;
    }
}
