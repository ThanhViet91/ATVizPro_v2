package com.examples.atvizpro.ui.activities;

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

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class SplashActivity extends AppCompatActivity implements IAppOpenAdListener {

    PulsatorLayout pulsator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        // initialise pulsator layout
        pulsator = (PulsatorLayout) findViewById(R.id.pulsator);
        pulsator.start();

        App.appOpenManager.setCallBack(this);
    }

    boolean isActive = false;
    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
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
            startActivity(intent);
            pulsator.stop();
            finish();
        }
    }

    @Override
    public void onDismiss() {
        if (isActive) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            pulsator.stop();
            finish();
        }
    }
}