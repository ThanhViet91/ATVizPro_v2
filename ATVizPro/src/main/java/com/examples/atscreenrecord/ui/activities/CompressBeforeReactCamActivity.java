package com.examples.atscreenrecord.ui.activities;

import static com.examples.atscreenrecord.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atscreenrecord.ui.utils.MyUtils.hideStatusBar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.ui.VideoTrimListener;
import com.examples.atscreenrecord.ui.VideoBeforeReactView;
import com.examples.atscreenrecord.utils.VideoUtil;

public class CompressBeforeReactCamActivity extends AppCompatActivity implements VideoTrimListener {

    static final String VIDEO_PATH_KEY = "video-file-path";
    private ProgressDialog mProgressDialog;
    private VideoBeforeReactView trimmerView;
    private String pathOriginalVideo = "";

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
        trimmerView = findViewById(R.id.trimmer_view);
        Bundle bd = getIntent().getExtras();
        if (bd != null) pathOriginalVideo = bd.getString(VIDEO_PATH_KEY);
        trimmerView.setOnTrimVideoListener(this);
        trimmerView.initVideoByURI(Uri.parse(pathOriginalVideo));
    }

    @Override
    protected void onResume() {
        super.onResume();
        trimmerView.showOrHideAdBanner();
        System.out.println("thanhlv CompressBeforeReactCamActivity onResume ");
    }

    @Override
    public void onPause() {
        super.onPause();
        trimmerView.onVideoPause();
        trimmerView.setRestoreState(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        trimmerView.onDestroy();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClickChoose() {
//        runCompressVideo();
        buildDialog(getResources().getString(R.string.prepare_video)).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
                nextToReactCam();
            }
        }, 3000);
    }

    public void runCompressVideo(){
        if (!pathOriginalVideo.equals("")) {
            new VideoUtil().compression(pathOriginalVideo, new VideoUtil.ITranscoding() {
                @Override
                public void onStartTranscoding(String outPath) {
                    outputCachePath = outPath;
                }

                @Override
                public void onFinishTranscoding(String code) {
                    if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
                    nextToReactCam();
                }

            });
        }
    }

    String outputCachePath;
    private void nextToReactCam() {
        Intent intent = new Intent(CompressBeforeReactCamActivity.this, ReactCamActivity.class);
        intent.putExtra(KEY_PATH_VIDEO, pathOriginalVideo);
        startActivity(intent);
        finish();
    }

    @Override
    public void onCancel() {
        trimmerView.onDestroy();
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