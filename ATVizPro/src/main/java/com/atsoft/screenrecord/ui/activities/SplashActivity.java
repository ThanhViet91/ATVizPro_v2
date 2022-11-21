package com.atsoft.screenrecord.ui.activities;

import static com.atsoft.screenrecord.ui.utils.MyUtils.hideStatusBar;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.atsoft.screenrecord.App;
import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    PulsatorLayout pulsator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        hideStatusBar(this);
        pulsator = (PulsatorLayout) findViewById(R.id.pulsator);
        pulsator.start();
        if (SettingManager2.isProApp(App.getAppContext())) {
            new Handler().postDelayed(this::startMainActivity, 3000);
        } else {
            createTimer();
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

    private void createTimer() {
        CountDownTimer countDownTimer =
                new CountDownTimer((long) 4910, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }
                    @Override
                    public void onFinish() {
                        if (SettingManager2.isProApp(App.getAppContext())) {
                            startMainActivity();
                            return;
                        }
                        Application application = getApplication();
                        // If the application is not an instance of MyApplication, log an error message and
                        // start the MainActivity without showing the app open ad.
                        if (!(application instanceof App)) {
                            startMainActivity();
                            return;
                        }
                        // Show the app open ad.
                        ((App) application).showAdIfAvailable(SplashActivity.this, () -> startMainActivity());
                    }
                };
        countDownTimer.start();
    }

    public void startMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        pulsator.stop();
    }
}