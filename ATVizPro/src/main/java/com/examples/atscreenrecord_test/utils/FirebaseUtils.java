package com.examples.atscreenrecord_test.utils;

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

//    private static AppConfigs _instance;
//    private FirebaseRemoteConfig config;
//
//    private AppConfigs(){
//
//    }
//
//    public FirebaseRemoteConfig getConfig(){
//        return this.config;
//    }
//
//    public void setConfig(FirebaseRemoteConfig config){
//        this.config = config;
//    }
//
//    public static AppConfigs getInstance(){
//        if(_instance==null){
//            _instance = new AppConfigs();
//        }
//        return _instance;
//    }

}
