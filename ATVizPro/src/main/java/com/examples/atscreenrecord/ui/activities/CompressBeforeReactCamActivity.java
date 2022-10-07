package com.examples.atscreenrecord.ui.activities;

import static com.examples.atscreenrecord.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atscreenrecord.ui.utils.MyUtils.hideStatusBar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.ui.ChooseVideoListener;
import com.examples.atscreenrecord.ui.VideoBeforeReactView;
import com.examples.atscreenrecord.ui.utils.MyUtils;

public class CompressBeforeReactCamActivity extends AppCompatActivity implements ChooseVideoListener {

    static final String VIDEO_PATH_KEY = "video-file-path";
    private ProgressDialog mProgressDialog;
    private VideoBeforeReactView prepareVideoView;
    private String pathOriginalVideo = "";
    private String actionType;

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("thanhlv onStart CompressBeforeReactCamActivity");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.react_cam_prepare_layout);
        hideStatusBar(this);
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
        System.out.println("thanhlv CompressBeforeReactCamActivity onResume ");
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
        buildDialog(getResources().getString(R.string.prepare_video)).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
                if (actionType.equals(MyUtils.ACTION_FOR_REACT)) nextToReactCam();
                if (actionType.equals(MyUtils.ACTION_FOR_COMMENTARY)) nextToCommentary();
            }
        }, 2000);
    }


    private void nextToCommentary() {
        Intent intent = new Intent(CompressBeforeReactCamActivity.this, CommentaryActivity.class);
        intent.putExtra(KEY_PATH_VIDEO, pathOriginalVideo);
        startActivity(intent);
        finish();
    }

    private void nextToReactCam() {
        Intent intent = new Intent(CompressBeforeReactCamActivity.this, ReactCamActivity.class);
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