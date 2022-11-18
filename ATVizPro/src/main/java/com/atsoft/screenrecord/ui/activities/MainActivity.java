package com.atsoft.screenrecord.ui.activities;

import static com.atsoft.screenrecord.Core.isConnected;
import static com.atsoft.screenrecord.ui.fragments.DialogSelectVideoSource.ARG_PARAM1;
import static com.atsoft.screenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_FACEBOOK;
import static com.atsoft.screenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_TWITCH;
import static com.atsoft.screenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_YOUTUBE;
import static com.atsoft.screenrecord.ui.services.ControllerService.NOTIFY_MSG_LIVESTREAM_STARTED;
import static com.atsoft.screenrecord.ui.services.ControllerService.NOTIFY_MSG_LIVESTREAM_STOPPED;
import static com.atsoft.screenrecord.ui.services.ControllerService.NOTIFY_MSG_RECORDING_CLOSED;
import static com.atsoft.screenrecord.ui.services.ControllerService.NOTIFY_MSG_RECORDING_STARTED;
import static com.atsoft.screenrecord.ui.services.ControllerService.NOTIFY_MSG_RECORDING_STOPPED;
import static com.atsoft.screenrecord.ui.services.ControllerService.mRecordingStarted;
import static com.atsoft.screenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_CONNECTION_FAILED;
import static com.atsoft.screenrecord.ui.utils.MyUtils.ACTION_CLOSE_POPUP;
import static com.atsoft.screenrecord.ui.utils.MyUtils.ACTION_GO_HOME;
import static com.atsoft.screenrecord.ui.utils.MyUtils.ACTION_GO_TO_EDIT;
import static com.atsoft.screenrecord.ui.utils.MyUtils.ACTION_GO_TO_PLAY;
import static com.atsoft.screenrecord.ui.utils.MyUtils.KEY_MESSAGE;
import static com.atsoft.screenrecord.ui.utils.MyUtils.MESSAGE_DISCONNECT_LIVE;
import static com.atsoft.screenrecord.ui.utils.MyUtils.hideStatusBar;
import static com.atsoft.screenrecord.ui.utils.MyUtils.isMyServiceRunning;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.atsoft.screenrecord.App;
import com.atsoft.screenrecord.BuildConfig;
import com.atsoft.screenrecord.Core;
import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;
import com.atsoft.screenrecord.ui.fragments.DialogBitrate;
import com.atsoft.screenrecord.ui.fragments.DialogFragmentBase;
import com.atsoft.screenrecord.ui.fragments.DialogFrameRate;
import com.atsoft.screenrecord.ui.fragments.DialogSelectVideoSource;
import com.atsoft.screenrecord.ui.fragments.DialogVideoResolution;
import com.atsoft.screenrecord.ui.fragments.FragmentFAQ;
import com.atsoft.screenrecord.ui.fragments.FragmentSettings;
import com.atsoft.screenrecord.ui.fragments.GuidelineLiveStreamFragment;
import com.atsoft.screenrecord.ui.fragments.GuidelineScreenRecordFragment;
import com.atsoft.screenrecord.ui.fragments.LiveStreamingFragment;
import com.atsoft.screenrecord.ui.fragments.SubscriptionFragment;
import com.atsoft.screenrecord.ui.services.ControllerService;
import com.atsoft.screenrecord.ui.services.ExecuteService;
import com.atsoft.screenrecord.ui.services.recording.RecordingService;
import com.atsoft.screenrecord.ui.services.streaming.StreamingService;
import com.atsoft.screenrecord.ui.utils.MyUtils;
import com.atsoft.screenrecord.utils.AdsUtil;
import com.atsoft.screenrecord.utils.CounterUtil;
import com.atsoft.screenrecord.utils.DisplayUtil;
import com.atsoft.screenrecord.utils.FirebaseUtils;
import com.atsoft.screenrecord.utils.OnSingleClickListener;
import com.atsoft.screenrecord.utils.PathUtil;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.takusemba.rtmppublisher.helper.StreamProfile;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Date;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class MainActivity extends BaseFragmentActivity {
    public static final int REQUEST_VIDEO_FOR_REACT_CAM = 1102;
    public static final int REQUEST_VIDEO_FOR_COMMENTARY = 1105;
    public static final int REQUEST_VIDEO_FOR_VIDEO_EDIT = 1107;
    public static final int REQUEST_SHOW_PROJECTS_DEFAULT = 105;
    private static final String THE_FIRST_TIME_SCREEN_RECORD = "action_first_record";
    private static final String THE_FIRST_TIME_LIVESTREAM = "action_first_livestream";
    public static boolean active = false;
    private static final int PERMISSION_REQUEST_CODE = 3004;
    private static final int PERMISSION_DRAW_OVER_WINDOW = 3005;
    private static final int PERMISSION_RECORD_DISPLAY = 3006;
    private static final int PERMISSION_ACCESS_ALL_FILE = 3007;
    public static final String KEY_PATH_VIDEO = "key_video_selected_path";
    public static final String KEY_VIDEO_NAME = "key_video_selected_name";
    public static final String KEY_FROM_FUNCTION = "key_from_code";

    private static final String[] mPermission = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    public int mMode = MyUtils.MODE_RECORDING;
    private Intent mScreenCaptureIntent = null;
    private int mScreenCaptureResultCode = MyUtils.RESULT_CODE_FAILED;
    private StreamProfile mStreamProfile;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
            mMode = savedInstanceState.getInt(MyUtils.KEY_CONTROLlER_MODE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) handleIncomingRequest(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
        registerSyncServiceReceiver();
    }

    private void registerSyncServiceReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyUtils.ACTION_SEND_MESSAGE_FROM_SERVICE);
        registerReceiver(mMessageReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
        unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        int all_frags = getSupportFragmentManager().getBackStackEntryCount();
        if (all_frags == 0) moveTaskToBack(true);
        else getSupportFragmentManager().popBackStack();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideStatusBar(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        initViews();
        Intent intent = getIntent();
        if (intent != null) handleIncomingRequest(intent);
    }

    private boolean isStarted = false;

    protected void onResume() {
        super.onResume();
        updateService();
        checkShowAd();
    }

    public void updateService() {
        if (isMyServiceRunning(getApplicationContext(), StreamingService.class)) {
            if (DisplayUtil.getDeviceWidthDpi() > 500)
                liveStreaming.setText(getString(R.string.disconnect_livestream));
            else liveStreaming.setText(getString(R.string.disconnect_live__));
            updateUILivestreamHome(mRecordingStarted);
        } else if (isMyServiceRunning(getApplicationContext(), RecordingService.class)) {
            if (!mRecordingStarted) updateUIRecordingHome(false);
            else if (!isStarted) updateUIRecordingHome(true);
        } else showUIDefaultRecord();

    }

    public void showUIDefaultRecord() {
        imgStartLive.setVisibility(View.GONE);
        tvTimer.setVisibility(View.GONE);
        tvDes.setVisibility(View.VISIBLE);
        tvDes.setText(getString(R.string.tap_to_start_nrecording_screen));
        mImgRec.setVisibility(View.VISIBLE);
        mStartRec.setVisibility(View.GONE);
        mStopRec.setVisibility(View.GONE);
        tvStartStop.setVisibility(View.VISIBLE);
        tvStartStop.setText(getString(R.string.start));
        liveStreaming.setText(getString(R.string.livestreaming));
        if (pulsator != null) pulsator.stop();
    }

    public void checkShowAd() {
        mAdManager.loadBanner();
    }

    String navigate_to, video_input = "";

    private void handleIncomingRequest(Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case MyUtils.ACTION_START_CAPTURE_NOW:
                    mImgRec.performClick();
                    break;
                case ACTION_GO_TO_EDIT:
                    navigate_to = ACTION_GO_TO_EDIT;
                    fromFunction = REQUEST_VIDEO_FOR_VIDEO_EDIT;
                    video_input = intent.getStringExtra(KEY_PATH_VIDEO);
                    showMyRecordings(REQUEST_VIDEO_FOR_VIDEO_EDIT, ACTION_GO_TO_EDIT, intent.getStringExtra(KEY_PATH_VIDEO));
                    break;
                case ACTION_GO_TO_PLAY:
                    navigate_to = ACTION_GO_TO_PLAY;
                    fromFunction = REQUEST_SHOW_PROJECTS_DEFAULT;
                    video_input = intent.getStringExtra(KEY_PATH_VIDEO);
                    showMyRecordings(REQUEST_SHOW_PROJECTS_DEFAULT, ACTION_GO_TO_PLAY, intent.getStringExtra(KEY_PATH_VIDEO));
                    break;
                case ACTION_GO_HOME:
                    removeAllFragment();
                    break;
                case ACTION_CLOSE_POPUP:
                    finish();
            }
        }
    }

    private void requestScreenCaptureIntent() {
        if (mScreenCaptureIntent == null) {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), PERMISSION_RECORD_DISPLAY);
            App.ignoreOpenAd = true;
        }
    }

    private ImageView mImgRec, mStartRec, mStopRec, imgStartLive;
    private PulsatorLayout pulsator;
    private TextView liveStreaming, tvStartStop, tvTimer, tvDes;
    private AdsUtil mAdManager;
    private FirebaseAnalytics mFirebaseAnalytics;

    private void initViews() {
        RelativeLayout mAdViewRoot = findViewById(R.id.adView);
        mAdManager = new AdsUtil(this, mAdViewRoot);
        mAdManager.createInterstitialAdmob();
        tvStartStop = findViewById(R.id.tv_start_stop);
        tvDes = findViewById(R.id.title_direction);
        tvTimer = findViewById(R.id.tv_timer_home);

        pulsator = findViewById(R.id.pulsator_main);
        initVideoSettings();
        updateUISettings();

        LinearLayout btn_set_resolution = findViewById(R.id.set_video_resolution);
        LinearLayout bnt_set_bitrate = findViewById(R.id.set_bitrate);
        LinearLayout btn_set_fps = findViewById(R.id.set_frame_rate);
        btn_set_resolution.setOnClickListener(view -> new DialogVideoResolution(this::updateUISettings).show(getSupportFragmentManager(), ""));
        bnt_set_bitrate.setOnClickListener(view -> new DialogBitrate(this::updateUISettings).show(getSupportFragmentManager(), ""));
        btn_set_fps.setOnClickListener(view -> new DialogFrameRate(this::updateUISettings).show(getSupportFragmentManager(), ""));

        ImageView btn_setting = findViewById(R.id.img_settings);
        btn_setting.setOnClickListener(view -> getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_layout_fragment, new FragmentSettings(), "")
                .addToBackStack("")
                .commit());

        mImgRec = findViewById(R.id.img_record);
        mImgRec.setOnClickListener(view -> {
            FirebaseUtils.logEventToFirebase(mFirebaseAnalytics, "", "click_start_record");
            if (isFirstTimeReach(THE_FIRST_TIME_SCREEN_RECORD)) return;
            openRecordService();
        });

        mStartRec = findViewById(R.id.img_start_record);
        mStartRec.setOnClickListener(view -> startRecordFromHome());

        mStopRec = findViewById(R.id.img_stop_record);
        mStopRec.setOnClickListener(view -> stopRecordFromHome());

        imgStartLive = findViewById(R.id.img_start_live_home);
        imgStartLive.setOnClickListener(view -> toggleLiveStreamFromHome());

        liveStreaming = findViewById(R.id.tv_live_streaming);
        ImageView btn_live = findViewById(R.id.img_live);
        btn_live.setOnClickListener(view -> {
            FirebaseUtils.logEventToFirebase(mFirebaseAnalytics, "", "click_livestream");
            if (isFirstTimeReach(THE_FIRST_TIME_LIVESTREAM)) return;
            if (isMyServiceRunning(getApplicationContext(), StreamingService.class)) {

                new AlertDialog.Builder(this)
                        .setTitle("Disconnect livestream!")
                        .setMessage("Do you want to disconnect livestream?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> sendDisconnectToService())
                        .setNegativeButton(android.R.string.no, (dialogInterface, i) -> {
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return;

            }
            showLiveStreamFragment();
        });

        LinearLayout react_cam = findViewById(R.id.ln_react_cam);
        react_cam.setOnClickListener(view -> {
            FirebaseUtils.logEventToFirebase(mFirebaseAnalytics, "", "click_react_cam");
            if (isMyServiceRunning(getApplicationContext(), RecordingService.class)) {
                MyUtils.showSnackBarNotification(mImgRec, "Camera is busy. Please stop screen recording before use this function!", Snackbar.LENGTH_LONG);
                return;
            }
            if (checkExecuteServiceBusy()) return;
            showInterstitialAd(REQUEST_VIDEO_FOR_REACT_CAM);

        });

        LinearLayout btn_editor = findViewById(R.id.ln_btn_video_editor);
        btn_editor.setOnClickListener(view -> {
            FirebaseUtils.logEventToFirebase(mFirebaseAnalytics, "", "click_video_editor");
            if (checkExecuteServiceBusy()) return;
            showInterstitialAd(REQUEST_VIDEO_FOR_VIDEO_EDIT);
        });

        LinearLayout btn_commentary = findViewById(R.id.ln_btn_commentary);
        btn_commentary.setOnClickListener(view -> {
            if (isMyServiceRunning(getApplicationContext(), RecordingService.class)) {
                MyUtils.showSnackBarNotification(mImgRec, "Microphone is busy. Please stop screen recording before use this function!", Snackbar.LENGTH_LONG);
                return;
            }
            FirebaseUtils.logEventToFirebase(mFirebaseAnalytics, "", "click_commentary");
            if (checkExecuteServiceBusy()) return;
            showInterstitialAd(REQUEST_VIDEO_FOR_COMMENTARY);
        });
        LinearLayout btn_projects = findViewById(R.id.ln_btn_projects);
        btn_projects.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                FirebaseUtils.logEventToFirebase(mFirebaseAnalytics, "", "click_my_project");
                showInterstitialAd(REQUEST_SHOW_PROJECTS_DEFAULT);
            }
        });

        LinearLayout lnFAQ = findViewById(R.id.ln_btn_faq);
        lnFAQ.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                showFAQFragment();
            }
        });
    }

    private boolean isLiveStarted = false;

    private void toggleLiveStreamFromHome() {
        if (isMyServiceRunning(getApplicationContext(), StreamingService.class)) {
            if (!isLiveStarted) {
                sendActionToService(MyUtils.ACTION_START_LIVESTREAM_FROM_HOME);
                isLiveStarted = true;
            } else {
                sendActionToService(MyUtils.ACTION_STOP_LIVESTREAM_FROM_HOME);
                isLiveStarted = false;
            }
        }
    }

    public void sendActionToService(String action) {
        if (MyUtils.isMyServiceRunning(MainActivity.this, ControllerService.class)) {
            Intent controller = new Intent(MainActivity.this, ControllerService.class);
            controller.setAction(action);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(controller);
            } else {
                startService(controller);
            }
        }
    }

    public boolean hasAllPermissionForReact() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && Settings.canDrawOverlays(this)) {
            return true;
        } else {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERMISSION_DRAW_OVER_WINDOW);
                App.ignoreOpenAd = true;
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 666);  // Comment 26
        }
        return false;
    }

    public boolean hasAllPermissionForEdit() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (!Environment.isExternalStorageManager()) {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
//                startActivityForResult(intent, PERMISSION_ACCESS_ALL_FILE);
//                App.ignoreOpenAd = true;
//                return false;
//            } else return true;
//        } else {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 777);
        }
