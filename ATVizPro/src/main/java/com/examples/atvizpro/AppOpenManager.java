package com.examples.atvizpro;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Date;

/** Prefetches App Open Ads. */
public class AppOpenManager implements LifecycleObserver, Application.ActivityLifecycleCallbacks {
    private static final String LOG_TAG = "AppOpenManager";
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/3419835294";
    private AppOpenAd appOpenAd = null;

    private AppOpenAd.AppOpenAdLoadCallback loadCallback;

    private final App myApplication;

    private Activity currentActivity;
    private boolean isFirstTime = true;
    public static boolean isPickFromGallery = false;

    /** Constructor */
    public AppOpenManager(App myApplication) {
        this.myApplication = myApplication;

        this.myApplication.registerActivityLifecycleCallbacks(this);

        ProcessLifecycleOwner.get().getLifecycle().addObserver((this));
    }


    /** LifecycleObserver methods */
    @OnLifecycleEvent(ON_START)
    public void onStart() {
        if (isPickFromGallery){
            isPickFromGallery = false;
            return;
        }
        showAdIfAvailable();
        Log.d(LOG_TAG, "onStart");
    }

    private static boolean isShowingAd = false;

    /** Shows the ad if one isn't already showing. */
    public void showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable()) {
            Log.d(LOG_TAG, "Will show ad.");

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            AppOpenManager.this.appOpenAd = null;
                            isShowingAd = false;
                            fetchAd();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {}

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                        }
                    };

            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            appOpenAd.show(currentActivity);

        } else {
            Log.d(LOG_TAG, "Can not show ad.");
            fetchAd();
        }
    }

    private long loadTime = 0;
    /** Request an ad */
    public void fetchAd() {
        // We will implement this below.

        if (isAdAvailable()) {
            return;
        }

        loadCallback =
                new AppOpenAd.AppOpenAdLoadCallback() {
                    /**
                     * Called when an app open ad has loaded.
                     *
                     * @param ad the loaded app open ad.
                     */
                    @Override
                    public void onAdLoaded(AppOpenAd ad) {
                        AppOpenManager.this.appOpenAd = ad;

                        AppOpenManager.this.loadTime = (new Date()).getTime();

                        if (AppOpenManager.this.isFirstTime) {
                            FullScreenContentCallback fullScreenContentCallback =
                                    new FullScreenContentCallback() {
                                        @Override
                                        public void onAdDismissedFullScreenContent() {
                                            // Set the reference to null so isAdAvailable() returns false.
                                            AppOpenManager.this.appOpenAd = null;
                                            isShowingAd = false;
                                            fetchAd();
                                        }

                                        @Override
                                        public void onAdFailedToShowFullScreenContent(AdError adError) {}

                                        @Override
                                        public void onAdShowedFullScreenContent() {
                                            isShowingAd = true;
                                        }
                                    };

                            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
                            appOpenAd.show(currentActivity);
                            AppOpenManager.this.isFirstTime= false;
                        }


                    }

                    /**
                     * Called when an app open ad has failed to load.
                     *
                     * @param loadAdError the error.
                     */
                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        // Handle the error.
                    }

                };
        AdRequest request = getAdRequest();
        AppOpenAd.load(
                myApplication, AD_UNIT_ID, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);


    }

    /** Utility method to check if ad was loaded more than n hours ago. */
    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }


    /** Creates and returns ad request. */
    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    /** Utility method that checks if ad exists and can be shown. */
    public boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        currentActivity = null;

    }
}