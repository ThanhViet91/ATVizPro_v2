package com.examples.atscreenrecord_test.ui.activities;

import static com.examples.atscreenrecord_test.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atscreenrecord_test.ui.utils.MyUtils.hideStatusBar;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.examples.atscreenrecord_test.R;
import com.examples.atscreenrecord_test.ui.services.ExecuteService;
import com.examples.atscreenrecord_test.ui.utils.MyUtils;
import com.examples.atscreenrecord_test.utils.AdsUtil;
import com.examples.atscreenrecord_test.utils.FFmpegUtil;
import com.google.android.gms.ads.AdListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ResultVideoFinishActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean isSaved = false;
    private RelativeLayout mAdView;
    private TextView title;
    private AdsUtil mAdManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_video_finish);
        hideStatusBar(this);
        isSaved = false;
        ImageView btn_save_video = findViewById(R.id.img_btn_save);
        title = findViewById(R.id.title_result);
        ImageView btn_share_video = findViewById(R.id.img_btn_share);
        ImageView btn_go_home = findViewById(R.id.img_btn_home);
        mAdView = findViewById(R.id.adView);
        videoView = findViewById(R.id.video_main);
        btn_share_video.setOnClickListener(this);
        btn_save_video.setOnClickListener(this);
        btn_go_home.setOnClickListener(this);
        handleIntent();
        addVideoView();
    }

    private String preName = "";
    private void handleIntent() {
        if (getIntent() != null){
            videoFile = getIntent().getStringExtra(KEY_PATH_VIDEO);
            if (getIntent().getAction().equals(MyUtils.ACTION_END_REACT)){
                Intent intent = new Intent(this, ExecuteService.class);
                stopService(intent);
                preName = "React";
                title.setText(getString(R.string.react_cam));
            }
            if (getIntent().getAction().equals(MyUtils.ACTION_END_RECORD)){
                title.setText(getString(R.string.record_screen));
                preName = "Record";
            }

            if (getIntent().getAction().equals(MyUtils.ACTION_END_COMMENTARY)){
                Intent intent = new Intent(this, ExecuteService.class);
                stopService(intent);
                title.setText(getString(R.string.commentary));
                preName = "Commentary";
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdManager = new AdsUtil(this, mAdView);
        mAdManager.loadBanner();
        mAdManager.getAdView().setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (mediaPlayer != null) {
                    checkHasChangeVideoCamView();
                }
            }
        });
    }

    MediaPlayer mediaPlayer;
    String videoFile = "";
    VideoView videoView;

    private void addVideoView() {

        if (videoFile == null) return;
        if (!videoFile.equals("")) {
            videoView.setVideoPath(videoFile);
        }
        videoView.setMediaController(new MediaController(this));
        videoView.requestFocus();
        videoView.setOnPreparedListener(mp -> {
            mediaPlayer = mp;
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            videoPrepared(mp);
        });
    }

    private void checkHasChangeVideoCamView() {
        if (screenVideo == null) {
            return;
        }
        int oldScreenWidth = screenVideo.getWidth();
        int oldScreenHeight = screenVideo.getHeight();
        screenVideo.post(() -> {
            if (oldScreenWidth != screenVideo.getWidth()
                    || oldScreenHeight != screenVideo.getHeight()) {
                videoPrepared(mediaPlayer);
            }
        });
    }

    private RelativeLayout screenVideo;
    private void videoPrepared(MediaPlayer mp) {
        ViewGroup.LayoutParams lpVideo = videoView.getLayoutParams();
        int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        double videoRatio = (double) videoWidth / (double) videoHeight;
        screenVideo = findViewById(R.id.screenVideo);
        int screenWidth = screenVideo.getWidth();
        int screenHeight = screenVideo.getHeight();
        double screenRatio = (double) screenWidth / (double) screenHeight;
        double diffRatio = videoRatio / screenRatio - 1;
        if (Math.abs(diffRatio) > 0.01) {
            if (diffRatio > 0) {
                //closed width
                lpVideo.width = screenWidth;
                lpVideo.height = (int) (lpVideo.width / videoRatio) + 1;
            } else {
                //closed height
                lpVideo.height = screenHeight + 1;
                lpVideo.width = (int) (lpVideo.height * videoRatio);
            }
        }
        videoView.setLayoutParams(lpVideo);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    String finalVideoSaved = "";
    public void saveVideo(String videoFile){
        if (!isSaved) {
            try {
                finalVideoSaved = FFmpegUtil.generateFileOutput(preName);
                copyFile(new File(videoFile), new File(finalVideoSaved));
                isSaved = true;
                Toast.makeText(getApplicationContext(), "Video is saved.", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                isSaved = false;
                Toast.makeText(getApplicationContext(), "Saving failed, has some problem!", Toast.LENGTH_SHORT).show();
            }
        } else
            Toast.makeText(getApplicationContext(), "Video is saved.", Toast.LENGTH_SHORT).show();
    }

    private void goHome(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(MyUtils.ACTION_GO_HOME);
        startActivity(intent);
    }
    public void onClick(View v) {
        if (v == findViewById(R.id.img_btn_save)) {
            saveVideo(videoFile);
        }

        if (v == findViewById(R.id.img_btn_share)) {
            MyUtils.shareVideo(getApplicationContext(), videoFile);
        }

        if (v == findViewById(R.id.img_btn_home)) {
            if (isSaved) {
                goHome();
                finish();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Save Video")
                    .setMessage("Do you want to save this video?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Continue with delete operation
                        saveVideo(videoFile);
                        goHome();
                        finish();
                    })
                    .setNegativeButton(android.R.string.no, (dialogInterface, i) ->{
                        goHome();
                        finish();
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }


        if (v == findViewById(R.id.img_btn_back_header)) {
            if (isSaved) {
                finish();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Save Video")
                    .setMessage("Do you want to save this video?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Continue with delete operation
                        saveVideo(videoFile);
                        finish();
                    })
                    .setNegativeButton(android.R.string.no, (dialogInterface, i) -> finish())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    @Override
    public void onBackPressed() {
        if (isSaved) {
            finish();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Save Video")
                .setMessage("Do you want to save this video?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // Continue with delete operation
                    saveVideo(videoFile);
                    finish();
                })
                .setNegativeButton(android.R.string.no, (dialogInterface, i) -> finish())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    void copyFile(File src, File dst) throws IOException {
        try (FileChannel inChannel = new FileInputStream(src).getChannel(); FileChannel outChannel = new FileOutputStream(dst).getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }
}