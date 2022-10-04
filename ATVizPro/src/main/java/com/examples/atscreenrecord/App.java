package com.examples.atscreenrecord;

import static com.examples.atscreenrecord.ui.activities.MainActivity.initialAds;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.examples.atscreenrecord.controllers.settings.SettingManager2;
import com.examples.atscreenrecord.ui.activities.MainActivity;
import com.examples.atscreenrecord.ui.utils.MyUtils;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Arrays;

public class App extends Application {

    public static final String CHANNEL_ID = "com.examples.atscreenrecord";
    private static Application context;


    public static Application getAppContext() {
        return App.context;
    }


    public static AppOpenManager appOpenManager;

    //    public static AppOpenManager getAppOpen() {
//        return appOpenManager;
//    }
    private void configs() {
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        AppConfigs.getInstance().setConfig(config);
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                //3600
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        config.setConfigSettingsAsync(settings);
        config.setDefaultsAsync(R.xml.remote_config_defaults);
        config.fetchAndActivate()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        initConfigs();
                    }
                });
    }

    private void initConfigs() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        App.context = this;
        if (!SettingManager2.getRemoveAds(this)) {
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                    initialAds = true;
                }
            });
        }

        appOpenManager = new AppOpenManager(this);

        createChannelNotification();

        configs();

//        if (Build.VERSION.SDK_INT >= 25) {
////            createShortcut();
//            if(Build.VERSION.SDK_INT >=26){
////                pinShortcut();
//            }
//
//        }else{
//            removeShortcuts();
//        }
    }

    private void createChannelNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "AT Screen Recorder", NotificationManager.IMPORTANCE_NONE);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }


    @TargetApi(25)
    private void createShortcut() {
        ShortcutManager sM = getSystemService(ShortcutManager.class);

        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
        intent1.setAction(MyUtils.ACTION_START_CAPTURE_NOW);

        ShortcutInfo shortcut1 = new ShortcutInfo.Builder(this, "shortcut_capture_now")
                .setIntent(intent1)
                .setShortLabel("Start Now")
                .setLongLabel("Start Service Now")
                .setDisabledMessage("Permission Denied, open app to resolve it")
                .setIcon(Icon.createWithResource(this, R.drawable.ic_app))
                .build();

        sM.setDynamicShortcuts(Arrays.asList(shortcut1));

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void pinShortcut() {
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        if (shortcutManager.isRequestPinShortcutSupported()) {

            ShortcutInfo pinShortcutInfo = new ShortcutInfo
                    .Builder(getApplicationContext(), "shortcut_capture_now")
                    .build();

            Intent pinnedShortcutCallbackIntent =
                    shortcutManager.createShortcutResultIntent(pinShortcutInfo);

//Get notified when a shortcut is pinned successfully//

            PendingIntent successCallback = PendingIntent.getBroadcast(getApplicationContext(), 0,
                    pinnedShortcutCallbackIntent, 0);
            shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.getIntentSender());
        }
    }

    @TargetApi(25)
    private void removeShortcuts() {
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        shortcutManager.disableShortcuts(Arrays.asList("shortcut_capture_now"));
        shortcutManager.removeAllDynamicShortcuts();
    }
}
