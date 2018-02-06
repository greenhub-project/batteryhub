package com.hmatalonga.greenhub.managers.storage;

import android.support.annotation.NonNull;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

import static com.hmatalonga.greenhub.util.LogUtils.LOGE;
import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Created by hugo on 17-10-2017.
 */

public class GreenHubDbMigration implements RealmMigration {
    private static final String TAG = makeLogTag(GreenHubDbMigration.class);

    @Override
    public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {
        // DynamicRealm exposes an editable schema
        RealmSchema schema = realm.getSchema();

        if (schema == null) return;

        // TODO: Check in future versions if schema.get() returns null at some point

        try {
            if (oldVersion == 1) {
                schema.get("Sample")
                        .addField("version", int.class);
                oldVersion++;
            }

            if (oldVersion == 2) {
                schema.get("Device")
                        .removeField("serialNumber");
                schema.get("Sample")
                        .addField("database", int.class);
                oldVersion++;
            }

            if (oldVersion == 3) {
                schema.get("BatteryDetails")
                        .addField("remainingCapacity", int.class);
                oldVersion++;
            }
        } catch (NullPointerException e) {
            LOGE(TAG, "Schema is null!");
            e.printStackTrace();
        }
    }
}
