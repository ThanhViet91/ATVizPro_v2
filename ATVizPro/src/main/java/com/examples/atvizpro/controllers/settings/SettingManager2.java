package com.examples.atvizpro.controllers.settings;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.examples.atvizpro.Constants;
import com.examples.atvizpro.Core;
import com.examples.atvizpro.R;
import com.examples.atvizpro.ui.utils.MyUtils;

public class SettingManager2 {

    public static int getCountdown(Context context){
        SharedPreferences preferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);
        String key = getStringRes(context, R.string.setting_common_countdown);
        String defValue = getStringRes(context, R.string.default_setting_countdown);
        String res = preferences.getString(key, defValue);
        return Integer.parseInt(res);
    }

    public static void setVideoBitrate(Context context, String value){
        SharedPreferences preferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);
        String key = getStringRes(context, R.string.setting_video_bitrate);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getVideoBitrate(Context context){
        SharedPreferences preferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);
        String key = getStringRes(context, R.string.setting_video_bitrate);
        String defValue = getStringRes(context, R.string.default_setting_bitrate);
        return preferences.getString(key, defValue);
    }

    public static void setVideoResolution(Context context, String value){
        SharedPreferences preferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);
        String key = getStringRes(context, R.string.setting_video_resolution);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getVideoResolution(Context context){
        SharedPreferences preferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);
        String key = getStringRes(context, R.string.setting_video_resolution);
        String defValue = getStringRes(context, R.string.default_setting_resolution);

        return preferences.getString(key, defValue);
    }

    public static void setVideoFPS(Context context, String value){
        SharedPreferences preferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);
        String key = getStringRes(context, R.string.setting_video_fps);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getVideoFPS(Context context){
        SharedPreferences preferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);
        String key = getStringRes(context, R.string.setting_video_fps);
        String defValue = getStringRes(context, R.string.default_setting_fps);
        return preferences.getString(key, defValue);
//        return Integer.parseInt(res);
    }


    @NonNull
    public static String getStringRes(Context context, int resId) {
        return context.getResources().getString(resId);
    }

    public static VideoSetting2 getVideoProfile(Context context) {
        VideoSetting2 videoSetting = null;

        String resolution = getVideoResolution(context);


        switch (resolution){
            case "SSD": videoSetting = VideoSetting2.VIDEO_PROFILE_SSD; break;
            case "SD": videoSetting = VideoSetting2.VIDEO_PROFILE_SD; break;
            case "HD": videoSetting = VideoSetting2.VIDEO_PROFILE_HD; break;
            case "FHD": videoSetting =  VideoSetting2.VIDEO_PROFILE_FHD; break;
            default: videoSetting = VideoSetting2.VIDEO_PROFILE_HD; break;
        }

        String fps = getVideoFPS(context);

        switch (fps) {
            case "60fps":
                videoSetting.setFPS(60);
                break;
            case "50fps":
                videoSetting.setFPS(50);
                break;
            case "30fps":
                videoSetting.setFPS(30);
                break;
            case "25fps":
                videoSetting.setFPS(25);
                break;
            case "24fps":
                videoSetting.setFPS(24);
                break;

        }

        String bitrate = getVideoBitrate(context);

        switch (bitrate) {
            case "12Mbps":
                videoSetting.setBitrate(12000);
                break;
            case "8Mbps":
                videoSetting.setBitrate(8000);
                break;
            case "6Mbps":
                videoSetting.setBitrate(6000);
                break;
            case "5Mbps":
                videoSetting.setBitrate(5000);
                break;
            case "4Mbps":
                videoSetting.setBitrate(4000);
                break;
            case "3Mbps":
                videoSetting.setBitrate(3000);
                break;
            case "2Mbps":
                videoSetting.setBitrate(2000);
                break;
            case "1Mbps":
                videoSetting.setBitrate(1000);
                break;
        }




        return videoSetting;
    }
}
