/*
 * Copyright (c) 2017 Hugo Matalonga & João Paulo Fernandes
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

package com.hmatalonga.greenhub.tasks;

import android.os.AsyncTask;

import com.hmatalonga.greenhub.models.data.BatterySession;
import com.hmatalonga.greenhub.util.DateUtils;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * DeleteSessionsTask.
 */
public class DeleteSessionsTask extends AsyncTask<Integer, Void, Boolean> {

    private static final String TAG = "DeleteSessionsTask";

    private boolean mResponse;

    @Override
    protected Boolean doInBackground(Integer... params) {
        mResponse = false;
        // Open the Realm
        Realm realm = Realm.getDefaultInstance();

        try {
            final int interval = params[0];
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<BatterySession> sessions =
                            realm.where(BatterySession.class).lessThan(
                                    "timestamp",
                                    DateUtils.getMilliSecondsInterval(interval)
                            ).findAll();
                    if (sessions != null && !sessions.isEmpty()) {
                        mResponse = sessions.deleteAllFromRealm();
                    }
                }
            });
        } finally {
            realm.close();
        }

        return mResponse;
    }
}
