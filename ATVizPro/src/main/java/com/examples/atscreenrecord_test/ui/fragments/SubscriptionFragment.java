package com.examples.atscreenrecord_test.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.examples.atscreenrecord_test.AppConfigs;
import com.examples.atscreenrecord_test.R;
import com.examples.atscreenrecord_test.adapter.PhotoAdapter;
import com.examples.atscreenrecord_test.controllers.settings.SettingManager2;
import com.examples.atscreenrecord_test.model.PhotoModel;
import com.examples.atscreenrecord_test.model.SubscriptionsItemModel;
import com.examples.atscreenrecord_test.ui.activities.MainActivity;
import com.examples.atscreenrecord_test.utils.AdsUtil;
import com.examples.atscreenrecord_test.utils.OnSingleClickListener;
import com.google.common.collect.ImmutableList;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class SubscriptionFragment extends Fragment {

    private Activity mParentActivity = null;
    private FragmentManager mFragmentManager;

    public SubscriptionFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mParentActivity = (MainActivity) context;
        mFragmentManager = getParentFragmentManager();
        WEEK_SUBS_ID = requireContext().getString(R.string.product_id_week);
        MONTH_SUBS_ID = requireContext().getString(R.string.product_id_month);
        YEAR_SUBS_ID = requireContext().getString(R.string.product_id_year);

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
        if (!purchase.isAcknowledged()) {
            billingClient.acknowledgePurchase(AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build(), billingResult -> {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    //Setting setIsRemoveAd to true
                    if (purchase.getProducts().get(0).contains(subs.get(selected).getKeyID())) {
                        SettingManager2.setProApp(requireContext(), true);
                    }
                }
            });
        }

    }

    private void showProducts() {
        ImmutableList<QueryProductDetailsParams.Product> productList = ImmutableList.of(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(getString(R.string.product_id_week))
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(getString(R.string.product_id_month))
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(getString(R.string.product_id_year))
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

                    mProductDetailsList = new ArrayList<>();
                    mProductDetailsList.addAll(prodDetailsList);
                    System.out.println("thanhlv mProductDetailsList  === " + mProductDetailsList.size());
                    updateSubs();
                }
        );
    }

    private String WEEK_SUBS_ID;
    private String MONTH_SUBS_ID;
    private String YEAR_SUBS_ID;
    @SuppressLint("SetTextI18n")
    public void updateSubs(){
        for (int i = 0; i < 3; i++) {
            String id = mProductDetailsList.get(i).getProductId();
            String price = mProductDetailsList.get(i).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();

            System.out.println("thanhlv mProductDetailsList  hhhhhhh === " + id + " / " + price);
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (id.equals(WEEK_SUBS_ID)) {
                        tvName1.setText(price + "/ " + subs.get(0).getName());
                    } else
                    if (id.equals(MONTH_SUBS_ID)) {
                        tvName2.setText(price + "/ " + subs.get(1).getName());
                    } else
                    if (id.equals(YEAR_SUBS_ID)) {
                        tvName3.setText(price + "/ " + subs.get(2).getName());
                    }
                }
            });


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

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.ss_layout, container, false);

        subs = new ArrayList<SubscriptionsItemModel>(AppConfigs.getInstance().getSubsModel());
        billingClient = BillingClient.newBuilder(requireContext())
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        connectGooglePlayBilling();
        return mViewRoot;
    }

    ArrayList<SubscriptionsItemModel> subs;
    int selected = 2;
    TextView tvName1, tvName2, tvName3;

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

        ln_sub_1.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                ln_sub_1.setBackgroundResource(R.drawable.shape_round_ss_checked);
                btn_check_1.setBackgroundResource(R.drawable.ic_select_sub);
                ln_sub_2.setBackgroundResource(R.drawable.shape_round_ss_check);
                btn_check_2.setBackgroundResource(R.drawable.ic_non_select_sub);
                ln_sub_3.setBackgroundResource(R.drawable.shape_round_ss_check);
                btn_check_3.setBackgroundResource(R.drawable.ic_non_select_sub);
                selected = 0;
            }
        });
        ln_sub_2.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                ln_sub_2.setBackgroundResource(R.drawable.shape_round_ss_checked);
                btn_check_2.setBackgroundResource(R.drawable.ic_select_sub);
                ln_sub_1.setBackgroundResource(R.drawable.shape_round_ss_check);
                btn_check_1.setBackgroundResource(R.drawable.ic_non_select_sub);
                ln_sub_3.setBackgroundResource(R.drawable.shape_round_ss_check);
                btn_check_3.setBackgroundResource(R.drawable.ic_non_select_sub);
                selected = 1;
            }
        });
        ln_sub_3.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                ln_sub_3.setBackgroundResource(R.drawable.shape_round_ss_checked);
                btn_check_3.setBackgroundResource(R.drawable.ic_select_sub);
                ln_sub_1.setBackgroundResource(R.drawable.shape_round_ss_check);
                btn_check_1.setBackgroundResource(R.drawable.ic_non_select_sub);
                ln_sub_2.setBackgroundResource(R.drawable.shape_round_ss_check);
                btn_check_2.setBackgroundResource(R.drawable.ic_non_select_sub);
                selected = 2;
            }
        });

        ImageView btnBuy = view.findViewById(R.id.btn_start_plan);
        btnBuy.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                launchPurchaseFlow(selected);
            }
        });

        ImageView btn_dismiss = view.findViewById(R.id.img_btn_back_header);
        btn_dismiss.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mFragmentManager.popBackStack();
            }
        });

        TextView tvTermsOfService = view.findViewById(R.id.tv_terms_of_service);
        TextView tvPrivacyNotice = view.findViewById(R.id.tv_privacy_notice);

        tvTermsOfService.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
//                mFragmentManager.popBackStack();
            }
        });
        tvPrivacyNotice.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
//                mFragmentManager.popBackStack();
            }
        });
    }

    private BillingClient billingClient;
    private ArrayList<ProductDetails> mProductDetailsList;

    ProductDetails productDetails;

    void launchPurchaseFlow(int sub) {
        for (int i = 0; i < mProductDetailsList.size(); i++) {
            if (mProductDetailsList.get(i).getProductId().equals(subs.get(sub).getKeyID())) {
                productDetails = mProductDetailsList.get(i);
                break;
            }
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

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
}