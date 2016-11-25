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

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * SettingsInfo data definition.
 */
@Table(name = "Settings")
public class Settings extends Model {

    @Column(name = "BluetoothEnabled")
    public boolean bluetoothEnabled;

    @Column(name = "LocationEnabled")
    public boolean locationEnabled;

    @Column(name = "PowerSaverEnabled")
    public boolean powersaverEnabled;

    @Column(name = "FlashLightEnabled")
    public boolean flashlightEnabled;

    @Column(name = "AutoRotateEnabled")
    public boolean autoRotateEnabled;

    @Column(name = "NfcEnabled")
    public boolean nfcEnabled;

    // Unknown source app installation on == 1, off == 0
    @Column(name = "UnknownSources")
    public int unknownSources;

    // Developer mode on == 1, off == 0
    @Column(name = "DeveloperMode")
    public int developerMode;
    
    public Settings() {
        super();
    }
}
