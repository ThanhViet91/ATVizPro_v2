package com.examples.atvizpro.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.examples.atvizpro.App;
import com.netcompss.ffmpeg4android.CommandValidationException;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.ffmpeg4android.Prefs;
import com.netcompss.loader.LoadJNI;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TranscodingAsyncTask extends AsyncTask<String, Integer, Integer> {
    ProgressDialog progressDialog;
    String workFolder = null;
    String demoVideoFolder = null;
    String demoVideoPath = null;
    String vkLogPath = null;
    private boolean commandValidationFailedFlag = false;
    Activity activity;
    String commandStr;
    String outputPath;
    VideoUtil.ITranscoding mCallback = null;

    public TranscodingAsyncTask (Activity act, String command, String outPath, VideoUtil.ITranscoding callback) {
        activity = act;
        commandStr = command;
        outputPath = outPath;
        mCallback = callback;
//        demoVideoFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/videokit/";
//        demoVideoPath = demoVideoFolder + "in.mp4";

        workFolder = activity.getFilesDir().getAbsolutePath() + "/";
        vkLogPath = workFolder + "vk.log";

        GeneralUtils.copyLicenseFromAssetsToSDIfNeeded(activity, workFolder);
//        GeneralUtils.copyDemoVideoFromAssetsToSDIfNeeded(activity, demoVideoFolder);
    }



    @Override
    protected void onPreExecute() {
        if (mCallback != null) mCallback.onStartTranscoding(outputPath);
//        progressDialog = new ProgressDialog(activity);
//        progressDialog.setMessage("Transcoding in progress...");
//        progressDialog.show();
    }

    protected Integer doInBackground(String... paths) {
        Log.i(Prefs.TAG, "doInBackground started...");

        // delete previous log
        boolean isDeleted = GeneralUtils.deleteFileUtil(workFolder + "/vk.log");
        Log.i(Prefs.TAG, "vk deleted: " + isDeleted);


        PowerManager powerManager = (PowerManager) activity.getSystemService(Activity.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK");
        Log.d(Prefs.TAG, "Acquire wake lock");
        wakeLock.acquire();


        LoadJNI vk = new LoadJNI();
        try {
            vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, App.getAppContext());
//            GeneralUtils.copyFileToFolder(vkLogPath, demoVideoFolder);

        } catch (CommandValidationException e) {
            Log.e(Prefs.TAG, "vk run exeption.", e);
            commandValidationFailedFlag = true;

        } catch (Throwable e) {
            Log.e(Prefs.TAG, "vk run exeption.", e);
        }
        finally {
            if (wakeLock.isHeld())
                wakeLock.release();
            else{
                Log.i(Prefs.TAG, "Wake lock is already released, doing nothing");
            }
        }
        Log.i(Prefs.TAG, "doInBackground finished");
        return Integer.valueOf(0);
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    @Override
    protected void onCancelled() {
        Log.i(Prefs.TAG, "onCancelled");
//        progressDialog.dismiss();
        if (mCallback != null) mCallback.onFinishTranscoding("onCancelled");
        super.onCancelled();
    }


    @Override
    protected void onPostExecute(Integer result) {
        Log.i(Prefs.TAG, "onPostExecute");
//        progressDialog.dismiss();

        if (mCallback != null) mCallback.onFinishTranscoding("onPostExecute");
        super.onPostExecute(result);
        String rc = null;
        if (commandValidationFailedFlag) {
            rc = "Command Vaidation Failed";
        }
        else {
            rc = GeneralUtils.getReturnCodeFromLog(vkLogPath);
        }
        final String status = rc;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, status, Toast.LENGTH_LONG).show();
                if (status.equals("Transcoding Status: Failed")) {
                    Toast.makeText(activity, "Check: " + vkLogPath + " for more information.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}