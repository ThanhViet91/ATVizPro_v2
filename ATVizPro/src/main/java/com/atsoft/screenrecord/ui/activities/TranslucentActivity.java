package com.atsoft.screenrecord.ui.activities;

import static com.atsoft.screenrecord.ui.utils.MyUtils.isMyServiceRunning;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.ui.services.ExecuteService;
import com.atsoft.screenrecord.ui.utils.MyUtils;

public class TranslucentActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translucent);
        if (getIntent() != null) {

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

    }

    private void stopProcessing() {
        if (isMyServiceRunning(this, ExecuteService.class)) {
            Intent controller = new Intent(this, ExecuteService.class);
            controller.setAction(MyUtils.ACTION_CANCEL_PROCESSING);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(controller);
            } else {
                startService(controller);
            }
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