package com.examples.atvizpro.ui.activities;

import static com.examples.atvizpro.ui.activities.CompressBeforeReactCamActivity.VIDEO_PATH_KEY;
import static com.examples.atvizpro.ui.fragments.DialogSelectVideoSource.ARG_PARAM1;
import static com.examples.atvizpro.ui.utils.MyUtils.hideStatusBar;
import static com.examples.atvizpro.ui.utils.MyUtils.isMyServiceRunning;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.examples.atvizpro.Constants;
import com.examples.atvizpro.Core;
import com.examples.atvizpro.R;
import com.examples.atvizpro.controllers.settings.SettingManager2;
import com.examples.atvizpro.ui.fragments.DialogBitrate;
import com.examples.atvizpro.ui.fragments.DialogFragmentBase;
import com.examples.atvizpro.ui.fragments.DialogFrameRate;
import com.examples.atvizpro.ui.fragments.DialogSelectVideoSource;
import com.examples.atvizpro.ui.fragments.DialogVideoResolution;
import com.examples.atvizpro.ui.fragments.FragmentFAQ;
import com.examples.atvizpro.ui.fragments.FragmentSettings;
import com.examples.atvizpro.ui.fragments.LiveStreamingFragment;
import com.examples.atvizpro.ui.fragments.ProjectsFragment;
import com.examples.atvizpro.ui.services.ControllerService;
import com.examples.atvizpro.ui.services.streaming.StreamingService;
import com.examples.atvizpro.ui.utils.MyUtils;
import com.examples.atvizpro.utils.PathUtil;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;
import com.takusemba.rtmppublisher.helper.StreamProfile;

