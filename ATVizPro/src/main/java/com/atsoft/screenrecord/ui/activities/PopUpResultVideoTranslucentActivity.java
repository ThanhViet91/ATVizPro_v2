package com.atsoft.screenrecord.ui.activities;

import static com.atsoft.screenrecord.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.atsoft.screenrecord.ui.services.ExecuteService.KEY_VIDEO_PATH;
import static com.atsoft.screenrecord.ui.utils.MyUtils.ACTION_GO_TO_EDIT;
import static com.atsoft.screenrecord.ui.utils.MyUtils.ACTION_GO_TO_PLAY;
import static com.atsoft.screenrecord.ui.utils.MyUtils.isMyServiceRunning;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.ui.services.ControllerService;
import com.atsoft.screenrecord.ui.services.ExecuteService;
import com.atsoft.screenrecord.ui.utils.MyUtils;
import com.atsoft.screenrecord.utils.AdsUtil;
import com.atsoft.screenrecord.utils.OnSingleClickListener;
import com.atsoft.screenrecord.utils.StorageUtil;
import com.bumptech.glide.Glide;

public class PopUpResultVideoTranslucentActivity extends AppCompatActivity{

    private String videoPath;
    public static boolean afterAdd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.popup_result_video_layout);
        Intent intent = getIntent();
        if (intent != null) {
            stopExecuteService();
            videoPath = intent.getStringExtra(KEY_VIDEO_PATH);
        }

        initBanner();

        ImageView btnClose = findViewById(R.id.btn_close_popup);
        btnClose.setOnClickListener(v -> onBackPressed());

        ImageView btnShare = findViewById(R.id.btn_share);
        btnShare.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                gotoShareVideo();
            }
        });

        ImageView btnEdit = findViewById(R.id.btn_edit);
        btnEdit.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                gotoEditVideo();
            }
        });

        ImageView btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                gotoDeleteVideo();
            }
        });

        ImageView btnPlayVideo = findViewById(R.id.view_play_video);
        ImageView thumbVideo = findViewById(R.id.thumbnail_video);
        Glide.with(this)
                .load(videoPath)
                .thumbnail(0.1f)
                .into(thumbVideo);
        btnPlayVideo.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                gotoPlayVideo();
            }
        });

    }

    @Override
    public void onBackPressed() {
        afterAdd = true;
        finishAndRemoveTask();
    }

    private AdsUtil mAdManager;
    private void initBanner() {
        RelativeLayout mAdViewRoot = findViewById(R.id.adView);
        mAdManager = new AdsUtil(this, mAdViewRoot);
    }

    public static boolean afterDelete = false;
    private void gotoDeleteVideo() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Video")
                .setMessage("Do you want to delete this video from My Recordings?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    StorageUtil.deleteFile(videoPath);
                    afterDelete = true;
                    onBackPressed();
                } )
                .setNegativeButton(android.R.string.no, (dialog, which) -> {} )
                .show();
    }


    private void gotoPlayVideo() {

        // open home >>> my recording >>> play video
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(ACTION_GO_TO_PLAY);
        intent.putExtra(KEY_PATH_VIDEO, videoPath);
        startActivity(intent);
    }

    private void gotoEditVideo() {
        // open home >>> my recording >>> edit
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(ACTION_GO_TO_EDIT);
        intent.putExtra(KEY_PATH_VIDEO, videoPath);
        startActivity(intent);
        finish();
    }

    private void gotoShareVideo() {
        MyUtils.shareVideo(this, videoPath);
    }

    private void stopExecuteService() {
        if (isMyServiceRunning(this, ExecuteService.class)) {
            Intent controller = new Intent(this, ExecuteService.class);
            controller.setAction(MyUtils.ACTION_EXIT_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(controller);
            } else {
                startService(controller);
            }
        }

        if (isMyServiceRunning(this, ControllerService.class)) {
            Intent controller = new Intent(this, ControllerService.class);
            controller.setAction(MyUtils.ACTION_EXIT_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(controller);
            } else {
                startService(controller);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdManager.loadBanner();
    }
}