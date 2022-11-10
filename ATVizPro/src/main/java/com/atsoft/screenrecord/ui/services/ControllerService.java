package com.atsoft.screenrecord.ui.services;

import static com.atsoft.screenrecord.Core.isConnected;
import static com.atsoft.screenrecord.ui.activities.MainActivity.KEY_PATH_VIDEO;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.atsoft.screenrecord.App;
import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.controllers.settings.CameraSetting;
import com.atsoft.screenrecord.controllers.settings.SettingManager;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;
import com.atsoft.screenrecord.ui.activities.MainActivity;
import com.atsoft.screenrecord.ui.activities.TranslucentActivity;
import com.atsoft.screenrecord.ui.services.recording.RecordingService;
import com.atsoft.screenrecord.ui.services.recording.RecordingService.RecordingBinder;
import com.atsoft.screenrecord.ui.services.streaming.StreamingService;
import com.atsoft.screenrecord.ui.services.streaming.StreamingService.StreamingBinder;
import com.atsoft.screenrecord.ui.utils.CameraPreview;
import com.atsoft.screenrecord.ui.utils.CustomOnScaleDetector;
import com.atsoft.screenrecord.ui.utils.MyUtils;
import com.atsoft.screenrecord.ui.utils.NotificationHelper;
import com.atsoft.screenrecord.utils.CounterUtil;
import com.atsoft.screenrecord.utils.DisplayUtil;
import com.atsoft.screenrecord.utils.OnSingleClickListener;
import com.takusemba.rtmppublisher.helper.StreamProfile;

public class ControllerService extends Service implements CustomOnScaleDetector.OnScaleListener {
    private static final String TAG = ControllerService.class.getSimpleName();
    public static final String NOTIFY_MSG_RECORDING_STARTED = "NOTIFY_MSG_RECORDING_STARTED";
    public static final String NOTIFY_MSG_RECORDING_CLOSED = "NOTIFY_MSG_RECORDING_CLOSED";
    public static final String NOTIFY_MSG_LIVESTREAM_STARTED = "NOTIFY_MSG_LIVESTREAM_STARTED";
    public static final String NOTIFY_MSG_LIVESTREAM_CLOSED = "NOTIFY_MSG_LIVESTREAM_CLOSED";
    public static final String NOTIFY_MSG_RECORDING_STOPPED = "NOTIFY_MSG_RECORDING_STOPPED";
    public static final String NOTIFY_MSG_LIVESTREAM_STOPPED = "NOTIFY_MSG_LIVESTREAM_STOPPED";
    //    private final boolean DEBUG = MyUtils.DEBUG;
    private BaseService mService;
    private Boolean mRecordingServiceBound = false;
    private View mViewRoot;
    private View mCameraLayout;
    private View mViewBlur;
    private WindowManager mWindowManager;
    WindowManager.LayoutParams paramViewRoot;
    WindowManager.LayoutParams paramCam;
    WindowManager.LayoutParams paramCountdown;
    WindowManager.LayoutParams paramBlur;
    private Intent mScreenCaptureIntent = null;
    private ImageView mImgClose, mImgRec, mImgStart, mImgStop, mImgCapture, mImgHome;
    public static Boolean mRecordingStarted = false;
    private Camera mCamera;
    private LinearLayout cameraPreview;
    private int mScreenWidth, mScreenHeight;
    private TextView mTvCountdown;
    private View mCountdownLayout;
    private int mCameraWidth = 160, mCameraHeight = 120;
    private StreamProfile mStreamProfile;
    private int mMode;
    private String videoFileEndRecord = "";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return START_NOT_STICKY;
        String action = intent.getAction();
        if (action != null) {
            handleIncomeAction(intent);
//            if (DEBUG) Log.i(TAG, "return START_REDELIVER_INTENT" + action);
            return START_NOT_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

//    private void updateUI() {
//        if (mMode == MyUtils.MODE_STREAMING) {
////            int type = SettingManager2.getLiveStreamType(this);
////            if (type == SOCIAL_TYPE_YOUTUBE) mImgRec.setBackgroundResource(R.drawable.ic_youtube);
////            if (type == SOCIAL_TYPE_FACEBOOK) mImgRec.setBackgroundResource(R.drawable.ic_facebook);
////            if (type == SOCIAL_TYPE_TWITCH) mImgRec.setBackgroundResource(R.drawable.ic_twitch);
//        } else {
//            mImgRec.setBackgroundResource(R.drawable.ic_fab_expand);
//        }
//    }

    private void handleIncomeAction(Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action))
            return;

