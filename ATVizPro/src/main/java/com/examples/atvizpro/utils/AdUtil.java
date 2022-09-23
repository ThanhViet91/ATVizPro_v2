package com.examples.atvizpro.utils;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.examples.atvizpro.controllers.settings.SettingManager2;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Random;

public class AdUtil {

    public static void createBannerAdmob(Context context, AdView adView) {
        if (SettingManager2.getRemoveAds(context)) {
            adView.setVisibility(View.GONE);
            return;
        }
        adView.setVisibility(View.VISIBLE);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                System.out.println("thanhlv createBannerAdmob  onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
                System.out.println("thanhlv createBannerAdmob  onAdFailedToLoad");
            }

            @Override
            public void onAdOpened() {
                System.out.println("thanhlv createBannerAdmob  onAdOpened");
            }

            @Override
            public void onAdClicked() {
                System.out.println("thanhlv createBannerAdmob  onAdClicked");

            }

            @Override
            public void onAdClosed() {
                System.out.println("thanhlv createBannerAdmob  onAdClosed");
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

}
