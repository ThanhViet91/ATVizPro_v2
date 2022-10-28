package com.examples.atscreenrecord_test.ui.activities;

import static com.examples.atscreenrecord_test.ui.services.ExecuteService.KEY_ACTION_STOP_SERVICE;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.examples.atscreenrecord_test.R;
import com.examples.atscreenrecord_test.ui.services.ExecuteService;
import com.examples.atscreenrecord_test.ui.utils.MyUtils;

public class PopUpResultVideoTranslucentActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_result_video_layout);
        if (getIntent() != null) {
            if (getIntent().getBooleanExtra(KEY_ACTION_STOP_SERVICE, false)) {
                stopProcessing();

            }
        }

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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


}