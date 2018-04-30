package com.hmatalonga.greenhub.managers.storage;

import android.support.annotation.NonNull;

import com.hmatalonga.greenhub.util.LogUtils;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Created by hugo on 17-10-2017.
 */
public class GreenHubDbMigration implements RealmMigration {
    private static final String TAG = makeLogTag(GreenHubDbMigration.class);

    @Override
    public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {
        RealmObjectSchema objectSchema;

        // DynamicRealm exposes an editable schema
        RealmSchema schema = realm.getSchema();

        if (schema == null) return;

        try {
            if (oldVersion == 1) {
                objectSchema = schema.get("Sample");
                if (objectSchema != null) {
                    objectSchema.addField("version", int.class);
                    oldVersion++;
                }
            }

            if (oldVersion == 2) {
                boolean migrated = true;
                objectSchema = schema.get("Device");
                if (objectSchema != null) {
                    objectSchema.removeField("serialNumber");
                } else {
                    migrated = false;
                }
                objectSchema = schema.get("Sample");
                if (objectSchema != null) {
                    objectSchema.addField("mDatabase", int.class);
                } else {
                    migrated = false;
                }
                if (migrated) {
                    oldVersion++;
                }
            }

            if (oldVersion == 3) {
                objectSchema = schema.get("BatteryDetails");
                if (objectSchema != null) {
                    objectSchema.addField("remainingCapacity", int.class);
                }
            }
        } catch (NullPointerException e) {
            LogUtils.logE(TAG, "Schema is null!");
            e.printStackTrace();
        }
    }
}
