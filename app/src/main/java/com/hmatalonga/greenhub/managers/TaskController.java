package com.hmatalonga.greenhub.managers;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Process;
import android.support.annotation.NonNull;

import com.hmatalonga.greenhub.BuildConfig;
import com.hmatalonga.greenhub.managers.sampling.UStats;
import com.hmatalonga.greenhub.models.ui.Task;
import com.hmatalonga.greenhub.util.SettingsUtils;
import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jaredrummler.android.processes.models.Statm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by hugo on 27-05-2017.
 */
public class TaskController {

    private Context mContext;

    private PackageManager mPackageManager;

    public TaskController(@NonNull final Context context) {
        mPackageManager = context.getPackageManager();
        mContext = context;
    }

    public List<Task> getRunningTasks() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return getRunningTasksStandard();
        } else {
            return getRunningTasksNougat();
        }
    }

    public void killApp(final Task task) {
        ActivityManager activityManager = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.killBackgroundProcesses(task.getPackageInfo().packageName);
        for (int pid : task.getProcesses()) {
            Process.killProcess(pid);
        }
    }

    public Drawable iconForApp(PackageInfo packageInfo) {
        try {
            return mPackageManager.getApplicationIcon(packageInfo.packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return packageInfo.applicationInfo.loadIcon(mPackageManager);
        }
    }

    private List<Task> getRunningTasksStandard() {
        List<Task> tasks = new ArrayList<>();
        List<AndroidAppProcess> list = AndroidProcesses.getRunningAppProcesses();

        if (list == null) return tasks;

        for (AndroidAppProcess process : list) {
            /** Exclude the app itself from the list */
            if (process.name.equals(BuildConfig.APPLICATION_ID)) continue;

            PackageInfo packageInfo = getPackageInfo(process, 0);

            if (packageInfo == null) continue;

            /** Remove system apps if necessary */
            if (isSystemApp(packageInfo) && SettingsUtils.isSystemAppsHidden(mContext)){
                continue;
            }

            /** Remove apps without label */
            if (packageInfo.applicationInfo == null) continue;

            String appLabel = packageInfo.applicationInfo.loadLabel(mPackageManager).toString();

            if (appLabel.isEmpty()) continue;

            Task task = getTaskByUid(tasks, process.uid);

            if (task == null) {
                task = new Task(process.uid, process.name);
                task.setPackageInfo(packageInfo);
                task.setLabel(appLabel);
                task.setMemory(getMemoryFromProcess(process));
                task.setIsAutoStart(isAutoStartApp(process.getPackageName()));
                task.setHasBackgroundService(hasBackgroundServices(process.getPackageName()));
                task.getProcesses().add(process.pid);
                tasks.add(task);
            } else {
                task.getProcesses().add(process.pid);
                task.setMemory(task.getMemory() + getMemoryFromProcess(process));
            }
        }

        if (! tasks.isEmpty()) {
            // Dirty quick sorting
            Collections.sort(tasks, new Comparator<Task>() {
                @Override
                public int compare(Task t1, Task t2) {
                    return t1.getLabel().compareTo(t2.getLabel());
                }
            });
        }

        return tasks;
    }

    @TargetApi(21)
    private List<Task> getRunningTasksNougat() {
        final List<Task> tasks = new ArrayList<>();

        AppOpsManager appOps = (AppOpsManager) mContext
                .getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), mContext.getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;

        if (!granted) return tasks;

        final List<UsageStats> list = UStats.getUsageStatsList(mContext);

        for (UsageStats stats : list) {
            String packageName = stats.getPackageName();
            /** Exclude the app itself from the list */
            if (packageName.equals(BuildConfig.APPLICATION_ID)) continue;

            PackageInfo packageInfo = getPackageInfo(packageName, 0);

            if (packageInfo == null) continue;

            /** Remove system apps if necessary */
            if (isSystemApp(packageInfo) && SettingsUtils.isSystemAppsHidden(mContext)){
                continue;
            }

            /** Remove apps without label */
            String appLabel = packageInfo.applicationInfo.loadLabel(mPackageManager).toString();
            if (appLabel.isEmpty()) continue;

            int uid = packageInfo.applicationInfo.uid;
            Task task = getTaskByUid(tasks, uid);

            if (task == null) {
                task = new Task(uid, packageInfo.applicationInfo.processName);
                task.setPackageInfo(packageInfo);
                task.setLabel(appLabel);
                task.setIsAutoStart(isAutoStartApp(packageName));
                task.setHasBackgroundService(hasBackgroundServices(packageName));
                tasks.add(task);
            }
        }

        // Dirty quick sorting
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return t1.getLabel().compareTo(t2.getLabel());
            }
        });

        return tasks;
    }

    private double getMemoryFromProcess(AndroidAppProcess process) {
        double memory = 0;
        try {
            Statm statm = process.statm();
            if (statm != null) {
                // Memory in MB
                memory = statm.getResidentSetSize() / 1024.0 / 1024.0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Math.round(memory * 100.0) / 100.0;
    }

    private boolean isSystemApp(PackageInfo packageInfo) {
        return packageInfo != null &&
                (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    private boolean isAutoStartApp(String packageName) {
        try {
            PackageInfo packageInfo = mPackageManager.
                    getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            if (packageInfo.requestedPermissions != null) {
                for (String permission : packageInfo.requestedPermissions) {
                    if (permission.equals(Manifest.permission.RECEIVE_BOOT_COMPLETED)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean hasBackgroundServices(String packageName) {
        try {
            PackageInfo packageInfo = mPackageManager.
                    getPackageInfo(packageName, PackageManager.GET_SERVICES);
            if (packageInfo.services != null) {
                for (ServiceInfo service : packageInfo.services) {
                    if (service.exported) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private PackageInfo getPackageInfo(AndroidAppProcess process, int flags) {
        PackageInfo packageInfo;
        try {
            packageInfo = process.getPackageInfo(mContext, flags);
        } catch (final PackageManager.NameNotFoundException e) {
            packageInfo = null;
        }
        return packageInfo;
    }

    @TargetApi(21)
    private PackageInfo getPackageInfo(String packageName, int flags) {
        PackageInfo packageInfo;
        try {
            packageInfo = mPackageManager.getPackageInfo(packageName, flags);
        } catch (final PackageManager.NameNotFoundException e) {
            packageInfo = null;
        }
        return packageInfo;
    }

    private Task getTaskByUid(List<Task> tasks, int uid) {
        for (Task task : tasks) {
            if (task.getUid() == uid) {
                return task;
            }
        }
        return null;
    }
}
