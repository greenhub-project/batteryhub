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

package hmatalonga.greenhub.managers.storage;

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
