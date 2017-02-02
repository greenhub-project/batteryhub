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

package hmatalonga.greenhub.tasks;

import android.os.AsyncTask;

import hmatalonga.greenhub.models.data.Sample;
import io.realm.Realm;

/**
 * DeleteSampleTask.
 */
public class DeleteSampleTask extends AsyncTask<Integer, Void, Void> {

    private static final String TAG = "DeleteSampleTask";

    @Override
    protected Void doInBackground(Integer... params) {
        // Open the Realm
        Realm realm = Realm.getDefaultInstance();
        try {
            final int id = params[0];
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Sample sample = realm.where(Sample.class).equalTo("id", id).findFirst();
                    if (sample != null) sample.deleteFromRealm();
                }
            });
        } finally {
            realm.close();
        }
        return null;
    }
}
