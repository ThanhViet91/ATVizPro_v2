package com.examples.atscreenrecord_test.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.examples.atscreenrecord_test.App;
import com.netcompss.ffmpeg4android.CommandValidationException;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.ffmpeg4android.Prefs;
import com.netcompss.loader.LoadJNI;

public class TranscodingAsyncTask extends AsyncTask<String, Integer, Integer> {
    public static final String ERROR_CODE = "error_code";
    ProgressDialog progressDialog;
    String workFolder = null;
    String demoVideoFolder = null;
    String demoVideoPath = null;
    String vkLogPath = null;
    private boolean commandValidationFailedFlag = false;
    Context context;
    String commandStr;
    String outputPath="";
    FFmpegUtil.ITranscoding mCallback = null;

    public TranscodingAsyncTask (Context act, String command, String outPath, FFmpegUtil.ITranscoding callback) {
        context = act;
        commandStr = command;
        outputPath = outPath;
        mCallback = callback;
//        demoVideoFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/videokit/";
//        demoVideoPath = demoVideoFolder + "in.mp4";
        workFolder = App.getAppContext().getFilesDir().getAbsolutePath() + "/";
        vkLogPath = workFolder + "vk.log";
//        GeneralUtils.copyLicenseFromAssetsToSDIfNeeded(context, workFolder);
//        GeneralUtils.copyDemoVideoFromAssetsToSDIfNeeded(context, demoVideoFolder);
        int rc = GeneralUtils.isLicenseValid(App.getAppContext(), workFolder);
        System.out.println("thanhlv isLicenseValid ====== " + rc);
    }



    @Override
    protected void onPreExecute() {
        if (mCallback != null) mCallback.onStartTranscoding(outputPath);
        System.out.println("thanhlv isLicenseValid ====== onPreExecute");
    }

    protected Integer doInBackground(String... paths) {
        Log.i(Prefs.TAG, "doInBackground started...");
        System.out.println("thanhlv isLicenseValid ======  doInBackground");

        // delete previous log
        boolean isDeleted = GeneralUtils.deleteFileUtil(workFolder + "/vk.log");
        Log.i(Prefs.TAG, "vk deleted: " + isDeleted);


//        PowerManager powerManager = (PowerManager) context.getSystemService(Activity.POWER_SERVICE);
//        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
//        Log.d(Prefs.TAG, "Acquire wake lock");
//        wakeLock.acquire();


        LoadJNI vk = new LoadJNI();
        try {
            System.out.println("thanhlv commandStr ====== " + commandStr);
            vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, App.getAppContext());
        } catch (CommandValidationException e) {
            Log.e(Prefs.TAG, "vk run exeption.", e);
            System.out.println("thanhlv isLicenseValid ======  CommandValidationException");
            commandValidationFailedFlag = true;
            return 0;
        } catch (Throwable e) {
            Log.e(Prefs.TAG, "vk run exeption.", e);
            System.out.println("thanhlv isLicenseValid ======  Throwable");
            return 0;
        }
//        finally {
//            if (wakeLock.isHeld())
//                wakeLock.release();
//            else{
//                Log.i(Prefs.TAG, "Wake lock is already released, doing nothing");
//            }
//        }
        Log.i(Prefs.TAG, "doInBackground finished");
        return 0;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    @Override
    protected void onCancelled() {
        Log.i(Prefs.TAG, "onCancelled");
        if (mCallback != null) mCallback.onFinishTranscoding(ERROR_CODE);
        super.onCancelled();
    }


    @Override
    protected void onPostExecute(Integer result) {
        System.out.println("thanhlv isLicenseValid ======  onPostExecute");
        Log.i(Prefs.TAG, "onPostExecute");

        if (mCallback != null) mCallback.onFinishTranscoding(outputPath);
        super.onPostExecute(result);
        String rc = null;
        if (commandValidationFailedFlag) {
            rc = "Command Vaidation Failed";
        }
        else {
            rc = GeneralUtils.getReturnCodeFromLog(vkLogPath);
        }
        final String status = rc;
        System.out.println("thanhlv dddddddddddddddd === "+commandStr +" "+status);
//        activity.runOnUiThread(new Runnable() {
//            public void run() {
//                Toast.makeText(activity, status, Toast.LENGTH_LONG).show();
//                if (status.equals("Transcoding Status: Failed")) {
//                    Toast.makeText(activity, "Check: " + vkLogPath + " for more information.", Toast.LENGTH_LONG).show();
//                }
//            }
//        });
    }

}