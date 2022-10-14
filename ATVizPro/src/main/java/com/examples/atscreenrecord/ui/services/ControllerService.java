package com.examples.atscreenrecord.ui.services;

import static com.examples.atscreenrecord.Core.isConnected;
import static com.examples.atscreenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_FACEBOOK;
import static com.examples.atscreenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_TWITCH;
import static com.examples.atscreenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_YOUTUBE;
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
import androidx.core.app.NotificationCompat;
import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.controllers.settings.CameraSetting;
import com.examples.atscreenrecord.controllers.settings.SettingManager;
import com.examples.atscreenrecord.controllers.settings.SettingManager2;
import com.examples.atscreenrecord.ui.activities.MainActivity;
import com.examples.atscreenrecord.ui.services.recording.RecordingService;
import com.examples.atscreenrecord.ui.services.recording.RecordingService.RecordingBinder;
import com.examples.atscreenrecord.ui.services.streaming.StreamingService;
import com.examples.atscreenrecord.ui.services.streaming.StreamingService.StreamingBinder;
import com.examples.atscreenrecord.ui.utils.CameraPreview;
import com.examples.atscreenrecord.ui.utils.CustomOnScaleDetector;
import com.examples.atscreenrecord.ui.utils.MyUtils;
import com.examples.atscreenrecord.ui.utils.NotificationHelper;
import com.examples.atscreenrecord.utils.OnSingleClickListener;
import com.takusemba.rtmppublisher.helper.StreamProfile;

public class ControllerService extends Service implements CustomOnScaleDetector.OnScaleListener {
    private static final String TAG = ControllerService.class.getSimpleName();
//    private final boolean DEBUG = MyUtils.DEBUG;
    private BaseService mService;
    private Boolean mRecordingServiceBound = false;
    private View mViewRoot;
    private View mCameraLayout;
    private WindowManager mWindowManager;
    WindowManager.LayoutParams paramViewRoot;
    WindowManager.LayoutParams paramCam;
    WindowManager.LayoutParams paramCountdown;
    private Intent mScreenCaptureIntent = null;
    private ImageView mImgClose, mImgRec, mImgStart, mImgStop, mImgCapture, mImgSetting;
    private Boolean mRecordingStarted = false;
    private Camera mCamera;
    private LinearLayout cameraPreview;
    private int mScreenWidth, mScreenHeight;
    private TextView mTvCountdown;
    private View mCountdownLayout;
    private int mCameraWidth = 160, mCameraHeight = 120;
    private StreamProfile mStreamProfile;
    private int mMode;

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

    private void updateUI() {
        if (mMode == MyUtils.MODE_STREAMING) {
            int type = SettingManager2.getLiveStreamType(this);
            if (type == SOCIAL_TYPE_YOUTUBE) mImgRec.setBackgroundResource(R.drawable.ic_youtube);
            if (type == SOCIAL_TYPE_FACEBOOK) mImgRec.setBackgroundResource(R.drawable.ic_facebook);
            if (type == SOCIAL_TYPE_TWITCH) mImgRec.setBackgroundResource(R.drawable.ic_twitch);
        } else {
            mImgRec.setBackgroundResource(R.drawable.icon_app);
        }
    }

