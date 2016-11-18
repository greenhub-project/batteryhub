/*
 * Copyright (c) 2011-2016, AMP Lab and University of Helsinki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 *
 * -------------------------------------------------------------------------------
 *
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

package hmatalonga.greenhub.models;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.models.data.ProcessInfo;

import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Package properties model.
 */
public class Package {

    private static final String TAG = makeLogTag(Package.class);

    private static WeakReference<Map<String, PackageInfo>> packages = null;

    /**
     * Helper to ensure the WeakReferenced `packages` is populated.
     *
     * @param context the Context
     * @param collectSignatures if needs to collect signatures or not
     * @return The content of `packages` or null in case of failure.
     */
    private static Map<String, PackageInfo> getPackages(Context context, boolean collectSignatures) {
        List<PackageInfo> packageList = null;
        Map<String, PackageInfo> map;

        if (packages != null && packages.get() != null && packages.get().size() != 0) {
            if (packages == null) return null;
            map = packages.get();

            return (map == null || map.size() == 0) ? null : map;
        }

        map = new HashMap<>();
        PackageManager manager = context.getPackageManager();

        if (manager == null) return null;

        try {
            if (collectSignatures) {
                packageList = manager.getInstalledPackages(
                        PackageManager.GET_SIGNATURES | PackageManager.GET_PERMISSIONS
                );
            } else {
                packageList = manager.getInstalledPackages(0);
            }
        } catch (Throwable th) {
            // Forget about it...
        }
        if (packageList == null) return null;

        for (PackageInfo info : packageList) {
            if (info == null ||
                    info.applicationInfo == null ||
                    info.applicationInfo.processName == null) {
                continue;
            }
            map.put(info.applicationInfo.processName, info);
        }

        packages = new WeakReference<>(map);

        return (map.size() == 0) ? null : map;
    }

    /**
     * Get info for a single package from the WeakReferenced packagelist.
     *
     * @param context The Context
     * @param processName The package to get info for.
     * @return info for a single package from the WeakReferenced packagelist.
     */
    public static PackageInfo getPackageInfo(Context context, String processName) {
        Map<String, PackageInfo> map = getPackages(context, true);

        return (map == null || !map.containsKey(processName)) ? null : map.get(processName);
    }

    /**
     * Returns a list of installed packages on the device. Will be called for
     * the first GreenHub sample on a phone, to get signatures for the malware
     * detection project. Later on, single package information is got by
     * receiving the package installed intent.
     *
     * @param context The Context
     * @param filterSystem if true, exclude system packages.
     * @return a list of installed packages on the device.
     */
    public static Map<String, ProcessInfo> getInstalledPackages(Context context, boolean filterSystem) {
        Map<String, PackageInfo> packageMap = getPackages(context, true);
        PackageManager pm = context.getPackageManager();

        if (pm == null) return null;

        Map<String, ProcessInfo> result = new HashMap<>();

        for (Map.Entry<String, PackageInfo> entry : packageMap.entrySet()) {
            try {
                String pkg = entry.getKey();
                PackageInfo pak = entry.getValue();
                if (pak != null) {
                    int vc = pak.versionCode;
                    ApplicationInfo appInfo = pak.applicationInfo;
                    String label = pm.getApplicationLabel(appInfo).toString();
                    // we need application UID to be able to use Android's
                    // TrafficStat API
                    // in order to get the traffic info of a particular app:
                    int appUid = appInfo.uid;
                    // get the amount of transmitted and received bytes by an
                    // app
                    // TODO: disabled for debugging
//					TrafficRecord trafficRecord = getAppTraffic(appUid);

                    int flags = pak.applicationInfo.flags;
                    // Check if it is a system app
                    boolean isSystemApp = (flags & ApplicationInfo.FLAG_SYSTEM) > 0;
                    isSystemApp = isSystemApp || (flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0;
                    if (filterSystem & isSystemApp)
                        continue;
                    if (pak.signatures.length > 0) {
                        List<String> sigList = getSignatures(pak);
                        ProcessInfo pi = new ProcessInfo();
                        pi.setName(pkg);
                        pi.setApplicationLabel(label);
                        pi.setVersionCode(vc);
                        pi.setpId(-1);
                        pi.setSystemApp(isSystemApp);
                        pi.setAppSignatures(sigList);
                        pi.setImportance(Config.IMPORTANCE_NOT_RUNNING);
                        pi.setInstallationPkg(pm.getInstallerPackageName(pkg));
                        pi.setVersionName(pak.versionName);
                        //TODO: disbaled for debugging
//						pi.setTrafficRecord(trafficRecord);
                        result.put(pkg, pi);
                    }
                }
            } catch (Throwable th) {
                // Forget about it...
            }
        }
        return result;
    }

    /**
     * Returns info about an installed package. Will be called when receiving
     * the PACKAGE_ADDED or PACKAGE_REPLACED intent.
     *
     * @param context
     * @param filterSystem
     *            if true, exclude system packages.
     * @return a list of installed packages on the device.
     */
    public static ProcessInfo getInstalledPackage(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        if (pm == null)
            return null;
        PackageInfo pak;
        try {
            pak = pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES | PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        if (pak == null)
            return null;

        ProcessInfo pi = new ProcessInfo();
        int vc = pak.versionCode;
        ApplicationInfo info = pak.applicationInfo;
        String label = pm.getApplicationLabel(info).toString();
        int flags = pak.applicationInfo.flags;
        // Check if it is a system app
        boolean isSystemApp = (flags & ApplicationInfo.FLAG_SYSTEM) > 0;
        isSystemApp = isSystemApp || (flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0;

        if (pak.signatures.length > 0) {
            List<String> sigList = getSignatures(pak);
            pi.setName(pkg);
            pi.setApplicationLabel(label);
            pi.setVersionCode(vc);
            pi.setpId(-1);
            pi.setSystemApp(isSystemApp);
            pi.setAppSignatures(sigList);
            pi.setImportance(Config.IMPORTANCE_NOT_RUNNING);
            pi.setInstallationPkg(pm.getInstallerPackageName(pkg));
            pi.setVersionName(pak.versionName);
        }
        return pi;
    }
}
