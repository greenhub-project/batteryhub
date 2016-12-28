/*
 * Copyright (c) 2016 Hugo Matalonga & João Paulo Fernandes
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

package hmatalonga.greenhub.models.data;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * BatterySession.
 */
public class BatterySession extends RealmObject {

    @PrimaryKey
    public int id;

    // Timestamp of session
    @Index
    public long timestamp;

    // Level of the battery (0 - 1.0) translates to percentage
    public float level;

    // If screen is on == 1, off == 0
    public int screenOn;

    // Trigger reason
    public String triggeredBy;
}
