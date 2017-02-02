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

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Process Info data definition.
 */
public class ProcessInfo extends RealmObject {

    // Process Id
    public int processId;

    // Process Name
    public String name;

    // Human readable application name
    public String applicationLabel;

    // If the app is a system app or update to a system app
    public boolean isSystemApp;

    // Foreground, visible, background, service, empty
    public String importance;

    // Version of app, human-readable
    public String versionName;

    // Version of app, android version code
    public int versionCode;

    // Package that installed this process, e.g. com.google.play
    public String installationPkg;

    // Package Permissions
    public RealmList<AppPermission> appPermissions;

    // Signatures of the app from PackageInfo.signatures (it can be empty)
    public RealmList<AppSignature> appSignatures;
}
