package com.examples.atscreenrecord.ui.activities;

import static com.examples.atscreenrecord.ui.utils.MyUtils.hideStatusBar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.examples.atscreenrecord.App;
import com.examples.atscreenrecord.AppConfigs;
import com.examples.atscreenrecord.IAppOpenAdListener;
import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.controllers.settings.SettingManager2;
import com.examples.atscreenrecord.ui.services.ControllerService;
import com.examples.atscreenrecord.ui.services.ExecuteService;
import com.examples.atscreenrecord.ui.utils.MyUtils;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class TranslucentActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translucent);

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