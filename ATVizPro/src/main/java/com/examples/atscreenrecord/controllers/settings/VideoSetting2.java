package com.examples.atscreenrecord.controllers.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;

public class VideoSetting2 {
    public static final int ORIENTATION_PORTRAIT = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;

    int mWidth, mHeight, mFPS, mBirate, mOrientation;
    public static VideoSetting2 VIDEO_PROFILE_SSD = new VideoSetting2(360, 480, 30, 1200 * 1024, ORIENTATION_PORTRAIT);
    public static VideoSetting2 VIDEO_PROFILE_SD = new VideoSetting2(480, 640, 30, 1200 * 1024, ORIENTATION_PORTRAIT);
    public static VideoSetting2 VIDEO_PROFILE_HD = new VideoSetting2(720, 1280, 30, 2000 * 1024, ORIENTATION_PORTRAIT);
    public static VideoSetting2 VIDEO_PROFILE_FHD = new VideoSetting2(1080, 1920, 30, 4000 * 1024, ORIENTATION_PORTRAIT);
    private String mOutputPath;

    public VideoSetting2() {
    }

    public VideoSetting2(int width, int height, int FPS, int bitrate, int orientation) {
        mWidth = width;
        mHeight = height;
        mFPS = FPS;
        mBirate = bitrate;
        mOrientation = orientation;
    }

    public static String getFormattedDuration(long duration) {
        long s = duration;
        return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public void setHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    public int getFPS() {
        return mFPS;
    }

    public int getBitrate() {
        return mBirate;
    }

    public void setFPS(int _fps) {
        mFPS = _fps;
    }

    public void setBitrate(int bitrate) {
        mBirate = bitrate;
    }

    public void setOrientation(int orientation){
        mOrientation = orientation;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public String getOutputPath() {
        return mOutputPath;
    }

    public String getResolutionString() {
        return mWidth+"x"+mHeight;
    }

    public void setOutputPath(String outputFile) {
        mOutputPath = outputFile;
    }

    @Override
    public String toString() {
        return "VideoSetting{" +
                "mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                ", mFPS=" + mFPS +
                ", mBirate=" + mBirate +
                ", mOrientation=" + mOrientation +
                '}';
    }


    public String getTitle() {
        if(!TextUtils.isEmpty(mOutputPath)){
            return mOutputPath.substring(mOutputPath.lastIndexOf('/'));
        }
        else return "Title ERROR";
    }
}
