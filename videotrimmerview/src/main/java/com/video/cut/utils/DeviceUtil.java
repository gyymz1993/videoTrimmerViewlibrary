package com.video.cut.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;

import com.video.cut.BaseUtils;

import java.util.List;

public final class DeviceUtil {

    public static int getDeviceWidth() {
        return BaseUtils.getContext().getResources().getDisplayMetrics().widthPixels;
    }

    public static int getDeviceHeight() {
        return BaseUtils.getContext().getResources().getDisplayMetrics().heightPixels;
    }

    public static boolean hasAppInstalled(String pkgName) {
        try {
            BaseUtils.getContext().getPackageManager().getPackageInfo(pkgName, PackageManager.PERMISSION_GRANTED);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isAppRunInBackground() {
        ActivityManager activityManager = (ActivityManager) BaseUtils.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.CUPCAKE) {
            appProcesses = activityManager.getRunningAppProcesses();
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(BaseUtils.getContext().getPackageName())) {
                // return true -> Run in background
                // return false - > Run in foreground
                return appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
            }
        }
        return false;
    }
}
