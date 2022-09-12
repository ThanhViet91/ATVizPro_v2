package com.examples.atvizpro.ui.activities;

import static com.examples.atvizpro.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atvizpro.ui.utils.MyUtils.hideStatusBar;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.examples.atvizpro.R;
import com.examples.atvizpro.controllers.settings.SettingManager2;
import com.examples.atvizpro.ui.services.ExecuteService;
import com.examples.atvizpro.utils.AdUtil;
import com.examples.atvizpro.utils.VideoUtil;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ReactCamFinishActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView btn_save_video;
    private ImageView btn_share_video;
    private ImageView btn_back;
    private boolean isSaved = false;
    private AdView mAdView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_react_cam_finish);
        hideStatusBar(this);

        isSaved = false;

        btn_save_video = findViewById(R.id.img_btn_save);
        btn_share_video = findViewById(R.id.img_btn_share);
        mAdView = findViewById(R.id.adView);

        btn_share_video.setOnClickListener(this);
        btn_save_video.setOnClickListener(this);

        btn_back = findViewById(R.id.img_btn_back_header);
        btn_back.setOnClickListener(this);
        addVideoView();

        System.out.println("thanhlv FinishReact intent === action : "+ getIntent().getAction());
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdUtil.createBannerAdmob(getApplicationContext(), mAdView);
        mAdView.setAdListener(new AdListener() {
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
        videoView = findViewById(R.id.video_main);
        if (getIntent() != null){
            videoFile = getIntent().getStringExtra(KEY_PATH_VIDEO);
            System.out.println("thanhlv addVideoView hhhhhh "+videoFile);
            if (getIntent().getBooleanExtra("from_notification", false)){
                Intent intent1 = new Intent(this, ExecuteService.class);
                stopService(intent1);
            }
        }

        if (videoFile == null) return;
        if (!videoFile.equals("")){
            videoView.setVideoPath(videoFile);

        }
        videoView.setMediaController(new MediaController(this));
        videoView.requestFocus();
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mp) {
                mediaPlayer = mp;
                mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                videoPrepared(mp);
            }
        });

    }

    private void checkHasChangeVideoCamView() {
        if (screenVideo == null) {
            return;
        }
        int oldScreenWidth = screenVideo.getWidth();
        int oldScreenHeight = screenVideo.getHeight();
        screenVideo.post(new Runnable() {
            @Override
            public void run() {
                if (oldScreenWidth != screenVideo.getWidth()
                        || oldScreenHeight != screenVideo.getHeight()) {
                    videoPrepared(mediaPlayer);
                }
            }
        });
    }

    private int videoWidth, videoHeight;
    RelativeLayout screenVideo;

    private void videoPrepared(MediaPlayer mp) {
        ViewGroup.LayoutParams lpVideo = videoView.getLayoutParams();
        videoWidth = mp.getVideoWidth();
        videoHeight = mp.getVideoHeight();
        double videoRatio = (double) videoWidth / (double) videoHeight;

        screenVideo = findViewById(R.id.screenVideo);
        int screenWidth = screenVideo.getWidth();
        int screenHeight = screenVideo.getHeight();
        double screenRatio = (double) screenWidth / (double) screenHeight;

        double diffRatio = videoRatio / screenRatio - 1;

        if (Math.abs(diffRatio) < 0.01) {
            // very good
        } else {
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

    public void onClick(View v) {
        if (v == findViewById(R.id.img_btn_save)) {
            if (!isSaved) {
                try {
                    copyFile(new File(videoFile), new File(VideoUtil.generateFileOutput()));
                    isSaved = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    isSaved = false;
                }
            }
            Toast.makeText(this, "Video is saved in your phone!", Toast.LENGTH_SHORT).show();
        }

        if (v == findViewById(R.id.img_btn_share)) {
            Uri fileUri = Uri.parse(videoFile);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.setType("video/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share video..."));
        }

        if (v == findViewById(R.id.img_btn_back_header)) {
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
}