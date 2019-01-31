package com.atmsoft.smartattendee;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Build;

import java.util.List;

import androidx.annotation.RequiresApi;

class Utils {
    @RequiresApi(api = Build.VERSION_CODES.N)
    static boolean isProcessRunning(Context context, String processName) {
        if (null == processName) {
            return false;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<RunningAppProcessInfo> lists = activityManager.getRunningAppProcesses();
        return lists.stream().anyMatch(p -> {
            if (null == p || null == p.processName) {
                return false;
            }
            return processName.equals(p.processName);
        });
    }
}
