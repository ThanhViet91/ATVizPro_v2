package com.examples.atvizpro;

import static com.examples.atvizpro.ui.activities.ReactCamActivity.rtmpCamera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/** A basic Camera preview class */
public class PreviewCallBack extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private int mCamera;

    public static String TAG = "BackCameraPreview";

    public PreviewCallBack(Context context, int cameraId) {
        super(context);

        mCamera = cameraId;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
//        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        if (mCamera == 1)
        rtmpCamera.startPreview(mCamera);
        if (mCamera == 0)
        rtmpCamera.startPreview(mCamera);

    }

    public static Camera getCameraInstance(int cameraId){
        Camera c = null;
        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.e(TAG,"Camera " + cameraId + " not available! " + e.toString() );
        }
        return c; // returns null if camera is unavailable
    }
}