    private void handleIncomeAction(Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action))
            return;

        switch (action) {
            case MyUtils.ACTION_DISCONNECT_LIVE_FROM_HOME:
                onClickStop();
//                System.out.println("thanhlv ------------- ACTION_DISCONNECT_LIVE_FROM_HOME");
                onClickClose(false);
                break;
            case MyUtils.ACTION_DISCONNECT_WHEN_STOP_LIVE:
                onClickStop();
//                System.out.println("thanhlv ------------- ACTION_DISCONNECT_WHEN_STOP_LIVE");
                onClickClose(true);
                break;
            case MyUtils.ACTION_INIT_CONTROLLER:
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

                updateUI();
                break;

            case MyUtils.ACTION_UPDATE_SETTING:
                handleUpdateSetting(intent);
                break;
            case MyUtils.ACTION_UPDATE_TYPE_LIVE:
                updateUI();
                break;
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
        CameraSetting profile = SettingManager.getCameraProfile(getApplicationContext());
        if (profile.getMode().equals(CameraSetting.CAMERA_MODE_OFF))
            toggleView(mCameraLayout, View.GONE);
        else {
            if (mCameraLayout != null) {
                mWindowManager.removeViewImmediate(mCameraLayout);
                releaseCamera();
                initCameraView();
            }
        }
    }

    private void updateCameraPosition() {
//        if (DEBUG)            Log.i(TAG, "updateCameraPosition: ");
        CameraSetting profile = SettingManager.getCameraProfile(getApplicationContext());
        paramCam.gravity = profile.getParamGravity();
        paramCam.x = 0;
        paramCam.y = 0;
        mWindowManager.updateViewLayout(mCameraLayout, paramCam);
    }

    private void updateCameraSize() {
        CameraSetting profile = SettingManager.getCameraProfile(getApplicationContext());
        calculateCameraSize(profile);
        onConfigurationChanged(getResources().getConfiguration());
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
//        if (DEBUG) Log.i(TAG, "ControllerService: onCreate");
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
        paramCam.y = 0;
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
                            paramCam.y = yy - (int) (event.getRawY() - y);

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

    @SuppressLint("InflateParams")
    private void initializeViews() {
//        if (DEBUG) Log.i(TAG, "StreamingControllerService: initializeViews()");

        mViewRoot = LayoutInflater.from(this).inflate(R.layout.layout_recording, null);

        View mViewCountdown = LayoutInflater.from(this).inflate(R.layout.layout_countdown, null);

        paramViewRoot.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
        paramViewRoot.x = 0;
        paramViewRoot.y = 0;

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mViewCountdown, paramCountdown);
        mWindowManager.addView(mViewRoot, paramViewRoot);

        mCountdownLayout = mViewCountdown.findViewById(R.id.countdown_container);
        mTvCountdown = mViewCountdown.findViewById(R.id.tvCountDown);

        toggleView(mCountdownLayout, View.GONE);

        mImgRec = mViewRoot.findViewById(R.id.imgRec);
        mImgCapture = mViewRoot.findViewById(R.id.imgCapture);
        mImgClose = mViewRoot.findViewById(R.id.imgClose);
        mImgStart = mViewRoot.findViewById(R.id.imgStart);
        mImgSetting = mViewRoot.findViewById(R.id.imgSetting);
        mImgStop = mViewRoot.findViewById(R.id.imgStop);
        toggleView(mImgStop, View.GONE);
        toggleNavigationButton(View.GONE);


        mImgCapture.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
//                MyUtils.toast(getApplicationContext(), "Capture clicked", Toast.LENGTH_SHORT);
                toggleNavigationButton(View.GONE);
                if (mCameraLayout.getVisibility() == View.GONE) {
                    toggleView(mCameraLayout, View.VISIBLE);
                } else {
                    toggleView(mCameraLayout, View.GONE);
                }
            }
        });

        mImgSetting.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                MyUtils.toast(getApplicationContext(), "Go home!", Toast.LENGTH_SHORT);
                toggleNavigationButton(View.GONE);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(MyUtils.ACTION_OPEN_SETTING_ACTIVITY);
                startActivity(intent);

            }
        });

        mImgStart.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
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
                            toggleView(mViewRoot, View.VISIBLE);
                            mRecordingStarted = true;

                            if (mMode == MyUtils.MODE_RECORDING) {
                                mService.startPerformService();
                                MyUtils.toast(getApplicationContext(), "Recording started", Toast.LENGTH_SHORT);
                            } else {
                                if (isConnected) {
                                    mService.startPerformService();
                                    MyUtils.toast(getApplicationContext(), "LiveStreaming started", Toast.LENGTH_SHORT);
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
                    Log.e(TAG, "Recording Service connection has not been established");
                    stopService();
                }
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
                onClickClose(false);

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

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mViewRoot, paramViewRoot);


                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);

                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (Xdiff < 20 && Ydiff < 20) {
                            if (isViewCollapsed()) {
                                //When user clicks on the image view of the collapsed layout,
                                //visibility of the collapsed layout will be changed to "View.GONE"
                                //and expanded view will become visible.
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
        mViewRoot.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    toggleNavigationButton(View.GONE);
            }
        });
    }

    boolean clickStop = false;
    boolean clickStart = false;

    private void onClickStop() {
        toggleNavigationButton(View.GONE);
        clickStop = true;
        if (mRecordingServiceBound) {
            //Todo: stop and save recording
            mRecordingStarted = false;

            mService.stopPerformService();

            if (mMode == MyUtils.MODE_RECORDING) {
//                        ((RecordingService)mService).insertVideoToGallery();
                if (clickStart)
                    MyUtils.toast(getApplicationContext(), "Record saving...", Toast.LENGTH_LONG);
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
        if (!keepRunningService) {
            stopService();
            SettingManager2.setLiveStreamType(getApplicationContext(), 0);
        }
        clickStop = false;
        MyUtils.sendBroadCastMessageFromService(this, MyUtils.MESSAGE_DISCONNECT_LIVE);

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

                MyUtils.toast(getApplicationContext(), "Recording service connected", Toast.LENGTH_SHORT);
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
        return mViewRoot == null || mViewRoot.findViewById(R.id.imgSetting).getVisibility() == View.GONE;
    }

    void toggleNavigationButton(int viewMode) {
        //Todo: make animation here
        mImgStart.setVisibility(viewMode);
        mImgSetting.setVisibility(viewMode);
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
        mViewRoot.setPadding(32, 32, 32, 32);
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
