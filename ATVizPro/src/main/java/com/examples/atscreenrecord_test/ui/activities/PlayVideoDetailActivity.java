package com.examples.atscreenrecord_test.ui.activities;

import static com.examples.atscreenrecord_test.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atscreenrecord_test.ui.activities.MainActivity.KEY_VIDEO_NAME;
import static com.examples.atscreenrecord_test.ui.utils.MyUtils.hideStatusBar;

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
import com.examples.atscreenrecord_test.ui.utils.MyUtils;
import com.examples.atscreenrecord_test.utils.AdsUtil;
import com.google.android.gms.ads.AdListener;

import java.io.File;

public class PlayVideoDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private RelativeLayout mAdView;
    private TextView title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video_detail);
        hideStatusBar(this);
        ImageView btn_delete_video = findViewById(R.id.img_btn_delete);
        title = findViewById(R.id.title_video);
        ImageView btn_share_video = findViewById(R.id.img_btn_share);
        mAdView = findViewById(R.id.adView);
        videoView = findViewById(R.id.video_main);
        btn_share_video.setOnClickListener(this);
        btn_delete_video.setOnClickListener(this);
        ImageView btn_back = findViewById(R.id.img_btn_back_header);
        btn_back.setOnClickListener(this);
        handleIntent();
        addVideoView();
    }

    private void handleIntent() {
        if (getIntent() != null) {
            videoFile = getIntent().getStringExtra(KEY_PATH_VIDEO);
            videoName = getIntent().getStringExtra(KEY_VIDEO_NAME);
            title.setText(videoName);
        }
    }

    private AdsUtil mAdManager;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.stopPlayback();
    }

    MediaPlayer mediaPlayer;
    String videoFile = "";
    String videoName = "";
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
        videoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void handleDeleteButton() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Video")
                .setMessage("Do you want to delete video?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteVideos(videoFile))
                .show();
    }

    private void deleteVideos(String filePath) {
        if (new File(filePath).delete()) {
            Toast.makeText(this, "Delete success!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Delete failed, have some problem.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClick(View v) {
        if (v == findViewById(R.id.img_btn_delete)) {
            handleDeleteButton();
        }

        if (v == findViewById(R.id.img_btn_share)) {
            MyUtils.shareVideo(this, videoFile);
//            File file = new File(videoFile);
//            Uri uri = FileProvider.getUriForFile(getApplicationContext(), "com.examples.atscreenrecord_test.provider", file);
//            Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.putExtra(Intent.EXTRA_SUBJECT, String.format("Share of %s", file.getName()));
//            intent.setType(URLConnection.guessContentTypeFromName(file.getName()));
//            intent.putExtra(Intent.EXTRA_STREAM, uri);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            startActivity(Intent.createChooser(intent, "Share File"));
        }
        if (v == findViewById(R.id.img_btn_back_header)) finish();
    }
}