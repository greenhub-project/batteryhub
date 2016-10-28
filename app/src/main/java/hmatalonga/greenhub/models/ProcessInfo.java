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

package hmatalonga.greenhub.models;

import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import hmatalonga.greenhub.util.StringHelper;

/**
 * Process Info data definition.
 */
public class ProcessInfo {

    private static final int FIELD_NUM = 9;

    // Process Id
    private int pId;

    // Process Name
    private String name;

    // Human readable application name
    private String applicationLabel;

    // If the app is a system app or update to a system app
    private boolean isSystemApp;

    // Foreground, visible, background, service, empty
    private String importance;

    // Version of app, human-readable
    private String versionName; // optional

    // Version of app, android version code
    private int versionCode;

    // Signatures of the app from PackageInfo.signatures (it can be empty)
    private List<String> appSignatures;

    // Package that installed this process, e.g. com.google.play
    private String installationPkg;

    public int getpId() {
        return pId;
    }

    public void setpId(int pId) {
        this.pId = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplicationLabel() {
        return applicationLabel;
    }

    public void setApplicationLabel(String applicationLabel) {
        this.applicationLabel = applicationLabel;
    }

    public boolean isSetApplicationLabel() {
        return this.applicationLabel != null;
    }

    public boolean isSystemApp() {
        return isSystemApp;
    }

    public void setSystemApp(boolean systemApp) {
        isSystemApp = systemApp;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public List<String> getAppSignatures() {
        return appSignatures;
    }

    public void setAppSignatures(@Nullable List<String> appSignatures) {
        this.appSignatures = appSignatures;
    }

    public String getInstallationPkg() {
        return installationPkg;
    }

    public void setInstallationPkg(@Nullable String installationPkg) {
        this.installationPkg = installationPkg;
    }

    private List<String> parseAppSignatures(String s) {
        List<String> sig = null;

        if (!s.equals("null")) {
            s = s.substring(1, s.length() - 1);
            String[] split = s.split(",");
            sig = Arrays.asList(split);
        }

        return sig;
    }

    public void parseString(String s) {
        String[] values = StringHelper.trimArray(s.split(";"));
        if (values.length == FIELD_NUM) {
            try {
                setpId(Integer.parseInt(values[0]));
                setName(values[1]);
                setApplicationLabel(values[2]);
                setSystemApp(Boolean.parseBoolean(values[3]));
                setImportance(values[4]);
                setVersionName(values[5]);
                setVersionCode(Integer.parseInt(values[6]));
                setAppSignatures(parseAppSignatures(values[7])); // List
                setInstallationPkg(values[8]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return String.valueOf(pId) + ";" + name + ";" + applicationLabel + ";" +
                String.valueOf(isSystemApp) + ";" + importance + ";" + versionName + ";" +
                String.valueOf(versionCode) + ";" + appSignatures + ";" + installationPkg;
    }
}
