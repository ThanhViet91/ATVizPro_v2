package com.examples.atscreenrecord.ui.activities;

import static com.examples.atscreenrecord.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atscreenrecord.ui.utils.MyUtils.hideStatusBar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.controllers.settings.SettingManager2;
import com.examples.atscreenrecord.ui.IVideoStreamView;
import com.examples.atscreenrecord.ui.VideoCommentaryView;
import com.examples.atscreenrecord.utils.StorageUtil;
import com.examples.atscreenrecord.utils.VideoUtil;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommentaryActivity extends AppCompatActivity implements IVideoStreamView {

    static final String VIDEO_PATH_KEY = "video-file-path";
    private ProgressDialog mProgressDialog;
    private VideoCommentaryView videoStreamView;
    private String pathOriginalVideo = "";
    private LottieAnimationView animationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_commentary);
        hideStatusBar(this);
        videoStreamView = findViewById(R.id.trimmer_view);
        Bundle bd = getIntent().getExtras();
        if (bd != null) pathOriginalVideo = bd.getString(VIDEO_PATH_KEY);
        videoStreamView.setOnTrimVideoListener(this);
        videoStreamView.initVideoByURI(Uri.parse(pathOriginalVideo));

        animationView = findViewById(R.id.animation_view);
        animationView.setVisibility(View.GONE);

        prepareAudioRecorder();
    }

    @Override
    protected void onResume() {
        super.onResume();
        createInterstitialAdmob();
        videoStreamView.showOrHideAdBanner();
    }

    @Override
    public void onPause() {
        super.onPause();
        videoStreamView.onVideoPause();
        videoStreamView.setRestoreState(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoStreamView.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    String cacheAudioFilePath;

    @SuppressLint("SimpleDateFormat")
    public String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }
    private MediaRecorder mediaRecorder;
    // this process must be done prior to the start of recording
    private void prepareAudioRecorder() {
        cacheAudioFilePath = StorageUtil.getCacheDir() + "/CacheAudio_" + getTimeStamp() + ".mp3";
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(16*96000);
        mediaRecorder.setAudioSamplingRate(96000);
        mediaRecorder.setOutputFile(cacheAudioFilePath);

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    InterstitialAd mInterstitialAdAdmob = null;

    public void createInterstitialAdmob() {
        if (SettingManager2.getRemoveAds(getApplicationContext())) {
            mInterstitialAdAdmob = null;
            return;
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(getApplicationContext(), "ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAdAdmob = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAdAdmob = null;
                        createInterstitialAdmob();
                    }
                });
    }

    private void copyFile(File src, File dst) throws IOException {
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

    public void showResultActivity(String videoPath){
        Intent intent = new Intent(this, ResultVideoFinishActivity.class);
        intent.putExtra(KEY_PATH_VIDEO, videoPath);
        System.out.println("thanhlv showResultActivity after execute "+videoPath);
        startActivity(intent);
    }
    public void showInterstitialAd(String videoPath){


        if (mInterstitialAdAdmob != null) {
            mInterstitialAdAdmob.show(this);
            mInterstitialAdAdmob.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    showResultActivity(videoPath);
                    finish();
//                    createInterstitialAdmob();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    showResultActivity(videoPath);
                    finish();
                }

                @Override
                public void onAdImpression() {
                }

                @Override
                public void onAdShowedFullScreenContent() {
                }
            });
        } else {
            showResultActivity(videoPath);
            finish();
        }
    }


    private void doExecuteCommentary() {
        if (!pathOriginalVideo.equals("") && !cacheAudioFilePath.equals("")) {
            new VideoUtil().commentaryAudio(this, pathOriginalVideo, cacheAudioFilePath, new VideoUtil.ITranscoding() {
                @Override
                public void onStartTranscoding(String outPath) {
                    animationView.setVisibility(View.VISIBLE);
                    animationView.playAnimation();
                }

                @Override
                public void onFinishTranscoding(String code) {
                    animationView.pauseAnimation();
                    animationView.setVisibility(View.GONE);
                    showInterstitialAd(code);
                }
            });
        }
    }
    @Override
    public void onClickNext() {
        doExecuteCommentary();
    }

    @Override
    public void onStartRecord() {
        mediaRecorder.start();
    }

    @Override
    public void onStopRecord() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    @Override
    public void onDeleteRecord() {
        prepareAudioRecorder();
    }

    @Override
    public void onCancel() {
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