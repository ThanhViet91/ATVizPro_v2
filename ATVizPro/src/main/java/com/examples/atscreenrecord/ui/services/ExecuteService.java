package com.examples.atscreenrecord.ui.services;

import static com.examples.atscreenrecord.App.CHANNEL_ID;
import static com.examples.atscreenrecord.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atscreenrecord.utils.TranscodingAsyncTask.ERROR_CODE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.request.target.ThumbnailImageViewTarget;
import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.model.VideoProfileExecute;
import com.examples.atscreenrecord.ui.activities.MainActivity;
import com.examples.atscreenrecord.ui.activities.ResultVideoFinishActivity;
import com.examples.atscreenrecord.ui.activities.TranslucentActivity;
import com.examples.atscreenrecord.ui.utils.MyUtils;
import com.examples.atscreenrecord.utils.VideoUtil;

import java.util.Random;


public class ExecuteService extends Service {


    private static final String ACTION_CANCEL = "ACTION_CANCEL";
    private static int NOTIFICATION_ID = 9;
    String originalVideoPath, cameraCachePath;
    long startTime, endTime, duration = 10000;
    int posX, posY, camSize;

    private boolean finishExecute = false;
    long countDownInterval = 1000;
    int progress = 0;
    CountDownTimer countDownTimer;
    public int generateProgress(int lastProgress) {
        return Math.min(99, (int)(lastProgress + new Random().nextInt(1+(int)(100*countDownInterval/duration))));
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (MyUtils.ACTION_CANCEL_PROCESSING.equals(intent.getAction())) {
                countDownTimer.cancel();
                VideoUtil.getInstance().cancelProcess();
                stopService();
                return START_NOT_STICKY;
            }
        }

        duration = intent.getLongExtra("bundle_video_execute_time", 0);
        NOTIFICATION_ID = (int) duration;
        Bundle bundle = intent.getExtras();
        VideoProfileExecute videoProfileExecute = null;
        if (bundle != null) {
            videoProfileExecute = (VideoProfileExecute) bundle.get("package_video_profile");

            originalVideoPath = videoProfileExecute.getOriginalVideoPath();
            cameraCachePath = videoProfileExecute.getOverlayVideoPath();
            startTime = videoProfileExecute.getStartTime();
            endTime = videoProfileExecute.getEndTime();
            posX = videoProfileExecute.getPosX();
            posY = videoProfileExecute.getPosY();
            camSize = videoProfileExecute.getCamSize();

        }
        int type = 0;
        if (videoProfileExecute != null) {
            type = videoProfileExecute.getType();
        }
        createNotification(type);
        System.out.println("thanhlv onStartCommand .......... " + duration);
        if (duration < 10000) duration = 10000;

        if (duration < 1000*60*10) {
            countDownInterval = 1000;
        } else countDownInterval = 2000;

        countDownTimer = new CountDownTimer(duration, 1000) {
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


        if (videoProfileExecute.getType() == MyUtils.TYPE_REACT_VIDEO)
        {
            flipCamera(cameraCachePath);
            System.out.println("thanhlv videoProfileExecute.getType() == MyUtils.TYPE_REACT_VIDEO");
        }
        if (videoProfileExecute.getType() == MyUtils.TYPE_COMMENTARY_VIDEO)
        {
            commentaryAudio(originalVideoPath, cameraCachePath);
            System.out.println("thanhlv videoProfileExecute.getType() == MyUtils.TYPE_COMMENTARY_VIDEO");
        }

        return START_NOT_STICKY;
    }

    private void commentaryAudio(String videoPath, String audioPath) {
        VideoUtil.getInstance().commentaryAudio(videoPath, audioPath, new VideoUtil.ITranscoding() {
                @Override
                public void onStartTranscoding(String outPath) {
                }

                @RequiresApi(api = Build.VERSION_CODES.S)
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
            });

    }

    private void flipCamera(String cameraCahePath) {
        VideoUtil.getInstance().flipHorizontal(cameraCahePath, new VideoUtil.ITranscoding() {
            @Override
            public void onStartTranscoding(String outputCachePath) {
            }

            @Override
            public void onFinishTranscoding(String code) {
                // finish flip cam and do react cam into video then
                if (!code.equals(ERROR_CODE))
                    executeFFmpegReactCam(code);
            }
        });
    }

    private String finalVideoCachePath = "";
    public void executeFFmpegReactCam(String overlayVideoPath) {
        VideoUtil.getInstance().reactCamera(originalVideoPath, overlayVideoPath, startTime, endTime, camSize,
                posX, posY, false, false, new VideoUtil.ITranscoding() {
                    @Override
                    public void onStartTranscoding(String outputCachePath) {
                    }

                    @RequiresApi(api = Build.VERSION_CODES.S)
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
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void updatePendingIntent(String finalVideoCachePath) {

        Intent intent = new Intent(this, ResultVideoFinishActivity.class);
        intent.setAction(MyUtils.ACTION_END_REACT);
        intent.putExtra(KEY_PATH_VIDEO, finalVideoCachePath);
        System.out.println("thanhlv updatePendingIntent "+finalVideoCachePath);
        @SuppressLint({"UnspecifiedImmutableFlag", "WrongConstant"})
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, intent, PendingIntent.FLAG_MUTABLE);
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

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void createNotification(int type) {

        Intent intent = new Intent(this, TranslucentActivity.class);
        @SuppressLint({"UnspecifiedImmutableFlag", "WrongConstant"})
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, intent, PendingIntent.FLAG_MUTABLE);

        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_layout);

        if (type == MyUtils.TYPE_REACT_VIDEO) notificationLayoutExpanded.setTextViewText(R.id.des, "React in processing");
        if (type == MyUtils.TYPE_COMMENTARY_VIDEO) notificationLayoutExpanded.setTextViewText(R.id.des, "Commentary in processing");
        notificationLayoutExpanded.setImageViewResource(R.id.ic_app, R.drawable.ic_app);

        notificationLayoutExpanded.setOnClickPendingIntent(R.id.btn_cancel_notification, pendingIntent);


        //Set notification information
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(this, CHANNEL_ID);
        } else
            notificationBuilder = new Notification.Builder(getApplicationContext());
        notificationBuilder
                .setOngoing(true)
                .setCustomBigContentView(notificationLayoutExpanded)
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
            }
            stopSelf();
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
