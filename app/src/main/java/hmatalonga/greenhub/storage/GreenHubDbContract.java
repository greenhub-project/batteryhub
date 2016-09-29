package hmatalonga.greenhub.storage;

import android.provider.BaseColumns;

/**
 * Created by hugo on 16-04-2016.
 */
public class GreenHubDbContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public GreenHubDbContract() {}

    /* Inner class that defines the table contents */
    public static abstract class GreenHubEntry implements BaseColumns {
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_SAMPLE = "sample";
        public static final String SAMPLES_VIRTUAL_TABLE = "sampleobjects";
        public static final String SQL_COMPACT_DATABASE = "PRAGMA auto_vacuum = 1;";
    }

}
