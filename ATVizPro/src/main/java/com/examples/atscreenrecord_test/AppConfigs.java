package com.examples.atscreenrecord_test;

import com.examples.atscreenrecord_test.model.ConfigsModel;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

public class AppConfigs {
    private static AppConfigs _instance;
    private FirebaseRemoteConfig config;

    private AppConfigs(){

    }

    public ConfigsModel getConfigModel(){
        ConfigsModel configsModel = new ConfigsModel();
        Gson gson = new Gson(); // Or use new GsonBuilder().create();
        configsModel = gson.fromJson(this.config.getString("Configs"), ConfigsModel.class);
        return configsModel;
    }

    public FirebaseRemoteConfig getConfig(){
        return this.config;
    }

    public void setConfig(FirebaseRemoteConfig config){
        this.config = config;
    }

    public static AppConfigs getInstance(){
        if(_instance==null){
            _instance = new AppConfigs();
        }
        return _instance;
    }

}
