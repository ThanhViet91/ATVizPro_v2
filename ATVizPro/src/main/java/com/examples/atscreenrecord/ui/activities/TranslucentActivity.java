package com.examples.atscreenrecord.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.ui.services.ExecuteService;
import com.examples.atscreenrecord.ui.utils.MyUtils;
import com.examples.atscreenrecord.utils.VideoUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class TranslucentActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translucent);

//        if (getIntent() != null && getIntent().getAction().equals(MyUtils.ACTION_SHOW_POPUP_GO_HOME)) {
//            new AlertDialog.Builder(getApplicationContext())
//                    .setTitle("Save Video")
//                    .setMessage("Do you want to save this video?")
//                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
//                        // Continue with delete operation
//                        saveVideo(getIntent().getStringExtra(KEY_PATH_VIDEO));
//                        gotoMain();
//                    })
//                    .setNegativeButton(android.R.string.no, (dialogInterface, i) -> gotoMain())
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .show();
//        } else {

            new AlertDialog.Builder(this)
                    .setTitle("Notice!")
                    .setMessage(getString(R.string.do_you_want_cancel_the_process))
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        stopProcessing();
                        finish();
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> finish())
                    .setOnCancelListener(dialogInterface -> finish())
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
//        }
    }
    public void gotoMain() {
        MyUtils.toast(getApplicationContext(), "Go home!", Toast.LENGTH_SHORT);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setAction(MyUtils.ACTION_OPEN_SETTING_ACTIVITY);
        startActivity(intent);
    }
    private void stopProcessing() {
        Intent controller = new Intent(this, ExecuteService.class);
        controller.setAction(MyUtils.ACTION_CANCEL_PROCESSING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(controller);
        } else {
            startService(controller);
        }
    }

    String finalVideoSaved = "";
    public static boolean isSaved = false;

    public void saveVideo(String videoFile) {
        if (!isSaved) {
            try {
                finalVideoSaved = VideoUtil.generateFileOutput("Record");
                copyFile(new File(videoFile), new File(finalVideoSaved));
                isSaved = true;
                Toast.makeText(getApplicationContext(), "Video is saved.", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                isSaved = false;
                Toast.makeText(getApplicationContext(), "Saving failed, has some problem!", Toast.LENGTH_SHORT).show();
            }
        } else
            Toast.makeText(getApplicationContext(), "Video is saved.", Toast.LENGTH_SHORT).show();
    }

    void copyFile(File src, File dst) throws IOException {
        try (FileChannel inChannel = new FileInputStream(src).getChannel(); FileChannel outChannel = new FileOutputStream(dst).getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


}