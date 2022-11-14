package com.atsoft.screenrecord.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.atsoft.screenrecord.AppConfigs;
import com.atsoft.screenrecord.Core;
import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;
import com.atsoft.screenrecord.model.Results;
import com.atsoft.screenrecord.model.SubscriptionsItemModel;
import com.atsoft.screenrecord.ui.utils.NetworkUtils;
import com.atsoft.screenrecord.utils.OnSingleClickListener;
import com.atsoft.screenrecord.utils.RetrofitClient;
import com.google.common.collect.ImmutableList;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionFragment extends Fragment {

    private Activity mParentActivity = null;
    private FragmentManager mFragmentManager;

    public interface SubscriptionListener {
        void onBuySuccess();
    }

    private SubscriptionListener mCallBack;

    public SubscriptionFragment() {
    }

    public SubscriptionFragment(SubscriptionListener callBack) {
        this.mCallBack = callBack;
    }

    String WEEKLY_ID, MONTHLY_ID, YEARLY_ID;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mFragmentManager = getParentFragmentManager();

        subs = new ArrayList<>(AppConfigs.getInstance().getSubsModel());
        subs.sort((t, t1) -> t.getSort() < t1.getSort() ? 1 : 0);

        WEEKLY_ID = AppConfigs.getInstance().getSubsModel().get(0).getKeyID();
        MONTHLY_ID = AppConfigs.getInstance().getSubsModel().get(1).getKeyID();
        YEARLY_ID = AppConfigs.getInstance().getSubsModel().get(2).getKeyID();
        System.out.println("thanhlv WEEKLY_ID = "+ WEEKLY_ID);
    }

    private View mViewRoot;

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

    void handlePurchase(Purchase purchase) {
//        if (mProgressDialog != null) mProgressDialog.dismiss();
        if (!purchase.isAcknowledged()) {
            billingClient.acknowledgePurchase(AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build(), billingResult -> {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    //Setting setIsRemoveAd to true
                    if (purchase.getProducts().get(0).contains(subs.get(selected).getKeyID())) {
                        SettingManager2.setProApp(requireContext(), true);
                        System.out.println("thanhlv buy OK");
                        mCallBack.onBuySuccess();
                        mFragmentManager.popBackStackImmediate();
                    }
                }
            });
        }

    }

    private void showProducts() {
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

        billingClient.queryProductDetailsAsync(
                params,
                (billingResult, prodDetailsList) -> {
                    // Process the result

//                    mProductDetailsList = new ArrayList<>();
//                    mProductDetailsList.addAll(prodDetailsList);
                    Core.productDetails = new ArrayList<>(prodDetailsList);
//                    updateSubs();
                    fillSubscription();
                }
        );
    }
