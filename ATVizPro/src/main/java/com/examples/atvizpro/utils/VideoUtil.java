package com.examples.atvizpro.utils;

import android.annotation.SuppressLint;
import android.app.Activity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoUtil {

    String inputVideoPath = "/sdcard/thanhtest1.mp4";

    @SuppressLint("SimpleDateFormat")
    public String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    String outputVideoPath;
    String outputCacheFile;

    public interface ITranscoding {
        void onStartTranscoding(String outputCachePath);
        void onFinishTranscoding(String code);
        void onUpdateProgressTranscoding(int progress);
    }


    public void compression(Activity act, String path, ITranscoding callback){
        outputCacheFile = StorageUtil.getCacheDir() + "/CacheCompress_" + getTimeStamp() + ".mp4";
        String cmd = "ffmpeg -i " + path + " -vcodec libx264 -b:v 10M -vf scale=720:-1 -preset ultrafast " + outputCacheFile;
        new TranscodingAsyncTask(act, cmd, outputCacheFile, callback).execute();
    }

    public void reactCamera(Activity act, String originalPath, String overlayPath, long startTime, long endTime,
                            int sizeCam, int posX, int posY, boolean isMuteAudioOriginal, boolean isMuteAudioOverlay, ITranscoding callback){
        String outputVideoPath = "/sdcard/thanhtest"+ getTimeStamp() +".mp4";
        String cmd = "ffmpeg -i " + overlayPath + " -i " + originalPath + " -filter_complex [0]scale="
                + sizeCam + ":-1[overlay];[1][overlay]overlay="
                + "enable='between(t," + parseSecond2Ms(startTime) + "," + parseSecond2Ms(endTime) + ")':x="+posX+":y="+posY+";[0:a][1:a]amix -preset ultrafast "+outputVideoPath;

        new TranscodingAsyncTask(act, cmd, outputVideoPath, callback).execute();

    }

    public String parseSecond2Ms(long second) {
        String ms = "";
        return ms + second/1000 + "." + second % 1000;
    }}
