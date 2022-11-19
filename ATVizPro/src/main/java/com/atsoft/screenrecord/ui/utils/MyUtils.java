package com.atsoft.screenrecord.ui.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
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
import androidx.core.content.FileProvider;

import com.atsoft.screenrecord.App;
import com.atsoft.screenrecord.AppConfigs;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;
import com.atsoft.screenrecord.utils.StorageUtil;
import com.google.android.material.snackbar.Snackbar;
import com.serenegiant.utils.UIThreadHelper;

import java.io.File;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyUtils {
    public static final boolean DEBUG = true;

    public static final int RESULT_CODE_FAILED = -999999;
    public static final String SCREEN_CAPTURE_INTENT_RESULT_CODE = "SCREEN_CAPTURE_INTENT_RESULT_CODE";
    public static final String ACTION_GO_HOME = "ACTION_GO_HOME";
    public static final String ACTION_SHOW_POPUP_RESULT = "ACTION_SHOW_POPUP_RESULT";
    public static final String ACTION_CLOSE_POPUP = "ACTION_CLOSE_POPUP";
    public static final String ACTION_SHOW_POPUP_GO_HOME = "ACTION_SHOW_POPUP_GO_HOME";
    public static final String ACTION_SHOW_POPUP_CONFIRM = "ACTION_SHOW_POPUP_CONFIRM";
    public static final String ACTION_UPDATE_SETTING = "ACTION_UPDATE_SETTING";
    public static final String ACTION_GO_TO_EDIT = "ACTION_GO_TO_EDIT";
    public static final String ACTION_GO_TO_PLAY = "ACTION_GO_TO_PLAY";
    public static final String NEW_URL = "NEW_URL";
    public static final String STREAM_PROFILE = "Stream_Profile";
    public static final String ACTION_DISCONNECT_LIVE_FROM_HOME = "ACTION_DISCONNECT_LIVE_FROM_HOME";
    public static final String ACTION_DISCONNECT_WHEN_STOP_LIVE = "ACTION_DISCONNECT_WHEN_STOP_LIVE";
    public static final String KEY_CAMERA_AVAILABLE = "KEY_CAMERA_AVAILABLE";
    public static final String KEY_CONTROLlER_MODE = "KEY_CONTROLLER_MODE";
    public static final String ACTION_INIT_CONTROLLER = "ACTION INIT CONTROLLER";
    public static final String SAMPLE_RMPT_URL = "rtmp://live.skysoft.us/live/thanh";
    public static final String KEY_MESSAGE = "KEY_MESSAGE";
    public static final String KEY_VALUE = "KEY_VALUE";
    public static final String KEY_SEND_PACKAGE_VIDEO = "key_send_package_video";
    public static final String ACTION_UPDATE_STREAM_PROFILE = "ACTION_UPDATE_STREAM_PROFILE";
    public static final String ACTION_UPDATE_TYPE_LIVE = "ACTION_UPDATE_TYPE_LIVE";
    public static final String ACTION_START_CAPTURE_NOW = "ACTION_START_CAPTURE_NOW";
    public static final String ACTION_SEND_MESSAGE_FROM_SERVICE = "ACTION_SEND_MESSAGE_FROM_SERVICE";
    public static final String ACTION_END_REACT = "ACTION_END_REACT";
    public static final String ACTION_END_RECORD = "ACTION_END_RECORD";
    public static final String ACTION_END_COMMENTARY = "ACTION_END_COMMENTARY";
    public static final String ACTION_GET_TIMER = "ACTION_GET_TIMER";
    public static final String ACTION_STOP_RECORDING_FROM_HOME = "ACTION_STOP_RECORDING_FROM_HOME";
    public static final String ACTION_START_RECORDING_FROM_HOME = "ACTION_START_RECORDING_FROM_HOME";
    public static final String ACTION_START_LIVESTREAM_FROM_HOME = "ACTION_START_LIVESTREAM_FROM_HOME";
    public static final String ACTION_STOP_LIVESTREAM_FROM_HOME = "ACTION_STOP_LIVESTREAM_FROM_HOME";
    public static final String ACTION_UPDATE_SHOW_HIDE_FAB = "ACTION_UPDATE_SHOW_HIDE_FAB";
    public static final String ACTION_RECORDING_ALREADY = "ACTION_RECORDING_ALREADY";
    public static final String MESSAGE_DISCONNECT_LIVE = "MESSAGE_DISCONNECT_LIVE";

    private static final String TAG = "my_utils";
    public static final int MODE_STREAMING = 101;
    public static final int MODE_RECORDING = 102;
    public static final String ACTION_FOR_REACT = "ACTION_FOR_REACT";
    public static final String ACTION_FOR_COMMENTARY = "ACTION_FOR_COMMENTARY";
    public static final String ACTION_FOR_EDIT = "ACTION_FOR_EDIT";
    public static final String ACTION_CANCEL_PROCESSING = "ACTION_CANCEL_PROCESSING";
    public static final String ACTION_EXIT_SERVICE = "ACTION_EXIT_SERVICE";
    public static final String APP_DIRECTORY_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/Recorder";

    public static boolean checkRandomPercentInterstitial() {
        return new Random().nextInt(100) < AppConfigs.getInstance().getConfigModel().getInterstitialPercent();
    }

    public static void sendBroadCastMessageFromService(Context context, String message) {
        Intent intent = new Intent(MyUtils.ACTION_SEND_MESSAGE_FROM_SERVICE);
        intent.putExtra(KEY_MESSAGE, message);
        context.sendBroadcast(intent);
    }
    public static void sendBroadCastMessageFromService(Context context, String message, long value) {
        Intent intent = new Intent(MyUtils.ACTION_SEND_MESSAGE_FROM_SERVICE);
        intent.putExtra(KEY_MESSAGE, message);
        intent.putExtra(KEY_VALUE, value);
        context.sendBroadcast(intent);
    }

    @NonNull
    public static String createFileName(Context context, @NonNull String ext) {
//        return "Record_" +getTimeStamp()+ ext;
        SettingManager2.setNumberRecordingFile(context, SettingManager2.getNumberRecordingFile(context) + 1);
        return "Recording-" + SettingManager2.getNumberRecordingFile(context) + ext;
    }

    public static boolean isVideo(Context context, String path) {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, Uri.fromFile(new File(path)));
            String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
            retriever.release();
            if (hasVideo != null && hasVideo.equals("yes")) { // neu la file video
                return true;
            }

        } catch (Exception ignored) {
            toast(context, "This file is error!", Toast.LENGTH_SHORT);
            retriever.release();
            return false;
        }
        toast(context, "This file is error!", Toast.LENGTH_SHORT);
        return false;
    }

    public static long getCurrentTimeStamp() {
        return (new Date()).getTime();
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    public static double getCacheSize() { //MB
        long size = 0;
        File[] files = new File(StorageUtil.getCacheDir()).listFiles();
        assert files != null;
        for (File f : files) {
            size = size + f.length();
        }

        return (double) size / (1024 * 1024);
    }

    public static long dirSize(File dir) {

        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    // Recursive call if it's a directory
                    if (file.isDirectory()) {
                        result += dirSize(file);
                    } else {
                        // Sum the file size in bytes
                        result += file.length();
                    }
                }
            }

            return result; // return the file size in byte
        }


        return 0;
    }

    @SuppressLint("DefaultLocale")
    public static String dirSizeString(File dir) {

        if (dir.exists()) {
            long  result = 0;
            result = dirSize(dir);
            if (result < 500 * 1024 * 1024) {
                return String.format("%.1f MB",result * 1f/ (1024 * 1024)); // return the file size in MB
            } else
                return String.format("%.1f GB",result * 1f/ (1024 * 1024 * 1024));
        }
        return "";
    }

    public static float fileSize(File file) {

        if (file.exists()) {
            return file.length() * 1f / (1024 * 1024); // return the file size MB
        }
        return 0;
    }

    public static double getAvailableSizeExternal() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        long blockSize = statFs.getBlockSize();
        return (double) statFs.getAvailableBlocks() * blockSize / (1024 * 1024 * 1024);

    }

    @NonNull
    public static String getBaseStorageDirectory() {
//        File directory = new File(APP_DIRECTORY_PATH);
        File directory = new File(App.getAppContext().getFilesDir(),"Projects");
        if (!directory.exists()) {
            boolean a = directory.mkdirs();
        }
        return directory.getAbsolutePath();
    }

    @NonNull
    public static String getCacheDirectory() {
        File directory = new File(StorageUtil.getCacheDir());
        if (!directory.exists()) {
            boolean a = directory.mkdirs();
        }
        return directory.getAbsolutePath();
    }

    public static long getDurationMs(Context context, String path) {
        if (path.equals("")) return 0;
        if (!new File(path).exists()) {
            return 0;
        }
        long timeInMs = 0;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, Uri.parse(path));
        } catch (Exception ignored) {

        }
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        retriever.release();
        if (time == null) return 0;
        timeInMs = Long.parseLong(time);
        return timeInMs;
    }

    public static String getDurationTime(Context context, String path) {
        if (path.equals("")) return "00:00";
        return parseLongToTime(getDurationMs(context, path));
    }

    @SuppressLint("DefaultLocale")
    public static String parseLongToTime(long durationInMillis) {
        long second = (durationInMillis / 1000) % 60;
        long minute = (durationInMillis / (1000 * 60)) % 60;
        long hour = (durationInMillis / (1000 * 60 * 60)) % 24;
        if (hour == 0) return String.format("%02d:%02d", minute, second);
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    public static void shareVideo(Context context, String videoFile) {
        File file = new File(videoFile);
        Uri uri = FileProvider.getUriForFile(context, "com.atsoft.screenrecord.provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, String.format("Share of %s", file.getName()));
        intent.setType(URLConnection.guessContentTypeFromName(file.getName()));
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Share Video"));
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
                    View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    public static void toast(Context mContext, String msg, int length) {
        UIThreadHelper.runOnUiThread(() -> Toast.makeText(mContext, msg, length).show());
    }

    public static boolean isValidFilenameSynctax(String filename) {
        for (int i = 0; i < filename.length(); i++) {
            char c = filename.charAt(i);
            if (c == '/' || c == '\\' || c == '%' || c == '"' || c == ':' || c == '*' || c == '<' || c == '>' || c == '|') {
                System.out.println("thanhlv isValidFilenameSynctax");
                return true;
            }
        }
        return false;
    }

    static final String HEXES = "0123456789ABCDEF";

    public static String getHex(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F))).append(' ');
        }
        return hex.toString();
    }

    public static final String IP_ADDRESS_PATTERN =
            "^rtmp://" +
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

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
