package com.examples.atscreenrecord.ui.services.streaming;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import com.examples.atscreenrecord.controllers.settings.SettingManager2;
import com.examples.atscreenrecord.controllers.settings.VideoSetting2;
import com.examples.atscreenrecord.ui.services.BaseService;
import com.examples.atscreenrecord.ui.utils.MyUtils;
import com.takusemba.rtmppublisher.Publisher;
import com.takusemba.rtmppublisher.PublisherListener;
import com.takusemba.rtmppublisher.helper.StreamProfile;

public class StreamingService extends BaseService implements PublisherListener {
    private static final boolean DEBUG = MyUtils.DEBUG;    // TODO set false on release
    public static final String KEY_NOTIFY_MSG = "stream service notify";

    public static final String NOTIFY_MSG_CONNECTION_FAILED = "Stream connection failed";
    public static final String NOTIFY_MSG_CONNECTION_STARTED = "Stream started";
    public static final String NOTIFY_MSG_CONNECTED = "Stream connected";
    public static final String NOTIFY_MSG_ERROR = "Stream connection error!";
    public static final String NOTIFY_MSG_UPDATED_STREAM_PROFILE = "Updated stream profile";
    public static final String NOTIFY_MSG_CONNECTION_DISCONNECTED = "Connection disconnected!";
    public static final String NOTIFY_MSG_STREAM_STOPPED = "Stream stopped";
    public static final String NOTIFY_MSG_REQUEST_START = "Request start stream";
    public static final String NOTIFY_MSG_REQUEST_STOP = "Request stop stream";

    private final IBinder mIBinder = new StreamingBinder();

    private static final String TAG = "StreamService_chienpm";
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private Intent mScreenCaptureIntent;
    private int mScreenCaptureResultCode;
    private int mScreenWidth, mScreenHeight, mScreenDensity;
    private Publisher mPublisher;
    private static final Object sSync = new Object();

    private StreamProfile mStreamProfile;

    private String mUrl = MyUtils.SAMPLE_RMPT_URL;

    //Implement Publisher listener
    @Override
    public void onStarted() {
        if (DEBUG) Log.i(TAG, "onStarted connect");
        notifyStreamingCallback(NOTIFY_MSG_CONNECTION_STARTED);
    }

    @Override
    public void onConnected() {
        System.out.println("thanhlv onConnected callback");
        notifyStreamingCallback(NOTIFY_MSG_CONNECTED);
        MyUtils.toast(getApplicationContext(), "Connection success!", Toast.LENGTH_LONG);
    }

    @Override
    public void onStopped() {
        if (DEBUG) Log.i(TAG, "onStopped live");
        notifyStreamingCallback(NOTIFY_MSG_STREAM_STOPPED);
    }

    @Override
    public void onDisconnected() {
        if (DEBUG) Log.i(TAG, "onDisconnected");
//        notifyStreamingCallback(NOTIFY_MSG_CONNECTION_DISCONNECTED);
    }

    @Override
    public void onFailedToConnect() {
//        if (mPublisher != null && mPublisher.isPublishing())
//            System.out.println("thanhlv ------------- onFailedToConnect");
//            mPublisher.closePublishing();
        notifyStreamingCallback(NOTIFY_MSG_CONNECTION_FAILED);
        if (DEBUG) Log.i(TAG, "onFailedToConnect");

        MyUtils.toast(getApplicationContext(), "Connection failed, please check stream link again!", Toast.LENGTH_LONG);
    }

    @Override
    public void onSentVideoData(int result, int timestamp) {
        Log.i(TAG, "Sent video data at " + timestamp + " - result: " + result);
    }

    public class StreamingBinder extends Binder {
        public StreamingService getService() {
            return StreamingService.this;
        }
    }

    public StreamingService() {

    }