        switch (action) {

            case MyUtils.ACTION_START_LIVESTREAM_FROM_HOME:
                System.out.println("thanhlv case MyUtils.ACTION_START_LIVESTREAM_FROM_HOME");
                handleStartRecording();
                break;
            case MyUtils.ACTION_STOP_LIVESTREAM_FROM_HOME:
            case MyUtils.ACTION_STOP_RECORDING_FROM_HOME:
                System.out.println("thanhlv case MyUtils.ACTION_STOP_RECORDING_FROM_HOME ACTION_STOP_LIVESTREAM_FROM_HOME");
                onClickStop();
                break;
            case MyUtils.ACTION_START_RECORDING_FROM_HOME:
                System.out.println("thanhlv case MyUtils.ACTION_START_RECORDING_FROM_HOME");
                handleStartRecording();
                break;
            case MyUtils.ACTION_DISCONNECT_LIVE_FROM_HOME:
                onClickStop();
                onClickClose(false);
                break;
            case MyUtils.ACTION_DISCONNECT_WHEN_STOP_LIVE:
                onClickStop();
                onClickClose(true);
                break;
            case MyUtils.ACTION_EXIT_SERVICE:
                stopService();
                break;
            case MyUtils.ACTION_UPDATE_SHOW_HIDE_FAB:
                if (mViewRoot != null)
                    toggleView(mViewRoot, SettingManager2.isEnableFAB(getApplicationContext()) ? View.VISIBLE : View.GONE);
                break;
            case MyUtils.ACTION_INIT_CONTROLLER:
                System.out.println("thanhlv case MyUtils.ACTION_INIT_CONTROLLER:");
                mMode = intent.getIntExtra(MyUtils.KEY_CONTROLlER_MODE, MyUtils.MODE_RECORDING);
                mScreenCaptureIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                if (mMode == MyUtils.MODE_STREAMING)
                    mStreamProfile = (StreamProfile) intent.getSerializableExtra(MyUtils.STREAM_PROFILE);
                boolean isCamera = intent.getBooleanExtra(MyUtils.KEY_CAMERA_AVAILABLE, false);

                if (isCamera && mCamera == null) {
//                    if (DEBUG) Log.i(TAG, "onStartCommand: before initCameraView");
                    initCameraView();
                }
                if (mScreenCaptureIntent == null) {
                    Log.i(TAG, "mScreenCaptureIntent is NULL");
                    stopService();
                } else if (!mRecordingServiceBound) {
//                    if (DEBUG) Log.i(TAG, "before run bindStreamService()" + action);
                    bindStreamingService();
                }
//                updateUI();
                if (mMode == MyUtils.MODE_RECORDING) {
                    toggleView(mViewRoot, View.GONE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            System.out.println("thanhlv case MyUtils.ACTION_INIT_CONTROLLER: ---> handleStartRecording");
                            handleStartRecording();
                        }
                    }, 600);
                }
                break;

