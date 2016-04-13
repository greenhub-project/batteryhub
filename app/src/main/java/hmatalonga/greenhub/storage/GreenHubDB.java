package hmatalonga.greenhub.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import hmatalonga.greenhub.Constants;
import hmatalonga.greenhub.database.Sample;

/**
 * Created by hugo on 13-04-2016.
 */
public class GreenHubDB {
    private static final String TAG = "GreenHubDB";

    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_SAMPLE = "sample";

    public static final String DATABASE_NAME = "greenhubdata";
    public static final String SAMPLES_VIRTUAL_TABLE = "sampleobjects";
    // TODO: Bump version here when changing the protocol, new one incompatible with old
    private static final int DATABASE_VERSION = 1;

    private static final HashMap<String, String> mColumnMap = buildColumnMap();

    private Sample lastSample = null;

    private SQLiteDatabase db = null;

    private SampleDbOpenHelper helper = null;

    private static GreenHubDB instance = null;

    private static Object dbLock = new Object();

    public static GreenHubDB getInstance(Context c) {
        if (instance == null)
            instance = new GreenHubDB(c);
        return instance;
    }

    private GreenHubDB(Context context) {
        synchronized (dbLock) {
            helper = new SampleDbOpenHelper(context);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        synchronized (dbLock) {
            if (db != null)
                db.close();
        }
        super.finalize();
    }

    /**
     *
     * Builds a map for all columns that may be requested, which will be given
     * to the SQLiteQueryBuilder. This is a good way to define aliases for
     * column names, but must include all columns, even if the value is the key.
     * This allows the ContentProvider to request columns w/o the need to know
     * real column names and create the alias itself.
     *
     * TODO: Needs to be updated when fields update.
     */
    private static HashMap<String, String> buildColumnMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(COLUMN_TIMESTAMP, COLUMN_TIMESTAMP);
        map.put(COLUMN_SAMPLE, COLUMN_SAMPLE);
        map.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);
        return map;
    }

    /**
     * Performs a database query.
     *
     * @param selection
     *            The selection clause
     * @param selectionArgs
     *            Selection arguments for "?" components in the selection
     * @param columns
     *            The columns to return
     * @return A Cursor over all rows matching the query
     */
    private Cursor query(String selection, String[] selectionArgs,
                         String[] columns, String groupBy, String having, String sortOrder) {
        /*
         * The SQLiteBuilder provides a map for all possible columns requested
         * to actual columns in the database, creating a simple column alias
         * mechanism by which the ContentProvider does not need to know the real
         * column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(SAMPLES_VIRTUAL_TABLE);
        builder.setProjectionMap(mColumnMap);

        Cursor cursor = builder.query(db, columns, selection, selectionArgs,
                groupBy, having, sortOrder);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    public int countSamples() {
        try {
            synchronized (dbLock) {
                if (db == null || !db.isOpen()) {
                    try{
                        db = helper.getWritableDatabase();
                    }catch (android.database.sqlite.SQLiteException ex){
                        Log.e(TAG, "Could not open database", ex);
                        return -1;
                    }
                }

                Cursor cursor = db.rawQuery("select count(timestamp) FROM "+SAMPLES_VIRTUAL_TABLE, null);

                if (cursor == null) {
                    // There are no results
                    return -1;
                } else {
                    int ret = -1;
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        ret = cursor.getInt(0);
                        cursor.moveToNext();
                    }
                    cursor.close();
                    return ret;
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return -1;
    }

    public SortedMap<Long, Sample> queryOldestSamples(int howmany) {
        SortedMap<Long, Sample> results = new TreeMap<Long, Sample>();
        try {
            synchronized (dbLock) {
                if (db == null || !db.isOpen()) {
                    try{
                        db = helper.getWritableDatabase();
                    } catch (android.database.sqlite.SQLiteException ex) {
                        return results;
                    }
                }
                String[] columns = mColumnMap.keySet().toArray(
                        new String[mColumnMap.size()]);

                Cursor cursor = query(null, null, columns, null, null,
                        COLUMN_TIMESTAMP + " ASC LIMIT " + howmany);

                if (cursor == null) {
                    // Log.d("CaratSampleDB", "query returned null");
                    // There are no results
                    return results;
                } else {
                    // Log.d("CaratSampleDB", "query is successfull!");
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        Sample s = fillSample(cursor);
                        if (s != null) {
                            results.put(cursor.getLong(cursor
                                    .getColumnIndex(BaseColumns._ID)), s);
                            cursor.moveToNext();
                        }
                    }
                    cursor.close();

                }
            }
        } catch (Throwable th) {
            Log.e(TAG, "Failed to query oldest samples!", th);
        }
        return results;
    }

    private int delete(String whereClause, String[] whereArgs) {
        int deleted = db.delete(SAMPLES_VIRTUAL_TABLE, whereClause, whereArgs);
        return deleted;
    }

    public int deleteSamples(Set<Long> rowids) {
        int ret = 0;
        try {
            synchronized (dbLock) {
                if (db == null || !db.isOpen()) {
                    db = helper.getWritableDatabase();
                }
                StringBuilder sb = new StringBuilder();
                int i = 0;
                sb.append("(");
                for (Long rowid : rowids) {
                    sb.append("" + rowid);
                    i++;
                    if (i != rowids.size()) {
                        sb.append(", ");
                    }
                }
                sb.append(")");
                ret = delete("rowid in " + sb.toString(), null);

                if (db != null && db.isOpen()) {
                    db.close();
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return ret;
    }

    private Sample queryLastSample() {
        String[] columns = mColumnMap.keySet().toArray(
                new String[mColumnMap.size()]);

        Cursor cursor = query(null, null, columns, null, null, COLUMN_TIMESTAMP
                + " DESC LIMIT 1");

        if (cursor == null) {
            // There are no results
            return null;
        } else {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                Sample s = fillSample(cursor);
                cursor.close();
                lastSample = s;
                return s;
            }
            cursor.close();
            return null;
        }
    }

    /*
     * Read a sample from the current position of the cursor. TODO: Needs to be
     * updated when fields update.
     */
    private Sample fillSample(Cursor cursor) {
        Sample s = null;

        byte[] sampleB = cursor.getBlob(cursor
                .getColumnIndex(GreenHubDB.COLUMN_SAMPLE));
        if (sampleB != null) {
            ObjectInputStream oi;
            try {
                oi = new ObjectInputStream(new ByteArrayInputStream(sampleB));
                Object o = oi.readObject();
                if (o != null)
                    s = SampleReader.readSample(o);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return s;
    }

    public Sample getLastSample(Context c) {
        try {
            synchronized (dbLock) {
                if (db == null || !db.isOpen()) {
                    try{
                        db = helper.getWritableDatabase();
                    }catch (android.database.sqlite.SQLiteException ex){
                        Log.e(TAG, "Could not open database", ex);
                        return lastSample;
                    }
                }
                if (lastSample == null)
                    queryLastSample();
            }
        } catch (Throwable th) {
            Log.e(TAG, "Failed to get last sample!", th);
        }
        return lastSample;
    }

    /**
     * Store the sample into the database
     * @param s the sample to be saved
     * @return positive int if the operation is successful, otherwise zero
     */
    public long putSample(Sample s) {
        long id = 0;
        try {
            synchronized (dbLock) {
                if (db == null || !db.isOpen()) {
                    db = helper.getWritableDatabase();
                }
                if (Constants.DEBUG)
                    Log.d(TAG, "CaratSampleDB.putSample(). About to save a sample to the DB. "
                                    + "uuid=" + s.getUuId()
                                    + ", timestamp=" + s.getTimestamp()
                                    + ", timezone=" + s.getTimeZone()
                                    + ", batteryLevel=" + s.getBatteryLevel()
                                    + ", batteryState=" + s.getBatteryState()
                                    + ", developerMode=" + s.getDeveloperMode()
                                    + ", distanceTraveled=" + s.getDistanceTraveled()
                                    + ", memoryActive=" + s.getMemoryActive()
                                    + ", memoryFree=" + s.getMemoryFree()
                                    + ", memoryInactive=" + s.getMemoryInactive()
                                    + ", memoryWired=" + s.getMemoryWired()
                                    + ", screenBrightness=" + s.getScreenBrightness()
                                    + ", networkStatus=" + s.getNetworkStatus()
                                    + ", screenOn=" + s.getScreenOn()
                                    + ", unknownSources=" + s.getUnknownSources()
                                    + ", batteryCapacity=" + s.getBatteryDetails().getBatteryCapacity()
                                    + ", batteryCharger=" + s.getBatteryDetails().getBatteryCharger()
                                    + ", batteryHealth=" + s.getBatteryDetails().getBatteryHealth()
                                    + ", batteryTechnology=" + s.getBatteryDetails().getBatteryTechnology()
                                    + ", batteryVoltage=" + s.getBatteryDetails().getBatteryVoltage()
//                		+ ", callStatus=" + s.callInfo.callStatus
//                		+ ", incomingCallTime=" + s.callInfo.incomingCallTime
//                		+ ", nonCallTime=" + s.callInfo.nonCallTime
//                		+ ", outgoingCallTime=" + s.callInfo.outgoingCallTime
                                    + ", cpuStatus=" + s.getCpuStatus()
                                    + ", mobileDataActivity=" + s.getNetworkDetails().getMobileDataActivity()
                                    + ", mobileDataStatus=" + s.getNetworkDetails().getMobileDataStatus()
                                    + ", mobileNetworkType=" + s.getNetworkDetails().getMobileNetworkType()
                                    + ", networkType=" + s.getNetworkDetails().getNetworkType()
                                    + ", wifiLinkSpeed=" + s.getNetworkDetails().getWifiLinkSpeed()
                                    + ", wifiSignalStrength=" + s.getNetworkDetails().getWifiSignalStrength()
                                    + ", wifiStatus=" + s.getNetworkDetails().getWifiStatus()
                                    + ", PiListSize=" + s.getPiListSize()
                    );

                // force init
                id = addSample(s);
                if (id >= 0) {
                    lastSample = SampleReader.readSample(s);
                }
                if (db != null && db.isOpen()) {
                    db.close();
                }
            }
        } catch (Throwable th) {
            Log.e(TAG, "Failed to add a sample!", th);
        }
        return id;
    }

    /**
     * Add a sample to the database.
     *
     * @return rowId or -1 if failed
     */
    private long addSample(Sample s) {
        if (Constants.DEBUG)
            Log.d("CaratSampleDB.addSample", "The sample's battery level=" + s.getBatteryLevel());

        ContentValues initialValues = new ContentValues();
        initialValues.put(COLUMN_TIMESTAMP, s.getTimestamp());
        // Write the sample hashmap as a blob
        if (s != null) {
            try {
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                ObjectOutputStream oo = new ObjectOutputStream(bo);
                oo.writeObject(SampleReader.writeSample(s));
                initialValues.put(COLUMN_SAMPLE, bo.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return db.insert(SAMPLES_VIRTUAL_TABLE, null, initialValues);
    }

    /**
     * This creates/opens the database.
     */
    private static class SampleDbOpenHelper extends SQLiteOpenHelper {
        private SQLiteDatabase mDatabase;

        /*
         * Note that FTS3 does not support column constraints and thus, you
         * cannot declare a primary key. However, "rowid" is automatically used
         * as a unique identifier, so when making requests, we will use "_id" as
         * an alias for "rowid"
         */
        private static final String FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE "
                + SAMPLES_VIRTUAL_TABLE + " USING fts3 (" + createStatement()
                + ");";

        private static final String createStatement() {
            Set<String> set = mColumnMap.keySet();
            StringBuilder b = new StringBuilder();
            int i = 0;
            int size = set.size() - 1;
            for (String s : set) {
                if (s.equals(BaseColumns._ID))
                    continue;
                if (i + 1 == size)
                    b.append(s);
                else
                    b.append(s + ", ");
                i++;
            }
            return b.toString();
        }

        SampleDbOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
            try {
                mDatabase.execSQL(FTS_TABLE_CREATE);
                /**
                 * Compact database here
                 */
                mDatabase.execSQL("PRAGMA auto_vacuum = 1;");
            } catch (Throwable th) {
                // Already created
                Log.e(TAG, "DB create failed!", th);
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * android.database.sqlite.SQLiteOpenHelper#onOpen(android.database.
         * sqlite.SQLiteDatabase)
         */
        @Override
        public void onOpen(SQLiteDatabase db) {
            mDatabase = db;
            super.onOpen(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + SAMPLES_VIRTUAL_TABLE);
            onCreate(db);
        }
    }
}