    void notifyStreamingCallback(String notify_msg) {
        if (DEBUG) Log.i(TAG, "sent notify stream " + notify_msg);
//        Intent intent = new Intent();
//        intent.setAction(MyUtils.ACTION_NOTIFY_FROM_STREAM_SERVICE);
//        intent.putExtra(KEY_MESSAGE, notify_msg);
//        sendBroadcast(intent);
        MyUtils.sendBroadCastMessageFromService(this, notify_msg);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("thanhlv ------------- onDestroy");
        closeStreaming();
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
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        if (width > height) {
            final float scale_x = width / 1920f;
            final float scale_y = height / 1080f;
            final float scale = Math.max(scale_x, scale_y);
            width = (int) (width / scale);
            height = (int) (height / scale);
        } else {
            final float scale_x = width / 1080f;
            final float scale_y = height / 1920f;
            final float scale = Math.max(scale_x, scale_y);
            width = (int) (width / scale);
            height = (int) (height / scale);
        }
        //just support portrait
        if (width < height) {
            mScreenWidth = width;
            mScreenHeight = height;
        } else {
            mScreenWidth = height;
            mScreenHeight = width;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (DEBUG) Log.i(TAG, "RecordingService: onBind()");
        mScreenCaptureIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
        mScreenCaptureResultCode = mScreenCaptureIntent.getIntExtra(MyUtils.SCREEN_CAPTURE_INTENT_RESULT_CODE, MyUtils.RESULT_CODE_FAILED);

        mStreamProfile = (StreamProfile) intent.getSerializableExtra(MyUtils.STREAM_PROFILE);

        if (mStreamProfile == null)
            throw new RuntimeException("Stream Profile is null");
        else
            mUrl = mStreamProfile.getStreamUrl();

        getScreenSize();
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mScreenCaptureResultCode, mScreenCaptureIntent);
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

        if (DEBUG) Log.d(TAG, "onBindStream: " + mScreenCaptureIntent);
        if (DEBUG) Log.d(TAG, "onBindStream: " + mStreamProfile.toString());
        return mIBinder;
    }

    @Override
    public void openPerformService() {
        System.out.println("thanhlv openPerformService ....");
        prepareConnection();
    }


    @Override
    public void startPerformService() {
        if (DEBUG) Log.i(TAG, "startPerformService: from StreamingService");
        notifyStreamingCallback(NOTIFY_MSG_REQUEST_START + " " + mUrl);
        startStreaming();
    }

    @Override
    public void stopPerformService() {
        if (DEBUG) Log.i(TAG, "stopPerformService: from StreamingService");
        notifyStreamingCallback(NOTIFY_MSG_REQUEST_STOP + " " + mUrl);
        stopStreaming();
    }

    @Override
    public void closePerformService() {
        if (DEBUG) Log.i(TAG, "stopPerformService: from StreamingService");
        notifyStreamingCallback(NOTIFY_MSG_REQUEST_STOP + " " + mUrl);
        System.out.println("thanhlv -------------- closePerformService");
        closeStreaming();
    }

    public void updateUrl(String url) {
        mUrl = url;
    }
    public void prepareConnection() {
        synchronized (sSync) {
            if (mPublisher == null) {
                try {
                    VideoSetting2 videoSetting = SettingManager2.getVideoProfile(getApplicationContext());

                    mPublisher = new Publisher.Builder()
                            .setUrl(mUrl)
                            .setSize(videoSetting.getWidth(), videoSetting.getHeight())
//                            .setSize(mScreenWidth, mScreenHeight)
                            .setAudioBitrate(Publisher.Builder.DEFAULT_AUDIO_BITRATE)
//                            .setVideoBitrate(videoSetting.getBitrate())
                            .setVideoBitrate(Publisher.Builder.DEFAULT_VIDEO_BITRATE)
                            .setDensity(mScreenDensity)
                            .setListener(this)
                            .setMediaProjection(mMediaProjection)
                            .build();
                } catch (final Exception e) {
                    mPublisher = null;
                }
            }
            if (mPublisher != null)
                mPublisher.openPublishing(mUrl);
        }
    }

    public void startStreaming() {
        synchronized (sSync) {
            if (mPublisher != null) {
                mPublisher.startPublishing();
            }
        }

    }

    public void updateStreamProfile(StreamProfile profile) {
        mStreamProfile = profile;
        if (!mUrl.equals(profile.getStreamUrl())) {
            mUrl = profile.getStreamUrl();
//            if (mPublisher != null) mPublisher.set
//            notifyStreamingCallback(NOTIFY_MSG_UPDATED_STREAM_PROFILE);
        }
    }

    public void stopStreaming() {
        if (mPublisher != null && mPublisher.isPublishing()) {
            mPublisher.stopPublishing();
        }
    }

    public void closeStreaming() {
        SettingManager2.setLiveStreamType(getApplicationContext(), 0);
        if (mPublisher != null && mPublisher.isPublishing()) {
            System.out.println("thanhlv ------------- closeStreaming");
            mPublisher.closePublishing();
        }
    }
}