//        }
        return false;
    }

    public boolean hasAllPermissionForCommentary() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && Settings.canDrawOverlays(this)) {
            return true;
        } else {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERMISSION_DRAW_OVER_WINDOW);
                App.ignoreOpenAd = true;
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 888);
        }
        return false;
    }

    public void openRecordService() {
        if (isMyServiceRunning(getApplicationContext(), StreamingService.class)) {
            MyUtils.showSnackBarNotification(mImgRec, "LiveStreaming is running!", Snackbar.LENGTH_LONG);
            return;
        }
        if (isMyServiceRunning(getApplicationContext(), RecordingService.class)) {
            MyUtils.showSnackBarNotification(mImgRec, "Recording is running!", Snackbar.LENGTH_LONG);
            return;
        }
        mMode = MyUtils.MODE_RECORDING;
        shouldStartControllerService();
    }

    private void stopRecordFromHome() {
        if (isMyServiceRunning(getApplicationContext(), RecordingService.class)) {
            Intent controller = new Intent(MainActivity.this, ControllerService.class);
            controller.setAction(MyUtils.ACTION_STOP_RECORDING_FROM_HOME);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(controller);
            } else {
                startService(controller);
            }
        }
    }

    private void startRecordFromHome() {
        if (isMyServiceRunning(getApplicationContext(), RecordingService.class)) {
            Intent controller = new Intent(MainActivity.this, ControllerService.class);
            controller.setAction(MyUtils.ACTION_START_RECORDING_FROM_HOME);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(controller);
            } else {
                startService(controller);
            }
        }
    }

    public void showLiveStreamFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_layout_fragment, new LiveStreamingFragment(), "")
                .addToBackStack("")
                .commit();
    }

    public void showFAQFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_layout_fragment, new FragmentFAQ(), "")
                .addToBackStack("")
                .commit();
    }

    private boolean checkExecuteServiceBusy() {
        boolean bb = false;
        if (isMyServiceRunning(this, ExecuteService.class)) {
            bb = true;
            new AlertDialog.Builder(this)
                    .setTitle("Please wait!")
                    .setMessage("Your previous video in processing, please check in status bar!")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        }
        return bb;
    }

    private boolean isFirstTimeReach(String type) {
        if (type.equals(THE_FIRST_TIME_SCREEN_RECORD))
            if (SettingManager2.getFirstTimeRecord(this)) {
                showTutorialScreenRecord();
                return true;
            }
        if (type.equals(THE_FIRST_TIME_LIVESTREAM))
            if (SettingManager2.getFirstTimeLiveStream(this)) {
                showTutorialLiveStream();
                return true;
            }
        return false;
    }

    private void showTutorialScreenRecord() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_layout_fragment, new GuidelineScreenRecordFragment(false))
                .addToBackStack("")
                .commit();
    }

    private void showTutorialLiveStream() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_layout_fragment, new GuidelineLiveStreamFragment(false))
                .addToBackStack("")
                .commit();
    }

    private void updateUISettings() {
        TextView tv_resolution = findViewById(R.id.tv_video_resolution);
        TextView tv_bitrate = findViewById(R.id.tv_bitrate);
        TextView tv_frame_rate = findViewById(R.id.tv_frame_rate);
        tv_resolution.setText(SettingManager2.getVideoResolution(this));
        tv_bitrate.setText(SettingManager2.getVideoBitrate(this));
        tv_frame_rate.setText(SettingManager2.getVideoFPS(this));
    }

    private void initVideoSettings() {
        Core.resolution = SettingManager2.getVideoResolution(this);
        Core.bitrate = SettingManager2.getVideoBitrate(this);
        Core.frameRate = SettingManager2.getVideoFPS(this);
    }

    private void showDialogPickVideo(int requestVideoFor) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PARAM1, requestVideoFor);
        DialogSelectVideoSource.newInstance(new DialogFragmentBase.ISelectVideoSourceListener() {
            @Override
            public void onClickCameraRoll() {
                showDialogPickFromGallery(requestVideoFor);
            }

            @Override
            public void onClickMyRecordings() {
                showMyRecordings(requestVideoFor);
            }
        }, bundle).show(getSupportFragmentManager(), "");
    }

    private void showMyRecordings(int fromFunction) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (!Environment.isExternalStorageManager()) {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
//                startActivityForResult(intent, PERMISSION_ACCESS_ALL_FILE);
//                App.ignoreOpenAd = true;
//                return;
//            }
//        }
        if (hasAllPermissionForEdit()) {
            Intent intent = new Intent(this, ProjectsActivity.class);
            intent.putExtra(KEY_FROM_FUNCTION, fromFunction);
            startActivity(intent);
        }
    }

    private void showMyRecordings(int fromFunction, String navigate, String videoPath) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (!Environment.isExternalStorageManager()) {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
//                startActivityForResult(intent, PERMISSION_ACCESS_ALL_FILE);
//                App.ignoreOpenAd = true;
//                return;
//            }
//        }
        if (hasAllPermissionForEdit()) {
            Intent intent = new Intent(this, ProjectsActivity.class);
            intent.setAction(navigate);
            intent.putExtra(KEY_FROM_FUNCTION, fromFunction);
            intent.putExtra(KEY_PATH_VIDEO, videoPath);
            startActivity(intent);
        }
    }

    public void showDialogPickFromGallery(int from_code) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setTypeAndNormalize("video/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_video_source)), from_code);
        App.ignoreOpenAd = true;
    }

    private int fromFunction;
    public FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
        @Override
        public void onAdClicked() {
        }

        @Override
        public void onAdDismissedFullScreenContent() {
            AdsUtil.lastTime = (new Date()).getTime();
            if (fromFunction == REQUEST_SHOW_PROJECTS_DEFAULT)
                showMyRecordings(REQUEST_SHOW_PROJECTS_DEFAULT);
            if (fromFunction == REQUEST_VIDEO_FOR_REACT_CAM && hasAllPermissionForReact())
                showDialogPickVideo(fromFunction);
            if (fromFunction == REQUEST_VIDEO_FOR_COMMENTARY && hasAllPermissionForCommentary())
                showDialogPickVideo(fromFunction);
            if (fromFunction == REQUEST_VIDEO_FOR_VIDEO_EDIT && hasAllPermissionForEdit())
                showDialogPickVideo(fromFunction);
            mAdManager.createInterstitialAdmob();
        }

        @Override
        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
            if (fromFunction == REQUEST_SHOW_PROJECTS_DEFAULT) {
                showMyRecordings(REQUEST_SHOW_PROJECTS_DEFAULT);
                return;
            }
            if (fromFunction == REQUEST_VIDEO_FOR_REACT_CAM && hasAllPermissionForReact()) {
                showDialogPickVideo(fromFunction);
                return;
            }
            if (fromFunction == REQUEST_VIDEO_FOR_COMMENTARY && hasAllPermissionForCommentary()) {
                showDialogPickVideo(fromFunction);
                return;
            }
            if (fromFunction == REQUEST_VIDEO_FOR_VIDEO_EDIT && hasAllPermissionForEdit()) {
                showDialogPickVideo(fromFunction);
            }
        }

        @Override
        public void onAdImpression() {
        }

        @Override
        public void onAdShowedFullScreenContent() {
            if (Core.countAdsShown == 5) {
                String action;
                switch (fromFunction) {
                    case REQUEST_VIDEO_FOR_COMMENTARY:
                        action = "Commentary";
                        break;
                    case REQUEST_VIDEO_FOR_REACT_CAM:
                        action = "React Cam";
                        break;
                    case REQUEST_VIDEO_FOR_VIDEO_EDIT:
                        action = "Video Editor";
                        break;
                    default:
                        action = "My Project";
                }
                FirebaseUtils.logEventShowInterstitialAd(mFirebaseAnalytics, action);
                Core.countAdsShown = 0;
            }
            Core.countAdsShown++;
        }
    };

    public void showInterstitialAd(int from_code) {
        fromFunction = from_code;
        if (mAdManager.interstitialAdAlready()) {
            mAdManager.showInterstitialAd(fullScreenContentCallback);
        } else {
            if (from_code == REQUEST_SHOW_PROJECTS_DEFAULT) {
                showMyRecordings(from_code);
                return;
            }
            if (from_code == REQUEST_VIDEO_FOR_REACT_CAM && hasAllPermissionForReact()) {
                showDialogPickVideo(from_code);
                return;
            }
            if (from_code == REQUEST_VIDEO_FOR_COMMENTARY && hasAllPermissionForCommentary()) {
                showDialogPickVideo(from_code);
                return;
            }
            if (from_code == REQUEST_VIDEO_FOR_VIDEO_EDIT && hasAllPermissionForEdit()) {
                showDialogPickVideo(from_code);
            }
        }
    }

    private void requestPermissions() {
        // PERMISSION DRAW OVER
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, PERMISSION_DRAW_OVER_WINDOW);
            App.ignoreOpenAd = true;
        }
        ActivityCompat.requestPermissions(this, mPermission, PERMISSION_REQUEST_CODE);
    }

    public void showPopupGo2DeviceSettings(int type) {
        if (type == 1) {//manual
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Change Permissions in Settings");
            alertDialogBuilder
                    .setMessage("" +
                            "\nClick SETTINGS to Manually Set\n" + "Permissions to use this function")
                    .setCancelable(true)
                    .setPositiveButton("SETTINGS", (dialog, id) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, 1000);
                        App.ignoreOpenAd = true;
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        if (type == 2) { // retry second time
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Second Chance");
            alertDialogBuilder
                    .setMessage("Click RETRY to Set Permissions to Allow\n\n" + "Click EXIT to the Close App")
                    .setCancelable(true)
                    .setPositiveButton("RETRY", (dialog, id) -> {
                        Intent i = new Intent(MainActivity.this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        App.ignoreOpenAd = true;
                    })
                    .setNegativeButton("EXIT", (dialog, id) -> {
                        dialog.cancel();
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        App.ignoreOpenAd = true;
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    int granted = PackageManager.PERMISSION_GRANTED;
                    for (int grantResult : grantResults) {
                        if (grantResult != granted) {
                            MyUtils.showSnackBarNotification(mImgRec, "Please grant all permissions to record screen.", Snackbar.LENGTH_LONG);
                            showPopupGo2DeviceSettings(1);
                            return;
                        }
                    }
                }
                break;
            case 666: // Allowed was selected so Permission granted for React
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    // do your work here
                    showDialogPickVideo(fromFunction);
                } else if (!shouldShowRequestPermissionRationale(permissions[0])
                        || !shouldShowRequestPermissionRationale(permissions[1])
                        || !shouldShowRequestPermissionRationale(permissions[2])
                        || !shouldShowRequestPermissionRationale(permissions[3])) {
                    // User selected the Never Ask Again Option Change settings in app settings manually
                    showPopupGo2DeviceSettings(1);
                } else {
                    // User selected Deny Dialog to EXIT App ==> OR <== RETRY to have a second chance to Allow Permissions
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                            || checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED
                            || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                            || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        showPopupGo2DeviceSettings(2);
                    }
                }
                break;

            case 888: // Allowed was selected so Permission granted for Commentary
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    // do your work here
                    showDialogPickVideo(fromFunction);
                } else if (!shouldShowRequestPermissionRationale(permissions[0])
                        || !shouldShowRequestPermissionRationale(permissions[1])
                        || !shouldShowRequestPermissionRationale(permissions[2])) {
                    // User selected the Never Ask Again Option Change settings in app settings manually
                    showPopupGo2DeviceSettings(1);
                } else {
                    // User selected Deny Dialog to EXIT App ==> OR <== RETRY to have a second chance to Allow Permissions
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED
                            || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                            || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        showPopupGo2DeviceSettings(2);
                    }
                }
                break;
            case 777: // Allowed was selected so Permission granted for Edit
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // do your work here
                    showDialogPickVideo(fromFunction);
                } else if (!shouldShowRequestPermissionRationale(permissions[0])
                        || !shouldShowRequestPermissionRationale(permissions[1])) {
                    // User selected the Never Ask Again Option Change settings in app settings manually
                    showPopupGo2DeviceSettings(1);
                } else {
                    // User selected Deny Dialog to EXIT App ==> OR <== RETRY to have a second chance to Allow Permissions
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                            || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        showPopupGo2DeviceSettings(2);
                    }
                }
                break;
        }
    }

    public void shouldStartControllerService() {
        if (!hasCaptureIntent())
            requestScreenCaptureIntent();
        if (hasPermission()) {
            startControllerService();
        } else {
            requestPermissions();
        }
    }

    private boolean hasCaptureIntent() {
        return mScreenCaptureIntent != null;// || mScreenCaptureResultCode == MyUtils.RESULT_CODE_FAILED;
    }

    String pathVideo = "";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        App.ignoreOpenAd = true;
        if (requestCode == REQUEST_VIDEO_FOR_REACT_CAM && resultCode == RESULT_OK) {
            final Uri selectedUri = data != null ? data.getData() : null;
            if (selectedUri != null) {
                try {
                    pathVideo = PathUtil.getPath(this, selectedUri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                if (!MyUtils.isVideo(this, pathVideo)) return;
                if (!new File(pathVideo).exists()) {
                    Toast.makeText(this, "File not found.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (MyUtils.getDurationMs(this, pathVideo) > 60000 && !SettingManager2.isProApp(this)) {
                    runOnUiThread(() -> getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.frame_layout_fragment,
                                    new SubscriptionFragment(() -> runOnUiThread(() -> {
                                        if (mAdManager != null) mAdManager.loadBanner();
                                        Intent intent = new Intent(MainActivity.this, ReactCamActivity.class);
                                        intent.putExtra(KEY_PATH_VIDEO, pathVideo);
                                        startActivity(intent);
                                    })))
                            .addToBackStack("")
                            .commitAllowingStateLoss());

                    return;
                }

                Intent intent = new Intent(MainActivity.this, ReactCamActivity.class);
                intent.putExtra(KEY_PATH_VIDEO, pathVideo);
                startActivity(intent);
            }
        }

        if (requestCode == REQUEST_VIDEO_FOR_COMMENTARY && resultCode == RESULT_OK) {
            final Uri selectedUri = data != null ? data.getData() : null;

            if (selectedUri != null) {
                try {
                    pathVideo = PathUtil.getPath(this, selectedUri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                if (!MyUtils.isVideo(this, pathVideo)) return;
                if (!new File(pathVideo).exists()) {
                    Toast.makeText(this, "File not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (MyUtils.getDurationMs(this, pathVideo) > 60000 && !SettingManager2.isProApp(this)) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.frame_layout_fragment,
                                    new SubscriptionFragment(() -> runOnUiThread(() -> {
                                        if (mAdManager != null) mAdManager.loadBanner();
                                        Intent intent = new Intent(MainActivity.this, CommentaryActivity.class);
                                        intent.putExtra(KEY_PATH_VIDEO, pathVideo);
                                        startActivity(intent);
                                    })))
                            .addToBackStack("")
                            .commitAllowingStateLoss();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, CommentaryActivity.class);
                intent.putExtra(KEY_PATH_VIDEO, pathVideo);
                startActivity(intent);
            }
        }

        if (requestCode == REQUEST_VIDEO_FOR_VIDEO_EDIT && resultCode == RESULT_OK) {
            final Uri selectedUri = data != null ? data.getData() : null;

            if (selectedUri != null) {
                try {
                    pathVideo = PathUtil.getPath(this, selectedUri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                if (!MyUtils.isVideo(this, pathVideo)) return;
                if (!new File(pathVideo).exists()) {
                    Toast.makeText(this, "File not found.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (MyUtils.getDurationMs(this, pathVideo) > 60000 && !SettingManager2.isProApp(this)) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.frame_layout_fragment,
                                    new SubscriptionFragment(() -> runOnUiThread(() -> {
                                        if (mAdManager != null) mAdManager.loadBanner();
                                        Bundle bundle = new Bundle();
                                        bundle.putString(KEY_PATH_VIDEO, pathVideo);
                                        Intent intent = new Intent(MainActivity.this, VideoEditorActivity.class);
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                    })))
                            .addToBackStack("")
                            .commitAllowingStateLoss();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString(KEY_PATH_VIDEO, pathVideo);
                Intent intent = new Intent(MainActivity.this, VideoEditorActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }

//        if (requestCode == PERMISSION_ACCESS_ALL_FILE) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                //Check if the permission is granted or not.
//                if (!Environment.isExternalStorageManager()) { //Permission is not available
//                    MyUtils.showSnackBarNotification(mImgRec, "Manager files permission not available.", Snackbar.LENGTH_SHORT);
//                } else {
//                    if (fromFunction == REQUEST_VIDEO_FOR_VIDEO_EDIT && !video_input.contains(".")) {
//                        showDialogPickVideo(fromFunction);
//                    } else {
//                        showMyRecordings(fromFunction, navigate_to, video_input);
//                    }
//                }
//            }
//        }

        if (requestCode == PERMISSION_DRAW_OVER_WINDOW) {
            //Check if the permission is granted or not.
            if (!Settings.canDrawOverlays(this)) { //Permission is not available
                MyUtils.showSnackBarNotification(mImgRec, "Draw over other app permission not available.", Snackbar.LENGTH_SHORT);
            }
        } else if (requestCode == PERMISSION_RECORD_DISPLAY) {
            if (resultCode != RESULT_OK) {
                MyUtils.showSnackBarNotification(mImgRec, "Recording display permission not available.", Snackbar.LENGTH_SHORT);
                mScreenCaptureIntent = null;
            } else {
                mScreenCaptureIntent = data;
                if (mScreenCaptureIntent != null)
                    mScreenCaptureIntent.putExtra(MyUtils.SCREEN_CAPTURE_INTENT_RESULT_CODE, resultCode);
                mScreenCaptureResultCode = resultCode;
                shouldStartControllerService();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void sendDisconnectToService() {
        if (isMyServiceRunning(getApplicationContext(), StreamingService.class)) {
            Intent controller = new Intent(MainActivity.this, ControllerService.class);
            controller.setAction(MyUtils.ACTION_DISCONNECT_LIVE_FROM_HOME);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(controller);
            } else {
                startService(controller);
            }
        }
    }

    public void updateIconService() {
        updateService();
        if (isMyServiceRunning(getApplicationContext(), StreamingService.class)) {
            Intent controller = new Intent(MainActivity.this, ControllerService.class);
            controller.setAction(MyUtils.ACTION_UPDATE_TYPE_LIVE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(controller);
            } else {
                startService(controller);
            }
        }
    }

    private void startControllerService() {
        Intent controller = new Intent(MainActivity.this, ControllerService.class);
        controller.setAction(MyUtils.ACTION_INIT_CONTROLLER);
        controller.putExtra(MyUtils.KEY_CAMERA_AVAILABLE, checkCameraHardware(this));
        controller.putExtra(MyUtils.KEY_CONTROLlER_MODE, mMode);
        controller.putExtra(Intent.EXTRA_INTENT, mScreenCaptureIntent);
        if (mMode == MyUtils.MODE_STREAMING) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(MyUtils.STREAM_PROFILE, mStreamProfile);
            controller.putExtras(bundle);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(controller);
        } else {
            startService(controller);
        }
    }

    public void removeAllFragment() {
        FragmentManager fm = getSupportFragmentManager();
        if (!fm.isStateSaved())
            for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                getSupportFragmentManager().popBackStack();
            }
    }

    /**
     * Check if this device has a camera
     */
    @SuppressLint("UnsupportedChromeOsCameraSystemFeature")
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public boolean hasPermission() {
        int granted = PackageManager.PERMISSION_GRANTED;
        return ContextCompat.checkSelfPermission(this, mPermission[0]) == granted
                && ContextCompat.checkSelfPermission(this, mPermission[1]) == granted
                && ContextCompat.checkSelfPermission(this, mPermission[2]) == granted
                && ContextCompat.checkSelfPermission(this, mPermission[3]) == granted
                && Settings.canDrawOverlays(this)
                && mScreenCaptureIntent != null
                && mScreenCaptureResultCode != MyUtils.RESULT_CODE_FAILED;
    }

    public void setStreamProfile(StreamProfile streamProfile) {
        this.mStreamProfile = streamProfile;
    }

    public void notifyUpdateStreamProfile(String url) {
        if (mMode == MyUtils.MODE_STREAMING) {
            Intent controller = new Intent(MainActivity.this, ControllerService.class);
            controller.setAction(MyUtils.ACTION_UPDATE_STREAM_PROFILE);
            controller.putExtra(MyUtils.NEW_URL, url);
            Bundle bundle = new Bundle();
            bundle.putSerializable(MyUtils.STREAM_PROFILE, mStreamProfile);
            controller.putExtras(bundle);
            startService(controller);
        }
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action) && MyUtils.ACTION_SEND_MESSAGE_FROM_SERVICE.equals(action)) {
                String notify_msg = intent.getStringExtra(KEY_MESSAGE);
                if (TextUtils.isEmpty(notify_msg)) return;
                if (NOTIFY_MSG_CONNECTION_FAILED.equals(notify_msg)) {
                    updateService();
                    showUIDefaultRecord();
                    isStarted = false;
                }
                if (MESSAGE_DISCONNECT_LIVE.equals(notify_msg)) {
                    isConnected = false;
                    updateService();
                }
                if (NOTIFY_MSG_RECORDING_STOPPED.equals(notify_msg)) updateUIRecordingHome(false);
                if (NOTIFY_MSG_RECORDING_STARTED.equals(notify_msg)) updateUIRecordingHome(true);
                if (NOTIFY_MSG_LIVESTREAM_STARTED.equals(notify_msg)) updateUILivestreamHome(true);
                if (NOTIFY_MSG_LIVESTREAM_STOPPED.equals(notify_msg)) updateUILivestreamHome(false);
                if (NOTIFY_MSG_RECORDING_CLOSED.equals(notify_msg)) {
                    showUIDefaultRecord();
                    isStarted = false;
                }
            }
        }
    };

    private void updateUIRecordingHome(boolean started) {
        if (!MyUtils.isMyServiceRunning(MainActivity.this, RecordingService.class)) return;
        if (started) {
            pulsator.start();
            CounterUtil.getInstance().setCallback((CounterUtil.ICounterUtil2) sec -> tvTimer.setText(sec));
            mImgRec.setVisibility(View.GONE);
            mStartRec.setVisibility(View.GONE);
            mStopRec.setVisibility(View.VISIBLE);
            tvStartStop.setText(getString(R.string.stop));
            tvDes.setVisibility(View.GONE);
            tvTimer.setVisibility(View.VISIBLE);
            isStarted = true;
        } else {
            mImgRec.setVisibility(View.GONE);
            mStartRec.setVisibility(View.VISIBLE);
            mStopRec.setVisibility(View.GONE);
            tvStartStop.setText(getString(R.string.start));
            tvDes.setVisibility(View.VISIBLE);
            tvTimer.setVisibility(View.GONE);
            pulsator.stop();
            isStarted = false;
        }
    }

    int typeLive;

    public void updateUILivestreamHome(boolean started) {
        if (!MyUtils.isMyServiceRunning(MainActivity.this, StreamingService.class)) return;
        typeLive = SettingManager2.getLiveStreamType(this);
        mImgRec.setVisibility(View.GONE);
        mStartRec.setVisibility(View.GONE);
        mStopRec.setVisibility(View.GONE);
        imgStartLive.setVisibility(View.VISIBLE);
        tvStartStop.setVisibility(View.GONE);
        tvDes.setVisibility(View.VISIBLE);
        tvDes.setText(getString(R.string.tap_to_start_nlivestream));
        if (started) {
            pulsator.start();
            tvDes.setVisibility(View.GONE);
            tvTimer.setVisibility(View.VISIBLE);
            CounterUtil.getInstance().setCallback((CounterUtil.ICounterUtil2) sec -> tvTimer.setText(sec));
            if (typeLive == SOCIAL_TYPE_YOUTUBE)
                imgStartLive.setBackgroundResource(R.drawable.ic_yt_live_home_load);
            if (typeLive == SOCIAL_TYPE_FACEBOOK)
                imgStartLive.setBackgroundResource(R.drawable.ic_fb_live_home_load);
            if (typeLive == SOCIAL_TYPE_TWITCH)
                imgStartLive.setBackgroundResource(R.drawable.ic_tw_live_home_load);
            isLiveStarted = true;
        } else {
            if (typeLive == SOCIAL_TYPE_YOUTUBE)
                imgStartLive.setBackgroundResource(R.drawable.ic_yt_live_home);
            if (typeLive == SOCIAL_TYPE_FACEBOOK)
                imgStartLive.setBackgroundResource(R.drawable.ic_fb_live_home);
            if (typeLive == SOCIAL_TYPE_TWITCH)
                imgStartLive.setBackgroundResource(R.drawable.ic_tw_live_home);
            tvDes.setVisibility(View.VISIBLE);
            tvTimer.setVisibility(View.GONE);
            pulsator.stop();
            isLiveStarted = false;
        }
    }
}

