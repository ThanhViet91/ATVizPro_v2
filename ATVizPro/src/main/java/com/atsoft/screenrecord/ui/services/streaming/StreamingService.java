package com.atsoft.screenrecord.ui.services.streaming;

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

import static com.atsoft.screenrecord.Core.isConnected;
import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;
import com.atsoft.screenrecord.controllers.settings.VideoSetting2;
import com.atsoft.screenrecord.ui.services.BaseService;
import com.atsoft.screenrecord.ui.utils.MyUtils;
import com.atsoft.screenrecord.utils.CounterUtil;
import com.takusemba.rtmppublisher.Publisher;
import com.takusemba.rtmppublisher.PublisherListener;
import com.takusemba.rtmppublisher.helper.StreamProfile;

public class StreamingService extends BaseService implements PublisherListener {
    private static final boolean DEBUG = MyUtils.DEBUG;    // TODO set false on release

    public static final String NOTIFY_MSG_CONNECTION_FAILED = "Stream connection failed";
    public static final String NOTIFY_MSG_CONNECTION_STARTED = "Stream started";
    public static final String NOTIFY_MSG_CONNECTED = "Stream connected";
    public static final String NOTIFY_MSG_ERROR = "Stream connection error!";
    public static final String NOTIFY_MSG_STREAM_STOPPED = "Stream stopped";
    public static final String NOTIFY_MSG_REQUEST_START = "Request start stream";
    public static final String NOTIFY_MSG_REQUEST_STOP = "Request stop stream";

    private final IBinder mIBinder = new StreamingBinder();

    private static final String TAG = "StreamService";
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private int mScreenWidth, mScreenHeight, mScreenDensity;
    private Publisher mPublisher;
    private static final Object sSync = new Object();

    private String mUrl = MyUtils.SAMPLE_RMPT_URL;

    //Implement Publisher listener
    @Override
    public void onStarted() {
        isConnected = false;
        hasNotice = false;

        System.out.println("thanhlv fffffffffff onStarted " + isConnected);
        notifyStreamingCallback(NOTIFY_MSG_CONNECTION_STARTED);
    }

    @Override
    public void onConnected() {
        isConnected = true;
        System.out.println("thanhlv fffffffffff onConnected " + isConnected);
        notifyStreamingCallback(NOTIFY_MSG_CONNECTED);
        MyUtils.toast(getApplicationContext(), "Connection success!", Toast.LENGTH_LONG);
    }

    @Override
    public void onStopped() {
//        if (DEBUG) Log.i(TAG, "onStopped live");
//        isConnected = false;
        notifyStreamingCallback(NOTIFY_MSG_STREAM_STOPPED);

        System.out.println("thanhlv fffffffffff onStopped " + isConnected);
    }

    @Override
    public void onDisconnected() {
        isConnected = false;
        System.out.println("thanhlv fffffffffff onDisconnected " + isConnected);
        CounterUtil.getInstance().stopCounter();
        if (DEBUG) Log.i(TAG, "onDisconnected");
//        notifyStreamingCallback(NOTIFY_MSG_CONNECTION_DISCONNECTED);
    }

    boolean hasNotice = false;

    @Override
    public void onFailedToConnect(String reason) {
        isConnected = false;

        System.out.println("thanhlv fffffffffff onFailedToConnect " + isConnected);
        CounterUtil.getInstance().stopCounter();
        notifyStreamingCallback(NOTIFY_MSG_CONNECTION_FAILED);
        if (DEBUG) Log.i(TAG, "onFailedToConnect");

        if (!hasNotice) {
            hasNotice = true;
            if (reason.contains("Error send packet")) {
                MyUtils.toast(getApplicationContext(), getString(R.string.livestreaming_is_stopped), Toast.LENGTH_LONG);
            } else
                MyUtils.toast(getApplicationContext(), "Connection failed, please check stream link again!", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onSentVideoData(int result, int timestamp) {
//        Log.i(TAG, "Sent video data at " + timestamp + " - result: " + result);
    }

    public class StreamingBinder extends Binder {
        public StreamingService getService() {
            return StreamingService.this;
        }
    }

    public StreamingService() {

    }

    void notifyStreamingCallback(String notify_msg) {
//        if (DEBUG) Log.i(TAG, "sent notify stream " + notify_msg);
        MyUtils.sendBroadCastMessageFromService(this, notify_msg);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
//        int width = metrics.widthPixels;
//        int height = metrics.heightPixels;
//        if (width > height) {
//            final float scale_x = width / 1920f;
//            final float scale_y = height / 1080f;
//            final float scale = Math.max(scale_x, scale_y);
//            width = (int) (width / scale);
//            height = (int) (height / scale);
//        } else {
//            final float scale_x = width / 1080f;
//            final float scale_y = height / 1920f;
//            final float scale = Math.max(scale_x, scale_y);
//            width = (int) (width / scale);
//            height = (int) (height / scale);
//        }//       //just support portrait
//        if (width < height) {
//            mScreenWidth = width;
//            mScreenHeight = height;
//        } else {
//            mScreenWidth = height;
//            mScreenHeight = width;
//        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (DEBUG) Log.i(TAG, "RecordingService: onBind()");
        Intent mScreenCaptureIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
        int mScreenCaptureResultCode = mScreenCaptureIntent.getIntExtra(MyUtils.SCREEN_CAPTURE_INTENT_RESULT_CODE, MyUtils.RESULT_CODE_FAILED);

        StreamProfile mStreamProfile = (StreamProfile) intent.getSerializableExtra(MyUtils.STREAM_PROFILE);

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
        if (DEBUG) Log.d(TAG, "onBindStream: " + mStreamProfile);
        return mIBinder;
    }

    @Override
    public void openPerformService() {
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
//        closeStreaming();
    }

    @Override
    public void closePerformService() {
//        if (DEBUG) Log.i(TAG, "stopPerformService: from StreamingService");
        notifyStreamingCallback(NOTIFY_MSG_REQUEST_STOP + " " + mUrl);
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
                            .setVideoBitrate(videoSetting.getBitrate())
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

//    public void updateStreamProfile(StreamProfile profile) {
//        mStreamProfile = profile;
//        if (!mUrl.equals(profile.getStreamUrl())) {
//            mUrl = profile.getStreamUrl();
////            if (mPublisher != null) mPublisher.set
////            notifyStreamingCallback(NOTIFY_MSG_UPDATED_STREAM_PROFILE);
//        }
//    }

    public void stopStreaming() {
        if (mPublisher != null && mPublisher.isPublishing()) {
            mPublisher.stopPublishing();
        }
    }

    public void closeStreaming() {
//        SettingManager2.setLiveStreamType(getApplicationContext(), 0);
        if (mPublisher != null && mPublisher.isPublishing()) {
            mPublisher.closePublishing();
        }
    }
}
