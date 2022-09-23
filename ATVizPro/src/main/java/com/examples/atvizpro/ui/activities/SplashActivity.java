package com.examples.atvizpro.ui.activities;

import static com.examples.atvizpro.ui.utils.MyUtils.hideStatusBar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.examples.atvizpro.App;
import com.examples.atvizpro.AppOpenManager;
import com.examples.atvizpro.IAppOpenAdListener;
import com.examples.atvizpro.R;
import com.examples.atvizpro.controllers.settings.SettingManager2;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class SplashActivity extends AppCompatActivity implements IAppOpenAdListener {

    PulsatorLayout pulsator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        hideStatusBar(this);

        isActive = true;
        // initialise pulsator layout
        pulsator = (PulsatorLayout) findViewById(R.id.pulsator);
        pulsator.start();

        if (SettingManager2.getRemoveAds(App.getAppContext())) {
            System.out.println("thanhlv Ad was removed");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    pulsator.stop();
                    finish();
                }
            }, 5000);
        } else {
            App.appOpenManager.setCallBack(this);
        }
    }

    boolean isActive = false;
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
    }

    @Override
    public void onLoadFail() {
        if (isActive) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            pulsator.stop();
            finish();
        }
    }

    @Override
    public void onDismiss() {
        if (isActive) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            pulsator.stop();
            finish();
        }
    }
}