//            case MyUtils.ACTION_UPDATE_SETTING:
//                handleUpdateSetting(intent);
//                break;
//            case MyUtils.ACTION_UPDATE_TYPE_LIVE:
//                updateUI();
//                break;
//            case MyUtils.ACTION_END_RECORD:
//                videoFileEndRecord = intent.getStringExtra(KEY_PATH_VIDEO);
//                break;
            case MyUtils.ACTION_UPDATE_STREAM_PROFILE:
                if (mMode == MyUtils.MODE_STREAMING && mService != null && mRecordingServiceBound) {
                    String url = intent.getStringExtra(MyUtils.NEW_URL);
                    ((StreamingService) mService).updateUrl(url);
                    ((StreamingService) mService).prepareConnection();
                } else {
                    Log.e(TAG, "handleIncomeAction: ", new Exception("Update stream profile error"));
                }
                break;

        }
    }

    @SuppressLint("NonConstantResourceId")
    private void handleUpdateSetting(Intent intent) {
        int key = intent.getIntExtra(MyUtils.ACTION_UPDATE_SETTING, -1);
        switch (key) {
            case R.string.setting_camera_size:
                updateCameraSize();
                break;
            case R.string.setting_camera_position:
                updateCameraPosition();
                break;
            case R.string.setting_camera_mode:
                updateCameraMode();
                break;
        }
    }

    private void updateCameraMode() {
//        CameraSetting profile = SettingManager.getCameraProfile(getApplicationContext());
//        if (profile.getMode().equals(CameraSetting.CAMERA_MODE_OFF))
//            toggleView(mCameraLayout, View.GONE);
//        else {
//            if (mCameraLayout != null) {
//                mWindowManager.removeViewImmediate(mCameraLayout);
//                releaseCamera();
//                initCameraView();
//            }
//        }
    }

    private void updateCameraPosition() {
//        if (DEBUG)            Log.i(TAG, "updateCameraPosition: ");
//        CameraSetting profile = SettingManager.getCameraProfile(getApplicationContext());
//        paramCam.gravity = profile.getParamGravity();
//        paramCam.x = 0;
//        paramCam.y = 0;
//        mWindowManager.updateViewLayout(mCameraLayout, paramCam);
    }

    private void updateCameraSize() {
//        CameraSetting profile = SettingManager.getCameraProfile(getApplicationContext());
//        calculateCameraSize(profile);
//        onConfigurationChanged(getResources().getConfiguration());
    }

    public ControllerService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        updateScreenSize();
        if (paramViewRoot == null) {
            initParam();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel chan = new NotificationChannel(NotificationHelper.CHANNEL_ID, NotificationHelper.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                chan.setLightColor(Color.BLUE);
                chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                assert manager != null;
                manager.createNotificationChannel(chan);

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID);
                Notification notification = notificationBuilder.setOngoing(true)
                        .setSmallIcon(R.drawable.ic_launcher_2_foreground)
                        .setContentTitle("Screen Recorder is running in background")
                        .setPriority(NotificationManager.IMPORTANCE_MIN)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build();
                startForeground(2, notification);
            }

        }
        if (mViewRoot == null)
            initializeViews();
    }

    private void initParam() {
//        if (DEBUG) Log.i(TAG, "initParam: ");
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        paramViewRoot = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        paramCam = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        paramCountdown = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        paramBlur = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                        WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                PixelFormat.TRANSLUCENT
        );

    }

    private void updateScreenSize() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }

    @SuppressLint({"ClickableViewAccessibility", "InflateParams"})
    private void initCameraView() {
//        if (DEBUG) Log.i(TAG, "StreamingControllerService: initializeCamera()");
        CameraSetting cameraProfile = SettingManager.getCameraProfile(getApplication());

        mCameraLayout = LayoutInflater.from(this).inflate(R.layout.layout_camera_view, null);

        if (cameraProfile.getMode().equals(CameraSetting.CAMERA_MODE_BACK))
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        else
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

        cameraPreview = mCameraLayout.findViewById(R.id.camera_preview);

        calculateCameraSize(cameraProfile);

        onConfigurationChanged(getResources().getConfiguration());

        paramCam.gravity = cameraProfile.getParamGravity();
        paramCam.x = 0;
        paramCam.y = DisplayUtil.getDeviceHeight() / 3;
        CameraPreview mPreview = new CameraPreview(this, mCamera);
        cameraPreview.addView(mPreview);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mCameraLayout, paramCam);
        mCamera.startPreview();

        //re-inflate controller
        mWindowManager.removeViewImmediate(mViewRoot);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mViewRoot, paramViewRoot);

        if (cameraProfile.getMode().equals(CameraSetting.CAMERA_MODE_OFF))
            toggleView(cameraPreview, View.GONE);

        camWidth = camViewSize[camSize];  // ~270px
        camHeight = camWidth * 4 / 3f;
        cameraPreview.setLayoutParams(new FrameLayout.LayoutParams((int) camWidth, (int) camHeight));

        if (SettingManager2.isEnableCamView(getApplicationContext())) {
            toggleView(mCameraLayout, View.VISIBLE);
            mImgCapture.setImageResource(R.drawable.ic_hide_cam_fab);
        } else {
            toggleView(mCameraLayout, View.GONE);
            mImgCapture.setImageResource(R.drawable.ic_show_cam_fab);
        }

        final CustomOnScaleDetector customOnScaleDetector = new CustomOnScaleDetector(this);

        final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, customOnScaleDetector);

        mCameraLayout.setOnTouchListener(new View.OnTouchListener() {
            private int x, y, xx, yy;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getPointerCount() > 1) {
                    int pointerId1 = event.getPointerId(0);
                    int pointerId2 = event.getPointerId(1);

                    if (event.getX(pointerId1) < 0 || event.getX(pointerId2) < 0
                            || event.getX(pointerId1) > v.getWidth() || event.getX(pointerId2) > v.getWidth()
                            || event.getY(pointerId1) < 0 || event.getY(pointerId2) < 0
                            || event.getY(pointerId1) > v.getHeight() || event.getY(pointerId2) > v.getHeight()) {
                    } else scaleGestureDetector.onTouchEvent(event);
                    hasZoom = true;
                }

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                        customOnScaleDetector.resetLast();
                        hasZoom = false;
                        xx = paramCam.x;
                        yy = paramCam.y;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() < 2 && !hasZoom) {
                            paramCam.x = xx - (int) (event.getRawX() - x);
                            paramCam.y = yy + (int) (event.getRawY() - y);

                            mWindowManager.updateViewLayout(mCameraLayout, paramCam);
                        }
                    case MotionEvent.ACTION_UP:
                    default:
                        return true;
                }

            }
        });

    }

    float camWidth, camHeight;
    int[] camViewSize = {240, 270, 300, 330, 360, 390, 420};

    boolean hasZoom = false;


    private void calculateCameraSize(CameraSetting cameraProfile) {
        int factor;
        switch (cameraProfile.getSize()) {
            case CameraSetting.SIZE_BIG:
                factor = 3;
                break;
            case CameraSetting.SIZE_MEDIUM:
                factor = 4;
                break;
            default: //small
                factor = 5;
                break;
        }
        if (mScreenWidth > mScreenHeight) {//landscape
            mCameraWidth = mScreenWidth / factor;
        } else {
            mCameraWidth = mScreenHeight / factor;
        }
        mCameraHeight = mCameraWidth * 3 / 4;
//        if (DEBUG) Log.i(TAG, "calculateCameraSize: " + mScreenWidth + "x" + mScreenHeight);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if (DEBUG) Log.i(TAG, "onConfigurationChanged: DETECTED" + newConfig.orientation);
        updateScreenSize();

        if (paramViewRoot != null) {
            paramViewRoot.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
            paramViewRoot.x = 0;
            paramViewRoot.y = 0;
        }

        if (cameraPreview != null) {
            int width = mCameraWidth, height = mCameraHeight;

            ViewGroup.LayoutParams params = cameraPreview.getLayoutParams();

            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                params.height = width;
                params.width = height;
            } else {
                params.height = height;
                params.width = width;
            }

            cameraPreview.setLayoutParams(params);
        }
    }

    private long timerHideFAB = 0;
    private Handler handlerTimerFAB = new Handler();
    private Runnable runnableFAB;

    private void startCountTimeFAB() {
        timerHideFAB = 0;
        runnableFAB = new Runnable() {
            @Override
            public void run() {
                try {
                    if (timerHideFAB == 4) {
                        toggleNavigationButton(View.GONE);
                        return;
                    }
                    timerHideFAB++;
//                    System.out.println("thanhlv startCountTimeFAB " + timerHideFAB);
                    handlerTimerFAB.postDelayed(this, 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handlerTimerFAB.postDelayed(runnableFAB, 0);
    }

    TextView tvTimer;

    @SuppressLint("InflateParams")
    private void initializeViews() {
//        if (DEBUG) Log.i(TAG, "StreamingControllerService: initializeViews()");

        mViewRoot = LayoutInflater.from(this).inflate(R.layout.layout_recording, null);

        View mViewCountdown = LayoutInflater.from(this).inflate(R.layout.layout_countdown, null);
        mViewBlur = LayoutInflater.from(this).inflate(R.layout.layout_bg_fab, null);

        paramViewRoot.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
        paramViewRoot.x = 0;
        paramViewRoot.y = 0;

        paramBlur.x = 0;
        paramBlur.y = 0;

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mViewCountdown, paramCountdown);
        mWindowManager.addView(mViewRoot, paramViewRoot);
        toggleView(mViewRoot, SettingManager2.isEnableFAB(getApplicationContext()) ? View.VISIBLE : View.GONE);
        mWindowManager.addView(mViewBlur, paramBlur);

        mCountdownLayout = mViewCountdown.findViewById(R.id.countdown_container);
        mTvCountdown = mViewCountdown.findViewById(R.id.tvCountDown);

        toggleView(mCountdownLayout, View.GONE);

        mImgRec = mViewRoot.findViewById(R.id.imgRec);
        tvTimer = mViewRoot.findViewById(R.id.tvTimer);
        mImgCapture = mViewRoot.findViewById(R.id.imgCapture);
        mImgClose = mViewRoot.findViewById(R.id.imgClose);
        mImgStart = mViewRoot.findViewById(R.id.imgStart);
        mImgHome = mViewRoot.findViewById(R.id.imgHome);
        mImgStop = mViewRoot.findViewById(R.id.imgStop);
        toggleView(mImgStop, View.GONE);
        toggleView(tvTimer, View.GONE);
        toggleNavigationButton(View.GONE);


        mImgCapture.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
//                MyUtils.toast(getApplicationContext(), "Capture clicked", Toast.LENGTH_SHORT);
                toggleNavigationButton(View.GONE);
                if (mCameraLayout.getVisibility() == View.GONE) {
                    toggleView(mCameraLayout, View.VISIBLE);
                    mImgCapture.setImageResource(R.drawable.ic_hide_cam_fab);
                    SettingManager2.setEnableCamView(getApplicationContext(), true);
                } else {
                    toggleView(mCameraLayout, View.GONE);
                    mImgCapture.setImageResource(R.drawable.ic_show_cam_fab);
                    SettingManager2.setEnableCamView(getApplicationContext(), false);
                }
            }
        });

        mImgHome.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                gotoMain();
            }
        });

        mImgStart.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                handleStartRecording();
            }
        });

        mImgStop.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                onClickStop();
            }
        });

        mImgClose.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (mMode == MyUtils.MODE_STREAMING) {
                    goShowPopupConfirmClose();
                } else onClickClose(false);

            }
        });

        mViewRoot.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mViewRoot.setX(0);
                        //remember the initial position.
                        initialX = paramViewRoot.x;
                        initialY = paramViewRoot.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (event.getRawX() < mScreenWidth / 2f) {
                            paramViewRoot.x = 0;
                        } else {
                            paramViewRoot.x = mScreenWidth;
                        }
                        paramViewRoot.y = initialY + (int) (event.getRawY() - initialTouchY);

                        if (event.getRawY() > mScreenHeight - 40) {
                            paramViewRoot.y = initialY + (int) (mScreenHeight - 40 - initialTouchY);
                        }

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mViewRoot, paramViewRoot);


                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);

                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
                                startCountTimeFAB();
                                toggleNavigationButton(View.VISIBLE);
                            } else {
                                toggleNavigationButton(View.GONE);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        paramViewRoot.x = initialX + (int) (event.getRawX() - initialTouchX);
                        paramViewRoot.y = initialY + (int) (event.getRawY() - initialTouchY);

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mViewRoot, paramViewRoot);
                        return true;
                }

                return false;
            }
        });

        mViewBlur.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!isViewCollapsed()) {
                            toggleNavigationButton(View.GONE);
                        }
                        return true;
                }
                return false;
            }
        });

        mViewRoot.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    toggleNavigationButton(View.GONE);
            }
        });
    }

    private void handleStartRecording() {
        toggleNavigationButton(View.GONE);
        clickStart = true;
        clickStop = false;
        if (mRecordingServiceBound) {

            toggleView(mCountdownLayout, View.VISIBLE);

            int countdown = (SettingManager.getCountdown(getApplication())) * 1000;

            new CountDownTimer(countdown, 1000) {

                @SuppressLint("DefaultLocale")
                public void onTick(long millisUntilFinished) {
                    toggleView(mViewRoot, View.GONE);
                    mTvCountdown.setText(String.format("%d", millisUntilFinished / 1000 + 1));
                }

                public void onFinish() {
                    toggleView(mCountdownLayout, View.GONE);

                    toggleView(mViewRoot, SettingManager2.isEnableFAB(getApplicationContext()) ? View.VISIBLE : View.GONE);
                    mRecordingStarted = true;

//                    startCountTime();

                    if (mMode == MyUtils.MODE_RECORDING) { //mode Recording
                        mService.startPerformService();
                        mImgRec.setImageResource(R.drawable.ic_fab_with_time);
                        toggleView(tvTimer, View.VISIBLE);
                        MyUtils.sendBroadCastMessageFromService(getApplicationContext(), NOTIFY_MSG_RECORDING_STARTED);
                        CounterUtil counterUtil = CounterUtil.getInstance();
                        counterUtil.setCallback(new CounterUtil.ICounterUtil() {
                            @Override
                            public void onTickString(String sec) {
                                tvTimer.setText(sec);
                            }
                        });
                        counterUtil.startCounter();
                    } else { //mode livestream
                        if (isConnected) {
                            mService.startPerformService();
                            mImgRec.setImageResource(R.drawable.ic_fab_with_time);
                            toggleView(tvTimer, View.VISIBLE);
                            MyUtils.sendBroadCastMessageFromService(getApplicationContext(), NOTIFY_MSG_LIVESTREAM_STARTED);
                            CounterUtil counterUtil = CounterUtil.getInstance();
                            counterUtil.setCallback(new CounterUtil.ICounterUtil() {
                                @Override
                                public void onTickString(String sec) {
                                    tvTimer.setText(sec);
                                }
                            });
                            counterUtil.startCounter();
                        } else {
                            mRecordingStarted = false;
                            MyUtils.toast(getApplicationContext(), "Please connect livestream!", Toast.LENGTH_LONG);
                        }
                    }

                }
            }.start();

        } else {
            mRecordingStarted = false;
            MyUtils.toast(getApplicationContext(), "Recording Service connection has not been established", Toast.LENGTH_LONG);
//                    Log.e(TAG, "Recording Service connection has not been established");
            stopService();
        }
    }

    public void gotoMain() {
        MyUtils.toast(getApplicationContext(), "Go home!", Toast.LENGTH_SHORT);
        toggleNavigationButton(View.GONE);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setAction(MyUtils.ACTION_OPEN_SETTING_ACTIVITY);
        startActivity(intent);
    }

    public void goShowPopupConfirmClose() {
        toggleNavigationButton(View.GONE);
        App.ignoreOpenAd = true;
        Intent intent = new Intent(getApplicationContext(), TranslucentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_PATH_VIDEO, videoFileEndRecord);
        intent.setAction(MyUtils.ACTION_SHOW_POPUP_CONFIRM);
        startActivity(intent);
    }


    boolean clickStop = false;
    boolean clickStart = false;

    private void onClickStop() {
        toggleNavigationButton(View.GONE);
//        handlerTimer.removeCallbacks(runnable);

        clickStop = true;
        if (mRecordingServiceBound) {
            //Todo: stop and save recording
            mRecordingStarted = false;
            mService.stopPerformService();
            toggleView(tvTimer, View.GONE);
            CounterUtil.getInstance().stopCounter();
            mImgRec.setImageResource(R.drawable.ic_fab_expand);

            if (mMode == MyUtils.MODE_RECORDING) {
//                        ((RecordingService)mService).insertVideoToGallery();
                MyUtils.sendBroadCastMessageFromService(this, NOTIFY_MSG_RECORDING_STOPPED);
                if (clickStart)
                    MyUtils.toast(getApplicationContext(), "Record saving...", Toast.LENGTH_LONG);
            } else {
                MyUtils.sendBroadCastMessageFromService(this, NOTIFY_MSG_LIVESTREAM_STOPPED);
            }
        } else {
            mRecordingStarted = true;
            MyUtils.toast(getApplicationContext(), "Recording Service connection has not been established", Toast.LENGTH_LONG);
        }
    }

    private void onClickClose(boolean keepRunningService) {
        if (mRecordingStarted) {
            mImgStop.performClick();
        }
        if (!clickStop) onClickStop();
        mService.closePerformService();
        clickStop = false;
        if (mMode == MyUtils.MODE_STREAMING)
            MyUtils.sendBroadCastMessageFromService(this, MyUtils.MESSAGE_DISCONNECT_LIVE);

        if (mMode == MyUtils.MODE_RECORDING)
            MyUtils.sendBroadCastMessageFromService(this, NOTIFY_MSG_RECORDING_CLOSED);

        if (!keepRunningService) {
            stopService();
            SettingManager2.setLiveStreamType(getApplicationContext(), 0);
        }
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


    private void toggleView(View view, int visible) {
        view.setVisibility(visible);
    }

    private void bindStreamingService() {
//        if (DEBUG) Log.i(TAG, "Controller: bindService()");
        Intent service;
        if (mMode == MyUtils.MODE_STREAMING) {
            if (mStreamProfile == null)
                throw new RuntimeException("Streaming proflie is null");

            service = new Intent(getApplicationContext(), StreamingService.class);
            Bundle bundle = new Bundle();

            bundle.putSerializable(MyUtils.STREAM_PROFILE, mStreamProfile);

            service.putExtras(bundle);

        } else {
            service = new Intent(getApplicationContext(), RecordingService.class);
        }

        service.putExtra(Intent.EXTRA_INTENT, mScreenCaptureIntent);

        bindService(service, mStreamingServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection mStreamingServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IBinder binder;
            if (mMode == MyUtils.MODE_STREAMING) {
                binder = (StreamingBinder) service;
                mService = ((StreamingBinder) binder).getService();
                mService.openPerformService();
//                MyUtils.toast(getApplicationContext(), "Livestream service connected", Toast.LENGTH_SHORT);
            } else {
                binder = (RecordingBinder) service;
                mService = ((RecordingBinder) binder).getService();
//                handleStartRecording();
//                MyUtils.toast(getApplicationContext(), "Recording service connected", Toast.LENGTH_SHORT);
            }
            mRecordingServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRecordingServiceBound = false;
            MyUtils.toast(getApplicationContext(), "Service disconnected " + name.toString(), Toast.LENGTH_SHORT);
        }
    };

    private boolean isViewCollapsed() {
        return mViewRoot == null || mViewRoot.findViewById(R.id.imgHome).getVisibility() == View.GONE;
    }

    void toggleNavigationButton(int viewMode) {
        //Todo: make animation here
        mImgStart.setVisibility(viewMode);
        mImgHome.setVisibility(viewMode);
        mImgCapture.setVisibility(viewMode);
        mImgClose.setVisibility(viewMode);
        mImgStop.setVisibility(viewMode);

        if (viewMode != View.GONE) {
            if (mRecordingStarted) {
                mImgStart.setVisibility(View.GONE);
            } else {
                mImgStop.setVisibility(View.GONE);
            }
        }

        if (viewMode == View.VISIBLE) {
            mViewRoot.setPadding(40, 40, 40, 40);
        } else {
            mViewRoot.setPadding(0, 0, 0, 0);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (paramViewRoot.x == 0) mViewRoot.setX(-36);
                    if (paramViewRoot.x == mScreenWidth) mViewRoot.setX(36);
//                    System.out.println("thanhlv mViewRoot.updateViewLayout= " + paramViewRoot.x);
                }
            }, 1500);
