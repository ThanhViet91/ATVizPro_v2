package com.examples.atvizpro.ui.activities;

import static com.examples.atvizpro.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atvizpro.ui.utils.MyUtils.hideStatusBar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.examples.atvizpro.R;
import com.examples.atvizpro.ui.ZVideoView;
import com.examples.atvizpro.ui.utils.CustomOnScaleDetector;
import com.examples.atvizpro.utils.StorageUtil;
import com.examples.atvizpro.utils.VideoUtil;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReactCamFinishActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView btn_save_video;
    private ImageView btn_share_video;
    private ImageView btn_back;
    private boolean isSaved = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(this);
        setContentView(R.layout.activity_react_cam_finish);

        isSaved = false;

        btn_save_video = findViewById(R.id.img_btn_save);
        btn_share_video = findViewById(R.id.img_btn_share);

        btn_share_video.setOnClickListener(this);
        btn_save_video.setOnClickListener(this);

        btn_back = findViewById(R.id.img_btn_back_header);
        btn_back.setOnClickListener(this);
        addVideoView();
    }


    MediaPlayer mediaPlayer;
    String videoFile;
    VideoView videoView;

//    String fileVideoPath;

    private void addVideoView() {
        videoView = findViewById(R.id.video_main);
        videoFile = getIntent().getStringExtra(KEY_PATH_VIDEO);
        videoView.setVideoPath(videoFile);
        videoView.setMediaController(new MediaController(this));
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mp) {
                mediaPlayer = mp;
                mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                videoPrepared(mp);
            }
        });

    }

    private int videoWidth, videoHeight;

    private void videoPrepared(MediaPlayer mp) {
        ViewGroup.LayoutParams lpVideo = videoView.getLayoutParams();
        videoWidth = mp.getVideoWidth();
        videoHeight = mp.getVideoHeight();
        double videoRatio = (double) videoWidth / (double) videoHeight;

        RelativeLayout screenVideo = findViewById(R.id.screenVideo);
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
                }
            }
            Toast.makeText(this, "Record is saved in your phone!", Toast.LENGTH_SHORT).show();

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