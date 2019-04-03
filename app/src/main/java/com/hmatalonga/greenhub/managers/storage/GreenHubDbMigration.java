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
                oldVersion++;
            }

            if (oldVersion == 4) {
                objectSchema = schema.get("SensorDetails");
                if (objectSchema != null) {
                    objectSchema.addField("fifoMaxEventCount", int.class);
                    objectSchema.addField("fifoReservedEventCount", int.class);
                    objectSchema.addField("highestDirectReportRateLevel", int.class);
                    objectSchema.addField("id", int.class);
                    objectSchema.addField("isAdditionalInfoSupported", boolean.class);
                    objectSchema.addField("isDynamicSensor", boolean.class);
                    objectSchema.addField("isWakeUpSensor", boolean.class);
                    objectSchema.addField("maxDelay", int.class);
                    objectSchema.addField("maximumRange", float.class);
                    objectSchema.addField("minDelay", int.class);
                    objectSchema.addField("name", String.class);
                    objectSchema.addField("power", float.class);
                    objectSchema.addField("reportingMode", int.class);
                    objectSchema.addField("resolution", float.class);
                    objectSchema.addField("stringType", String.class);
                    objectSchema.addField("codeType", int.class);
                    objectSchema.addField("vendor", String.class);
                    objectSchema.addField("version", int.class);
                }
                oldVersion++;
            }
        } catch (NullPointerException e) {
            LogUtils.logE(TAG, "Schema is null!");
            e.printStackTrace();
        }
    }
}
