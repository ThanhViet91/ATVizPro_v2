package com.atsoft.screenrecord.ui.services.recording;

import static com.atsoft.screenrecord.ui.services.ExecuteService.KEY_VIDEO_PATH;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import androidx.core.app.NotificationCompat;

import com.atsoft.screenrecord.App;
import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.controllers.encoder.MediaAudioEncoder;
import com.atsoft.screenrecord.controllers.encoder.MediaEncoder;
import com.atsoft.screenrecord.controllers.encoder.MediaMuxerWrapper;
import com.atsoft.screenrecord.controllers.encoder.MediaScreenEncoderHard;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;
import com.atsoft.screenrecord.controllers.settings.VideoSetting2;
import com.atsoft.screenrecord.ui.activities.PopUpResultVideoTranslucentActivity;
import com.atsoft.screenrecord.ui.services.BaseService;
import com.atsoft.screenrecord.ui.utils.MyUtils;
import com.atsoft.screenrecord.ui.utils.NotificationHelper;

import java.io.IOException;

public class RecordingService extends BaseService {
    private final IBinder mIBinder = new RecordingBinder();

    private static final String TAG = RecordingService.class.getSimpleName();
    public static final String NOTIFY_MSG_RECORDING_DONE = "NOTIFY_MSG_RECORDING_DONE";
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private Intent mScreenCaptureIntent;
    private int mScreenCaptureResultCode = -999999;
    private int mScreenWidth, mScreenHeight, mScreenDensity;
    private MediaMuxerWrapper mMuxer;
    private static final Object sSync = new Object();
    private VideoSetting2 mCurrentVideoSetting;
    private VideoSetting2 mResultVideo;

    public class RecordingBinder extends Binder {
        public RecordingService getService() {
            return RecordingService.this;
        }
    }

    public RecordingService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);

    }

    private void getScreenSize() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mScreenDensity = metrics.densityDpi;
    }

    @Override
    public IBinder onBind(Intent intent) {
        mScreenCaptureIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
        mScreenCaptureResultCode = mScreenCaptureIntent.getIntExtra(MyUtils.SCREEN_CAPTURE_INTENT_RESULT_CODE, MyUtils.RESULT_CODE_FAILED);

        getScreenSize();
        try {
            mMediaProjection = mMediaProjectionManager.getMediaProjection(mScreenCaptureResultCode, mScreenCaptureIntent);
        } catch (Exception ignored) {

        }
        DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display defaultDisplay;
        if (dm != null) {
            defaultDisplay = dm.getDisplay(Display.DEFAULT_DISPLAY);
        } else {
            throw new IllegalStateException("Cannot display manager?!?");
        }
        if (defaultDisplay == null) {
            throw new RuntimeException("No display found.");
        }
        return mIBinder;
    }

    @Override
    public void openPerformService() {
    }

    @Override
    public void startPerformService() {
        startRecording();
    }

    @Override
    public void stopPerformService() {
        mResultVideo = stopRecording();
    }

    @Override
    public void closePerformService() {

    }

    public void startRecording() {
        synchronized (sSync) {
            if (mMuxer == null) {

                try {
                    mMuxer = new MediaMuxerWrapper(this, ".mp4");    // if you record audio only, ".m4a" is also OK.

                    mCurrentVideoSetting = SettingManager2.getVideoProfile(getApplicationContext());
                    new MediaScreenEncoderHard(mMuxer, mMediaEncoderListener, mMediaProjection, mCurrentVideoSetting, mScreenDensity);
                    new MediaAudioEncoder(mMuxer, mMediaEncoderListener);

                    mMuxer.prepare();
                    mMuxer.startRecording();
                } catch (final IOException ignored) {
                }
            }
        }
    }

    public void pauseScreenRecord() {
        synchronized (sSync) {
            if (mMuxer != null) {
                mMuxer.pauseRecording();
            }
        }
    }

    public void resumeScreenRecord() {
        synchronized (sSync) {
            if (mMuxer != null) {
                mMuxer.resumeRecording();
            }
        }
    }

    //Return output file
    public VideoSetting2 stopRecording() {
        String outputFile;
        synchronized (sSync) {
            if (mMuxer != null) {
                outputFile = mMuxer.getOutputPath();
                mCurrentVideoSetting.setOutputPath(outputFile);
                mMuxer.stopRecording();
                mMuxer = null;
                try {
                    sSync.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                showResultActivity(outputFile);
            }
        }
        return mCurrentVideoSetting;
    }

    public void showResultActivity(String finalVideoCachePath) {

        App.ignoreOpenAd = true;
        Intent myIntent = new Intent(getApplicationContext(), PopUpResultVideoTranslucentActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myIntent.putExtra(KEY_VIDEO_PATH, finalVideoCachePath);
        startActivity(myIntent);
        MyUtils.sendBroadCastMessageFromService(this, NOTIFY_MSG_RECORDING_DONE);
    }

    public void insertVideoToGallery() {
        String outputFile = mResultVideo.getOutputPath();
        if (TextUtils.isEmpty(outputFile))
            return;

        //send video to gallery
        ContentResolver cr = getContentResolver();

        ContentValues values = new ContentValues(2);

        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, outputFile);

        // Add a new record (identified by uri) without the video, but with the values just set.
        Uri uri = cr.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
    }


    private static final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
        }
    };
}
