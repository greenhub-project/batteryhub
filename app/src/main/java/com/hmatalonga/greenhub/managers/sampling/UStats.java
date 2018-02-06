package com.hmatalonga.greenhub.managers.sampling;

import android.annotation.TargetApi;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import java.util.Calendar;
import java.util.List;

/**
 * Created by hugo on 27-05-2017.
 */
public class UStats {
    @TargetApi(21)
    public static List<UsageStats> getUsageStatsList(final Context context){
        UsageStatsManager usm = getUsageStatsManager(context);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        long startTime = calendar.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        return usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
    }

    @SuppressWarnings("ResourceType")
    private static UsageStatsManager getUsageStatsManager(final Context context){
        return (UsageStatsManager) context.getSystemService("usagestats");
    }
}
