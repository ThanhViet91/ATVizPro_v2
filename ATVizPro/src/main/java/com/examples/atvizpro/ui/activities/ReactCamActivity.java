package com.examples.atvizpro.ui.activities;

import static com.examples.atvizpro.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atvizpro.ui.utils.MyUtils.hideStatusBar;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
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
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.examples.atvizpro.R;
import com.examples.atvizpro.controllers.settings.SettingManager2;
import com.examples.atvizpro.model.VideoReactCamExecute;
import com.examples.atvizpro.ui.ZVideoView;
import com.examples.atvizpro.ui.services.ExecuteService;
import com.examples.atvizpro.ui.utils.CustomOnScaleDetector;
import com.examples.atvizpro.utils.StorageUtil;
import com.examples.atvizpro.utils.VideoUtil;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.pedro.rtplibrary.rtmp.RtmpLiveStream;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReactCamActivity extends AppCompatActivity implements View.OnClickListener,
        SurfaceHolder.Callback, CustomOnScaleDetector.OnScaleListener {

    private SurfaceHolder mHolder;
    ZVideoView videoView;
    private SeekBar seekbar;

    public static RtmpLiveStream rtmpCamera;

    private ImageView toggleReactCam;

    private LottieAnimationView animationView;
    private boolean onProcessReact = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(this);
        setContentView(R.layout.activity_react_cam);

        animationView = findViewById(R.id.animation_view);
        animationView.setVisibility(View.GONE);

        toggleReactCam = findViewById(R.id.img_btn_react_cam);
        toggleReactCam.setOnClickListener(this);

        seekbar = findViewById(R.id.seekbar);

        btn_back = findViewById(R.id.img_btn_back_header);
        btn_back.setOnClickListener(this);

        cameraView = new SurfaceView(this);
        if (rtmpCamera == null) rtmpCamera = new RtmpLiveStream(cameraView);
        addVideoView();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initCamView();
            }
        }, 2000);

        if (!SettingManager2.getRemoveAds(getApplicationContext())) createInterstitialAdmob();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SettingManager2.getRemoveAds(getApplicationContext())) mInterstitialAdAdmob = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(root!=null){
//            mWindowManager.removeViewImmediate(mViewRoot);
//        }
        if (mCameraLayout != null) {
            root.removeView(mCameraLayout);
            rtmpCamera.stopPreview();
        }
        if (mCameraLayout != null) {
            root.removeView(mCameraLayout);
            rtmpCamera.stopPreview();
        }
    }

    MediaPlayer mediaPlayer;
    String videoFile;
    int videoDuration = 0;

    private void addVideoView() {
        videoView = findViewById(R.id.video_main);
        videoFile = getIntent().getStringExtra(KEY_PATH_VIDEO);
        videoView.setVideoPath(videoFile);
//        videoView.setMediaController(new MediaController(this));
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mp) {
                videoDuration = mp.getDuration();
                System.out.println("thanhlv addVideoView duration===  "+ videoDuration);
//                videoDuration = videoView.getDuration();
                seekbar.setMax(videoDuration / 1000);
                mediaPlayer = mp;
                mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                videoPrepared(mp);

            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) videoView.seekTo(progress * 1000);
            }
        });
    }

    private int videoWidth, videoHeight, newVideoWidth, newVideoHeight;

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

        xLeft = (screenWidth - lpVideo.width) / 2f;
        xRight = xLeft + lpVideo.width;
        yTop = screenVideo.getY() + (screenHeight + 1 - lpVideo.height) / 2f;
        yBottom = yTop + lpVideo.height - 1;

        newVideoWidth = lpVideo.width;
        newVideoHeight = lpVideo.height;
    }

    float yTop, yBottom, xLeft, xRight;
    LinearLayout cameraPreview;
    SurfaceView cameraView;
    View mCameraLayout;
    ImageView btn_back;
    float camWidth, camHeight;

    int[] camOverlaySize = {180, 210, 240, 270, 300, 330, 360};
    int[] camViewSize = new int[7];

    @Override
    protected void onPause() {
        super.onPause();
    }

    RelativeLayout root;

    private void initCamView() {
        root = findViewById(R.id.root_container);
        mCameraLayout = LayoutInflater.from(this).inflate(R.layout.layout_camera_view, null);
        cameraPreview = mCameraLayout.findViewById(R.id.camera_preview);

//        if (cameraView.getParent() != null) {
//            ((ViewGroup) cameraView.getParent()).removeView(cameraView);
//        }
//        cameraPreview.removeView(cameraView);
        cameraPreview.addView(cameraView);
        mHolder = cameraView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        for (int i = 0; i < 7; i++) {
            camViewSize[i] = camOverlaySize[i] * newVideoWidth / videoWidth;
        }
        camWidth = camViewSize[camSize];  // ~240px
        camHeight = camWidth * 4 / 3f;

        cameraPreview.setLayoutParams(new FrameLayout.LayoutParams((int) camWidth, (int) camHeight));
        mCameraLayout.post(new Runnable() {
            @Override
            public void run() {
                mCameraLayout.setX(xRight - camWidth);
                mCameraLayout.setY(yBottom - camHeight);
            }
        });
        videoView.seekTo(0);

        root.addView(mCameraLayout);
        final CustomOnScaleDetector customOnScaleDetector = new CustomOnScaleDetector(this);
        final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, customOnScaleDetector);

        mCameraLayout.setOnTouchListener(new View.OnTouchListener() {
            private int x, y;
            private int pointerId1, pointerId2;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (onProcessReact) return true;
                if (event.getPointerCount() > 1) {
                    pointerId1 = event.getPointerId(0);
                    pointerId2 = event.getPointerId(1);
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

    String cameraCahePath = "", cameraCahePath_flip = "";

    public void onClick(View v) {

        if (v == findViewById(R.id.img_btn_react_cam)) {
            onProcessReact = false;
            if (!rtmpCamera.isRecording()) {
                onProcessReact = true;
                cameraCahePath = StorageUtil.getCacheDir() + "/CacheCamOverlay_" + getTimeStamp() + ".mp4";
                try {
                    if (!rtmpCamera.isStreaming()) {
                        if (rtmpCamera.prepareAudio() && rtmpCamera.prepareVideo()) {
                            rtmpCamera.startRecord(cameraCahePath);
                            getStartReactCam();
                            Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        rtmpCamera.startRecord(cameraCahePath);
                        getStartReactCam();
                        Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    rtmpCamera.stopRecord();
                    videoView.stopPlayback();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                getEndReactCam();
                rtmpCamera.stopRecord();
            }
        }

        if (v == findViewById(R.id.img_btn_back_header)) {
            if (!cameraCahePath.equals("") && !cameraCahePath_flip.equals("")) {
                boolean deleteCamCache = new File(cameraCahePath).delete() && new File(cameraCahePath_flip).delete();
            }
            mCameraLayout.setVisibility(View.VISIBLE);
//            onBackPressed();
            finish();
        }
    }

    String finalVideoPath = "";

    private void finishReactCam(String ouputCacheFile) {
        Toast.makeText(this, ouputCacheFile, Toast.LENGTH_SHORT).show();
        finalVideoPath = ouputCacheFile;
        if (!cameraCahePath.equals("")) {
            boolean deleteCameraCache = new File(cameraCahePath).delete();
        }
        mCameraLayout.setVisibility(View.GONE);

        if (mInterstitialAdAdmob != null) {
            mInterstitialAdAdmob.show(this);
            mInterstitialAdAdmob.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    createInterstitialAdmob();
                    Intent intent = new Intent(ReactCamActivity.this, ReactCamFinishActivity.class);
                    intent.putExtra(KEY_PATH_VIDEO, finalVideoPath);
                    startActivity(intent);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                }

                @Override
                public void onAdImpression() {
                }

                @Override
                public void onAdShowedFullScreenContent() {
                }
            });
        } else {
            Intent intent = new Intent(ReactCamActivity.this, ReactCamFinishActivity.class);
            intent.putExtra(KEY_PATH_VIDEO, ouputCacheFile);
            startActivity(intent);
        }
    }

    InterstitialAd mInterstitialAdAdmob = null;

    public void createInterstitialAdmob() {
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

    private void getEndReactCam() {
        endTime = mediaPlayer.getCurrentPosition();
        videoView.stopPlayback();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation").setMessage(getString(R.string.confirm_execute_react_cam));
        builder.setCancelable(true);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                VideoReactCamExecute videoProfile = new VideoReactCamExecute(videoFile, cameraCahePath,
                        startTime, endTime, camOverlaySize[camSize], posX, posY, false, false);
                Bundle bundle = new Bundle();
                bundle.putSerializable("package_video_react",videoProfile);
                Intent intent = new Intent(ReactCamActivity.this, ExecuteService.class);
                intent.putExtras(bundle);
                intent.putExtra("bundle_video_react_time", (long)((endTime - startTime + videoDuration)/2.5));
                startService(intent);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void flipCamera() {
        new VideoUtil().flipHorizontal(cameraCahePath, new VideoUtil.ITranscoding() {
            @Override
            public void onStartTranscoding(String outputCachePath) {
                animationView.setVisibility(View.VISIBLE);
                animationView.playAnimation();
                cameraCahePath_flip = outputCachePath;
            }

            @Override
            public void onFinishTranscoding(String code) {
                executeFFmpegReactCam();
            }

            @Override
            public void onUpdateProgressTranscoding(int progress) {
            }
        });
    }

    public void executeFFmpegReactCam() {


                new VideoUtil().reactCamera(videoFile, cameraCahePath_flip, startTime, endTime, camOverlaySize[camSize],
                        posX, posY, false, false, new VideoUtil.ITranscoding() {
                            @Override
                            public void onStartTranscoding(String outputCachePath) {
                            }

                            @Override
                            public void onFinishTranscoding(String code) {
                                animationView.pauseAnimation();
                                animationView.setVisibility(View.GONE);
                                finishReactCam(code);
                            }

                            @Override
                            public void onUpdateProgressTranscoding(int progress) {
                            }
                        });

    }

    long startTime, endTime = 0;
    int posX, posY;

    private void getStartReactCam() {
        startTime = mediaPlayer.getCurrentPosition();
        videoView.start();
        posX = (int) ((mCameraLayout.getX() - xLeft) * videoWidth / newVideoWidth);
        posY = (int) ((mCameraLayout.getY() - yTop) * videoHeight / newVideoHeight + 1);
    }

    public void surfaceCreated(SurfaceHolder holder) {

        System.out.println("thanhlv surfaceCreated");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        rtmpCamera.startPreview(1);
        System.out.println("thanhlv surfaceChanged");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        rtmpCamera.stopPreview();
        System.out.println("thanhlv surfaceDestroyed");
        mHolder.removeCallback(null);
        mHolder = null;
    }

    private int camSize = 3;

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