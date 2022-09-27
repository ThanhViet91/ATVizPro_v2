package com.examples.atscreenrecord.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Handler;

import com.examples.atscreenrecord.App;
import com.examples.atscreenrecord.ui.utils.MyUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoUtil {

    @SuppressLint("SimpleDateFormat")
    public String getTimeStamp() {
        return new SimpleDateFormat("MMdd_HHmmss").format(new Date());
    }

    private String outputVideoPath;
    private final Context context = App.getAppContext();

    public interface ITranscoding {
        void onStartTranscoding(String outputCachePath);
        void onFinishTranscoding(String code);
    }

    public static String generateFileOutput() {
        String filePath = "";
        try {
            File outputFile = new File(MyUtils.getBaseStorageDirectory(), MyUtils.createFileName(".mp4"));
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            filePath = outputFile.getAbsolutePath();
        } catch (final NullPointerException e) {
            throw new RuntimeException("This app has no permission of writing external storage");
        }
        return filePath;
    }

    public void compression(String path, ITranscoding callback) {
        String outputCacheFile = StorageUtil.getCacheDir() + "/CacheCompress_" + getTimeStamp() + ".mp4";
        String cmd = "ffmpeg -i " + path + " -vcodec libx264 -b:v 10M -vf scale=720:-1 -preset ultrafast " + outputCacheFile;
        new TranscodingAsyncTask(context, cmd, outputCacheFile, callback).execute();
    }

    public void reactCamera(String originalPath, String overlayPath, long startTime, long endTime,
                            int sizeCam, int posX, int posY, boolean isMuteAudioOriginal, boolean isMuteAudioOverlay, ITranscoding callback) {
        outputVideoPath = StorageUtil.getCacheDir() + "/CacheReactCam_" + getTimeStamp() + ".mp4";
        String cmd = "ffmpeg -i " + overlayPath + " -i " + originalPath + " -filter_complex [0]scale="
                + sizeCam + ":-1[overlay];[1][overlay]overlay="
                + "enable='between(t," + parseSecond2Ms(startTime) + "," + parseSecond2Ms(endTime) + ")':x=" + posX + ":y=" + posY + ";[0:a][1:a]amix -preset ultrafast " + outputVideoPath;
        new TranscodingAsyncTask(context, cmd, outputVideoPath, callback).execute();
    }


    public void commentaryAudio(Activity act, String originalVideoPath, String audioPath, ITranscoding callback) {
        outputVideoPath = StorageUtil.getCacheDir() + "/CacheCommentaryAudio_" + getTimeStamp() + ".mp4";
        String cmd = "ffmpeg -i " + originalVideoPath + " -i " + audioPath + " -vcodec copy -filter_complex amix -map 0:v -map 0:a -map 1:a -preset ultrafast " + outputVideoPath;
        new TranscodingAsyncTask(act, cmd, outputVideoPath, callback).execute();
    }


    public void trimVideo(Activity act, String originalVideoPath, long startTime, long endTime, ITranscoding callback) {
        outputVideoPath = StorageUtil.getCacheDir() + "/CacheTrimVideo_" + getTimeStamp() + ".mp4";
        String cmd = "ffmpeg -ss " + parseSecond2Ms(startTime) + " -i " + originalVideoPath + " -to " + parseSecond2Ms(endTime) + " -c:v copy -c:a copy -preset ultrafast " + outputVideoPath;
        new TranscodingAsyncTask(act, cmd, outputVideoPath, callback).execute();
    }

    public void addText(Activity act, String originalVideoPath, String text, int color, int size, String position, ITranscoding callback) {
        outputVideoPath = StorageUtil.getCacheDir() + "/CacheAddText_" + getTimeStamp() + ".mp4";
        textAsBitmap(text, size, color, null);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String cmd = "ffmpeg -i " + originalVideoPath + " -i " + overlayImagePath.getAbsolutePath() + " -filter_complex [0:v][1:v]overlay=" + position+ " -preset ultrafast " + outputVideoPath;
                new TranscodingAsyncTask(act, cmd, outputVideoPath, callback).execute();
            }
        }, 500);
    }

    private File overlayImagePath;
    public void textAsBitmap(String text, float textSize, int textColor, Typeface typeface) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setAlpha(255);
        if (typeface != null) paint.setTypeface(typeface);
        paint.setTextAlign(Paint.Align.LEFT);

        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        canvas.drawText(text, 0, baseline, paint);
        String extStorageDirectory = App.getAppContext().getExternalFilesDir(Environment.DIRECTORY_DCIM).toString();
        overlayImagePath = new File(extStorageDirectory, "text.png");
        System.out.println("thanhlv textAsBitmap "+overlayImagePath.getAbsolutePath());
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(overlayImagePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        image.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        try {
            if (outStream != null) {
                outStream.flush();
            }

            if (outStream != null) {
                outStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeSpeed(Activity act, String originalVideoPath, String speed, ITranscoding callback) {
        outputVideoPath = StorageUtil.getCacheDir() + "/CacheChangeSpeed_" + getTimeStamp() + ".mp4";
        String cmd = "ffmpeg -i " + originalVideoPath + " -filter_complex [0:v]setpts=" + convertSpeed(speed) + "*PTS[v];[0:a]atempo="+ speed +"[a] -map [v] -map [a] -preset ultrafast " + outputVideoPath;
        new TranscodingAsyncTask(act, cmd, outputVideoPath, callback).execute();
    }

    @SuppressLint("DefaultLocale")
    private String convertSpeed(String speed) {
        float ss = Float.parseFloat(speed);
        String ff = String.format("%.2f", 1f/ss);
        return ff.replace(",", ".");
    }

    public void addImage(Activity act, String originalVideoPath, String imagePath, String position, ITranscoding callback) {
        outputVideoPath = StorageUtil.getCacheDir() + "/CacheAddImage_" + getTimeStamp() + ".mp4";
        String cmd = "ffmpeg -i " + originalVideoPath + " -i " + imagePath + " -filter_complex [0:v][1:v]overlay=" + position+ " -preset ultrafast " + outputVideoPath;
        new TranscodingAsyncTask(act, cmd, outputVideoPath, callback).execute();
    }

    public void flipHorizontal(String originalVideoPath, ITranscoding callback) {
        outputVideoPath = StorageUtil.getCacheDir() + "/CacheCamera_hflip_" + getTimeStamp() + ".mp4";
        String cmd = "ffmpeg -i " + originalVideoPath + " -vf hflip -preset ultrafast " + outputVideoPath;
        new TranscodingAsyncTask(context, cmd, outputVideoPath, callback).execute();
    }

    public void flipVertical(String originalVideoPath, ITranscoding callback) {
        outputVideoPath = StorageUtil.getCacheDir() + "/CacheCamera_vflip_" + getTimeStamp() + ".mp4";
        String cmd = "ffmpeg -i " + originalVideoPath + " -vf vflip -preset ultrafast " + outputVideoPath;
        new TranscodingAsyncTask(context, cmd, outputVideoPath, callback).execute();
    }

    public void rotate(String originalVideoPath, int angle, ITranscoding callback) {
        outputVideoPath = StorageUtil.getCacheDir() + "/CacheCamera_rotate_" + getTimeStamp() + ".mp4";
        String cmd = "ffmpeg -i "+ originalVideoPath + " -vf transpose="+angle+" -preset ultrafast " + outputVideoPath;
        new TranscodingAsyncTask(context, cmd, outputVideoPath, callback).execute();
    }

    public String parseSecond2Ms(long second) {
        String ms = "";
        return ms + second / 1000 + "." + second % 1000;
    }
}