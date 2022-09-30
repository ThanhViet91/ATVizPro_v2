package com.examples.atscreenrecord.ui.activities;

import static com.examples.atscreenrecord.Core.isConnected;
import static com.examples.atscreenrecord.ui.activities.CompressBeforeReactCamActivity.VIDEO_PATH_KEY;
import static com.examples.atscreenrecord.ui.fragments.DialogSelectVideoSource.ARG_PARAM1;
import static com.examples.atscreenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_FACEBOOK;
import static com.examples.atscreenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_TWITCH;
import static com.examples.atscreenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_YOUTUBE;
import static com.examples.atscreenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_CONNECTION_DISCONNECTED;
import static com.examples.atscreenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_CONNECTION_FAILED;
import static com.examples.atscreenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_CONNECTION_STARTED;
import static com.examples.atscreenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_ERROR;
import static com.examples.atscreenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_STREAM_STOPPED;
import static com.examples.atscreenrecord.ui.utils.MyUtils.KEY_MESSAGE;
import static com.examples.atscreenrecord.ui.utils.MyUtils.MESSAGE_DISCONNECT_LIVE;
import static com.examples.atscreenrecord.ui.utils.MyUtils.hideStatusBar;
import static com.examples.atscreenrecord.ui.utils.MyUtils.isMyServiceRunning;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.examples.atscreenrecord.AppOpenManager;
import com.examples.atscreenrecord.Core;
import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.controllers.settings.SettingManager2;
import com.examples.atscreenrecord.ui.fragments.DialogBitrate;
import com.examples.atscreenrecord.ui.fragments.DialogFragmentBase;
import com.examples.atscreenrecord.ui.fragments.DialogFrameRate;
import com.examples.atscreenrecord.ui.fragments.DialogSelectVideoSource;
import com.examples.atscreenrecord.ui.fragments.DialogVideoResolution;
import com.examples.atscreenrecord.ui.fragments.FragmentFAQ;
import com.examples.atscreenrecord.ui.fragments.FragmentSettings;
import com.examples.atscreenrecord.ui.fragments.GuidelineLiveStreamFragment;
import com.examples.atscreenrecord.ui.fragments.GuidelineScreenRecordFragment;
import com.examples.atscreenrecord.ui.fragments.LiveStreamingFragment;
import com.examples.atscreenrecord.ui.services.ControllerService;
import com.examples.atscreenrecord.ui.services.ExecuteService;
import com.examples.atscreenrecord.ui.services.streaming.StreamingService;
import com.examples.atscreenrecord.ui.utils.MyUtils;
import com.examples.atscreenrecord.utils.AdUtil;
import com.examples.atscreenrecord.utils.PathUtil;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.ImmutableList;
import com.takusemba.rtmppublisher.helper.StreamProfile;

