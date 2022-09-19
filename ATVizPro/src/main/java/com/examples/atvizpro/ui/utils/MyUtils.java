package com.examples.atvizpro.ui.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.examples.atvizpro.App;
import com.examples.atvizpro.utils.StorageUtil;
import com.google.android.material.snackbar.Snackbar;
import com.examples.atvizpro.controllers.settings.VideoSetting;
import com.examples.atvizpro.data.entities.Video;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Suppress;

public class MyUtils {
    public static final boolean DEBUG = true;

    public static final int RESULT_CODE_FAILED = -999999;
    public static final String SCREEN_CAPTURE_INTENT_RESULT_CODE = "SCREEN_CAPTURE_INTENT_RESULT_CODE";
    public static final String ACTION_OPEN_SETTING_ACTIVITY = "ACTION_OPEN_SETTING_ACTIVITY";
    public static final String ACTION_OPEN_LIVE_ACTIVITY = "ACTION_OPEN_LIVE_ACTIVITY";
    public static final String ACTION_OPEN_VIDEO_MANAGER_ACTIVITY = "ACTION_OPEN_VIDOE_MANAGER_ACTIVITY";
    public static final String ACTION_UPDATE_SETTING = "ACTION_UPDATE_SETTING";
    public static final int SELECTED_MODE_EMPTY = 0;
    public static final int SELECTED_MODE_ALL = 1;
    public static final int SELECTED_MODE_MULTIPLE = 2;
    public static final int SELECTED_MODE_SINGLE = 3;
    public static final String DRIVE_MASTER_FOLDER = "Zecorder";
    public static final String STREAM_PROFILE = "Stream_Profile";
    public static final String ACTION_NOTIFY_FROM_STREAM_SERVICE = "ACTION_NOTIFY_FROM_STREAM_SERVICE";
    public static final String KEY_CAMERA_AVAILABLE = "KEY_CAMERA_AVAILABLE";
    public static final String KEY_CONTROLlER_MODE = "KEY_CONTROLLER_MODE";
    public static final String ACTION_INIT_CONTROLLER = "ACTION INIT CONTROLLER";
//    public static final String SAMPLE_RMPT_URL = "rtmp://10.199.220.239/live/test";
//    public static final String SAMPLE_RMPT_URL = "rtmps://live-api-s.facebook.com:443/rtmp/FB-2214421522061173-0-AbwXJyetmt7gRPGb";
    public static final String SAMPLE_RMPT_URL = "rtmp://live.skysoft.us/live/thanh";
    public static final String KEY_STREAM_URL = "rtmp stream";
    public static final String KEY_STREAM_LOG = "Stream log";
    public static final String KEY_STREAM_IS_TESTED = "KEY_STREAM_IS_TESTED";
    public static final String ACTION_UPDATE_STREAM_PROFILE = "ACTION_UPDATE_STREAM_PROFILE";
    public static final String ACTION_START_CAPTURE_NOW = "ACTION_START_CAPTURE_NOW";

    private static final String TAG = "chienpm_utils";
    public static final int MODE_STREAMING = 101;
    public static final int MODE_RECORDING = 102;

    @NonNull
    public static String createFileName(@NonNull String ext) {
        return "Record_" +getTimeStamp()+ ext;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    public static double getCacheSize() { //MB
        long size = 0;
        File[] files = new File(StorageUtil.getCacheDir()).listFiles();
        assert files != null;
        for (File f:files) {
            size = size+f.length();
        }

        return (double) size/(1024*1024);
    }

    public static long dirSize(File dir) {

        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            if (fileList != null) {
                for(int i = 0; i < fileList.length; i++) {
                    // Recursive call if it's a directory
                    if(fileList[i].isDirectory()) {
                        result += dirSize(fileList[i]);
                    } else {
                        // Sum the file size in bytes
                        result += fileList[i].length();
                    }
                }
            }
            return result; // return the file size
        }
        return 0;
    }

