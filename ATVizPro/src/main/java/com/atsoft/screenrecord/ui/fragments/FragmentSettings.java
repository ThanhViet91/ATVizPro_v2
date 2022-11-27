package com.atsoft.screenrecord.ui.fragments;

import static com.atsoft.screenrecord.ui.utils.MyUtils.ACTION_UPDATE_SHOW_HIDE_FAB;
import static com.atsoft.screenrecord.ui.utils.MyUtils.dirSizeString;
import static com.atsoft.screenrecord.ui.utils.MyUtils.getAvailableSizeExternal;
import static com.atsoft.screenrecord.ui.utils.MyUtils.getBaseStorageDirectory;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.atsoft.screenrecord.App;
import com.atsoft.screenrecord.AppConfigs;
import com.atsoft.screenrecord.Core;
import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.adapter.SettingsAdapter;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;
import com.atsoft.screenrecord.model.Results;
import com.atsoft.screenrecord.model.SettingsItem;
import com.atsoft.screenrecord.ui.activities.MainActivity;
import com.atsoft.screenrecord.utils.AdsUtil;
import com.atsoft.screenrecord.utils.OnSingleClickListener;
import com.atsoft.screenrecord.utils.RetrofitClient;
import com.google.common.collect.ImmutableList;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentSettings extends Fragment implements SettingsAdapter.SettingsListener {

    RecyclerView recyclerView;
    ArrayList<SettingsItem> settingsItems = new ArrayList<>();
    private FragmentManager mFragmentManager;
    private MainActivity mActivity;
    private SettingsAdapter adapter;
    View mViewRoot;
    private AdsUtil mAdManager;

    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mViewRoot != null) return mViewRoot;
        mViewRoot = inflater.inflate(R.layout.fragment_settings_home, container, false);
        settingsItems.clear();
        settingsItems.add(new SettingsItem(getString(R.string.upgrade_to_pro), R.drawable.ic_crown));
        settingsItems.add(new SettingsItem(getString(R.string.restore_purchase), R.drawable.ic_restore));
        settingsItems.add(new SettingsItem(getString(R.string.how_to_record_your_screen), R.drawable.ic_how_to_record));
        settingsItems.add(new SettingsItem(getString(R.string.how_to_livestream), R.drawable.ic_how_to_live_setting));
        settingsItems.add(new SettingsItem(getString(R.string.floating_button), R.drawable.ic_fab_settings));
        settingsItems.add(new SettingsItem(getString(R.string.support_us_by_rating_our_app), R.drawable.ic_heart));
        settingsItems.add(new SettingsItem(getString(R.string.share_app_to_friends), R.drawable.ic_share_settings2));
        settingsItems.add(new SettingsItem(getString(R.string.available_storage_2_43gb) + " " + String.format("%.1f", getAvailableSizeExternal()) + " GB", R.drawable.ic_available_storage));
        settingsItems.add(new SettingsItem(getString(R.string.recording_cache_0_kb) + " " + dirSizeString(new File(getBaseStorageDirectory())), R.drawable.ic_recording_cache));
        settingsItems.add(new SettingsItem(getString(R.string.contact_us), R.drawable.ic_letter));

        mAdView = mViewRoot.findViewById(R.id.adView);
        mAdManager = new AdsUtil(requireContext(), mAdView);

        return mViewRoot;
    }

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


        billingClient = BillingClient.newBuilder(requireContext())
                .enablePendingPurchases()
                .setListener((billingResult, list) -> {

                })
                .build();
        connectGooglePlayBilling();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mFragmentManager = getParentFragmentManager();
        mActivity = (MainActivity) getActivity();
    }

    RelativeLayout mAdView;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        if (mAdManager != null) mAdManager.loadBanner();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private BillingClient billingClient;

    private void getPurchaseHistory() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                    if (mProgressDialog != null) mProgressDialog.dismiss();
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

                                if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                    if (purchasesHistoryList != null)
                                        mPurchasesHistoryList = new ArrayList<>(purchasesHistoryList);
                                    try {
                                        getPublicTime();
                                    } catch (Exception e) {
                                        requireActivity().runOnUiThread(() -> checkPurchase(mPurchasesHistoryList, System.currentTimeMillis()));
                                    }
                                } else {
                                    requireActivity().runOnUiThread(() -> showPopup("Something went wrong!", "Check your network connection and try again"));
                                }
                            }
                    );
                }
            }
        });
    }

    private ProgressDialog mProgressDialog;

    private void buildDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(getContext(), "", "Connecting...");
        }
    }

    private void showPopup(String title, String des) {
        if (getContext() == null) return;
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
            SettingManager2.setProApp(App.getAppContext(), false);
            showPopup("Something went wrong!", "This item maybe purchased by a different account. Please change account and try again");
            if (mAdManager != null) mAdManager.loadBanner();
            return;
        }

        boolean hasId = false;
        for (PurchaseHistoryRecord item : mPurchasesHistoryList) {
            if (item.getProducts().get(0).contains(AppConfigs.getInstance().getSubsModel().get(0).getKeyID())) {
                hasId = true;
                if ((currentTime - item.getPurchaseTime()) / 1000 > 7 * 24 * 60 * 60) {
                    // qua han
                    SettingManager2.setProApp(App.getAppContext(), false);
                    showPopup("Restore Failed!", "Your subscription has expired, please upgrade to proversion!");
                } else {
                    SettingManager2.setProApp(App.getAppContext(), true);
                    showPopup("Restore Successfully!", "You've successfully restored your purchase!");
                    if (adapter != null) adapter.notifyDataSetChanged();
                    if (mAdManager != null) mAdManager.loadBanner();
                    return;
                }
            }
            if (item.getProducts().get(0).contains(AppConfigs.getInstance().getSubsModel().get(1).getKeyID())) {
                hasId = true;
                if ((currentTime - item.getPurchaseTime()) / 1000 > 30 * 24 * 60 * 60) {
                    // qua han
                    SettingManager2.setProApp(App.getAppContext(), false);
                    showPopup("Restore Failed!", "Your subscription has expired, please upgrade to proversion!");
                } else {
                    SettingManager2.setProApp(App.getAppContext(), true);
                    showPopup("Restore Successfully!", "You've successfully restored your purchase!");
                    if (adapter != null) adapter.notifyDataSetChanged();
                    if (mAdManager != null) mAdManager.loadBanner();
                    return;
                }
            }
            if (item.getProducts().get(0).contains(AppConfigs.getInstance().getSubsModel().get(2).getKeyID())) {
                hasId = true;
                if ((currentTime - item.getPurchaseTime()) / 1000 > 365 * 24 * 60 * 60) {
                    // qua han
                    SettingManager2.setProApp(App.getAppContext(), false);
                    showPopup("Restore Failed!", "Your subscription has expired, please upgrade to proversion!");
                } else {
                    SettingManager2.setProApp(App.getAppContext(), true);
                    showPopup("Restore Successfully!", "You've successfully restored your purchase!");
                    if (adapter != null) adapter.notifyDataSetChanged();
                    if (mAdManager != null) mAdManager.loadBanner();
                    return;
                }
            }

        }
        if (!hasId) {
            SettingManager2.setProApp(App.getAppContext(), false);
            showPopup("Something went wrong!", "This item maybe purchased by a different account. Please change account and try again");
        }
        if (adapter != null) adapter.notifyDataSetChanged();
        if (mAdManager != null) mAdManager.loadBanner();
    }

    public void getPublicTime() {
        String tz = TimeZone.getDefault().getID();
        Call<Results> call = RetrofitClient.getInstance().getMyApi().getTimeZone(tz);
        call.enqueue(new Callback<Results>() {
            @Override
            public void onResponse(@NonNull Call<Results> call, @NonNull Response<Results> response) {

                if (response.body() == null) {
                    checkPurchase(mPurchasesHistoryList, System.currentTimeMillis());
                } else
                    checkPurchase(mPurchasesHistoryList, response.body().getDateTimeMs());
            }

            @Override
            public void onFailure(@NonNull Call<Results> call, @NonNull Throwable t) {
                checkPurchase(mPurchasesHistoryList, System.currentTimeMillis());
            }
        });
    }

    @Override
    public void onClickItem(String code) {

        if (code.equals(getString(R.string.upgrade_to_pro))) {
            mFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_fragment, new SubscriptionFragment())
                    .addToBackStack("")
                    .commit();
        }
        if (code.equals(getString(R.string.restore_purchase))) {
            buildDialog();
            getPurchaseHistory();
        }

        if (code.equals(getString(R.string.share_app_to_friends))) {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    AppConfigs.getInstance().getConfigModel().getShareText());
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Choose one"));
        }


        if (code.equals(getString(R.string.how_to_record_your_screen))) {
            mFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_fragment, new GuidelineScreenRecordFragment(true))
                    .addToBackStack("")
                    .commit();
        }

        if (code.equals(getString(R.string.how_to_livestream))) {
            mFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_fragment, new GuidelineLiveStreamFragment(true))
                    .addToBackStack("")
                    .commit();
        }

        if (code.equals(getString(R.string.floating_button))) {
            mActivity.sendActionToService(ACTION_UPDATE_SHOW_HIDE_FAB);
        }

        if (code.equals(getString(R.string.contact_us))) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{AppConfigs.getInstance().getConfigModel().getFeedbackEmail()});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
            intent.putExtra(Intent.EXTRA_TEXT, "Enter text,\n");
            try {
                startActivity(Intent.createChooser(intent, "Send mail"));
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(getContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        }

        if (code.equals(getString(R.string.support_us_by_rating_our_app))) {
            rateApp();
        }


    }

    public void rateApp() {
        try {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        } catch (ActivityNotFoundException e) {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }

    private Intent rateIntentForUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, requireContext().getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        intent.addFlags(flags);
        return intent;
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

    String WEEKLY_ID, MONTHLY_ID, YEARLY_ID;

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

        billingClient.queryProductDetailsAsync(
                params,
                (billingResult, prodDetailsList) -> {
                    // Process the result
                    Core.productDetails = new ArrayList<>(prodDetailsList);
                }
        );
    }
}
