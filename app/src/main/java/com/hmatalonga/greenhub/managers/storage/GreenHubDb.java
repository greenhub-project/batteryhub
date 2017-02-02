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

package com.hmatalonga.greenhub.managers.storage;

import java.util.ArrayList;
import java.util.Iterator;

import com.hmatalonga.greenhub.models.data.BatterySession;
import com.hmatalonga.greenhub.models.data.BatteryUsage;
import com.hmatalonga.greenhub.models.data.Sample;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;

import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * GreenHub database provider.
 *
 * Created by hugo on 16-04-2016.
 */
public class GreenHubDb {
    private static final String TAG = makeLogTag(GreenHubDb.class);

    private Realm mRealm;

    public GreenHubDb() {
        try {
            mRealm = Realm.getDefaultInstance();
        } catch (RealmMigrationNeededException e) {
            // handle migration exception io.realm.exceptions.RealmMigrationNeededException
            e.printStackTrace();
        }
    }

    public void getDefaultInstance() {
        if (mRealm.isClosed()) {
            mRealm = Realm.getDefaultInstance();
        }
    }

    public void close() {
        mRealm.close();
    }

    public boolean isClosed() {
        return mRealm.isClosed();
    }

    public long count(Class className) {
        long size = -1;
        if (className.equals(Sample.class)) {
            size = mRealm.where(Sample.class).count();
        } else if (className.equals(BatteryUsage.class)) {
            size = mRealm.where(BatteryUsage.class).count();
        } else if (className.equals(BatterySession.class)) {
            size = mRealm.where(BatterySession.class).count();
        }
        return size;
    }

    public Sample lastSample() {
        if (mRealm.where(Sample.class).count() > 0) {
            return mRealm.where(Sample.class).findAll().last();
        }
        return null;
    }

    /**
     * Store the sample into the database
     *
     * @param sample the sample to be saved
     */
    public void saveSample(Sample sample) {
        mRealm.beginTransaction();
        mRealm.copyToRealm(sample);
        mRealm.commitTransaction();
    }

    /**
     * Store the usage details into the database
     *
     * @param usage the usage details to be saved
     */
    public void saveUsage(BatteryUsage usage) {
        mRealm.beginTransaction();
        mRealm.copyToRealm(usage);
        mRealm.commitTransaction();
    }

    /**
     * Store a new battery session into the database
     *
     * @param session the session to be saved
     */
    public void saveSession(BatterySession session) {
        mRealm.beginTransaction();
        mRealm.copyToRealm(session);
        mRealm.commitTransaction();
    }

    public Iterator<Integer> allSamplesIds() {
        ArrayList<Integer> list = new ArrayList<>();
        RealmResults<Sample> samples = mRealm.where(Sample.class).findAll();
        if (!samples.isEmpty()) {
            for (Sample sample : samples) {
                list.add(sample.id);
            }
        }
        return list.iterator();
    }

    public RealmResults<BatteryUsage> betweenUsages(long from, long to) {
        return mRealm.where(BatteryUsage.class).between("timestamp", from, to).findAllSorted("timestamp");
    }
}