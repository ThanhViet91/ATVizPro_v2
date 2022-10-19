package com.examples.atscreenrecord.ui.activities;

import static com.examples.atscreenrecord.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atscreenrecord.ui.utils.MyUtils.hideStatusBar;
import static com.examples.atscreenrecord.ui.utils.MyUtils.isMyServiceRunning;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.ui.ChooseVideoListener;
import com.examples.atscreenrecord.ui.VideoBeforeReactView;
import com.examples.atscreenrecord.ui.services.ControllerService;
import com.examples.atscreenrecord.ui.services.ExecuteService;
import com.examples.atscreenrecord.ui.services.recording.RecordingService;
import com.examples.atscreenrecord.ui.services.streaming.StreamingService;
import com.examples.atscreenrecord.ui.utils.MyUtils;

public class PrepareVideoActivity extends AppCompatActivity implements ChooseVideoListener {

    static final String VIDEO_PATH_KEY = "video-file-path";
    private ProgressDialog mProgressDialog;
    private VideoBeforeReactView prepareVideoView;
    private String pathOriginalVideo = "";
    private String actionType;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.react_cam_prepare_layout);
        hideStatusBar(this);
        turnOffServiceUseCamera();
        prepareVideoView = findViewById(R.id.trimmer_view);
        Intent intent = getIntent();
        if (intent != null) {
            pathOriginalVideo = intent.getStringExtra(VIDEO_PATH_KEY);
            actionType = intent.getAction();
        }
        prepareVideoView.setOnChooseVideoListener(this);
        prepareVideoView.initVideoByURI(Uri.parse(pathOriginalVideo));
    }

    @Override
    protected void onResume() {
        super.onResume();
        prepareVideoView.showOrHideAdBanner();
    }

    @Override
    public void onPause() {
        super.onPause();
        prepareVideoView.onVideoPause();
        prepareVideoView.setRestoreState(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        prepareVideoView.onDestroy();
    }

    @Override
    public void onClickChoose() {
//        runCompressVideo();
        if (checkServiceBusy()) return;
        buildDialog(getResources().getString(R.string.prepare_video)).show();
        new Handler().postDelayed(() -> {
            if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
            if (actionType.equals(MyUtils.ACTION_FOR_REACT)) nextToReactCam();
            if (actionType.equals(MyUtils.ACTION_FOR_COMMENTARY)) nextToCommentary();
        }, 2000);
    }
    private void turnOffServiceUseCamera() {
        if (isMyServiceRunning(this, RecordingService.class)) {
            Intent intent = new Intent(this, RecordingService.class);
            stopService(intent);
        }
        if (isMyServiceRunning(this, StreamingService.class)) {
            Intent intent = new Intent(this, StreamingService.class);
            stopService(intent);
        }
        if (isMyServiceRunning(this, ControllerService.class)) {
            Intent intent = new Intent(this, ControllerService.class);
            stopService(intent);
        }
    }

    private boolean checkServiceBusy() {
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

    private void nextToCommentary() {
        Intent intent = new Intent(PrepareVideoActivity.this, CommentaryActivity.class);
        intent.putExtra(KEY_PATH_VIDEO, pathOriginalVideo);
        startActivity(intent);
        finish();
    }

    private void nextToReactCam() {
        Intent intent = new Intent(PrepareVideoActivity.this, ReactCamActivity.class);
        intent.putExtra(KEY_PATH_VIDEO, pathOriginalVideo);
        startActivity(intent);
        finish();
    }

    @Override
    public void onCancel() {
        prepareVideoView.onDestroy();
        finish();
    }

    private ProgressDialog buildDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "", msg);
        }
        mProgressDialog.setMessage(msg);
        return mProgressDialog;
    }

}