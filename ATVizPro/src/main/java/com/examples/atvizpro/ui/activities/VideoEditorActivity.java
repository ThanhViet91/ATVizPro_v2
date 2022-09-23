package com.examples.atvizpro.ui.activities;

import static com.examples.atvizpro.ui.utils.MyUtils.hideStatusBar;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.examples.atvizpro.R;
import com.examples.atvizpro.controllers.settings.SettingManager2;
import com.examples.atvizpro.ui.VideoEditorView;
import com.examples.atvizpro.ui.VideoStreamListener;
import com.examples.atvizpro.ui.fragments.IOptionFragmentListener;
import com.examples.atvizpro.ui.fragments.OptionAddImageFragment;
import com.examples.atvizpro.ui.fragments.OptionAddTextFragment;
import com.examples.atvizpro.ui.fragments.OptionChangeSpeedFragment;
import com.examples.atvizpro.ui.fragments.OptionTrimFragment;
import com.examples.atvizpro.ui.utils.MyUtils;
import com.examples.atvizpro.utils.VideoUtil;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class VideoEditorActivity extends AppCompatActivity implements IOptionFragmentListener,
        VideoStreamListener, VideoEditorView.VideoEditorListener {

    static final String VIDEO_PATH_KEY = "video-file-path";
    private VideoEditorView videoEditorView;
    private String pathOriginalVideo = "";

    private LottieAnimationView animationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_editor_layout);
        hideStatusBar(this);
        videoEditorView = findViewById(R.id.trimmer_view);
        Bundle bd = getIntent().getExtras();
        if (bd != null) pathOriginalVideo = bd.getString(VIDEO_PATH_KEY);
        videoEditorView.setOnEditVideoListener(this);
        videoEditorView.initVideoByURI(Uri.parse(pathOriginalVideo));
        videoEditorView.setVideoEditorListener(this);
        animationView = findViewById(R.id.animation_view);
        animationView.setVisibility(View.GONE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        videoEditorView.showOrHideAdBanner();
        createInterstitialAdmob();
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
            default:
                MyUtils.showSnackBarNotification(videoEditorView, "This fun is coming soon!!", Snackbar.LENGTH_SHORT);
        }

    }

    @Override
    public void onClickClose() {
    }

    @Override
    public void onClickDone() {

        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation();
    }

    @Override
    public void onClickNext() {
        //pressSave
        try {
            copyFile(new File(cacheOutputPath), new File(VideoUtil.generateFileOutput()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        showInterstitialAd();
    }


    InterstitialAd mInterstitialAdAdmob = null;

    public void createInterstitialAdmob() {
        if (SettingManager2.getRemoveAds(this)) {
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

    public void showInterstitialAd(){
        if (mInterstitialAdAdmob != null) {
            mInterstitialAdAdmob.show(this);
            mInterstitialAdAdmob.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                }

                @Override
                public void onAdDismissedFullScreenContent() {

                    Toast.makeText(getApplicationContext(), "Video is saved in your phone!", Toast.LENGTH_SHORT).show();
                    finish();
                    createInterstitialAdmob();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    Toast.makeText(getApplicationContext(), "Video is saved in your phone!", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), "Video is saved in your phone!", Toast.LENGTH_SHORT).show();
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

    String cacheOutputPath = "";

    @Override
    public void onFinishProcess(String outPath) {
        cacheOutputPath = outPath;
        animationView.pauseAnimation();
        animationView.setVisibility(View.GONE);
        videoEditorView.onPressSave();
        videoEditorView.initVideoByURI(Uri.parse(outPath));

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
        finish();
    }

    @Override
    public void onClickVideoOption(String opt) {
        if (!opt.equals("")) {
            showOptionFragment(opt);
        }
    }
}