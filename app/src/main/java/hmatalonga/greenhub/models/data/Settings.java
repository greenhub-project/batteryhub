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

/**
 * SettingsInfo data definition.
 */
public class Settings {

    private static final int FIELD_NUM = 5;

    private boolean bluetoothEnabled;

    private boolean locationEnabled;

    private boolean powersaverEnabled;

    private boolean flashlightEnabled;

    private boolean autoRotateEnabled;

    private boolean nfcEnabled;

    public boolean isBluetoothEnabled() {
        return bluetoothEnabled;
    }

    public void setBluetoothEnabled(boolean bluetoothEnabled) {
        this.bluetoothEnabled = bluetoothEnabled;
    }

    public boolean isLocationEnabled() {
        return locationEnabled;
    }

    public void setLocationEnabled(boolean locationEnabled) {
        this.locationEnabled = locationEnabled;
    }

    public boolean isPowersaverEnabled() {
        return powersaverEnabled;
    }

    public void setPowersaverEnabled(boolean powersaverEnabled) {
        this.powersaverEnabled = powersaverEnabled;
    }

    public boolean isFlashlightEnabled() {
        return flashlightEnabled;
    }

    public void setFlashlightEnabled(boolean flashlightEnabled) {
        this.flashlightEnabled = flashlightEnabled;
    }

    public boolean isAutoRotateEnabled() {
        return autoRotateEnabled;
    }

    public void setAutoRotateEnabled(boolean autoRotateEnabled) {
        this.autoRotateEnabled = autoRotateEnabled;
    }

    public boolean isNfcEnabled() {
        return nfcEnabled;
    }

    public void setNfcEnabled(boolean nfcEnabled) {
        this.nfcEnabled = nfcEnabled;
    }
}
