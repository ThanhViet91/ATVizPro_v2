package com.atsoft.screenrecord.ui.fragments;

import static com.atsoft.screenrecord.ui.utils.MyUtils.ACTION_UPDATE_SHOW_HIDE_FAB;
import static com.atsoft.screenrecord.ui.utils.MyUtils.dirSize;
import static com.atsoft.screenrecord.ui.utils.MyUtils.getAvailableSizeExternal;
import static com.atsoft.screenrecord.ui.utils.MyUtils.getBaseStorageDirectory;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.QueryPurchasesParams;
import com.atsoft.screenrecord.AppConfigs;
import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.adapter.SettingsAdapter;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;
import com.atsoft.screenrecord.model.SettingsItem;
import com.atsoft.screenrecord.ui.activities.MainActivity;
import com.atsoft.screenrecord.utils.AdsUtil;
import com.atsoft.screenrecord.utils.OnSingleClickListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

public class FragmentSettings extends Fragment implements SettingsAdapter.SettingsListener {

    RecyclerView recyclerView;
    ArrayList<SettingsItem> settingsItems = new ArrayList<>();
    private FragmentManager mFragmentManager;
    private MainActivity mActivity;
    private SettingsAdapter adapter;
    View mViewRoot;

    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

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
        settingsItems.add(new SettingsItem(getString(R.string.recording_cache_0_kb) + " " + String.format("%.1f MB", dirSize(new File(getBaseStorageDirectory())) * 1f / (1024 * 1024)), R.drawable.ic_recording_cache));
        settingsItems.add(new SettingsItem(getString(R.string.contact_us), R.drawable.ic_letter));

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

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mFragmentManager = getParentFragmentManager();
        mActivity = (MainActivity) getActivity();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        RelativeLayout mAdView = mViewRoot.findViewById(R.id.adView);
        new AdsUtil(getContext(), mAdView).loadBanner();
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

    private void getPurchase() {
        billingClient = BillingClient.newBuilder(requireContext()).enablePendingPurchases().setListener((billingResult, list) -> {
        }).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                getPurchase();
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    billingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                            (billingResult1, list) -> {
                                if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                    SettingManager2.setProApp(requireContext(), list.size() > 0);
                                }
                            });
                }
            }
        });
    }

    @Override
    public void onClickItem(String code) {

        if (code.equals(getString(R.string.upgrade_to_pro))) {
            System.out.println("thanhlv upgrade_to_pro");
            mFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_fragment, new SubscriptionFragment())
                    .addToBackStack("")
                    .commit();
        }
        if (code.equals(getString(R.string.restore_purchase))) {
            System.out.println("thanhlv restore_purchase");
            getPurchase();
        }

        if (code.equals(getString(R.string.share_app_to_friends))) {
            System.out.println("thanhlv share_app_to_friends");
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    AppConfigs.getInstance().getConfigModel().getShareText());
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

        if (code.equals(getString(R.string.floating_button))) {
//            boolean ss = SettingManager2.isEnableFAB(requireContext());
//            SettingManager2.setEnableFAB(requireContext(), !ss);
            mActivity.sendActionToService(ACTION_UPDATE_SHOW_HIDE_FAB);
            System.out.println("thanhlv floating_button " + SettingManager2.isEnableFAB(requireContext()));
        }

        if (code.equals(getString(R.string.contact_us))) {
            System.out.println("thanhlv contact_us");
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{AppConfigs.getInstance().getConfigModel().getFeedbackEmail()});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Hi ATSoft");
            intent.putExtra(Intent.EXTRA_TEXT, "Hi ATSoft,\n");
            try {
                startActivity(Intent.createChooser(intent, "Send mail"));
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(getContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        }

        if (code.equals(getString(R.string.support_us_by_rating_our_app))) {
            System.out.println("thanhlv support_us_by_rating_our_app");
//            String url = "https://play.google.com/store/apps/developer?id=Zzic&hl=vi&gl=US";
//            Intent i = new Intent(Intent.ACTION_VIEW);
//            i.setData(Uri.parse(url));
//            startActivity(i);
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
        if (Build.VERSION.SDK_INT >= 21) {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        } else {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }
}
