package com.examples.atscreenrecord_test;

import com.examples.atscreenrecord_test.model.ConfigsModel;
import com.examples.atscreenrecord_test.model.SubscriptionsItemModel;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AppConfigs {
    private static AppConfigs _instance;
    private FirebaseRemoteConfig config;

    private AppConfigs() {

    }

    public ConfigsModel getConfigModel() {
        ConfigsModel configsModel = new ConfigsModel();
        Gson gson = new Gson(); // Or use new GsonBuilder().create();
        configsModel = gson.fromJson(this.config.getString("Configs"), ConfigsModel.class);
        return configsModel;
    }

    public ArrayList<SubscriptionsItemModel> getSubsModel() {
        ArrayList<SubscriptionsItemModel> subs = new ArrayList<>();
        JSONArray arr = null;
        Gson gson = new Gson(); // Or use new GsonBuilder().create();
        try {
            arr = new JSONArray(this.config.getString("Subscriptions"));

            for (int i = 0; i < arr.length(); i++) { // Walk through the Array.
                JSONObject obj = null;
                obj = arr.getJSONObject(i);
                SubscriptionsItemModel sub = gson.fromJson(obj.toString(), SubscriptionsItemModel.class);
                subs.add(sub);
                // Do whatever.
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return subs;
    }

    public FirebaseRemoteConfig getConfig() {
        return this.config;
    }

    public void setConfig(FirebaseRemoteConfig config) {
        this.config = config;
    }

    public static AppConfigs getInstance() {
        if (_instance == null) {
            _instance = new AppConfigs();
        }
        return _instance;
    }

}