//    @SuppressLint("SetTextI18n")
//    public void updateSubs(){
//        for (int i = 0; i < 3; i++) {
//            String id = mProductDetailsList.get(i).getProductId();
//            String price = "";
//            if (mProductDetailsList.get(i).getSubscriptionOfferDetails() != null)
//                price = mProductDetailsList.get(i).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
//            String finalPrice = price;
//            requireActivity().runOnUiThread(() -> {
//                if (id.equals(subs.get(0).getKeyID())) {
//                    tvName1.setText(finalPrice + "/ " + subs.get(0).getName());
//                } else
//                if (id.equals(subs.get(1).getKeyID())) {
//                    tvName2.setText(finalPrice + "/ " + subs.get(1).getName());
//                } else
//                if (id.equals(subs.get(2).getKeyID())) {
//                    tvName3.setText(finalPrice + "/ " + subs.get(2).getName());
//                }
//
//
//            });
//        }
//
//        requireActivity().runOnUiThread(() -> {
//            lnHideSubs.setVisibility(View.GONE);
//            btnBuy.setAlpha(1f);
//            btnBuy.setEnabled(true);
//        });
//    }

    @SuppressLint("SetTextI18n")
    public void fillSubscription() {
        for (int i = 0; i < 3; i++) {
            for (ProductDetails product : Core.productDetails) {
                String id = product.getProductId();
                String price = "";
                if (product.getSubscriptionOfferDetails() != null)
                    price = product.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                if (id.equals(subs.get(i).getKeyID())) {
                    if (i == 0) tvName1.setText(price + "/ " + subs.get(0).getName());
                    if (i == 1) tvName2.setText(price + "/ " + subs.get(1).getName());
                    if (i == 2) tvName3.setText(price + "/ " + subs.get(2).getName());
                    break;
                }
            }
        }
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.subscriptions_layout, container, false);


        return mViewRoot;
    }

    private void checkInternetConnect() {
        if (!NetworkUtils.isConnected(requireContext())) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Internet unavailable.")
                    .setMessage("Please connect to network to purchase service.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    } )
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {} )
                    .show();
        }
    }

    ArrayList<SubscriptionsItemModel> subs;
    int selected = 0;
    TextView tvName1, tvName2, tvName3;
    ImageView btnBuy;
    LinearLayout lnHideSubs;

    TextView tvRestore;

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lnHideSubs = view.findViewById(R.id.hide_sub);
        tvName1 = view.findViewById(R.id.name_sub_1);
        TextView tvDes1 = view.findViewById(R.id.des_sub_1);
        tvName2 = view.findViewById(R.id.name_sub_2);
        TextView tvDes2 = view.findViewById(R.id.des_sub_2);
        tvName3 = view.findViewById(R.id.name_sub_3);
        TextView tvDes3 = view.findViewById(R.id.des_sub_3);

        tvDes1.setText(subs.get(0).getDescription());
        tvDes2.setText(subs.get(1).getDescription());
        tvDes3.setText(subs.get(2).getDescription());

        ImageView btn_check_1 = view.findViewById(R.id.img_check_sub_1);
        ImageView btn_check_2 = view.findViewById(R.id.img_check_sub_2);
        ImageView btn_check_3 = view.findViewById(R.id.img_check_sub_3);
        LinearLayout ln_sub_1 = view.findViewById(R.id.check1);
        LinearLayout ln_sub_2 = view.findViewById(R.id.check2);
        LinearLayout ln_sub_3 = view.findViewById(R.id.check3);

        ln_sub_1.setOnClickListener(view1 -> {
            ln_sub_1.setBackgroundResource(R.drawable.shape_round_ss_checked);
            btn_check_1.setBackgroundResource(R.drawable.ic_select_sub);
            ln_sub_2.setBackgroundResource(R.drawable.shape_round_ss_check);
            btn_check_2.setBackgroundResource(R.drawable.ic_non_select_sub);
            ln_sub_3.setBackgroundResource(R.drawable.shape_round_ss_check);
            btn_check_3.setBackgroundResource(R.drawable.ic_non_select_sub);
            selected = 0;
        });
        ln_sub_2.setOnClickListener(view12 -> {
            ln_sub_2.setBackgroundResource(R.drawable.shape_round_ss_checked);
            btn_check_2.setBackgroundResource(R.drawable.ic_select_sub);
            ln_sub_1.setBackgroundResource(R.drawable.shape_round_ss_check);
            btn_check_1.setBackgroundResource(R.drawable.ic_non_select_sub);
            ln_sub_3.setBackgroundResource(R.drawable.shape_round_ss_check);
            btn_check_3.setBackgroundResource(R.drawable.ic_non_select_sub);
            selected = 1;
        });
        ln_sub_3.setOnClickListener(view13 -> {
            ln_sub_3.setBackgroundResource(R.drawable.shape_round_ss_checked);
            btn_check_3.setBackgroundResource(R.drawable.ic_select_sub);
            ln_sub_1.setBackgroundResource(R.drawable.shape_round_ss_check);
            btn_check_1.setBackgroundResource(R.drawable.ic_non_select_sub);
            ln_sub_2.setBackgroundResource(R.drawable.shape_round_ss_check);
            btn_check_2.setBackgroundResource(R.drawable.ic_non_select_sub);
            selected = 2;
        });

        btnBuy = view.findViewById(R.id.btn_start_plan);
        btnBuy.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                buildDialog();
                btnBuy.setEnabled(false);
                System.out.println("thanhlv setOnClickListener");
                launchPurchaseFlow(selected);
            }
        });



        billingClient = BillingClient.newBuilder(requireContext())
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        connectGooglePlayBilling();
        System.out.println("thanhlv billingClient " +billingClient);

        if (Core.productDetails.size() > 0) {
            lnHideSubs.setVisibility(View.GONE);
            btnBuy.setAlpha(1f);
            btnBuy.setEnabled(true);
            fillSubscription();
        } else {
            lnHideSubs.setVisibility(View.VISIBLE);
            btnBuy.setAlpha(0.5f);
            btnBuy.setEnabled(false);
            showPopup("Something went wrong!", "Check your network connection and try again");

        }

        ImageView btn_dismiss = view.findViewById(R.id.img_btn_back_header);
        btn_dismiss.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mFragmentManager.popBackStackImmediate();
            }
        });


        tvRestore = view.findViewById(R.id.tv_btn_restore);

        tvRestore.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                buildDialog();
                tvRestore.setEnabled(false);
                getPurchaseHistory();

            }
        });


        TextView tvTermsOfService = view.findViewById(R.id.tv_terms_of_service);
        TextView tvPrivacyNotice = view.findViewById(R.id.tv_privacy_notice);

        tvTermsOfService.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                String url = AppConfigs.getInstance().getConfigModel().getTermsURL();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        tvPrivacyNotice.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                String url = AppConfigs.getInstance().getConfigModel().getPrivacyPolicyURL();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }

    private ProgressDialog mProgressDialog;
    private void buildDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(getContext(), "", "Connecting...");
        }
    }


    private BillingClient billingClient, billingClient2;
    private ArrayList<ProductDetails> mProductDetailsList;
    ProductDetails productDetail;
    void launchPurchaseFlow(int sub) {
        for (int i = 0; i < Core.productDetails.size(); i++) {
            if (Core.productDetails.get(i).getProductId().equals(subs.get(sub).getKeyID())) {
                productDetail = Core.productDetails.get(i);
                break;
            }
        }
        if (productDetail == null) return;
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetail)
                                .setOfferToken(productDetail.getSubscriptionOfferDetails().get(0).getOfferToken())
                                .build()
                );
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        if (mProgressDialog != null) mProgressDialog.dismiss();
        if (billingClient != null) billingClient.launchBillingFlow(requireActivity(), billingFlowParams);
        btnBuy.setEnabled(true);
        System.out.println("thanhlv launchPurchaseFlow");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (billingClient != null)
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

    private void getPurchaseHistory() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                if (mProgressDialog != null) mProgressDialog.dismiss();
