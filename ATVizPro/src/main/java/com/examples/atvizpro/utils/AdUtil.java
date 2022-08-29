package com.examples.atvizpro.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.examples.atvizpro.R;
import com.examples.atvizpro.ui.utils.MyUtils;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class AdUtil {

    public static void createBannerAdmob(Context context, AdView adView) {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
            }

            @Override
            public void onAdClicked() {
            }

            @Override
            public void onAdClosed() {
            }
        });
        adView.loadAd(adRequest);
    }

    public static void initAds(Context context) {
        MobileAds.initialize(context, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {

            }
        });
    }

    public static boolean isAppear() {
        int a = new Random().nextInt(100);
        System.out.println("thanhlv random ==== " + a);
        return a < 70;
    }

    static InterstitialAd mInterstitialAdAdmob;

    public static void createInterstitialAdmob(Context context) {


        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, context.getResources().getString(R.string.admob_intersitial_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAdAdmob = interstitialAd;
                        mInterstitialAdAdmob.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {

                                createInterstitialAdmob(context);

                                Log.d("TAG", "The ad was dismissed.");
//                                if (startScreen == 1) {
//                                    startActivity(new Intent(MainActivity.this, ScanActivity.class));
//                                }
//                                if (startScreen == 2) {
//                                    startActivity(new Intent(MainActivity.this, CreateActivity.class));
//                                }
//                                if (startScreen == 3) {
//                                    startActivity(new Intent(MainActivity.this, HistoryActivity.class));
//                                }
//                                if (startScreen == 4) {
//                                    startActivity(new Intent(MainActivity.this, SettingActivity.class));
//                                }
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                Log.d("TAG", "The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                mInterstitialAdAdmob = null;
                                Log.d("TAG", "The ad was shown.");
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAdAdmob = null;
                        createInterstitialAdmob(context);
                    }
                });
    }

}
