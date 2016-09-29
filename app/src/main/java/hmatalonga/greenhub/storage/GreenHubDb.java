package hmatalonga.greenhub.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
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
import hmatalonga.greenhub.storage.GreenHubDbContract.GreenHubEntry;

/**
 * Created by hugo on 16-04-2016.
 */
public class GreenHubDb {
    private static final String TAG = "GreenHubDb";

    private static final HashMap<String, String> mColumnMap;
    private static final Object dbLock;

    private static GreenHubDb instance = null;

    private Sample lastSample = null;
    private SQLiteDatabase db = null;
    private GreenHubDbHelper helper = null;

    static {
        dbLock = new Object();
        mColumnMap = buildColumnMap();
    }

    public static GreenHubDb getInstance(Context context) {
        if (instance == null)
            instance = new GreenHubDb(context);
        return instance;
    }

    private GreenHubDb(Context context) {
        synchronized (dbLock) {
            helper = new GreenHubDbHelper(context);
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
        HashMap<String, String> map = new HashMap<>();
        map.put(GreenHubEntry.COLUMN_TIMESTAMP, GreenHubEntry.COLUMN_TIMESTAMP);
        map.put(GreenHubEntry.COLUMN_SAMPLE, GreenHubEntry.COLUMN_SAMPLE);
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
        builder.setTables(GreenHubEntry.SAMPLES_VIRTUAL_TABLE);
        builder.setProjectionMap(mColumnMap);

        Cursor cursor = builder.query(db, columns, selection, selectionArgs,
                groupBy, having, sortOrder);

        if (cursor == null)
            return null;
        else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        return cursor;
    }

    public int countSamples() {
        try {
            synchronized (dbLock) {
                if (db == null || !db.isOpen()) {
                    try {
                        db = helper.getWritableDatabase();
                    } catch (android.database.sqlite.SQLiteException ex){
                        Log.e(TAG, "Could not open database", ex);
                        return -1;
                    }
                }

                Cursor cursor = db.rawQuery("select count(timestamp) FROM " +
                        GreenHubEntry.SAMPLES_VIRTUAL_TABLE, null);

                if (cursor == null)
                    // There are no results
                    return -1;
                else {
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

    public SortedMap<Long, Sample> queryOldestSamples(int how_many) {
        SortedMap<Long, Sample> results = new TreeMap<>();

        try {
            synchronized (dbLock) {
                if (db == null || !db.isOpen()) {
                    try {
                        db = helper.getWritableDatabase();
                    } catch (android.database.sqlite.SQLiteException ex) {
                        return results;
                    }
                }
                String[] columns = mColumnMap.keySet().toArray(new String[mColumnMap.size()]);

                Cursor cursor = query(null, null, columns, null, null, GreenHubEntry.COLUMN_TIMESTAMP +
                        " ASC LIMIT " + how_many);

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
        return db.delete(GreenHubEntry.SAMPLES_VIRTUAL_TABLE, whereClause, whereArgs);
    }

    public int deleteSamples(Set<Long> row_ids) {
        int ret = 0;

        try {
            synchronized (dbLock) {
                if (db == null || !db.isOpen())
                    db = helper.getWritableDatabase();

                StringBuilder sb = new StringBuilder();
                int i = 0;
                sb.append("(");

                for (Long row_id : row_ids) {
                    sb.append("").append(row_id);
                    i++;
                    if (i != row_ids.size()) {
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
        String[] columns = mColumnMap.keySet().toArray(new String[mColumnMap.size()]);

        Cursor cursor = query(null, null, columns, null, null, GreenHubEntry.COLUMN_TIMESTAMP
                + " DESC LIMIT 1");

        if (cursor == null)
            // There are no results
            return null;
        else {
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

        byte[] sampleB = cursor.getBlob(cursor.getColumnIndex(GreenHubEntry.COLUMN_SAMPLE));
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
                    try {
                        db = helper.getWritableDatabase();
                    } catch (android.database.sqlite.SQLiteException ex){
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
                if (db == null || !db.isOpen())
                    db = helper.getWritableDatabase();

                // force init
                id = addSample(s);

                if (id >= 0)
                    lastSample = SampleReader.readSample(s);

                if (db != null && db.isOpen())
                    db.close();
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
        initialValues.put(GreenHubEntry.COLUMN_TIMESTAMP, s.getTimestamp());
        // Write the sample hashmap as a blob
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(SampleReader.writeSample(s));
            initialValues.put(GreenHubEntry.COLUMN_SAMPLE, bo.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return db.insert(GreenHubEntry.SAMPLES_VIRTUAL_TABLE, null, initialValues);
    }

    public static class GreenHubDbHelper extends SQLiteOpenHelper {
        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;


        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "GreenHub.db";

        private static final String FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE "
                + GreenHubEntry.SAMPLES_VIRTUAL_TABLE + " USING fts3 (" + createStatement()
                + ");";

        private static String createStatement() {
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
                    b.append(s).append(", ");
                i++;
            }
            return b.toString();
        }

        public GreenHubDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
            try {
                mDatabase.execSQL(FTS_TABLE_CREATE);
                mDatabase.execSQL(GreenHubEntry.SQL_COMPACT_DATABASE);
            }
            catch (NullPointerException | SQLException e) {
                e.printStackTrace();
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL("DROP TABLE IF EXISTS " + GreenHubEntry.SAMPLES_VIRTUAL_TABLE);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
