package com.examples.atvizpro.ui.services;

import static com.examples.atvizpro.App.CHANNEL_ID;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;

import com.examples.atvizpro.R;
import com.examples.atvizpro.ui.activities.MainActivity;
import com.examples.atvizpro.utils.VideoUtil;


public class ExecuteService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        CountDownTimer countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long l) {
                notificationBuilder.setProgress(100, 100-(int) l/100, false);
                startForeground(getID(), notification);
//                notificationManager.notify(100, notification);
            }

            @Override
            public void onFinish() {

//                notificationBuilder.setProgress(0, 0, false);
//                notificationManager.notify(100, notification);

                stopService();
            }
        };
        countDownTimer.start();


//        new VideoUtil().reactCamera(this, videoFile, cameraCahePath_flip, startTime, endTime, camOverlaySize[camSize],
//                posX, posY, false, false, new VideoUtil.ITranscoding() {
//                    @Override
//                    public void onStartTranscoding(String outputCachePath) {
//                    }
//                    @Override
//                    public void onFinishTranscoding(String code) {
//
//                    }
//                    @Override
//                    public void onUpdateProgressTranscoding(int progress) {
//                    }
//                });

    }
    NotificationManager notificationManager;
    Notification.Builder notificationBuilder = null;
    Notification notification;
    private void createNotification() {

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Set notification information:

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(this, CHANNEL_ID);
        } else
            notificationBuilder = new Notification.Builder(getApplicationContext());
        notificationBuilder
                .setOngoing(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{0L})
                .setContentTitle("AT Screen Record")
                .setContentText("In progress...")
                .setSmallIcon(R.drawable.ic_app)
                .setContentIntent(pendingIntent)
                .setProgress(100, 0, false);

        //Send the notification:
        notification = notificationBuilder.build();
//        notificationManager.notify(100, notification);
        startForeground(getID(), notification);
    }

    int i = 10;
    private int getID() {
        return 11;
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
