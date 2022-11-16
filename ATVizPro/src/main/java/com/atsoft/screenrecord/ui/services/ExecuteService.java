package com.atsoft.screenrecord.ui.services;

import static com.atsoft.screenrecord.App.CHANNEL_ID;
import static com.atsoft.screenrecord.ui.services.recording.RecordingService.NOTIFY_MSG_RECORDING_DONE;
import static com.atsoft.screenrecord.utils.TranscodingAsyncTask.ERROR_CODE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;

import com.atsoft.screenrecord.App;
import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.model.VideoProfileExecute;
import com.atsoft.screenrecord.ui.activities.PopUpResultVideoTranslucentActivity;
import com.atsoft.screenrecord.ui.activities.TranslucentActivity;
import com.atsoft.screenrecord.ui.utils.MyUtils;
import com.atsoft.screenrecord.utils.FFmpegUtil;

import java.util.Random;


public class ExecuteService extends Service {

    private static final int NOTIFICATION_ID = 1509;
    public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
    public static final String KEY_VIDEO_PATH = "KEY_VIDEO_PATH";

    String originalVideoPath, cameraCachePath;
    long startTime, endTime, expectTime = 10000;
    int posX, posY, camSize;

    private boolean finishExecute = false;
    long countDownInterval = 1000;
    int progress = 0;
    CountDownTimer countDownTimer;

    public int generateProgress(int lastProgress) {
        return Math.min(99, lastProgress + new Random().nextInt(1 + (int) (100 * countDownInterval / expectTime)));
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getAction() != null) {

                if (MyUtils.ACTION_CANCEL_PROCESSING.equals(intent.getAction())) {
                    if (countDownTimer != null) countDownTimer.cancel();
                    FFmpegUtil.getInstance().cancelProcess();
                    stopService();
                    return START_NOT_STICKY;
                }
                if (MyUtils.ACTION_EXIT_SERVICE.equals(intent.getAction())) {
                    stopService();
                    return START_NOT_STICKY;
                }

                createNotification(intent.getAction());
            }
            handleData(intent);
        }
        return START_NOT_STICKY;
    }

    private void handleData(Intent intent) {
        expectTime = intent.getLongExtra("bundle_video_execute_time", 0);
        Bundle bundle = intent.getExtras();
        VideoProfileExecute videoProfileExecute;
        if (bundle != null) {
            videoProfileExecute = (VideoProfileExecute) bundle.get(MyUtils.KEY_SEND_PACKAGE_VIDEO);
            originalVideoPath = videoProfileExecute.getOriginalVideoPath();
            cameraCachePath = videoProfileExecute.getOverlayVideoPath();
            startTime = videoProfileExecute.getStartTime();
            endTime = videoProfileExecute.getEndTime();
            posX = videoProfileExecute.getPosX();
            posY = videoProfileExecute.getPosY();
            camSize = videoProfileExecute.getCamSize();
        }
        //do react
        if (intent.getAction() != null
                && intent.getAction().equals(MyUtils.ACTION_FOR_REACT)) flipCamera(cameraCachePath);

        //do commentary
        if (intent.getAction() != null
                && intent.getAction().equals(MyUtils.ACTION_FOR_COMMENTARY))
            executeCommentaryAudio(originalVideoPath, cameraCachePath);


        fakeCountProgress();
    }

    private void fakeCountProgress() {
        // neu thoi gian thuc thi < 10s thi fake thanh 10s
        if (expectTime < 10000) expectTime = 10000;

        // expectTime < 3p thi step = 1s, >3p thi step = 2s
        if (expectTime < 1000 * 60 * 3) {
            countDownInterval = 1000;
        } else countDownInterval = 2000;

        countDownTimer = new CountDownTimer(expectTime, 1000) {
            @Override
            public void onTick(long l) {
                if (!finishExecute) {
                    progress = generateProgress(100 - (int) (100 * l / expectTime));
                    System.out.println("thanhlv progress == " + progress);
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
    }

    public void showPopUpResult(String path) {
        App.ignoreOpenAd = true;
        Intent myIntent = new Intent(ExecuteService.this, PopUpResultVideoTranslucentActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myIntent.putExtra(KEY_VIDEO_PATH, path);
        startActivity(myIntent);
        MyUtils.sendBroadCastMessageFromService(this, NOTIFY_MSG_RECORDING_DONE);
    }


    private void executeCommentaryAudio(String videoPath, String audioPath) {
        FFmpegUtil.getInstance().commentaryAudio(videoPath, audioPath, new FFmpegUtil.ITranscoding() {
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
                    startForeground(NOTIFICATION_ID, notification);
                    showPopUpResult(code);
                }
            }
        });

    }

    private void flipCamera(String cameraCachePath) {
        FFmpegUtil.getInstance().flipHorizontal(cameraCachePath, new FFmpegUtil.ITranscoding() {
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

    public void executeFFmpegReactCam(String overlayVideoPath) {
        FFmpegUtil.getInstance().reactCamera(originalVideoPath, overlayVideoPath, startTime, endTime, camSize,
                posX, posY, false, false, new FFmpegUtil.ITranscoding() {
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
                            startForeground(NOTIFICATION_ID, notification);
                            showPopUpResult(code);

                        }
                    }
                });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private Notification.Builder notificationBuilder = null;
    private Notification notification;
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void createNotification(String type) {
        Intent intent = new Intent(this, TranslucentActivity.class);
        intent.putExtra(ACTION_STOP_SERVICE, false);
        @SuppressLint({"UnspecifiedImmutableFlag", "WrongConstant"})
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, intent, PendingIntent.FLAG_MUTABLE);
        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_layout);
        if (type.equals(MyUtils.ACTION_FOR_REACT))
            notificationLayoutExpanded.setTextViewText(R.id.des, "ReactCam in processing");
        if (type.equals(MyUtils.ACTION_FOR_COMMENTARY))
            notificationLayoutExpanded.setTextViewText(R.id.des, "Commentary in processing");
        notificationLayoutExpanded.setImageViewResource(R.id.ic_app, R.drawable.ic_app);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.btn_cancel_notification, pendingIntent);

        //Set notification information
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(this, CHANNEL_ID);
        } else
            notificationBuilder = new Notification.Builder(getApplicationContext());
        notificationBuilder
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setContentTitle(getString(R.string.full_app_name))
                .setSmallIcon(R.drawable.ic_app)
                .setDefaults(Notification.DEFAULT_ALL)
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