import java.net.MulticastSocket;
import java.net.URISyntaxException;
import java.util.List;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_VIDEO_FOR_REACT_CAM = 1102;
    public static final int REQUEST_VIDEO_FOR_COMMENTARY = 1105;
    public static final int REQUEST_VIDEO_FOR_VIDEO_EDIT = 1107;
    public static final int REQUEST_SHOW_PROJECTS_DEFAULT = 105;
    private static final String THE_FIRST_TIME_SCREEN_RECORD = "action_first_record";
    private static final String THE_FIRST_TIME_LIVESTREAM = "action_first_livestream";
    public static boolean active = false;
    private static final int PERMISSION_REQUEST_CODE = 3004;
    private static final int PERMISSION_DRAW_OVER_WINDOW = 3005;
    private static final int PERMISSION_RECORD_DISPLAY = 3006;
    public static final String KEY_PATH_VIDEO = "key_video_selected_path";

    public void showProductRemoveAds() {
        handlerProductList(mProductDetailsList);
    }

    private static final String[] mPermission = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    public int mMode = MyUtils.MODE_RECORDING;
    private Intent mScreenCaptureIntent = null;
    private int mScreenCaptureResultCode = MyUtils.RESULT_CODE_FAILED;
    private StreamProfile mStreamProfile;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MyUtils.KEY_CONTROLlER_MODE, mMode);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mMode = savedInstanceState.getInt(MyUtils.KEY_CONTROLlER_MODE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        System.out.println("thanhlv getIntentttttt ==== " + intent.getAction());
        if (intent.getAction().equals(MyUtils.ACTION_GO_HOME)) {
            removeAllFragment();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
        registerSyncServiceReceiver();

    }

    private void registerSyncServiceReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyUtils.ACTION_SEND_MESSAGE_FROM_SERVICE);
        registerReceiver(mMessageReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
        unregisterReceiver(mMessageReceiver);
    }

    void handlePurchase(Purchase purchase) {
        // Purchase retrieved from BillingClient#queryPurchasesAsync or your PurchasesUpdatedListener.
        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                    if (billingResult.getResponseCode() == 0) {
                        if (purchase.getProducts().get(0).contains(getString(R.string.product_id_remove_ads))) {
                            SettingManager2.setRemoveAds(getApplicationContext(), true);
                            initialAds = false;
                        }
                    }
                });
            }
        }
    }


    private final PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
        // To be implemented in a later section.

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            //TODO
        } else {
            //TODO
        }

    };

    private BillingClient billingClient;

    private AdView mAdView;

    @Override
    protected void onPause() {
        super.onPause();
        pulsator.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideStatusBar(this);
        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
//        SettingManager2.setRemoveAds(this, false);
        initViews();
        connectGooglePlayBilling();
        if (!hasPermission()) requestPermissions();
        Intent intent = getIntent();
        if (intent != null)
            handleIncomingRequest(intent);
    }

    private void connectGooglePlayBilling() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    showProducts();
                    getPurchaseHistory();
                } else {
                    //check show ad banner when connectGGBill fail
                    if (!SettingManager2.getRemoveAds(getApplicationContext())) {
                        runOnUiThread(() -> {
                            System.out.println("thanhlv createBannerAdmob connectGooglePlayBilling ");
                            AdUtil.createBannerAdmob(getApplicationContext(), mAdView);
                        });
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

    }

    private List<ProductDetails> mProductDetailsList;

    private void showProducts() {
        QueryProductDetailsParams queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(
                                ImmutableList.of(
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId("at_screen_record_remove_ads_10")
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build(),
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId("at_screen_record_remove_ads_20")
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build(),
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId("remove_ads_thanh_10")
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build(),
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId("remove_ads_thanh_20")
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build(),
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId("remove_ads_50")
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build(),
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId("remove_ads_40")
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build(),
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId("remove_ads_30")
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build(),
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId("remove_ads_20")
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build(),
                                        QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId("remove_ads_10")
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build()
                                )
                        ).build();

        billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
                (billingResult, productDetailsList) -> {
                    // check billingResult
                    // process returned productDetailsList
                    mProductDetailsList = productDetailsList;
                }
        );


    }

    private void handlerProductList(List<ProductDetails> productDetailsList) {
        if (productDetailsList == null || productDetailsList.size() == 0) return;
        ImmutableList productDetailsParamsList;
        productDetailsParamsList = ImmutableList.of(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                        .setProductDetails(productDetailsList.get(0))
                        // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                        // for a list of offers that are available to the user
//                                                .setOfferToken(productDetailsList.get(0).getSubscriptionOfferDetails().get(0).getOfferToken())
                        .build()
        );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        // Launch the billing flow
        BillingResult billingResult = billingClient.launchBillingFlow(this, billingFlowParams);
    }

    public void getPurchaseHistory() {
        billingClient.queryPurchaseHistoryAsync(
                QueryPurchaseHistoryParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                (billingResult, purchasesHistoryList) -> {
                    // check billingResult
                    // process returned purchase history list, e.g. display purchase history

                    if (purchasesHistoryList != null) {
                        for (Object purchase : purchasesHistoryList) {
                            if (purchase.toString().contains(getString(R.string.product_id_remove_ads))) {
                                SettingManager2.setRemoveAds(getApplicationContext(), true);
                                initialAds = false;
                                break;
                            }
                        }
                    }
                    if (!SettingManager2.getRemoveAds(getApplicationContext())) {
                        runOnUiThread(() -> {
                            System.out.println("thanhlv createBannerAdmob getPurchaseHistory ");
                            AdUtil.createBannerAdmob(getApplicationContext(), mAdView);
                        });

                    }
                }
        );
    }

    void confirmPurchase(Purchase purchase) {
        if (purchase.getProducts().get(0).equals(getString(R.string.product_id_remove_ads)))
            SettingManager2.setRemoveAds(this, true);
    }

    void verifyPurchase(Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        ConsumeResponseListener listener = (billingResult, s) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                confirmPurchase(purchase);
            }
        };
        billingClient.consumeAsync(consumeParams, listener);
    }

    public static boolean initialAds = false;

    protected void onResume() {
        super.onResume();
        System.out.println("thanhlv onResume mainnnnnnnnn");
        pulsator.start();
        updateService();
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                (billingResult, list) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase purchase : list) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                                verifyPurchase(purchase);
                            }
                        }
                    }
                }
        );
        checkShowAd();
    }

    public void updateService() {
        if (isMyServiceRunning(getApplicationContext(), StreamingService.class)) {
            int type = SettingManager2.getLiveStreamType(this);
            if (isConnected) {
                liveStreaming.setText(getString(R.string.disconnect_livestream));
            } else
                liveStreaming.setText(getString(R.string.livestreaming));
            imgLiveType.setVisibility(View.VISIBLE);
            if (type == SOCIAL_TYPE_YOUTUBE)
                imgLiveType.setBackgroundResource(R.drawable.ic_youtube);
            if (type == SOCIAL_TYPE_FACEBOOK)
                imgLiveType.setBackgroundResource(R.drawable.ic_facebook);
            if (type == SOCIAL_TYPE_TWITCH)
                imgLiveType.setBackgroundResource(R.drawable.ic_twitch);
        } else {
            imgLiveType.setVisibility(View.GONE);
            liveStreaming.setText(getString(R.string.livestreaming));
        }
    }


    public void checkShowAd() {
        if (initialAds) {
            AdUtil.createBannerAdmob(getApplicationContext(), mAdView);
            createInterstitialAdmob();
        }
    }

    private void handleIncomingRequest(Intent intent) {
        if (intent.getAction() != null) {
            System.out.println("thanhlv Main intent === action : " + intent.getAction());
            switch (intent.getAction()) {
                case MyUtils.ACTION_START_CAPTURE_NOW:
                    mImgRec.performClick();
                    break;
                case "from_notification":
                    break;
            }
        }
    }

    private void requestScreenCaptureIntent() {
        if (mScreenCaptureIntent == null) {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), PERMISSION_RECORD_DISPLAY);
        }
    }

    private ImageView mImgRec, imgLiveType;
    private PulsatorLayout pulsator;
    private TextView liveStreaming;

    private void initViews() {
        mAdView = findViewById(R.id.adView);
        pulsator = findViewById(R.id.pulsator);
        liveStreaming = findViewById(R.id.tv_live_streaming);
        imgLiveType = findViewById(R.id.img_live_type);
        pulsator.start();
        generateVideoSettings();
        updateVideoSettings();
        mImgRec = findViewById(R.id.img_record);
        ImageView btn_setting = findViewById(R.id.img_settings);
        LinearLayout btn_set_resolution = findViewById(R.id.set_video_resolution);
        LinearLayout bnt_set_bitrate = findViewById(R.id.set_bitrate);
        LinearLayout btn_set_fps = findViewById(R.id.set_frame_rate);
        btn_setting.setOnClickListener(view -> getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_layout_fragment, new FragmentSettings(), "")
                .addToBackStack("")
                .commit());
        btn_set_resolution.setOnClickListener(view -> new DialogVideoResolution(this::updateVideoSettings).show(getSupportFragmentManager(), ""));
        bnt_set_bitrate.setOnClickListener(view -> new DialogBitrate(this::updateVideoSettings).show(getSupportFragmentManager(), ""));
        btn_set_fps.setOnClickListener(view -> new DialogFrameRate(this::updateVideoSettings).show(getSupportFragmentManager(), ""));
        mImgRec.setOnClickListener(view -> {
            if (isFirstTimeReach(THE_FIRST_TIME_SCREEN_RECORD)) return;
            if (isMyServiceRunning(getApplicationContext(), StreamingService.class)) {
                MyUtils.showSnackBarNotification(mImgRec, "LiveStream service is running!", Snackbar.LENGTH_LONG);
                return;
            }
            if (isMyServiceRunning(getApplicationContext(), ControllerService.class)) {
                MyUtils.showSnackBarNotification(mImgRec, "Recording service is running!", Snackbar.LENGTH_LONG);
                return;
            }
            mMode = MyUtils.MODE_RECORDING;
            shouldStartControllerService();
        });
        LinearLayout lnFAQ = findViewById(R.id.ln_btn_faq);
        lnFAQ.setOnClickListener(view -> showFAQFragment());
        LinearLayout react_cam = findViewById(R.id.ln_react_cam);
        react_cam.setOnClickListener(view -> {
            if (checkServiceBusy()) return;
            showDialogPickVideo(REQUEST_VIDEO_FOR_REACT_CAM);
        });
        ImageView btn_live = findViewById(R.id.img_live);
        btn_live.setOnClickListener(view -> {
            if (isMyServiceRunning(getApplicationContext(), StreamingService.class)) {
                if (isConnected) {
                    new AlertDialog.Builder(this)
                            .setTitle("Disconnect livestream!")
                            .setMessage("Do you want to disconnect livestream?")
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                // Continue with delete operation
                                sendDisconnectToService();
                            })
                            .setNegativeButton(android.R.string.no, (dialogInterface, i) -> {

                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    return;
                }
            }
            if (isFirstTimeReach(THE_FIRST_TIME_LIVESTREAM)) return;
            showLiveStreamFragment();
        });
        LinearLayout btn_commentary = findViewById(R.id.ln_btn_commentary);
        btn_commentary.setOnClickListener(view -> showDialogPickVideo(REQUEST_VIDEO_FOR_COMMENTARY));
        LinearLayout btn_projects = findViewById(R.id.ln_btn_projects);
        btn_projects.setOnClickListener(view -> showInterstitialAd(REQUEST_SHOW_PROJECTS_DEFAULT));
        LinearLayout btn_editor = findViewById(R.id.ln_btn_video_editor);
        btn_editor.setOnClickListener(view -> showDialogPickVideo(REQUEST_VIDEO_FOR_VIDEO_EDIT));
    }

    private void showLiveStreamFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_layout_fragment, new LiveStreamingFragment(), "")
                .addToBackStack("")
                .commit();
    }

    public void showFAQFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_layout_fragment, new FragmentFAQ(), "")
                .addToBackStack("")
                .commit();
    }

    private boolean checkServiceBusy() {
        boolean bb = false;
        if (isMyServiceRunning(this, ExecuteService.class)) {
            bb = true;
            new AlertDialog.Builder(this)
                    .setTitle("Please wait!")
                    .setMessage("Your previous video in processing, please check in status bar!")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        }
        return bb;
    }

    private boolean isFirstTimeReach(String type) {
        if (type.equals(THE_FIRST_TIME_SCREEN_RECORD))
            if (SettingManager2.getFirstTimeRecord(this)) {
                showTutorialScreenRecord();
                SettingManager2.setFirstTimeRecord(this, false);
                return true;
            }

        if (type.equals(THE_FIRST_TIME_LIVESTREAM))
            if (SettingManager2.getFirstTimeLiveStream(this)) {
                showTutorialLiveStream();
                SettingManager2.setFirstTimeLiveStream(this, false);
                return true;
            }

        return false;
    }

    private void showTutorialScreenRecord() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_layout_fragment, new GuidelineScreenRecordFragment())
                .addToBackStack("")
                .commit();
    }

    private void showTutorialLiveStream() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_layout_fragment, new GuidelineLiveStreamFragment())
                .addToBackStack("")
                .commit();
    }

    private void updateVideoSettings() {

        TextView tv_resolution = findViewById(R.id.tv_video_resolution);
        TextView tv_bitrate = findViewById(R.id.tv_bitrate);
        TextView tv_frame_rate = findViewById(R.id.tv_frame_rate);

        tv_resolution.setText(SettingManager2.getVideoResolution(this));
        tv_bitrate.setText(SettingManager2.getVideoBitrate(this));
        tv_frame_rate.setText(SettingManager2.getVideoFPS(this));
    }

    private void generateVideoSettings() {
        Core.resolution = SettingManager2.getVideoResolution(this);
        Core.bitrate = SettingManager2.getVideoBitrate(this);
        Core.frameRate = SettingManager2.getVideoFPS(this);
    }

    private void showDialogPickVideo(int requestVideoFor) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PARAM1, requestVideoFor);
        DialogSelectVideoSource.newInstance(new DialogFragmentBase.ISelectVideoSourceListener() {
            @Override
            public void onClick() {
            }

            @Override
            public void onClickCameraRoll() {
                showDialogPickFromGallery(requestVideoFor);
            }

            @Override
            public void onClickMyRecordings() {
//                showInterstitialAd(requestVideoFor);
                showMyRecordings(requestVideoFor);
            }
        }, bundle).show(getSupportFragmentManager(), "");
    }

    private void showMyRecordings(int from_code) {
        Intent intent = new Intent(this, ProjectsActivity.class);
        intent.putExtra("key_from_code", from_code);
        startActivity(intent);
    }

    public void showDialogPickFromGallery(int from_code) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI); // todo: this thing might need some work :/, eg open from google drive, stuff like that
        intent.setTypeAndNormalize("video/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_video_source)), from_code);
    }


    InterstitialAd mInterstitialAdAdmob = null;

    int loadAgain = 0;

    public void createInterstitialAdmob() {
        if (SettingManager2.getRemoveAds(this)) {
            mInterstitialAdAdmob = null;
            return;
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(getApplicationContext(), "ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAdAdmob = interstitialAd;
                        loadAgain = 0;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAdAdmob = null;
                    }
                });
    }

    public void showInterstitialAd(int from_code) {
        if (mInterstitialAdAdmob != null) {
            mInterstitialAdAdmob.show(this);
            mInterstitialAdAdmob.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    showMyRecordings(from_code);
                    createInterstitialAdmob();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    showMyRecordings(from_code);
                }

                @Override
                public void onAdImpression() {
                }

                @Override
                public void onAdShowedFullScreenContent() {
                }
            });
        } else {
            showMyRecordings(from_code);
        }
    }

    private void requestPermissions() {

        // PERMISSION DRAW OVER
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, PERMISSION_DRAW_OVER_WINDOW);
        }
        ActivityCompat.requestPermissions(this, mPermission, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                int granted = PackageManager.PERMISSION_GRANTED;
                for (int grantResult : grantResults) {
                    if (grantResult != granted) {
                        MyUtils.showSnackBarNotification(mImgRec, "Please grant all permissions to record screen.", Snackbar.LENGTH_LONG);
                        return;
                    }
                }
//                shouldStartControllerService();
            }
        }
    }

    public void shouldStartControllerService() {
        if (!hasCaptureIntent())
            requestScreenCaptureIntent();

        if (hasPermission()) {
            startControllerService();
        } else {
            requestPermissions();
        }
    }

    private boolean hasCaptureIntent() {
        return mScreenCaptureIntent != null;// || mScreenCaptureResultCode == MyUtils.RESULT_CODE_FAILED;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        AppOpenManager.isPickFromGallery = true;
        if (requestCode == REQUEST_VIDEO_FOR_REACT_CAM && resultCode == RESULT_OK) {
            final Uri selectedUri = data != null ? data.getData() : null;
            if (selectedUri != null) {
                String pathVideo = "";
                try {
                    pathVideo = PathUtil.getPath(this, selectedUri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                Bundle bundle = new Bundle();
                bundle.putString(VIDEO_PATH_KEY, pathVideo);
                Intent intent = new Intent(MainActivity.this, CompressBeforeReactCamActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }

        if (requestCode == REQUEST_VIDEO_FOR_COMMENTARY && resultCode == RESULT_OK) {
            final Uri selectedUri = data != null ? data.getData() : null;

            if (selectedUri != null) {
                String pathVideo = "";

                try {
                    pathVideo = PathUtil.getPath(this, selectedUri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                Bundle bundle = new Bundle();
                bundle.putString(VIDEO_PATH_KEY, pathVideo);
                Intent intent = new Intent(MainActivity.this, CommentaryActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }

        if (requestCode == REQUEST_VIDEO_FOR_VIDEO_EDIT && resultCode == RESULT_OK) {
            final Uri selectedUri = data != null ? data.getData() : null;

            if (selectedUri != null) {
                String pathVideo = "";

                try {
                    pathVideo = PathUtil.getPath(this, selectedUri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                Bundle bundle = new Bundle();
                bundle.putString(VIDEO_PATH_KEY, pathVideo);
                Intent intent = new Intent(MainActivity.this, VideoEditorActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }

        if (requestCode == PERMISSION_DRAW_OVER_WINDOW) {

            //Check if the permission is granted or not.
            if (resultCode != RESULT_OK) { //Permission is not available
                MyUtils.showSnackBarNotification(mImgRec, "Draw over other app permission not available.", Snackbar.LENGTH_SHORT);
            }
        } else if (requestCode == PERMISSION_RECORD_DISPLAY) {
            if (resultCode != RESULT_OK) {
                MyUtils.showSnackBarNotification(mImgRec, "Recording display permission not available.", Snackbar.LENGTH_SHORT);
                mScreenCaptureIntent = null;
            } else {
                mScreenCaptureIntent = data;
                if (mScreenCaptureIntent != null) {
                    mScreenCaptureIntent.putExtra(MyUtils.SCREEN_CAPTURE_INTENT_RESULT_CODE, resultCode);
                }
                mScreenCaptureResultCode = resultCode;

                shouldStartControllerService();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void sendDisconnectToService() {
        if (isMyServiceRunning(getApplicationContext(), StreamingService.class)) {
            Intent controller = new Intent(MainActivity.this, ControllerService.class);
            controller.setAction(MyUtils.ACTION_DISCONNECT_LIVE_FROM_HOME);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(controller);
            } else {
                startService(controller);
            }
        }
    }

    public void updateIconService() {
        updateService();
        if (isMyServiceRunning(getApplicationContext(), StreamingService.class)) {
            Intent controller = new Intent(MainActivity.this, ControllerService.class);
            controller.setAction(MyUtils.ACTION_UPDATE_TYPE_LIVE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(controller);
            } else {
                startService(controller);
            }
        }
    }

    private void startControllerService() {
        Intent controller = new Intent(MainActivity.this, ControllerService.class);

        controller.setAction(MyUtils.ACTION_INIT_CONTROLLER);

        controller.putExtra(MyUtils.KEY_CAMERA_AVAILABLE, checkCameraHardware(this));

        controller.putExtra(MyUtils.KEY_CONTROLlER_MODE, mMode);

        controller.putExtra(Intent.EXTRA_INTENT, mScreenCaptureIntent);

        if (mMode == MyUtils.MODE_STREAMING) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(MyUtils.STREAM_PROFILE, mStreamProfile);
            controller.putExtras(bundle);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(controller);
        } else {
            startService(controller);
        }
        updateService();
    }

    public void removeAllFragment() {
//        List<Fragment> all_frags = getSupportFragmentManager().getFragments();
//        for (Fragment frag : all_frags) {
//            getSupportFragmentManager().beginTransaction().remove(frag).commit();
//        }
//        while (getSupportFragmentManager().getFragments().size() > 0)
//            getSupportFragmentManager().popBackStack();
        for(int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); ++i) {
            getSupportFragmentManager().popBackStack();
        }
    }

    /**
     * Check if this device has a camera
     */
    @SuppressLint("UnsupportedChromeOsCameraSystemFeature")
    private boolean checkCameraHardware(Context context) {
        // this device has a camera
        // no camera on this device
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public boolean hasPermission() {
        int granted = PackageManager.PERMISSION_GRANTED;
        return ContextCompat.checkSelfPermission(this, mPermission[0]) == granted
                && ContextCompat.checkSelfPermission(this, mPermission[1]) == granted
                && ContextCompat.checkSelfPermission(this, mPermission[2]) == granted
                && ContextCompat.checkSelfPermission(this, mPermission[3]) == granted
                && Settings.canDrawOverlays(this)
                && mScreenCaptureIntent != null
                && mScreenCaptureResultCode != MyUtils.RESULT_CODE_FAILED;
    }

    public void setStreamProfile(StreamProfile streamProfile) {
        this.mStreamProfile = streamProfile;
    }

    String mURL = "";

    public void sendNewURL(String url) {
        mURL = url;
    }

    public void notifyUpdateStreamProfile() {
        if (mMode == MyUtils.MODE_STREAMING) {
            Intent controller = new Intent(MainActivity.this, ControllerService.class);
            controller.setAction(MyUtils.ACTION_UPDATE_STREAM_PROFILE);
            controller.putExtra(MyUtils.NEW_URL, mURL);
            Bundle bundle = new Bundle();
            bundle.putSerializable(MyUtils.STREAM_PROFILE, mStreamProfile);
            controller.putExtras(bundle);
            startService(controller);
        }
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action) &&
                    MyUtils.ACTION_SEND_MESSAGE_FROM_SERVICE.equals(action)) {

                String notify_msg = intent.getStringExtra(KEY_MESSAGE);
                if (TextUtils.isEmpty(notify_msg))
                    return;
                updateService();
                System.out.println("thanhlv messsssssssssss " + notify_msg);
                switch (notify_msg) {

//                    case NOTIFY_MSG_CONNECTION_STARTED:
//                    case NOTIFY_MSG_STREAM_STOPPED:
//                        updateService();
//                        break;
//                    case NOTIFY_MSG_CONNECTION_DISCONNECTED:
//                    case MESSAGE_DISCONNECT_LIVE:
//                        updateService();
//                        break;
                    case NOTIFY_MSG_CONNECTION_FAILED:
//                        liveStreaming.setText(getString(R.string.livestreaming));
                        break;
                    default:
                }
            }
        }
    };
}

