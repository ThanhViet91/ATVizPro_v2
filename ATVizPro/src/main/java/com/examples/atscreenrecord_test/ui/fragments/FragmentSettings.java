package com.examples.atscreenrecord_test.ui.fragments;

import static com.examples.atscreenrecord_test.ui.utils.MyUtils.dirSize;
import static com.examples.atscreenrecord_test.ui.utils.MyUtils.getAvailableSizeExternal;
import static com.examples.atscreenrecord_test.ui.utils.MyUtils.getBaseStorageDirectory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.android.billingclient.api.QueryPurchasesParams;
import com.examples.atscreenrecord_test.AppConfigs;
import com.examples.atscreenrecord_test.R;
import com.examples.atscreenrecord_test.adapter.SettingsAdapter;
import com.examples.atscreenrecord_test.controllers.settings.SettingManager2;
import com.examples.atscreenrecord_test.model.SettingsItem;
import com.examples.atscreenrecord_test.ui.activities.MainActivity;
import com.examples.atscreenrecord_test.utils.AdsUtil;
import com.examples.atscreenrecord_test.utils.OnSingleClickListener;
import com.google.common.collect.ImmutableList;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FragmentSettings extends Fragment implements SettingsAdapter.SettingsListener {

    RecyclerView recyclerView;
    ArrayList<SettingsItem> settingsItems = new ArrayList<>();
    private FragmentManager mFragmentManager;

    private BillingClient billingClient;
    View mViewRoot;

    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mViewRoot = inflater.inflate(R.layout.fragment_settings_home, container, false);
        settingsItems.clear();
        settingsItems.add(new SettingsItem(getString(R.string.upgrade_to_pro), R.drawable.ic_crown));
        settingsItems.add(new SettingsItem(getString(R.string.restore_purchase), R.drawable.ic_restore));
        settingsItems.add(new SettingsItem(getString(R.string.how_to_record_your_screen), R.drawable.ic_how_to_live));
        settingsItems.add(new SettingsItem(getString(R.string.how_to_livestream), R.drawable.ic_recorder_settings));
//        settingsItems.add(new SettingsItem(getString(R.string.personalized_ads_off), R.drawable.ic_noti_ads));
        settingsItems.add(new SettingsItem(getString(R.string.support_us_by_rating_our_app), R.drawable.ic_heart));
        settingsItems.add(new SettingsItem(getString(R.string.share_app_to_friends), R.drawable.ic_share_settings2));
        settingsItems.add(new SettingsItem(getString(R.string.available_storage_2_43gb) + " "+String.format("%.1f", getAvailableSizeExternal()) + " GB", R.drawable.ic_available_storage));
        settingsItems.add(new SettingsItem(getString(R.string.recording_cache_0_kb) + " "+String.format("%.1f MB",dirSize(new File(getBaseStorageDirectory()))*1f/(1024*1024)), R.drawable.ic_recording_cache));
        settingsItems.add(new SettingsItem(getString(R.string.contact_us), R.drawable.ic_letter));

        billingClient = BillingClient.newBuilder(requireContext())
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        connectGooglePlayBilling();

        return mViewRoot;
    }

    private ArrayList<ProductDetails> mProductDetailsList;
    private void showProducts() {
        ImmutableList<QueryProductDetailsParams.Product> productList = ImmutableList.of(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(getString(R.string.product_id_remove_ads))
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
        );

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(
                params,
                (billingResult, prodDetailsList) -> {
                    // Process the result
                    System.out.println("thanhlv prodDetailsList  === "+prodDetailsList.size());
                    mProductDetailsList = new ArrayList<>();
                    mProductDetailsList.addAll(prodDetailsList);
//                    launchPurchaseFlow2(mProductDetailsList.get(0));
                }
        );
    }

    public void getPurchase() {
        billingClient = BillingClient.newBuilder(requireContext()).enablePendingPurchases().setListener((billingResult, list) -> {}).build();
        final BillingClient finalBillingClient = billingClient;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    finalBillingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                            (billingResult1, list) -> {
                                if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK){
                                    System.out.println("thanhlv ggggggggggg: "+list);
                                }
                            });
                }
            }
        });
    }
    private void connectGooglePlayBilling() {
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
                connectGooglePlayBilling();
            }
        });

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
//    @SuppressLint("NotifyDataSetChanged")
//    void handlePurchase(Purchase purchase) {
//
//        if (!purchase.isAcknowledged()) {
//            billingClient.acknowledgePurchase(AcknowledgePurchaseParams
//                    .newBuilder()
//                    .setPurchaseToken(purchase.getPurchaseToken())
//                    .build(), billingResult -> {
//
//                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//                    //Setting setIsRemoveAd to true
//                    if (purchase.getProducts().get(0).contains(getString(R.string.product_id_remove_ads))) {
//                        SettingManager2.setRemoveAds(requireContext(), true);
//                        System.out.println("thanhlvSettingManager2.setRemoveAds(requireContext(), true); ");
//                        if (adapter != null) adapter.notifyDataSetChanged();
//                    }
//                }
//            });
//        }
//
//    }
    private void handlePurchase(Purchase purchase) {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Handle the success of the consume operation.
                    System.out.println("thanhlv: success");
                    adapter.notifyDataSetChanged();
                }
            }
        };

        billingClient.consumeAsync(consumeParams, listener);
    }

    public SettingsAdapter adapter;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_position);
        adapter = new SettingsAdapter(getContext(), settingsItems);
        adapter.setListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        ImageView btn_back = view.findViewById(R.id.img_btn_back_header);
        btn_back.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                ((MainActivity) requireActivity()).checkShowAd();
                mFragmentManager.popBackStack();
            }
        });

    }
    void launchPurchaseFlow2(ProductDetails productDetails) {
        assert productDetails.getSubscriptionOfferDetails() != null;
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(productDetails.getSubscriptionOfferDetails().get(0).getOfferToken())
                                .build()
                );
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        billingClient.launchBillingFlow(requireActivity(), billingFlowParams);
    }

    void launchPurchaseFlow(ProductDetails productDetails) {
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                );
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        billingClient.launchBillingFlow(requireActivity(), billingFlowParams);
    }
    void restorePurchases() {
        billingClient = BillingClient.newBuilder(requireContext()).enablePendingPurchases().setListener((billingResult, list) -> {
        }).build();
        final BillingClient finalBillingClient = billingClient;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                connectGooglePlayBilling();
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    finalBillingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(), (billingResult1, list) -> {
                                if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                    System.out.println("thanhlv ffffffffffffff = == " + list.size());
                                    SettingManager2.setRemoveAds(requireContext(), list.size() > 0);
                                }
                            });
                }
            }
        });
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mFragmentManager = getParentFragmentManager();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        RelativeLayout mAdView = mViewRoot.findViewById(R.id.adView);
        new AdsUtil(getContext(), mAdView).loadBanner();
        if (adapter != null) adapter.notifyDataSetChanged();
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                (billingResult, list) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase purchase : list) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                                handlePurchase(purchase);
                            }
                        }
                    }
                }
        );
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onClickItem(String code) {

        if (code.equals(getString(R.string.upgrade_to_pro))) {
            System.out.println("thanhlv upgrade_to_pro");
            if (mProductDetailsList == null || mProductDetailsList.size() == 0) return;
            launchPurchaseFlow2(mProductDetailsList.get(0));
//            mFragmentManager.beginTransaction()
//                    .replace(R.id.frame_layout_fragment, new SubscriptionFragment())
//                    .addToBackStack("")
//                    .commit();
        }
        if (code.equals(getString(R.string.restore_purchase))) {
            System.out.println("thanhlv restore_purchase");
//            restorePurchases();
            getPurchase();
        }

        if (code.equals(getString(R.string.share_app_to_friends))) {
            System.out.println("thanhlv share_app_to_friends");
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Hey check out my app at: https://play.google.com/store/apps/details?id=Zzic&hl=vi&gl=US"/* + BuildConfig.APPLICATION_ID*/);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "choose one"));
        }


        if (code.equals(getString(R.string.how_to_record_your_screen))) {
            System.out.println("thanhlv how_to_record_your_screen");
            mFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_fragment, new GuidelineScreenRecordFragment(true))
                    .addToBackStack("")
                    .commit();
        }

        if (code.equals(getString(R.string.how_to_livestream))) {
            System.out.println("thanhlv how_to_livestream");
            mFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_fragment, new GuidelineLiveStreamFragment(true))
                    .addToBackStack("")
                    .commit();
        }

        if (code.equals(getString(R.string.contact_us))) {
            System.out.println("thanhlv contact_us");
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL  , new String[]{AppConfigs.getInstance().getConfigModel().getFeedbackEmail()});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Hi ATSoft");
            intent.putExtra(Intent.EXTRA_TEXT   , "Hi ATSoft,\n");
            try {
                startActivity(Intent.createChooser(intent, "Send mail"));
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(getContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        }

        if (code.equals(getString(R.string.support_us_by_rating_our_app))) {
            System.out.println("thanhlv support_us_by_rating_our_app");
            String url = "https://play.google.com/store/apps/developer?id=Zzic&hl=vi&gl=US";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }


    }
}
