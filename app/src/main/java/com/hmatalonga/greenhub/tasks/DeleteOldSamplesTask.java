package com.hmatalonga.greenhub.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.hmatalonga.greenhub.Config;
import com.hmatalonga.greenhub.models.data.Sample;

import io.realm.Realm;
import io.realm.RealmResults;

public class DeleteOldSamplesTask extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... params) {
        // Open the Realm
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    RealmResults<Sample> results =
                            realm.where(Sample.class).sort("timestamp").findAll();
                    int size = results.size();
                    for (int i = 0; i < Config.SAMPLES_MAX_STORAGE_NUM && i < size; i++) {
                        Sample sample = results.get(i);
                        if (sample == null) continue;
                        sample.deleteFromRealm();
                    }
                }
            });
        } finally {
            realm.close();
        }
        return null;
    }
}