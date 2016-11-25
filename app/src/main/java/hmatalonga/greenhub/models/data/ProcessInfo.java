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

import java.util.List;

/**
 * Process Info data definition.
 */
@Table(name = "ProcessInfos")
public class ProcessInfo extends Model {

    // Sample FK
    @Column(name = "Sample")
    public Sample sample;

    // Process Id
    @Column(name = "ProcessId")
    public int processId;

    // Process Name
    @Column(name = "Name")
    public String name;

    // Human readable application name
    @Column(name = "ApplicationLabel")
    public String applicationLabel;

    // If the app is a system app or update to a system app
    @Column(name = "IsSystemApp")
    public boolean isSystemApp;

    // Foreground, visible, background, service, empty
    @Column(name = "Importance")
    public String importance;

    // Version of app, human-readable
    @Column(name = "VersionName")
    public String versionName;

    // Version of app, android version code
    @Column(name = "VersionCode")
    public int versionCode;

    // Package that installed this process, e.g. com.google.play
    @Column(name = "InstallationPkg")
    public String installationPkg;
    
    public ProcessInfo() {
        super();
    }

    // Signatures of the app from PackageInfo.signatures (it can be empty)
    public List<AppSignature> appSignatures() {
        return getMany(AppSignature.class, "ProcessInfo");
    }
}