//            System.out.println("thanhlv mViewRoot.setPadding(0, 0, 0, 0); " + paramViewRoot.x);

            timerHideFAB = 0;
            handlerTimerFAB.removeCallbacks(runnableFAB);
        }
        mViewBlur.setVisibility(viewMode);
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mViewRoot != null) {
            mWindowManager.removeViewImmediate(mViewRoot);
        }
        if (mCameraLayout != null) {
            mWindowManager.removeView(mCameraLayout);
            releaseCamera();
        }

        if (mService != null && mRecordingServiceBound) {
            unbindService(mStreamingServiceConnection);
            mService.stopSelf();
            mRecordingServiceBound = false;
        }
    }

    private int camSize = 3;

    @Override
    public void zoomOut() {
        camSize++;
        if (camSize > 6) {
            camSize = 6;
            return;
        }
        camWidth = camViewSize[camSize];
        camHeight = camWidth * 4 / 3f;
        cameraPreview.setLayoutParams(new FrameLayout.LayoutParams((int) camWidth, (int) camHeight));
        paramCam.x = (int) (paramCam.x - (camWidth - camViewSize[camSize - 1]) / 2f);
        paramCam.y = (int) (paramCam.y - (camWidth - camViewSize[camSize - 1]) * 2 / 3f);
        mWindowManager.updateViewLayout(mCameraLayout, paramCam);
        hasZoom = true;

    }

    @Override
    public void zoomIn() {
        camSize--;
        if (camSize < 0) {
            camSize = 0;
            return;
        }
        camWidth = camViewSize[camSize];
        camHeight = camWidth * 4 / 3f;
        cameraPreview.setLayoutParams(new FrameLayout.LayoutParams((int) camWidth, (int) camHeight));
        paramCam.x = (int) (paramCam.x + (camViewSize[camSize + 1] - camWidth) / 2f);
        paramCam.y = (int) (paramCam.y + (camViewSize[camSize + 1] - camWidth) * 2 / 3f);
        mWindowManager.updateViewLayout(mCameraLayout, paramCam);
        hasZoom = true;
    }
}