import java.net.URISyntaxException;
import java.util.Objects;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_VIDEO_FOR_REACT_CAM = 1102;
    public static final int REQUEST_VIDEO_FOR_COMMENTARY = 1105;
    public static final int REQUEST_VIDEO_FOR_VIDEO_EDIT = 1107;
    public static boolean active = false;
    private static final boolean DEBUG = MyUtils.DEBUG;
    private static final int PERMISSION_REQUEST_CODE = 3004;
    private static final int PERMISSION_DRAW_OVER_WINDOW = 3005;
    private static final int PERMISSION_RECORD_DISPLAY = 3006;
    private static String[] mPermission = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public int mMode = MyUtils.MODE_RECORDING;

    private Intent mScreenCaptureIntent = null;


    private int mScreenCaptureResultCode = MyUtils.RESULT_CODE_FAILED;

    private StreamProfile mStreamProfile;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MyUtils.KEY_CONTROLlER_MODE, mMode);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState!=null){
            mMode = savedInstanceState.getInt(MyUtils.KEY_CONTROLlER_MODE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(this);
        setContentView(R.layout.activity_main);

        initAds();
        initViews();

        Intent intent = getIntent();
        if(intent!=null)
            handleIncomingRequest(intent);
    }

    private void initAds() {

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdView mAdView = findViewById(R.id.adView);


        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    private void handleIncomingRequest(Intent intent) {
        if(intent != null) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case MyUtils.ACTION_START_CAPTURE_NOW:
                    mImgRec.performClick();
                    break;
            }
        }
    }

    private void requestScreenCaptureIntent() {
        if(mScreenCaptureIntent == null){
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), PERMISSION_RECORD_DISPLAY);
        }
    }

    private ImageView mImgRec, mImgFAQ, btn_setting;

    private LinearLayout btn_set_resolution, bnt_set_bitrate, btn_set_fps;

    private void initViews() {

        // initialise pulsator layout
        PulsatorLayout pulsator = (PulsatorLayout) findViewById(R.id.pulsator);
        pulsator.start();


        //initData()
        generateVideoSettings();
        updateUI();

        //
        mImgRec =  findViewById(R.id.img_record);
        btn_setting =  findViewById(R.id.img_settings);
        btn_set_resolution =  findViewById(R.id.set_video_resolution);
        bnt_set_bitrate =  findViewById(R.id.set_bitrate);
        btn_set_fps =  findViewById(R.id.set_frame_rate);


        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.frame_layout_fragment, new FragmentSettings(), "")
                        .addToBackStack("")
                        .commit();

            }
        });

        btn_set_resolution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DialogVideoResolution(new DialogFragmentBase.IVideoSettingListener() {
                    @Override
                    public void onClick() {
                        updateUI();
                    }
                }).show(getSupportFragmentManager(), "");
            }
        });

        bnt_set_bitrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DialogBitrate(new DialogFragmentBase.IVideoSettingListener() {
                    @Override
                    public void onClick() {
                        updateUI();
                    }
                }).show(getSupportFragmentManager(), "");
            }
        });

        btn_set_fps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DialogFrameRate(new DialogFragmentBase.IVideoSettingListener() {
                    @Override
                    public void onClick() {
                        updateUI();
                    }
                }).show(getSupportFragmentManager(), "");
            }
        });


        mImgRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMyServiceRunning(getApplicationContext(), StreamingService.class))
                {
                    MyUtils.showSnackBarNotification(mImgRec, "You are in Streaming Mode. Please close stream controller", Snackbar.LENGTH_INDEFINITE);
                    return;
                }
                if(isMyServiceRunning(getApplicationContext(), ControllerService.class)){
                    MyUtils.showSnackBarNotification(mImgRec,"Recording service is running!", Snackbar.LENGTH_LONG);
                    return;
                }
                mMode = MyUtils.MODE_RECORDING;

                shouldStartControllerService();

            }
        });

        LinearLayout lnFAQ = findViewById(R.id.ln_btn_faq);
        lnFAQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.frame_layout_fragment, new FragmentFAQ(), "")
                        .addToBackStack("")
                        .commit();
            }
        });

        LinearLayout react_cam = findViewById(R.id.ln_react_cam);
        react_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogPickVideo(REQUEST_VIDEO_FOR_REACT_CAM);
            }
        });

        ImageView btn_live = findViewById(R.id.img_live);
        btn_live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.frame_layout_fragment, new LiveStreamingFragment(), "")
                        .addToBackStack("")
                        .commit();
            }
        });


        LinearLayout btn_commentary = findViewById(R.id.ln_btn_commentary);
        btn_commentary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogPickVideo(REQUEST_VIDEO_FOR_COMMENTARY);
            }
        });

        LinearLayout btn_projects = findViewById(R.id.ln_btn_projects);
        btn_projects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.frame_layout_fragment, new ProjectsFragment(), "")
                        .addToBackStack("")
                        .commit();
            }
        });

        LinearLayout btn_editor = findViewById(R.id.ln_btn_video_editor);
        btn_editor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogPickVideo(REQUEST_VIDEO_FOR_VIDEO_EDIT);
            }
        });

    }

    private void updateUI() {

        TextView tv_resolution = findViewById(R.id.tv_video_resolution);
        TextView tv_bitrate = findViewById(R.id.tv_bitrate);
        TextView tv_frame_rate = findViewById(R.id.tv_frame_rate);

        tv_resolution.setText(SettingManager2.getVideoResolution(this));
        tv_bitrate.setText(SettingManager2.getVideoBitrate(this));
        tv_frame_rate.setText(SettingManager2.getVideoFPS(this));
    }

    private void generateVideoSettings() {
//        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);

        Core.resolution = SettingManager2.getVideoResolution(this);
        Core.bitrate = SettingManager2.getVideoBitrate(this);
        Core.frameRate =  SettingManager2.getVideoFPS(this);
    }

    private void showDialogPickVideo(int requestVideoFor) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PARAM1, requestVideoFor);
        DialogSelectVideoSource.newInstance(new DialogFragmentBase.ISelectVideoSourceListener() {
            @Override
            public void onClick() {
            }

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

    private void showMyRecordings(int from_code) {

        Bundle bundle = new Bundle();
        bundle.putInt("key_from_code", from_code);
//        ProjectsFragment projectsFragment = ProjectsFragment.newInstance(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_layout_fragment, ProjectsFragment.newInstance(bundle), "")
                .addToBackStack("")
                .commit();
    }

    public void showDialogPickFromGallery(int from_code) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI); // todo: this thing might need some work :/, eg open from google drive, stuff like that
        intent.setTypeAndNormalize("video/*");
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_video_source)), from_code);
    }

    private void requestPermissions() {

        // PERMISSION DRAW OVER
        if(!Settings.canDrawOverlays(this)){
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, PERMISSION_DRAW_OVER_WINDOW);
        }
        ActivityCompat.requestPermissions(this, mPermission, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                int granted = PackageManager.PERMISSION_GRANTED;
                for (int grantResult : grantResults) {
                    if (grantResult != granted) {
                        MyUtils.showSnackBarNotification(mImgRec, "Please grant all permissions to record screen.", Snackbar.LENGTH_LONG);
                        return;
                    }
                }
//                shouldStartControllerService();
            }
        }
    }

    public void shouldStartControllerService() {
        if(!hasCaptureIntent())
            requestScreenCaptureIntent();

        if(hasPermission()) {
            startControllerService();
        }
        else{
            requestPermissions();
//            if(!hasCaptureIntent())
//                requestScreenCaptureIntent();
        }
    }

    private boolean hasCaptureIntent() {
        return mScreenCaptureIntent != null;// || mScreenCaptureResultCode == MyUtils.RESULT_CODE_FAILED;
    }

    public static final String KEY_PATH_VIDEO = "key_video_selected_path";
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_VIDEO_FOR_REACT_CAM && resultCode == RESULT_OK) {
            System.out.println("thanhlv REQUEST_VIDEO_TRIMMER");
            final Uri selectedUri = data.getData();

            if (selectedUri != null) {
                String pathVideo = "";
                try {
                    pathVideo = PathUtil.getPath(this, selectedUri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                Bundle bundle = new Bundle();
                bundle.putString(VIDEO_PATH_KEY, pathVideo);
                Intent intent = new Intent(MainActivity.this, CompressBeforeReactCamActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            } else {
//                Toast.makeText(MainActivity.this, "R.string.toast_cannot_retrieve_selected_video", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_VIDEO_FOR_COMMENTARY && resultCode == RESULT_OK) {
            System.out.println("thanhlv REQUEST_VIDEO_FOR_COMMENTARY");
            final Uri selectedUri = data.getData();

            if (selectedUri != null) {
                String pathVideo = "";

                try {
                    pathVideo = PathUtil.getPath(this, selectedUri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                System.out.println("thanhlv REQUEST_VIDEO_FOR_COMMENTARY === " + pathVideo);
                Bundle bundle = new Bundle();
                bundle.putString(VIDEO_PATH_KEY, pathVideo);
                Intent intent = new Intent(MainActivity.this, CommentaryActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }


        if (requestCode == REQUEST_VIDEO_FOR_VIDEO_EDIT && resultCode == RESULT_OK) {
            System.out.println("thanhlv REQUEST_VIDEO_FOR_COMMENTARY");
            final Uri selectedUri = data.getData();

            if (selectedUri != null) {
                String pathVideo = "";

                try {
                    pathVideo = PathUtil.getPath(this, selectedUri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                System.out.println("thanhlv REQUEST_VIDEO_FOR_COMMENTARY === " + pathVideo);
                Bundle bundle = new Bundle();
                bundle.putString(VIDEO_PATH_KEY, pathVideo);
                Intent intent = new Intent(MainActivity.this, VideoEditorActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }


        if (requestCode == PERMISSION_DRAW_OVER_WINDOW) {

            //Check if the permission is granted or not.
            if (resultCode != RESULT_OK) { //Permission is not available
                MyUtils.showSnackBarNotification(mImgRec, "Draw over other app permission not available.",Snackbar.LENGTH_SHORT);
            }
        }
        else if( requestCode == PERMISSION_RECORD_DISPLAY) {
            if(resultCode != RESULT_OK){
                MyUtils.showSnackBarNotification(mImgRec, "Recording display permission not available.",Snackbar.LENGTH_SHORT);
                mScreenCaptureIntent = null;
            }
            else{
                mScreenCaptureIntent = data;
                mScreenCaptureIntent.putExtra(MyUtils.SCREEN_CAPTURE_INTENT_RESULT_CODE, resultCode);
                mScreenCaptureResultCode = resultCode;

                shouldStartControllerService();
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startControllerService() {
        Intent controller = new Intent(MainActivity.this, ControllerService.class);

        controller.setAction(MyUtils.ACTION_INIT_CONTROLLER);

        controller.putExtra(MyUtils.KEY_CAMERA_AVAILABLE, checkCameraHardware(this));

        controller.putExtra(MyUtils.KEY_CONTROLlER_MODE, mMode);

        controller.putExtra(Intent.EXTRA_INTENT, mScreenCaptureIntent);

        if(mMode == MyUtils.MODE_STREAMING) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(MyUtils.STREAM_PROFILE, mStreamProfile);
            controller.putExtras(bundle);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(controller);
        }
        else{
            startService(controller);
        }

        if(mMode==MyUtils.MODE_RECORDING)
            finish();
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean hasPermission(){
        int granted = PackageManager.PERMISSION_GRANTED;

        return ContextCompat.checkSelfPermission(this, mPermission[0]) == granted
                && ContextCompat.checkSelfPermission(this, mPermission[1]) == granted
                && ContextCompat.checkSelfPermission(this, mPermission[2]) == granted
                && Settings.canDrawOverlays(this)
                && mScreenCaptureIntent != null
                && mScreenCaptureResultCode != MyUtils.RESULT_CODE_FAILED;
    }

    public void setStreamProfile(StreamProfile streamProfile) {
        this.mStreamProfile = streamProfile;

    }

    public void notifyUpdateStreamProfile() {
        if(mMode == MyUtils.MODE_STREAMING){
            Intent controller = new Intent(MainActivity.this, ControllerService.class);

            controller.setAction(MyUtils.ACTION_UPDATE_STREAM_PROFILE);
            Bundle bundle = new Bundle();
            bundle.putSerializable(MyUtils.STREAM_PROFILE, mStreamProfile);
            controller.putExtras(bundle);
            startService(controller);
        }
    }


}

