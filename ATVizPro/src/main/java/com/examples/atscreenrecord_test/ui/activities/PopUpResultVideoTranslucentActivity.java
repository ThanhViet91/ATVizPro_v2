package com.examples.atscreenrecord_test.ui.activities;

import static com.examples.atscreenrecord_test.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atscreenrecord_test.ui.services.ExecuteService.KEY_VIDEO_PATH;
import static com.examples.atscreenrecord_test.ui.utils.MyUtils.ACTION_GO_TO_EDIT;
import static com.examples.atscreenrecord_test.ui.utils.MyUtils.ACTION_GO_TO_PLAY;
import static com.examples.atscreenrecord_test.ui.utils.MyUtils.isMyServiceRunning;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.examples.atscreenrecord_test.App;
import com.examples.atscreenrecord_test.R;
import com.examples.atscreenrecord_test.ui.services.ExecuteService;
import com.examples.atscreenrecord_test.ui.utils.MyUtils;
import com.examples.atscreenrecord_test.utils.AdsUtil;
import com.examples.atscreenrecord_test.utils.StorageUtil;

public class PopUpResultVideoTranslucentActivity extends AppCompatActivity{

    private String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_result_video_layout);

        Intent intent = getIntent();
        if (intent != null) {
            stopExecuteService();
            videoPath = intent.getStringExtra(KEY_VIDEO_PATH);
        }

        showBanner();

        ImageView btnClose = findViewById(R.id.btn_close_popup);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageView btnShare = findViewById(R.id.btn_share);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoShareVideo();
            }
        });

        ImageView btnEdit = findViewById(R.id.btn_edit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoEditVideo();
            }
        });

        ImageView btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoDeleteVideo();
            }
        });

        ImageView btnPlayVideo = findViewById(R.id.thumbnail_video);
        Glide.with(this)
                .load(videoPath)
                .thumbnail(0.1f)
                .into(btnPlayVideo);
        btnPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoPlayVideo();
            }
        });

    }


    private AdsUtil mAdManager;
    private void showBanner() {
        RelativeLayout mAdViewRoot = findViewById(R.id.adView);
        mAdManager = new AdsUtil(this, mAdViewRoot);
    }

    private void gotoDeleteVideo() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Video")
                .setMessage("Do you want to delete this video from My Recordings?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    StorageUtil.deleteFile(videoPath);
                    onBackPressed();
                } )
                .show();
    }


    private void gotoPlayVideo() {

        // open home >>> my recording >>> play video
//        ProjectsActivity.gotoPlayVideoDetail(this, videoPath, videoName);
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
    }

    private void gotoShareVideo() {
        MyUtils.shareVideo(this, videoPath);
//        onBackPressed();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdManager.loadBanner();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.ignoreOpenAd = true;
    }
}