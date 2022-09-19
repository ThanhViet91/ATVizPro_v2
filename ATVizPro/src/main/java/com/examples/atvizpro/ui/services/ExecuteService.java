package com.examples.atvizpro.ui.services;

import static com.examples.atvizpro.App.CHANNEL_ID;
import static com.examples.atvizpro.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atvizpro.utils.TranscodingAsyncTask.ERROR_CODE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;

import com.examples.atvizpro.R;
import com.examples.atvizpro.model.VideoReactCamExecute;
import com.examples.atvizpro.ui.activities.MainActivity;
import com.examples.atvizpro.ui.activities.ReactCamFinishActivity;
import com.examples.atvizpro.utils.VideoUtil;

import java.util.Random;


public class ExecuteService extends Service {


    private static int NOTIFICATION_ID = 9;
    String originalVideoPath, cameraCachePath;
    long startTime, endTime, duration = 10000;
    int posX, posY, camSize;

    private boolean finishExecute = false;
    long countDownInterval = 1000;
    int progress = 0;

    public int generateProgress(int lastProgress) {
        return Math.min(99, (int)(lastProgress + new Random().nextInt(1+(int)(100*countDownInterval/duration))));
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        duration = intent.getLongExtra("bundle_video_react_time", 0);
        NOTIFICATION_ID = (int) duration;

        createNotification();
        System.out.println("thanhlv onStartCommand .......... " + duration);
        if (duration < 10000) duration = 10000;

        if (duration < 1000*60*10) {
            countDownInterval = 1000;
        } else countDownInterval = 2000;

        CountDownTimer countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long l) {
                if (!finishExecute) {
                    progress = generateProgress(100-(int)(100*l/duration));
                    System.out.println("thanhlv progressssssssss == "+l +" // "+progress);
                    notificationBuilder.setProgress(100, progress, false);
                    notificationBuilder.setContentText("In progress: " + progress + "%");
                    startForeground(NOTIFICATION_ID, notification);
                }
            }

            @Override
            public void onFinish() {
                if (!finishExecute) {
                    notificationBuilder.setProgress(0, 0, true);
                    notificationBuilder.setContentText("In progress: 99%");
                    startForeground(NOTIFICATION_ID, notification);
                }
            }
        };
        countDownTimer.start();

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            VideoReactCamExecute videoReactCamExecute = (VideoReactCamExecute) bundle.get("package_video_react");

            originalVideoPath = videoReactCamExecute.getOriginalVideoPath();
            cameraCachePath = videoReactCamExecute.getOverlayVideoPath();
            startTime = videoReactCamExecute.getStartTime();
            endTime = videoReactCamExecute.getEndTime();
            posX = videoReactCamExecute.getPosX();
            posY = videoReactCamExecute.getPosY();
            camSize = videoReactCamExecute.getCamSize();
            flipCamera(cameraCachePath);
        }

        return START_NOT_STICKY;
    }

    private void flipCamera(String cameraCahePath) {
        new VideoUtil().flipHorizontal(cameraCahePath, new VideoUtil.ITranscoding() {
            @Override
            public void onStartTranscoding(String outputCachePath) {
            }

            @Override
            public void onFinishTranscoding(String code) {
                // finish flip cam and do react cam into video then
                if (!code.equals(ERROR_CODE))
                    executeFFmpegReactCam(code);
            }

            @Override
            public void onUpdateProgressTranscoding(int progress) {
            }
        });
    }

    private String finalVideoCachePath = "";
    public void executeFFmpegReactCam(String overlayVideoPath) {




        new VideoUtil().reactCamera(originalVideoPath, overlayVideoPath, startTime, endTime, camSize,
                posX, posY, false, false, new VideoUtil.ITranscoding() {
                    @Override
                    public void onStartTranscoding(String outputCachePath) {
                    }

                    @Override
                    public void onFinishTranscoding(String code) {
                        if (!code.equals(ERROR_CODE)) {
                            finishExecute = true;
                            notificationBuilder.setProgress(0, 0, false);
                            notificationBuilder.setContentText("In progress: 100%");
                            finalVideoCachePath = code;

                            updatePendingIntent(finalVideoCachePath);
                            startForeground(NOTIFICATION_ID, notification);

                        }
                    }

                    @Override
                    public void onUpdateProgressTranscoding(int progress) {
                    }
                });
    }

    private void updatePendingIntent(String finalVideoCachePath) {

        Intent intent = new Intent(this, ReactCamFinishActivity.class);
        intent.putExtra(KEY_PATH_VIDEO, finalVideoCachePath);
        intent.putExtra("from_notification", true);
        System.out.println("thanhlv updatePendingIntent "+finalVideoCachePath);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        createNotification();
    }

    Notification.Builder notificationBuilder = null;
    Notification notification;

    private void createNotification() {

        Intent intent = new Intent(this, MainActivity.class);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Set notification information
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(this, CHANNEL_ID);
        } else
            notificationBuilder = new Notification.Builder(getApplicationContext());
        notificationBuilder
                .setOngoing(true)
                .setVibrate(new long[]{0L})
                .setContentTitle("AT Screen Recorder")
                .setContentText("In progress...")
                .setSmallIcon(R.drawable.ic_app)
                .setContentIntent(pendingIntent)
                .setProgress(100, 0, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            notificationBuilder.setCategory(Notification.CATEGORY_NAVIGATION);
        }
        //Send the notification:
        notification = notificationBuilder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void stopService() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopForeground(true);
                stopSelf();
            } else {
                stopSelf();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService();
    }
}
