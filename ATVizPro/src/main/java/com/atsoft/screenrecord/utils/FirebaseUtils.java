package com.atsoft.screenrecord.utils;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class FirebaseUtils {

    public void generateFirebaseRemoteConfig() {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
    }

    public static void logEventShowInterstitialAd(FirebaseAnalytics firebaseAnalytics, String action) {
        Bundle bundle = new Bundle();
        bundle.putString("action", action);
        firebaseAnalytics.logEvent("show_interstitial_5_times", bundle);
//        System.out.println("thanhlv logEventShowInterstitialAd " + action);
    }

    public static void logEventToFirebase(FirebaseAnalytics firebaseAnalytics, String action, String func) {
        Bundle bundle = new Bundle();
        bundle.putString("action", action);
        firebaseAnalytics.logEvent(func, bundle);
//        System.out.println("thanhlv logEventToFirebase " + func);
    }

}
