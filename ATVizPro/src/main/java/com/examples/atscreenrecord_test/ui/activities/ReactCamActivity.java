package com.examples.atscreenrecord_test.ui.activities;

import static com.examples.atscreenrecord_test.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atscreenrecord_test.ui.fragments.PopupConfirm.KEY_NEGATIVE;
import static com.examples.atscreenrecord_test.ui.fragments.PopupConfirm.KEY_POSITIVE;
import static com.examples.atscreenrecord_test.ui.fragments.PopupConfirm.KEY_TITLE;
import static com.examples.atscreenrecord_test.ui.utils.MyUtils.hideStatusBar;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.examples.atscreenrecord_test.R;
import com.examples.atscreenrecord_test.model.VideoProfileExecute;
import com.examples.atscreenrecord_test.ui.fragments.IConfirmPopupListener;
import com.examples.atscreenrecord_test.ui.fragments.PopupConfirm;
import com.examples.atscreenrecord_test.ui.services.ExecuteService;
import com.examples.atscreenrecord_test.ui.utils.CustomOnScaleDetector;
import com.examples.atscreenrecord_test.ui.utils.MyUtils;
import com.examples.atscreenrecord_test.utils.AdsUtil;
import com.examples.atscreenrecord_test.utils.StorageUtil;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.pedro.rtplibrary.rtmp.RtmpLiveStream;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReactCamActivity extends AppCompatActivity implements View.OnClickListener,
        SurfaceHolder.Callback, CustomOnScaleDetector.OnScaleListener {

    private SurfaceHolder mHolder;
    VideoView videoView;
//    private SeekBar seekbar;

    public RtmpLiveStream rtmpCamera;

    private ImageView btnRetake;

    private boolean inRecording = false;
    private ProgressBar progressBar;
    ObjectAnimator animationProgressBar;
    private TextView tvDurationCounter, btnNext;

    int timeCounter = 0;
    private TextView number_countdown;
    private LinearLayout layoutCountdown;
    private ImageView toggleReactCam, btnInformation, thumbVideo;
    private AdsUtil mAdManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_react_cam);
        hideStatusBar(this);

        LottieAnimationView animationView = findViewById(R.id.animation_view);
        animationView.setVisibility(View.GONE);
        toggleReactCam = findViewById(R.id.img_btn_react_cam);
        thumbVideo = findViewById(R.id.thumbnail_video);
        btnInformation = findViewById(R.id.img_btn_info);
        btnRetake = findViewById(R.id.img_btn_discard);
        btnNext = findViewById(R.id.tv_next_react_cam);
        btnRetake.setVisibility(View.GONE);
        btnRetake.setOnClickListener(this);
        btnInformation.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        tvDurationCounter = findViewById(R.id.tv_count_duration);
        toggleReactCam.setOnClickListener(this);

        btn_back = findViewById(R.id.img_btn_back_header);
        btn_back.setOnClickListener(this);

        progressBar = findViewById(R.id.progressBar);
        animationProgressBar = ObjectAnimator.ofInt(progressBar, "progress", 0, 500); // see this max value coming back here, we animate towards that value
        animationProgressBar.setInterpolator(new LinearInterpolator());
        progressBar.setProgress(0);
        tvDurationCounter.setText("");

        layoutCountdown = findViewById(R.id.ln_countdown);
        number_countdown = findViewById(R.id.tv_number_countdown);

        addVideoView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RelativeLayout mAdview = findViewById(R.id.adView);
        mAdManager = new AdsUtil(this, mAdview);
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

        mAdManager.createInterstitialAdmob();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rtmpCamera = null;
    }

    MediaPlayer mediaPlayer;
    String videoFile = "";
    int videoDuration = 0;

    private void addVideoView() {
        videoView = findViewById(R.id.video_main1);
        videoFile = getIntent().getStringExtra(KEY_PATH_VIDEO);
        if (!videoFile.equals("")) {
            videoView.setVideoPath(videoFile);
            Glide.with(this)
                    .load(videoFile)
                    .into(thumbVideo);
        }
//        videoView.setMediaController(new MediaController(this));
        videoView.requestFocus();
        videoView.setOnPreparedListener(mp -> {
            videoDuration = mp.getDuration();
            animationProgressBar.setDuration(videoDuration);
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            videoPrepared(mp);
            mediaPlayer = mp;
            videoView.setOnCompletionListener(mediaPlayer -> {
//                endTime = mediaPlayer.getCurrentPosition() + 50;
                getEndReactCam();
            });
            initCamView();
        });
    }


    private int videoWidth, videoHeight, newVideoWidth, newVideoHeight;
    RelativeLayout screenVideo;
    boolean hasChangeViewPos = false;

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
        videoView.seekTo(100);

        if (hasChangeViewPos) {
            xLeftOld = xLeft;
            yTopOld = yTop;
        }
        xLeft = (screenWidth - lpVideo.width) / 2f;
        xRight = xLeft + lpVideo.width;
        yTop = screenVideo.getY() + (screenHeight + 1 - lpVideo.height) / 2f;
        yBottom = yTop + lpVideo.height - 1;

        newVideoWidth = lpVideo.width;
        newVideoHeight = lpVideo.height;
    }

    float yTop, yBottom, xLeft, xRight, yTopOld, xLeftOld;
    LinearLayout cameraPreview;
    SurfaceView cameraView;
    View mCameraLayout;
    ImageView btn_back;
    float camWidth, camHeight;

    int[] camOverlaySize = {180, 210, 240, 270, 300, 330, 360};
    int[] camViewSize = new int[7];

    @Override
    public void onPause() {
        super.onPause();
        if (!hasEndReact) {
            getEndReactCam();
        } else {
            countDownTimer.cancel();
            layoutCountdown.setVisibility(View.GONE);
            thumbVideo.setVisibility(View.VISIBLE);
            toggleReactCam.setEnabled(true);
            inRecording = false;
        }
    }

    RelativeLayout root;

    private void updateCamView(float scale) {
        for (int i = 0; i < 7; i++) {
            camViewSize[i] = camOverlaySize[i] * newVideoWidth / videoWidth;
        }
        camWidth = camViewSize[camSize];  // ~240px
        camHeight = camWidth * 4 / 3f;
        cameraPreview.setLayoutParams(new FrameLayout.LayoutParams((int) camWidth, (int) camHeight));
        mCameraLayout.post(() -> {
//            System.out.println("thanhlv updateCamview gggg");

            float newY = (mCameraLayout.getY() - yTopOld) * scale + yTop;
            float newX = (mCameraLayout.getX() - xLeftOld) * scale + xLeft;

            mCameraLayout.setX(newX);
            mCameraLayout.setY(newY);
        });
    }

    private void checkHasChangeVideoCamView() {
        hasChangeViewPos = false;
        if (screenVideo == null) {
            return;
        }
        int oldScreenWidth = screenVideo.getWidth();
        int oldScreenHeight = screenVideo.getHeight();
        screenVideo.post(() -> {
            if (oldScreenWidth != screenVideo.getWidth()
                    || oldScreenHeight != screenVideo.getHeight()) {
                hasChangeViewPos = true;
                videoPrepared(mediaPlayer);
                updateCamView(Math.min(screenVideo.getWidth() * 1f / oldScreenWidth, screenVideo.getHeight() * 1f / oldScreenHeight));
            }
        });
    }

    private void initCamView() {

        root = findViewById(R.id.root_container);
        mCameraLayout = getLayoutInflater().inflate(R.layout.layout_camera_view, root, false);
        if (cameraPreview != null && cameraView.getParent() != null) {
            ((ViewGroup) cameraView.getParent()).removeView(cameraView); // <- fix
        }
        cameraView = new SurfaceView(this);
        rtmpCamera = new RtmpLiveStream(cameraView);
        cameraPreview = mCameraLayout.findViewById(R.id.camera_preview);
        cameraPreview.addView(cameraView);
        mHolder = cameraView.getHolder();
        mHolder.addCallback(this);
//        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        for (int i = 6; i >= 0; i--) {
            camViewSize[i] = camOverlaySize[i] * newVideoWidth / videoWidth;
            if (camViewSize[i] >= 300) camSize = i;
        }
        camWidth = camViewSize[camSize];  // ~240px
        camHeight = camWidth * 4 / 3f;

        cameraPreview.setLayoutParams(new FrameLayout.LayoutParams((int) camWidth, (int) camHeight));
        mCameraLayout.post(() -> {
            mCameraLayout.setX(xRight - camWidth);
            mCameraLayout.setY(yBottom - camHeight);
        });
        root.addView(mCameraLayout);
        final CustomOnScaleDetector customOnScaleDetector = new CustomOnScaleDetector(this);
        final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, customOnScaleDetector);

        mCameraLayout.setOnTouchListener(new View.OnTouchListener() {
            private int x, y;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (inRecording) return true;
                if (event.getPointerCount() > 1) {
                    int pointerId1 = event.getPointerId(0);
                    int pointerId2 = event.getPointerId(1);
                    if (event.getX(pointerId1) < 0 || event.getX(pointerId2) < 0
                            || event.getX(pointerId1) > v.getWidth() || event.getX(pointerId2) > v.getWidth()
                            || event.getY(pointerId1) < 0 || event.getY(pointerId2) < 0
                            || event.getY(pointerId1) > v.getHeight() || event.getY(pointerId2) > v.getHeight()) {
                        //
                    } else scaleGestureDetector.onTouchEvent(event);
                    hasZoom = true;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) (v.getX() - event.getRawX());
                        y = (int) (v.getY() - event.getRawY());
                        customOnScaleDetector.resetLast();
                        hasZoom = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() < 2 && !hasZoom) {
                            v.setX(event.getRawX() + x);
                            v.setY(event.getRawY() + y);
                            if (v.getY() < yTop) {
                                v.setY(yTop);
                            } else if (v.getY() + camHeight >= yBottom) {
                                v.setY(yBottom - camHeight);
                            } else {
                                v.setY(event.getRawY() + y);
                            }
                            if (v.getX() < xLeft) {
                                v.setX(xLeft);
                            } else if (v.getX() >= xRight - camWidth) {
                                v.setX(xRight - camWidth);
                            } else {
                                v.setX(event.getRawX() + x);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                    default:
                        return true;
                }
            }
        });
    }

    boolean hasZoom = false;

    @SuppressLint("SimpleDateFormat")
    public String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    String cameraCahePath = "";
    boolean hasCamVideo = false;
    private final CountDownTimer countDownTimer = new CountDownTimer(2900, 1000) {
        @SuppressLint({"DefaultLocale"})
        public void onTick(long millisUntilFinished) {
            layoutCountdown.setVisibility(View.VISIBLE);
            number_countdown.setText(String.format("%d" , millisUntilFinished / 1000 + 1));
        }

        public void onFinish() {
            layoutCountdown.setVisibility(View.GONE);
            toggleReactCam.setEnabled(true);
            try {
                getStartReactCam();
            } catch (IOException e) {
                e.printStackTrace();
                rtmpCamera.stopRecord();
                videoView.stopPlayback();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void onClick(View v) {

        if (v == findViewById(R.id.img_btn_discard)) {
            showPopupConfirm("Retake this video?", "Start over","Cancel",  false);
        }

        if (v == findViewById(R.id.tv_next_react_cam)) {
            showPopupConfirm(getString(R.string.confirm_execute_react_cam), null, null, true);
        }

        if (v == findViewById(R.id.img_btn_info)) {
            showPopupConfirm(getString(R.string.camera_site_can_zoom_and_move_before_start), null, null, false);
        }

        if (v == findViewById(R.id.img_btn_react_cam)) {
            toggleReactCam.setEnabled(false);
            inRecording = true;
            if (hasCamVideo) return;

            if (!rtmpCamera.isRecording()) {
                cameraCahePath = StorageUtil.getCacheDir() + "/CacheCamOverlay_" + getTimeStamp() + ".mp4";
                if (!rtmpCamera.isStreaming()) {
                    if (rtmpCamera.prepareAudio() && rtmpCamera.prepareVideo()) {
                        countDownTimer.start();
                    } else {
                        Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    countDownTimer.start();
                }
            } else {
                getEndReactCam();
            }
        }

        if (v == findViewById(R.id.img_btn_back_header)) {
            if (!rtmpCamera.isRecording()) {
                if (hasCamVideo) {
                    showPopupConfirm("Discard the last clip?","Discard", "Cancel",  false);
                } else finish();
            } else {
                showPopupConfirm("Do you want to cancel processing?","Yes", "No",  false);
            }
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (!rtmpCamera.isRecording()) {
            if (hasCamVideo) {
                showPopupConfirm("Discard the last clip?","Discard", "Cancel",  false);
            } else finish();
        } else {
            showPopupConfirm("Do you want to cancel processing?","Yes", "No",  false);
        }
    }

    public void retakeVideo() {
        rtmpCamera.startPreview(1);
        progressBar.setProgress(0);
        thumbVideo.setVisibility(View.VISIBLE);
        tvDurationCounter.setText("");
        progressBar.setBackgroundResource(R.drawable.ic_play_react_svg);
        inRecording = false;
        if (hasCamVideo) {
            boolean deleteCamCache = new File(cameraCahePath).delete();
            cameraCahePath = "";
            hasCamVideo = false;
        }
        btnInformation.setVisibility(View.VISIBLE);
        btnRetake.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);

        toggleReactCam.setEnabled(true);
    }

    public void discardVideo() {
        if (hasCamVideo) {
            boolean deleteCamCache = new File(cameraCahePath).delete();
        }
        finish();
    }


    public FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
        @Override
        public void onAdClicked() {
        }

        @Override
        public void onAdDismissedFullScreenContent() {

            AdsUtil.lastTime = (new Date()).getTime();
            startExecuteService();
        }

        @Override
        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
            startExecuteService();
        }

        @Override
        public void onAdImpression() {
        }

        @Override
        public void onAdShowedFullScreenContent() {
        }
    };

    public void showInterstitialAd() {
        if (mAdManager.interstitialAdAlready()) {
            mAdManager.showInterstitialAd(fullScreenContentCallback);
        } else {
            startExecuteService();
        }
    }

    private void startExecuteService() {
        VideoProfileExecute videoProfile = new VideoProfileExecute(MyUtils.TYPE_REACT_VIDEO, videoFile, cameraCahePath,
                startTime, endTime, camOverlaySize[camSize], posX, posY, false, false);
        Bundle bundle = new Bundle();
        bundle.putSerializable("package_video_profile", videoProfile);
        Intent intent = new Intent(this, ExecuteService.class);
        intent.putExtras(bundle);
        intent.putExtra("bundle_video_execute_time", (long) ((endTime + videoDuration) / 2.5));
        startService(intent);
    }


    private final Handler mCounterUpdateHandler = new Handler();
    private final Runnable mUpdateCounter = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(() -> tvDurationCounter.setText(parseTime(timeCounter/1000)));
            timeCounter = timeCounter + 50;
            mCounterUpdateHandler.postDelayed(this, 50);
        }
    };

    private void showPopupConfirm(String title, String pos, String neg, boolean requiredFinish) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, title);
        bundle.putString(KEY_POSITIVE, pos);
        bundle.putString(KEY_NEGATIVE, neg);
        PopupConfirm.newInstance(new IConfirmPopupListener(){
            @Override
            public void onClickPositiveButton() {
                if (requiredFinish) {
                    showInterstitialAd();
                    finish();
                } else {
                    doPositiveButton(pos);
                }
            }
            @Override
            public void onClickNegativeButton() {
            }
        }, bundle).show(getSupportFragmentManager(), "");
    }

    @SuppressLint("DefaultLocale")
    private String parseTime(int timeCounter) {
        int hh = timeCounter / 3600;
        int mm = timeCounter / 60;
        int ss = timeCounter % 60;
        if (hh == 0) return String.format("%02d:%02d", mm, ss);
        return String.format("%d:%02d:%02d", hh, mm, ss);
    }

    private void doPositiveButton(String action) {
        if (action == null) return;
        if (action.equals("Start over")) retakeVideo();
        if (action.equals("Discard")) discardVideo();
        if (action.equals("Yes")) doCancelReact();
    }

    public void doCancelReact() {
        if (rtmpCamera.isRecording()) rtmpCamera.stopRecord();
        finish();
    }

    boolean hasEndReact = true;
    private void getEndReactCam() {
        if (videoView.isPlaying()) videoView.pause();
//        endTime = mediaPlayer.getCurrentPosition() + 50; //laggy of mediaplayer refer https://issuetracker.google.com/issues/36907697
        endTime = timeCounter;
        new Handler().postDelayed(() -> {
            rtmpCamera.stopRecord();
            rtmpCamera.stopPreview();
            hasCamVideo = true;
        }, 100);
        progressBar.clearAnimation();
        progressBar.setBackgroundResource(R.drawable.ic_play_react_svg_pause);
        animationProgressBar.cancel();
        mCounterUpdateHandler.removeCallbacks(mUpdateCounter);
        btnInformation.setVisibility(View.GONE);
        btnRetake.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
        hasEndReact = true;
    }

    int startTime, endTime = 0;
    int posX, posY;

    private void getStartReactCam() throws IOException {

        thumbVideo.setVisibility(View.GONE);
        rtmpCamera.startRecord(cameraCahePath);
        videoView.start();
        startTime = 0;
        timeCounter = 0;
        animationProgressBar.start();
        mCounterUpdateHandler.post(mUpdateCounter);
        posX = (int) ((mCameraLayout.getX() - xLeft) * videoWidth / newVideoWidth);
        posY = (int) ((mCameraLayout.getY() - yTop) * videoHeight / newVideoHeight + 1);
        hasCamVideo = false;
        hasEndReact = false;
    }

    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder == null) {
            // preview surface does not exist
            return;
        }
        rtmpCamera.startPreview(1);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (rtmpCamera != null) rtmpCamera.stopPreview();
        if (mHolder != null) mHolder.removeCallback(null);
        mHolder = null;
        rtmpCamera = null;
    }


    private int camSize = 6;

    @Override
    public void zoomOut() {
        camSize++;
        if (camSize > 6) {
            camSize = 6;
            return;
        }
        camWidth = camViewSize[camSize];
        camHeight = camWidth * 4 / 3f;
        cameraPreview.setLayoutParams(new FrameLayout.LayoutParams((int) camWidth, (int) camHeight));
        mCameraLayout.setX(mCameraLayout.getX() - (camWidth - camViewSize[camSize - 1]) / 2f);
        mCameraLayout.setY(mCameraLayout.getY() - (camWidth - camViewSize[camSize - 1]) * 2 / 3f);

        if (mCameraLayout.getY() < yTop) {
            mCameraLayout.setY(yTop);
        } else if (mCameraLayout.getY() + camHeight >= yBottom) {
            mCameraLayout.setY(yBottom - camHeight);
        }

        if (mCameraLayout.getX() < xLeft) {
            mCameraLayout.setX(xLeft);
        } else if (mCameraLayout.getX() >= xRight - camWidth) {
            mCameraLayout.setX(xRight - camWidth);
        }
        hasZoom = true;
    }

    @Override
    public void zoomIn() {
        camSize--;
        if (camSize < 0) {
            camSize = 0;
            return;
        }
        camWidth = camViewSize[camSize];
        camHeight = camWidth * 4 / 3f;
        cameraPreview.setLayoutParams(new FrameLayout.LayoutParams((int) camWidth, (int) camHeight));
        mCameraLayout.setX(mCameraLayout.getX() + (camViewSize[camSize + 1] - camWidth) / 2f);
        mCameraLayout.setY(mCameraLayout.getY() + (camViewSize[camSize + 1] - camWidth) * 2 / 3f);
        hasZoom = true;
    }
}