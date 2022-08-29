package com.examples.atvizpro.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.TextUtils;

import com.examples.atvizpro.R;
import com.examples.atvizpro.ui.utils.MyUtils;

import java.io.File;
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


    public String generteFileOutput() {
        String filePath = "";
        try {
            File outputFile = new File(MyUtils.getBaseStorageDirectory(), MyUtils.createFileName(".mp4"));

            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            filePath = outputFile.getAbsolutePath();
        }catch (final NullPointerException e) {
            throw new RuntimeException("This app has no permission of writing external storage");
        }

        return filePath;
    }
    public void compression(Activity act, String path, ITranscoding callback){
        outputCacheFile = StorageUtil.getCacheDir() + "/CacheCompress_" + getTimeStamp() + ".mp4";
        String cmd = "ffmpeg -i " + path + " -vcodec libx264 -b:v 10M -vf scale=720:-1 -preset ultrafast " + outputCacheFile;
        new TranscodingAsyncTask(act, cmd, outputCacheFile, callback).execute();
    }

    public void reactCamera(Activity act, String originalPath, String overlayPath, long startTime, long endTime,
                            int sizeCam, int posX, int posY, boolean isMuteAudioOriginal, boolean isMuteAudioOverlay, ITranscoding callback){

        outputVideoPath = generteFileOutput();
        String cmd = "ffmpeg -i " + overlayPath + " -i " + originalPath + " -filter_complex [0]scale="
                + sizeCam + ":-1[overlay];[1][overlay]overlay="
                + "enable='between(t," + parseSecond2Ms(startTime) + "," + parseSecond2Ms(endTime) + ")':x="+posX+":y="+posY+";[0:a][1:a]amix -preset ultrafast "+outputVideoPath;

        new TranscodingAsyncTask(act, cmd, outputVideoPath, callback).execute();

    }


    public void commentaryAudio(Activity act, String originalVideoPath, String audioPath, ITranscoding callback){
        outputVideoPath = generteFileOutput();
        String cmd = "ffmpeg -i "+ originalVideoPath +" -i "+ audioPath +" -vcodec copy -filter_complex amix -map 0:v -map 0:a -map 1:a "+outputVideoPath;

        new TranscodingAsyncTask(act, cmd, outputVideoPath, callback).execute();

    }



    public void trimVideo(Activity act, String originalVideoPath, long startTime, long endTime, ITranscoding callback){
        outputVideoPath = generteFileOutput();
        String cmd = "ffmpeg -ss "+ parseSecond2Ms(startTime) + " -i "+ originalVideoPath + " -to " +parseSecond2Ms(endTime) + " -c:v copy -c:a copy " +outputVideoPath;

        new TranscodingAsyncTask(act, cmd, outputVideoPath, callback).execute();
    }

    public void addText(Activity act, String originalVideoPath, String text, String color, String size, String position, ITranscoding callback){
        outputVideoPath = generteFileOutput();
//        String fontPath = new File(String.valueOf(R.font.roboto_bold)).getAbsolutePath();
//        String fontPath = "/storage/emulated/0/MarvelEditor/.Font/roboto_black.ttf";
        String cmd = "ffmpeg -i "+ originalVideoPath + " -vf drawtext=text=" + text + ":fontcolor=#ffffff:fontsize=40:" + position + " -c:v libx264 -c:a copy  " +outputVideoPath;


        new TranscodingAsyncTask(act, cmd, outputVideoPath, callback).execute();
    }


    public void changeSpeed(Activity act, String originalVideoPath, String text, String color, String size, String position, ITranscoding callback){
        outputVideoPath = generteFileOutput();
        String fontPath = new File(String.valueOf(R.font.roboto_bold)).getAbsolutePath();
        String cmd = "ffmpeg -i "+ originalVideoPath + " -vf \"drawtext=fontfile= "+ fontPath + ": text=\'" + text+ "\': fontcolor=" + color
                + ": fontsize=" + size+ ": " + position + " -c:v libx264 -c:a copy -movflags +faststart" +outputVideoPath;


        new TranscodingAsyncTask(act, cmd, outputVideoPath, callback).execute();
    }

    public void addImage(Activity act, String originalVideoPath, String imagePath, String position, ITranscoding callback){
        outputVideoPath = generteFileOutput();

        String cmd = "ffmpeg -i " +  originalVideoPath + " -i " + imagePath+ " -filter_complex " + position +  " -c:a copy " + outputVideoPath;

        new TranscodingAsyncTask(act, cmd, outputVideoPath, callback).execute();
    }


    public void flipHorizontal (Activity act, String originalVideoPath, ITranscoding callback){
        outputVideoPath = generteFileOutput();

        String cmd = "ffmpeg -i " +  originalVideoPath + " -vf hflip " + outputVideoPath;

        new TranscodingAsyncTask(act, cmd, outputVideoPath, callback).execute();
    }


    public String parseSecond2Ms(long second) {
        String ms = "";
        return ms + second/1000 + "." + second % 1000;
    }}
