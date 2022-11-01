package com.examples.atscreenrecord_test.ui.activities;

import static com.examples.atscreenrecord_test.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atscreenrecord_test.ui.utils.MyUtils.hideStatusBar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.examples.atscreenrecord_test.R;
import com.examples.atscreenrecord_test.ui.IVideoStreamView;
import com.examples.atscreenrecord_test.ui.VideoEditorView;
import com.examples.atscreenrecord_test.ui.fragments.IOptionFragmentListener;
import com.examples.atscreenrecord_test.ui.fragments.OptionAddImageFragment;
import com.examples.atscreenrecord_test.ui.fragments.OptionAddTextFragment;
import com.examples.atscreenrecord_test.ui.fragments.OptionChangeSpeedFragment;
import com.examples.atscreenrecord_test.ui.fragments.OptionRotateFragment;
import com.examples.atscreenrecord_test.ui.fragments.OptionTrimFragment;
import com.examples.atscreenrecord_test.ui.utils.MyUtils;
import com.examples.atscreenrecord_test.utils.AdsUtil;
import com.examples.atscreenrecord_test.utils.FFmpegUtil;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;

public class VideoEditorActivity extends AppCompatActivity implements IOptionFragmentListener,
        IVideoStreamView, VideoEditorView.VideoEditorListener {

    private VideoEditorView videoEditorView;
    private String pathOriginalVideo = "";

    private LottieAnimationView animationView;
    private AdsUtil mAdManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editor);
        hideStatusBar(this);
        videoEditorView = findViewById(R.id.trimmer_view);
        Intent intent = getIntent();
        if (intent != null) {
            pathOriginalVideo = intent.getStringExtra(KEY_PATH_VIDEO);
        }
        videoEditorView.setOnEditVideoListener(this);
        videoEditorView.initVideoByURI(Uri.parse(pathOriginalVideo));
        videoEditorView.setVideoEditorListener(this);
        animationView = findViewById(R.id.animation_view);
        animationView.setVisibility(View.GONE);

        mAdManager = new AdsUtil(this, null);
        mAdManager.createInterstitialAdmob();

    }

    @Override
    protected void onResume() {
        super.onResume();
        videoEditorView.showOrHideAdBanner();
    }

    @Override
    public void onPause() {
        super.onPause();
        videoEditorView.onVideoPause();
        videoEditorView.setRestoreState(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoEditorView.onDestroy();
    }


    private void showOptionFragment(String opt) {
        Bundle bundle = new Bundle();

        bundle.putLong("video_duration", videoEditorView.getVideoDuration());
        bundle.putString("video_path", pathOriginalVideo);
        switch (opt) {
            case "Trim":
                OptionTrimFragment.newInstance(this, bundle).show(getSupportFragmentManager(), "");
                break;
            case "Text":
                OptionAddTextFragment.newInstance(this, bundle).show(getSupportFragmentManager(), "");
                break;

            case "Speed":
                OptionChangeSpeedFragment.newInstance(this, bundle).show(getSupportFragmentManager(), "");
                break;

            case "Image":
                OptionAddImageFragment.newInstance(this, bundle).show(getSupportFragmentManager(), "");
                break;

            case "Rotate":
                OptionRotateFragment.newInstance(this, bundle).show(getSupportFragmentManager(), "");
                break;
            default:
                MyUtils.showSnackBarNotification(videoEditorView, "This fun is coming soon!!", Snackbar.LENGTH_SHORT);
        }

    }

    @Override
    public void onClickClose() {
    }

    boolean isStartCompress = false;
    @Override
    public void onClickDone() {
        isStartCompress = true;
        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation();
    }

    boolean enableSave = false;
    public static boolean finishEdit = false;
    @Override
    public void onClickNext() {
        //pressSave
        try {
            copyFile(new File(cacheOutputPath), new File(FFmpegUtil.generateFileOutput("Edit")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        showInterstitialAd();
        finishEdit = true;
    }

    public FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
        @Override
        public void onAdClicked() {
        }

        @Override
        public void onAdDismissedFullScreenContent() {

            AdsUtil.lastTime = (new Date()).getTime();
            Toast.makeText(getApplicationContext(), "Video is saved.", Toast.LENGTH_SHORT).show();
            mAdManager.createInterstitialAdmob();

            finish();
        }

        @Override
        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
            Toast.makeText(getApplicationContext(), "Video is saved.", Toast.LENGTH_SHORT).show();
            finish();

        }

        @Override
        public void onAdImpression() {
        }

        @Override
        public void onAdShowedFullScreenContent() {
        }
    };

    public void showInterstitialAd(){
        if (mAdManager.interstitialAdAlready()) {
            mAdManager.showInterstitialAd(fullScreenContentCallback);
        } else {
            Toast.makeText(getApplicationContext(), "Video is saved.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    private String cacheOutputPath = "";

    @Override
    public void onFinishProcess(String outPath) {
        cacheOutputPath = outPath;
        pathOriginalVideo = outPath;
        animationView.pauseAnimation();
        animationView.setVisibility(View.GONE);
        videoEditorView.onPressSave();
        videoEditorView.initVideoByURI(Uri.parse(outPath));
        enableSave = true;
        isStartCompress = false;
    }


    @Override
    public void onStartRecord() {

    }

    @Override
    public void onStopRecord() {

    }

    @Override
    public void onDeleteRecord() {

    }

    @Override
    public void onCancel() {
        if (enableSave) {
            new AlertDialog.Builder(this)
                    .setTitle("Save Video")
                    .setMessage("Do you want to save this video?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Continue with delete operation
                        try {
                            copyFile(new File(cacheOutputPath), new File(FFmpegUtil.generateFileOutput("Edit")));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        finish();
                    })
                    .setNegativeButton(android.R.string.no, (dialogInterface, i) ->{
                        finish();
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            if (!isStartCompress) {
                finish();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Cancel edit!")
                    .setMessage("Do you want to cancel processing?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Continue with delete operation
                        finish();
                    })
                    .setNegativeButton(android.R.string.no, (dialogInterface, i) ->{
//                        finish();
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
//            finish();
        }

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (enableSave) {
            new AlertDialog.Builder(this)
                    .setTitle("Save Video")
                    .setMessage("Do you want to save this video?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Continue with delete operation
                        try {
                            copyFile(new File(cacheOutputPath), new File(FFmpegUtil.generateFileOutput("Edit")));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        finish();
                    })
                    .setNegativeButton(android.R.string.no, (dialogInterface, i) ->{
                        finish();
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            if (!isStartCompress) {
                finish();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Cancel edit!")
                    .setMessage("Do you want to cancel processing?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Continue with delete operation
                        finish();
                    })
                    .setNegativeButton(android.R.string.no, (dialogInterface, i) ->{
//                        finish();
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
//            finish();
        }

    }

    @Override
    public void onClickVideoOption(String opt) {
        if (!opt.equals("")) {
            if (!isStartCompress)
            showOptionFragment(opt);
        }
    }
}