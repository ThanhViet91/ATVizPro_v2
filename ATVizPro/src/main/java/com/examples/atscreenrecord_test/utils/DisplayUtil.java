package com.examples.atscreenrecord_test.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;

import com.examples.atscreenrecord_test.App;

import java.util.List;

public class DisplayUtil {

    public static DisplayMetrics getDisplayMetrics(){

        return App.getAppContext().getResources().getDisplayMetrics();
    }

    public static void info() {

        float xdpi = getDisplayMetrics().xdpi;
        float density = getDisplayMetrics().density;
        int densityDpi = getDisplayMetrics().densityDpi;
        float scaledDensity = getDisplayMetrics().scaledDensity;
        int ww = getDisplayMetrics().widthPixels;
    }
    public static float dpToPx(float dp) {
        return dp * getDisplayMetrics().density;

    }

    public static int dpToPx(int dp) {
        return (int) (dp * getDisplayMetrics().density + 0.5f);
    }

    public static float pxToDp(float px) {
        return px / getDisplayMetrics().density;
    }

    public static int pxToDp(int px) {
        return (int) (px / getDisplayMetrics().density + 0.5f);
    }

    public static float spToPx(float sp) {
        return sp * getDisplayMetrics().scaledDensity;
    }

    public static int spToPx(int sp) {
        return (int) (getDisplayMetrics().scaledDensity < 2 ? (sp * getDisplayMetrics().scaledDensity + 0.5f) : sp);
    }

    public static float pxToSp(float px) {
        return px / getDisplayMetrics().scaledDensity;
    }

    public static int pxToSp(int px) {
        return (int) (getDisplayMetrics().scaledDensity < 2 ? (px / getDisplayMetrics().scaledDensity + 0.5f) : px);
    }

    public static int getDeviceWidth() {
        return App.getAppContext().getResources().getDisplayMetrics().widthPixels;
    }

    public static float getDeviceWidthDpi() {
        return App.getAppContext().getResources().getDisplayMetrics().widthPixels / App.getAppContext().getResources().getDisplayMetrics().density;
    }

    public static int getDeviceHeight(){
        return App.getAppContext().getResources().getDisplayMetrics().heightPixels;
    }

    @SuppressLint("WrongConstant")
    public static boolean hasAppInstalled(String pkgName) {
        try {
            App.getAppContext().getPackageManager().getPackageInfo(pkgName, PackageManager.PERMISSION_GRANTED);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isAppRunInBackground() {
        ActivityManager activityManager = (ActivityManager) App.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(App.getAppContext().getPackageName())) {
                // return true -> Run in background
                // return false - > Run in foreground
                return appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
            }
        }
        return false;
    }
}