    public static double getAvailableSizeExternal() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        long blockSize = statFs.getBlockSize();
        return (double) statFs.getAvailableBlocks()*blockSize/(1024*1024*1024);

    }

    @NonNull
    public static String getBaseStorageDirectory() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES) + "/Recorder");
        if (!directory.exists()) directory.mkdirs();
        return directory.getAbsolutePath();
    }

    @SuppressLint("ObsoleteSdkInt")
    public static String getPhotoDirectory(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            return context.getExternalFilesDir(Environment.DIRECTORY_DCIM) + "/Recorder";
        }
        else
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/Recorder";

    }

    @NonNull
    public static String getCacheDirectory() {
        File directory = new File(StorageUtil.getCacheDir().toString());
        if (!directory.exists()) directory.mkdirs();
        return directory.getAbsolutePath();
    }


    public static void showSnackBarNotification(View view, String msg, int length) {
        Snackbar.make(view, msg, length).show();
    }


    public static void hideStatusBar(Activity activity) {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            setFullscreen(activity);
        }
    }
    @SuppressLint("WrongConstant")
    public static void setFullscreen(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activity.getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.getWindow().setDecorFitsSystemWindows(false);
            activity.getWindow().getInsetsController().hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
//                hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            activity.getWindow().getInsetsController().setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }



    public static void toast(Context mContext, String msg, int length) {
        Toast.makeText(mContext, msg, length).show();
    }

    public static boolean isValidFilenameSynctax(String filename) {
        for(int i = 0; i< filename.length(); i++){
            char c = filename.charAt(i);
            if(c == '/' || c =='\\' || c=='"' || c == ':' || c=='*'||c=='<'|| c =='>' || c == '|')
                return true;
        }
        return false;
    }

    public static void saveCanvas(Canvas canvas) {

    }

    public static void logBytes(String msg, byte[] bytes) {
        StringBuilder str = new StringBuilder(msg + ": "+bytes.length+"\n");

        if(bytes == null || bytes.length < 1)
            str.append("bytes is null or length < 1");
        else{

            String base64Encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
            str.append("\nbase64: ").append(base64Encoded);//.append("\nbytes: ");
//            for(int i = 0; i < bytes.length; i++){
////                str.append( bytes[i]).append(" ");
//                str.append(String.format("%02x ", bytes[i]));
//            }
//            str.append(getHex(bytes));

        }
        Log.i(TAG, str.toString());
    }

    static final String HEXES = "0123456789ABCDEF";
    public static String getHex( byte [] raw ) {
        if ( raw == null ) {
            return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * raw.length );
        for ( final byte b : raw ) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F))).append(' ');
        }
        return hex.toString();
    }

    public static void shootPicture(ByteBuffer buf, int mWidth, int mHeight) {

        Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;

        File captureFile = new File(MyUtils.getBaseStorageDirectory(), MyUtils.createFileName(".jpg"));

        if (!captureFile.getParentFile().exists()) {
            captureFile.getParentFile().mkdirs();
        }

        if (captureFile.toString().endsWith(".jpg")) {
            compressFormat = Bitmap.CompressFormat.JPEG;
        }
        BufferedOutputStream os = null;
        try {
            try {
                os = new BufferedOutputStream(new FileOutputStream(captureFile));
                final Bitmap bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                buf.clear();
                bmp.copyPixelsFromBuffer(buf);
                bmp.compress(compressFormat, 100, os);

                //Log
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();

                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                Log.i(TAG, "shootPicture: "+encoded);
                //endlog

                bmp.recycle();
                os.flush();
            } finally {
                if (os != null) os.close();
            }
        } catch (final FileNotFoundException e) {
            Log.w(TAG, "failed to save file", e);
        } catch (final IOException e) {
            Log.w(TAG, "failed to save file", e);
        }
    }

    public static final String IP_ADDRESS_PATTERN =
            "^rtmp://"+
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])" +
            "/\\S" +
            "/\\S$";
    static String DOMAIN_PATTERN = "^rtmps?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]/[a-zA-Z0-9_.]*[a-zA-Z0-9_.]/[a-zA-Z0-9_.]*[a-zA-Z0-9_.]";

    public static final Pattern ipPattern = Pattern.compile(IP_ADDRESS_PATTERN);
    public static final Pattern domainPattern = Pattern.compile(DOMAIN_PATTERN);

    public static boolean isValidStreamUrlFormat(String url) {
        Matcher matcher = domainPattern.matcher(url);
        return matcher.find();
    }

    public static Video tryToExtractVideoInfoFile(Context context, VideoSetting videoSetting) {
        Video mVideo = null;
        try {

            File file = new File(Uri.parse(videoSetting.getOutputPath()).toString());
            Log.i(TAG, "tryToExtractVideoInfoFile: "+file);
            long size = file.length();
            String title = file.getName();

            String localPath = videoSetting.getOutputPath();
            int bitrate = videoSetting.getBitrate();
            int width = videoSetting.getWidth();
            int height = videoSetting.getHeight();
            int fps = videoSetting.getFPS();
            //Todo: fix it
            long duration = 0;

//            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            //use one of overloaded setDataSource() functions to set your data source
//            retriever.setDataSource(file.getAbsolutePath());
//            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//            String sBitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
//            String sWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
//            String sHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
//            String sFps = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE);

//            try {
//                bitrate = Integer.parseInt(sBitrate);
//            }catch (Exception e){
//                bitrate = videoSetting.getBitrate();
//            }
//
//            try {
//                duration = Long.parseLong(time)/1000;
//            } catch (Exception e){
//                duration = 0;
//            }


            mVideo = new Video(title, duration, bitrate, fps, width, height, size, localPath, 0, "", "");

//            retriever.release();

            Log.i(TAG, "tryToExtractVideoInfoFile: "+mVideo.toString());

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "tryToExtractVideoInfoFile: error-"+ e.getMessage());
        }
        return mVideo;
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRunningOnEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }


    public static void hideSoftInput(@NonNull final Activity activity) {
        hideSoftInput(activity.getWindow());
    }
    public static void hideSoftInput(@NonNull final Window window) {
        View view = window.getCurrentFocus();
        if (view == null) {
            View decorView = window.getDecorView();
            View focusView = decorView.findViewWithTag("keyboardTagView");
            if (focusView == null) {
                view = new EditText(window.getContext());
                view.setTag("keyboardTagView");
                ((ViewGroup) decorView).addView(view, 0, 0);
            } else {
                view = focusView;
            }
            view.requestFocus();
        }
        hideSoftInput(view);
    }
    public static void hideSoftInput(@NonNull final View view) {
        InputMethodManager imm =
                (InputMethodManager) App.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