//                getPurchase();
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    billingClient.queryPurchaseHistoryAsync(
                            QueryPurchaseHistoryParams.newBuilder()
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build(),
                            (billingResult1, purchasesHistoryList) -> {
                                if (mProgressDialog != null) mProgressDialog.dismiss();
                                System.out.println("thanhlv mPurchasesHistoryList  tvRestore.setEnabled(true);" +purchasesHistoryList.size());
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvRestore.setEnabled(true);
                                    }
                                });
                                mPurchasesHistoryList = new ArrayList<>(purchasesHistoryList);
                                try {
                                    System.out.println("thanhlv mPurchasesHistoryList" + mPurchasesHistoryList.size());
                                    getPublicTime();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    checkPurchase(mPurchasesHistoryList, System.currentTimeMillis());
                                }
                            }
                    );
                }
            }
        });
    }
    private void showPopup(String title, String des) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(des)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                })
                .show();
    }
    private ArrayList<PurchaseHistoryRecord> mPurchasesHistoryList;
    @SuppressLint("NotifyDataSetChanged")
    private void checkPurchase(List<PurchaseHistoryRecord> purchasesHistoryList, long currentTime) {

        if (purchasesHistoryList == null || purchasesHistoryList.size() == 0) {
            SettingManager2.setProApp(requireContext(), false);
            showPopup("Something went wrong!", "This item maybe purchased by a different account. Please change account and try again");
            return;
        }

        boolean hasId = false;
        for (PurchaseHistoryRecord item : mPurchasesHistoryList) {
            if (item.getProducts().get(0).contains(AppConfigs.getInstance().getSubsModel().get(0).getKeyID()) ) {
                hasId = true;
                if ((currentTime - item.getPurchaseTime())/1000 > 7 * 24 * 60 * 60) {
                    // qua han
                    SettingManager2.setProApp(requireContext(), false);
                    showPopup("Restore Failed!", "Your subscription has expired, please upgrade to proversion!");
                } else {
                    SettingManager2.setProApp(requireContext(), true);
                    showPopup( "Restore Successfully!", "You've successfully restored your purchase!");
                    return;
                }
            }
            if (item.getProducts().get(0).contains(AppConfigs.getInstance().getSubsModel().get(1).getKeyID()) ) {
                hasId = true;
                if ((currentTime - item.getPurchaseTime())/1000 > 2592000) {
                    // qua han
                    SettingManager2.setProApp(requireContext(), false);
                    showPopup("Restore Failed!", "Your subscription has expired, please upgrade to proversion!");
                } else {
                    SettingManager2.setProApp(requireContext(), true);
                    showPopup( "Restore Successfully!", "You've successfully restored your purchase!");
                    return;
                }
            }
            if (item.getProducts().get(0).contains(AppConfigs.getInstance().getSubsModel().get(2).getKeyID()) ) {
                hasId = true;
                if ((currentTime - item.getPurchaseTime())/1000 > 365 * 24 * 60 * 60) {
                    // qua han
                    SettingManager2.setProApp(requireContext(), false);
                    showPopup("Restore Failed!", "Your subscription has expired, please upgrade to proversion!");
                } else {
                    SettingManager2.setProApp(requireContext(), true);
                    showPopup( "Restore Successfully!", "You've successfully restored your purchase!");
                    return;
                }
            }
        }
        if (!hasId) {
            SettingManager2.setProApp(requireContext(), false);
            showPopup("Something went wrong!", "This item maybe purchased by a different account. Please change account and try again");
        }
    }
    public void getPublicTime() throws JSONException {
        String tz = TimeZone.getDefault().getID();
        Call<Results> call = RetrofitClient.getInstance().getMyApi().getTimeZone(tz);
        call.enqueue(new Callback<Results>() {
            @Override
            public void onResponse(Call<Results> call, Response<Results> response) {

                if (response.body() == null) {
                    checkPurchase(mPurchasesHistoryList, System.currentTimeMillis());
                } else
                    checkPurchase(mPurchasesHistoryList, ((Results) response.body()).getDateTimeMs());
            }
            @Override
            public void onFailure(Call<Results> call, Throwable t) {
                checkPurchase(mPurchasesHistoryList, System.currentTimeMillis());
            }
        });
    }
}