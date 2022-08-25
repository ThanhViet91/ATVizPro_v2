package com.examples.atvizpro.ui.activities;

import static com.examples.atvizpro.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atvizpro.ui.utils.MyUtils.hideStatusBar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.examples.atvizpro.R;
import com.examples.atvizpro.ui.VideoTrimListener;
import com.examples.atvizpro.ui.VideoTrimmerView;
import com.examples.atvizpro.utils.VideoUtil;

public class CompressBeforeReactCamActivity extends AppCompatActivity implements VideoTrimListener {

    static final String VIDEO_PATH_KEY = "video-file-path";
    private ProgressDialog mProgressDialog;
    private VideoTrimmerView trimmerView;
    private String pathOriginalVideo = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(this);
        setContentView(R.layout.react_cam_prepare_layout);
        trimmerView = findViewById(R.id.trimmer_view);
        Bundle bd = getIntent().getExtras();
        if (bd != null) pathOriginalVideo = bd.getString(VIDEO_PATH_KEY);
        trimmerView.setOnTrimVideoListener(this);
        trimmerView.initVideoByURI(Uri.parse(pathOriginalVideo));
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
        runCompressVideo();
        buildDialog(getResources().getString(R.string.prepare_video)).show();
    }

    public void runCompressVideo(){
        if (!pathOriginalVideo.equals("")) {
            new VideoUtil().compression(this, pathOriginalVideo, new VideoUtil.ITranscoding() {
                @Override
                public void onStartTranscoding(String outPath) {
                    outputCachePath = outPath;
                }

                @Override
                public void onFinishTranscoding(String code) {
                    if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
                    nextToReactCam();
                }

                @Override
                public void onUpdateProgressTranscoding(int progress) {

                }
            });
        }
    }

    String outputCachePath;
    private void nextToReactCam() {
        Intent intent = new Intent(CompressBeforeReactCamActivity.this, ReactCamActivity.class);
        intent.putExtra(KEY_PATH_VIDEO, outputCachePath);
        startActivity(intent);
    }

    @Override
    public void onFinishTrim(String in) {
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