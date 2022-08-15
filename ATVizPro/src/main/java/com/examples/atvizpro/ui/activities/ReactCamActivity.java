package com.examples.atvizpro.ui.activities;

import static com.examples.atvizpro.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atvizpro.ui.utils.MyUtils.hideStatusBar;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.examples.atvizpro.R;
import com.examples.atvizpro.ui.ZVideoView;
import com.examples.atvizpro.ui.utils.CustomOnScaleDetector;
import com.examples.atvizpro.utils.StorageUtil;
import com.examples.atvizpro.utils.VideoUtil;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReactCamActivity extends AppCompatActivity implements View.OnClickListener,
        SurfaceHolder.Callback, CustomOnScaleDetector.OnScaleListener {

    private SurfaceHolder mHolder;
    ZVideoView videoView;
    private SeekBar seekbar;

    public static RtmpCamera1 rtmpCamera;

    private ImageView saveReactCam;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(this);
        setContentView(R.layout.activity_react_cam);

        saveReactCam = findViewById(R.id.saveReact);
        saveReactCam.setOnClickListener(this);
        saveReactCam.setVisibility(View.GONE);

        seekbar = findViewById(R.id.seekbar);
        btn_reactCam = findViewById(R.id.img_btn_react_cam);
        btn_reactCam.setOnClickListener(this);

        btn_back = findViewById(R.id.img_btn_back_header);
        btn_back.setOnClickListener(this);

        cameraView = new SurfaceView(this);
        if (rtmpCamera == null)
        rtmpCamera = new RtmpCamera1(cameraView);
        addVideoView();
    }


    MediaPlayer mediaPlayer;
    String videoFile;
    private void addVideoView() {
        videoView = findViewById(R.id.video_main);
        videoFile = getIntent().getStringExtra(KEY_PATH_VIDEO);
        videoView.setVideoPath(videoFile);
        System.out.println("thanhlv file  videoooo = "+videoFile);
//        videoView.setMediaController(new MediaController(this));
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mp) {
                seekbar.setMax(mp.getDuration()/1000);
                mediaPlayer = mp;
                mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                videoPrepared(mp);
                initCamView();
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
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress*1000);
                }
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

        double diffRatio = videoRatio/screenRatio - 1;

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

        xLeft = (screenWidth - lpVideo.width)/2f;
        xRight = xLeft + lpVideo.width;
        yTop = screenVideo.getY() + (screenHeight + 1 - lpVideo.height)/2f;
        yBottom = yTop + lpVideo.height - 1;

        newVideoWidth = lpVideo.width;
        newVideoHeight = lpVideo.height;

    }

    float yTop, yBottom, xLeft, xRight;
    LinearLayout cameraPreview;
    SurfaceView cameraView;
    View mCameraLayout;
    ImageView btn_reactCam, btn_back;
    float camWidth, camHeight;

    int[] camOverlaySize = {180, 210, 240, 270, 300, 330, 360};
    int[] camViewSize = new int[7];

    @Override
    protected void onPause() {
        super.onPause();
//        rtmpCamera.stopPreview();
    }

    private void initCamView() {

        RelativeLayout root = findViewById(R.id.root_container);
        mCameraLayout = LayoutInflater.from(this).inflate(R.layout.layout_camera_view, null, true);
        cameraPreview = mCameraLayout.findViewById(R.id.camera_preview);
        if (cameraView.getParent() != null) {
            ((ViewGroup)cameraView.getParent()).removeView(cameraView);
        }
        cameraPreview.addView(cameraView);
        mHolder = cameraView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        for (int i = 0; i < 7; i++) {
            camViewSize[i] = camOverlaySize[i] * newVideoWidth / videoWidth;
        }
        camWidth = camViewSize[camSize];  // ~240px
        camHeight = camWidth * 4/3f;

        root.addView(mCameraLayout);

        cameraPreview.setLayoutParams(new FrameLayout.LayoutParams((int) camWidth, (int)camHeight));
        mCameraLayout.post(new Runnable() {
            @Override
            public void run() {
                mCameraLayout.setX(xRight-camWidth);
                mCameraLayout.setY(yBottom - camHeight);
            }
        });
        videoView.seekTo(0);

        final CustomOnScaleDetector customOnScaleDetector = new CustomOnScaleDetector(this);

        final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, customOnScaleDetector);

        mCameraLayout.setOnTouchListener(new View.OnTouchListener() {
            private int x, y;
            private int pointerId1, pointerId2;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getPointerCount() > 1) {
                    pointerId1 = event.getPointerId(0);
                    pointerId2 = event.getPointerId(1);

                    if (event.getX(pointerId1) < 0 || event.getX(pointerId2) < 0
                    || event.getX(pointerId1) > v.getWidth() || event.getX(pointerId2) > v.getWidth()
                    || event.getY(pointerId1) < 0 || event.getY(pointerId2) < 0
                            || event.getY(pointerId1) > v.getHeight() || event.getY(pointerId2) > v.getHeight()) {

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
                        if (event.getPointerCount() < 2 && !hasZoom ){
                            v.setX(event.getRawX() + x);
                            v.setY(event.getRawY() + y);
                            if (v.getY() < yTop) {
                                v.setY(yTop);
                            } else if (v.getY() + camHeight >= yBottom){
                                v.setY(yBottom - camHeight);
                            } else {
                                v.setY(event.getRawY() + y);
                            }
                            if (v.getX() < xLeft) {
                                v.setX(xLeft);
                            } else if (v.getX() >= xRight - camWidth){
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

    File file_path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath(), "Thanh");
    @SuppressLint("SimpleDateFormat")
    public String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }
    public String createFilePath() {
        if (!file_path.exists()) {
            file_path.mkdirs();
        }
        return file_path.getAbsolutePath() + File.separator + getTimeStamp()+".mp4";
    }

    String cameraCahePath;

    public void onClick(View v) {

        if (v == findViewById(R.id.img_btn_react_cam)) {

            if (!rtmpCamera.isRecording()) {
                cameraCahePath = StorageUtil.getCacheDir() + "/CacheCamOverlay_" + getTimeStamp() + ".mp4";
                System.out.println("thanhlv file  cammmm = "+cameraCahePath);
                try {
                    if (!rtmpCamera.isStreaming()) {
                        if (rtmpCamera.prepareAudio() && rtmpCamera.prepareVideo()) {

                            rtmpCamera.startRecord(cameraCahePath);
                            getStartReactCam();

                            Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error preparing stream, This device cant do it",
                                    Toast.LENGTH_SHORT).show();
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
            }
            else {
                getEndReactCam();
                rtmpCamera.stopRecord();

                Toast.makeText(this, "saved file " + cameraCahePath, Toast.LENGTH_SHORT).show();
            }
        }

        if (v == findViewById(R.id.img_btn_back_header)) {
            finish();
        }

        if (v == findViewById(R.id.saveReact)) {
            new VideoUtil().reactCamera(this, videoFile, cameraCahePath, startTime, endTime, camOverlaySize[camSize],
                    posX, posY, false, false, new VideoUtil.ITranscoding() {
                        @Override
                        public void onStartTranscoding(String outputCachePath) {

                        }

                        @Override
                        public void onFinishTranscoding(String code) {
                            finishReactCam();
                        }

                        @Override
                        public void onUpdateProgressTranscoding(int progress) {

                        }
                    });
        }
    }

    private void finishReactCam() {

        System.out.println("thanhlv finishReactCam " );
    }

    private void getEndReactCam() {
        endTime = mediaPlayer.getCurrentPosition();
        videoView.stopPlayback();
        saveReactCam.setVisibility(View.VISIBLE);
    }

    long startTime, endTime = 0;
    int posX, posY;
    private void getStartReactCam() {
        saveReactCam.setVisibility(View.GONE);
        startTime = mediaPlayer.getCurrentPosition();
        videoView.start();
        posX = (int) ((mCameraLayout.getX() - xLeft) * videoWidth / newVideoWidth);
        posY = (int) ((mCameraLayout.getY() - yTop) * videoHeight / newVideoHeight + 1);
    }



    public void surfaceCreated(SurfaceHolder holder) {
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }
            rtmpCamera.startPreview(1);

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        rtmpCamera.stopPreview();
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
        camHeight = camWidth * 4/3f;
        cameraPreview.setLayoutParams(new FrameLayout.LayoutParams((int) camWidth, (int) camHeight));
        mCameraLayout.setX(mCameraLayout.getX() - (camWidth - camViewSize[camSize-1])/2f);
        mCameraLayout.setY(mCameraLayout.getY() - (camWidth - camViewSize[camSize-1])*2/3f);

        if (mCameraLayout.getY() < yTop) {
            mCameraLayout.setY(yTop);
        } else if (mCameraLayout.getY() + camHeight >= yBottom){
            mCameraLayout.setY(yBottom - camHeight);
        }

        if (mCameraLayout.getX() < xLeft) {
            mCameraLayout.setX(xLeft);
        } else if (mCameraLayout.getX() >= xRight - camWidth){
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
        camHeight = camWidth * 4/3f;
        cameraPreview.setLayoutParams(new FrameLayout.LayoutParams((int) camWidth, (int) camHeight));
        mCameraLayout.setX(mCameraLayout.getX() + (camViewSize[camSize+1] - camWidth)/2f);
        mCameraLayout.setY(mCameraLayout.getY() + (camViewSize[camSize+1] - camWidth)*2/3f);
        hasZoom = true;
    }


}