package com.atsoft.screenrecord.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.atsoft.screenrecord.AppConfigs;
import com.atsoft.screenrecord.BuildConfig;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;
import com.atsoft.screenrecord.ui.utils.MyUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Date;

public class AdsUtil {

    //DEV
    public static final String AD_SDK_ID_DEV = "ca-app-pub-3940256099942544~3347511713";
    public static final String AD_OPEN_APP_ID_DEV = "ca-app-pub-3940256099942544/3419835294";
    final String AD_BANNER_ID_DEV = "ca-app-pub-3940256099942544/6300978111";
    final String AD_INTERSTITIAL_ID_DEV = "ca-app-pub-3940256099942544/1033173712";

    //RELEASE
    public static final String AD_SDK_ID = "ca-app-pub-2210513256218688~4750691232";
    public static final String AD_OPEN_APP_ID = "ca-app-pub-2210513256218688/4175976164";
    final String AD_BANNER_ID = "ca-app-pub-2210513256218688/7767736814";
    final String AD_INTERSTITIAL_ID = "ca-app-pub-2210513256218688/1025400348";


    public AdsUtil(Context context, ViewGroup adViewRoot) {
        mAdViewRoot = adViewRoot;
        mContext = context;
        if (mAdViewRoot != null)
            initialAdView(mContext);
    }

    private Context mContext;
    private ViewGroup mAdViewRoot;
    private AdView adView;
    private boolean isLoaded = false;
    private AdRequest adRequest;

    public AdView getAdView() {
        return this.adView;
    }

    public void loadBanner() {
        if (SettingManager2.isProApp(mContext)) {
            mAdViewRoot.setVisibility(View.GONE);
        } else
            mAdViewRoot.setVisibility(View.VISIBLE);
    }


    public void initialAdView(Context context) {
        if (SettingManager2.isProApp(mContext)) return;
        if (isLoaded) return;
        adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(BuildConfig.DEBUG ? AD_BANNER_ID_DEV : AD_BANNER_ID);
        mAdViewRoot.addView(adView);
        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                isLoaded = true;
            }
        });
    }

    private InterstitialAd mInterstitialAdAdmob = null;

    public InterstitialAd getInterstitialAdAdmob() {
        return this.mInterstitialAdAdmob;
    }

    public static long lastTime = 0;

    public boolean interstitialAdAlready() {
        return !SettingManager2.isProApp(mContext)
                && this.mInterstitialAdAdmob != null
                && MyUtils.checkRandomPercentInterstitial()
                && ((new Date()).getTime() - lastTime) > AppConfigs.getInstance().getConfigModel().getFrequencyCapping() * 1000L;
    }

    public void createInterstitialAdmob() {
        if (SettingManager2.isProApp(mContext)) {
            mInterstitialAdAdmob = null;
            return;
        }
        String interstitialID = (BuildConfig.DEBUG ? AD_INTERSTITIAL_ID_DEV : AD_INTERSTITIAL_ID);
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(mContext, interstitialID, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAdAdmob = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                mInterstitialAdAdmob = null;
            }
        });
    }

    public void showInterstitialAd(FullScreenContentCallback fullScreenContentCallback) {
        mInterstitialAdAdmob.show((Activity) mContext);
        mInterstitialAdAdmob.setFullScreenContentCallback(fullScreenContentCallback);
    }

}
