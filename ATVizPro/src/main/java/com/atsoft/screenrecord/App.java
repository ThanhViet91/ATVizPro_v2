package com.atsoft.screenrecord;

import static com.atsoft.screenrecord.utils.AdsUtil.AD_OPEN_APP_ID;
import static com.atsoft.screenrecord.utils.AdsUtil.AD_OPEN_APP_ID_DEV;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback;
import com.google.common.collect.ImmutableList;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Application class that initializes, loads and show ads when activities change states.
 */
public class App extends Application
        implements ActivityLifecycleCallbacks, LifecycleObserver {

    private AppOpenAdManager appOpenAdManager;
    private Activity currentActivity;
    private static final String TAG = "MyApplication";
    public static final String CHANNEL_ID = "com.atsoft.screenrecord";
    private static Application context;
    private FirebaseAnalytics mFirebaseAnalytics;

    public static Application getAppContext() {
        return App.context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.registerActivityLifecycleCallbacks(this);

        // Log the Mobile Ads SDK version.
        Log.d(TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion());
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        appOpenAdManager = new AppOpenAdManager();
        App.context = this;
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener((billingResult, list) -> {}).build();
        configs();
        getPurchaseHistory();
        createChannelNotification();
    }

    private BillingClient billingClient;
    private void getPurchaseHistory() {
        if (billingClient == null) return;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
//                getPurchaseHistory();
                MobileAds.initialize(context, initializationStatus -> {});
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    billingClient.queryPurchaseHistoryAsync(
                            QueryPurchaseHistoryParams.newBuilder()
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build(),
                            (billingResult1, purchasesHistoryList) -> {
                                if(billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK){
                                    checkPurchase(purchasesHistoryList, System.currentTimeMillis());
                                }  else  {
                                    MobileAds.initialize(context, initializationStatus -> {});
                                }
                            }
                    );
                }
            }
        });
    }

    private void checkPurchase(List<PurchaseHistoryRecord> purchasesHistoryList, long currentTime) {
        if (purchasesHistoryList == null || purchasesHistoryList.size() == 0) {
            SettingManager2.setProApp(context, false);
            MobileAds.initialize(context, initializationStatus -> {});
            System.out.println("thanhlv (purchasesHistoryList == null || purchasesHistoryList.size() == 0");
            return;
        }
        for (PurchaseHistoryRecord item : purchasesHistoryList) {
            if (item.getProducts().get(0).contains(AppConfigs.getInstance().getSubsModel().get(0).getKeyID()) ) {
                if ((currentTime - item.getPurchaseTime())/1000 < 7 * 24 * 60 * 60) {
                    SettingManager2.setProApp(context, true);
                    System.out.println("thanhlv ((currentTime - item.getPurchaseTime())/1000 < 7 * 24 * 60 * 60) ");
                    return;
                }
            }
            if (item.getProducts().get(0).contains(AppConfigs.getInstance().getSubsModel().get(1).getKeyID()) ) {
                if ((currentTime - item.getPurchaseTime())/1000 < 30 * 24 * 60 * 60) {
                    SettingManager2.setProApp(context, true);
                    System.out.println("thanhlv ((currentTime - item.getPurchaseTime())/1000 < 30 * 24 * 60 * 60) ");
                    return;
                }
            }
            if (item.getProducts().get(0).contains(AppConfigs.getInstance().getSubsModel().get(2).getKeyID()) ) {
                if ((currentTime - item.getPurchaseTime())/1000 < 365 * 24 * 60 * 60) {
                    SettingManager2.setProApp(context, true);
                    System.out.println("thanhlv ((currentTime - item.getPurchaseTime())/1000 < 365 * 24 * 60 * 60) ");
                    return;
                }
            }
        }
          // khong co goi nao giong hoac da het han
            SettingManager2.setProApp(context, false);
        MobileAds.initialize(context, initializationStatus -> {});
        System.out.println("thanhlv  // khong co goi nao giong hoac da het han");

    }

    private void connectGooglePlayBilling() {
        if (billingClient == null) return;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    showProducts();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
//                connectGooglePlayBilling();
                MobileAds.initialize(context, initializationStatus -> {});
            }
        });

    }
    String WEEKLY_ID = "", MONTHLY_ID = "", YEARLY_ID = "";
    private void showProducts() {
        WEEKLY_ID = AppConfigs.getInstance().getSubsModel().get(0).getKeyID();
        MONTHLY_ID = AppConfigs.getInstance().getSubsModel().get(1).getKeyID();
        YEARLY_ID = AppConfigs.getInstance().getSubsModel().get(2).getKeyID();

        ImmutableList<QueryProductDetailsParams.Product> productList = ImmutableList.of(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(WEEKLY_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(MONTHLY_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(YEARLY_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
        );

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        if (billingClient != null)
        billingClient.queryProductDetailsAsync(
                params,
                (billingResult, prodDetailsList) -> {
                    // Process the result
                    Core.productDetails = new ArrayList<>(prodDetailsList);
                }
        );
    }

    private void configs() {
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        AppConfigs.getInstance().setConfig(config);
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                //3600
                .setMinimumFetchIntervalInSeconds(10)
                .build();
        config.setConfigSettingsAsync(settings);
        config.setDefaultsAsync(R.xml.remote_config_defaults);
        config.fetchAndActivate()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        connectGooglePlayBilling();
                    }
                });
    }

    private void createChannelNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.full_app_name), NotificationManager.IMPORTANCE_NONE);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static boolean ignoreOpenAd = false;

    /**
     * LifecycleObserver method that shows the app open ad when the app moves to foreground.
     */
    @OnLifecycleEvent(Event.ON_START)
    protected void onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.

        if (SettingManager2.isProApp(App.getAppContext())) {
            return;
        }
        if (ignoreOpenAd){
            ignoreOpenAd = false;
            return;
        }
        appOpenAdManager.showAdIfAvailable(currentActivity);
    }


    /**
     * ActivityLifecycleCallback methods.
     */
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        // An ad activity is started when an ad is showing, which could be AdActivity class from Google
        // SDK or another activity class implemented by a third party mediation partner. Updating the
        // currentActivity only when an ad is not showing will ensure it is not an ad activity, but the
        // one that shows the ad.
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    /**
     * Shows an app open ad.
     *
     * @param activity                 the activity that shows the app open ad
     * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
     */
    public void showAdIfAvailable(
            @NonNull Activity activity,
            @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
        // We wrap the showAdIfAvailable to enforce that other classes only interact with MyApplication
        // class.
        if (SettingManager2.isProApp(App.getAppContext())) {
            return;
        }
        if (ignoreOpenAd){
            ignoreOpenAd = false;
            return;
        }
        appOpenAdManager.showAdIfAvailable(activity, onShowAdCompleteListener);
    }

    /**
     * Interface definition for a callback to be invoked when an app open ad is complete
     * (i.e. dismissed or fails to show).
     */
    public interface OnShowAdCompleteListener {
        void onShowAdComplete();
    }

    /**
     * Inner class that loads and shows app open ads.
     */
    private static class AppOpenAdManager {

        private static final String LOG_TAG = "AppOpenAdManager";

        private AppOpenAd appOpenAd = null;
        private boolean isLoadingAd = false;
        private boolean isShowingAd = false;

        /**
         * Keep track of the time an app open ad is loaded to ensure you don't show an expired ad.
         */
        private long loadTime = 0;

        /**
         * Constructor.
         */
        public AppOpenAdManager() {
        }

        /**
         * Load an ad.
         *
         * @param context the context of the activity that loads the ad
         */
        private void loadAd(Context context) {
            // Do not load ad if there is an unused ad or one is already loading.
            if (isLoadingAd || isAdAvailable()) {
                return;
            }

            isLoadingAd = true;
            AdRequest request = new AdRequest.Builder().build();
            AppOpenAd.load(
                    context,
                    BuildConfig.DEBUG ? AD_OPEN_APP_ID_DEV : AD_OPEN_APP_ID,
                    request,
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                    new AppOpenAdLoadCallback() {
                        /**
                         * Called when an app open ad has loaded.
                         *
                         * @param ad the loaded app open ad.
                         */
                        @Override
                        public void onAdLoaded(@NonNull AppOpenAd ad) {
                            appOpenAd = ad;
                            isLoadingAd = false;
                            loadTime = (new Date()).getTime();
                        }

                        /**
                         * Called when an app open ad has failed to load.
                         *
                         * @param loadAdError the error.
                         */
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            isLoadingAd = false;
                        }
                    });
        }

        /**
         * Check if ad was loaded more than n hours ago.
         */
        private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
            long dateDifference = (new Date()).getTime() - loadTime;
            long numMilliSecondsPerHour = 3600000;
            return (dateDifference < (numMilliSecondsPerHour * numHours));
        }

        /**
         * Check if ad exists and can be shown.
         */
        private boolean isAdAvailable() {
            // Ad references in the app open beta will time out after four hours, but this time limit
            // may change in future beta versions. For details, see:
            // https://support.google.com/admob/answer/9341964?hl=en
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
        }

        /**
         * Show the ad if one isn't already showing.
         *
         * @param activity the activity that shows the app open ad
         */
        private void showAdIfAvailable(@NonNull final Activity activity) {
            showAdIfAvailable(
                    activity,
                    () -> {
                        // Empty because the user will go back to the activity that shows the ad.
                    });
        }

        /**
         * Show the ad if one isn't already showing.
         *
         * @param activity                 the activity that shows the app open ad
         * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
         */
        private void showAdIfAvailable(
                @NonNull final Activity activity,
                @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
            // If the app open ad is already showing, do not show the ad again.
            if (isShowingAd) {
                Log.d(LOG_TAG, "The app open ad is already showing.");
                return;
            }

            // If the app open ad is not available yet, invoke the callback then load the ad.
            if (!isAdAvailable()) {
                Log.d(LOG_TAG, "The app open ad is not ready yet.");
                onShowAdCompleteListener.onShowAdComplete();
                loadAd(activity);
                return;
            }

            Log.d(LOG_TAG, "Will show ad.");

            appOpenAd.setFullScreenContentCallback(
                    new FullScreenContentCallback() {
                        /** Called when full screen content is dismissed. */
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            appOpenAd = null;
                            isShowingAd = false;

                            Log.d(LOG_TAG, "onAdDismissedFullScreenContent.");
                            onShowAdCompleteListener.onShowAdComplete();
                            loadAd(activity);
                        }

                        /** Called when fullscreen content failed to show. */
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            appOpenAd = null;
                            isShowingAd = false;
                            Log.d(LOG_TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                            onShowAdCompleteListener.onShowAdComplete();
                            loadAd(activity);
                        }

                        /** Called when fullscreen content is shown. */
                        @Override
                        public void onAdShowedFullScreenContent() {
                            Log.d(LOG_TAG, "onAdShowedFullScreenContent.");
                        }
                    });

            isShowingAd = true;
            appOpenAd.show(activity);
        }
    }
}
