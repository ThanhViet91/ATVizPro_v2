package com.examples.atscreenrecord.ui.activities;

import static com.examples.atscreenrecord.ui.utils.MyUtils.hideStatusBar;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.examples.atscreenrecord.App;
import com.examples.atscreenrecord.AppConfigs;
import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.controllers.settings.SettingManager2;

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
        if (SettingManager2.getRemoveAds(App.getAppContext())) {
//            System.out.println("thanhlv Ad was removed");
            new Handler().postDelayed(this::startMainActivity, 3000);
        } else {
            createTimer();
        }
        SettingManager2.setInterstitialPercent(this, AppConfigs.getInstance().getConfigModel().getInterstitialPercent());
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
                new CountDownTimer((long) 5 * 1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
//                        System.out.println("thanhlv remainnnnnnn ===  " + millisUntilFinished/1000);
                    }

                    @Override
                    public void onFinish() {
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