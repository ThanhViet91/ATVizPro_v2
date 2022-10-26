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

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.QueryPurchasesParams;
import com.examples.atscreenrecord_test.AppConfigs;
import com.examples.atscreenrecord_test.R;
import com.examples.atscreenrecord_test.adapter.SettingsAdapter;
import com.examples.atscreenrecord_test.controllers.settings.SettingManager2;
import com.examples.atscreenrecord_test.model.SettingsItem;
import com.examples.atscreenrecord_test.ui.activities.MainActivity;
import com.examples.atscreenrecord_test.utils.AdsUtil;
import com.examples.atscreenrecord_test.utils.OnSingleClickListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

public class FragmentSettings extends Fragment implements SettingsAdapter.SettingsListener {

    RecyclerView recyclerView;
    ArrayList<SettingsItem> settingsItems = new ArrayList<>();
    private FragmentManager mFragmentManager;
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
        settingsItems.add(new SettingsItem(getString(R.string.how_to_record_your_screen), R.drawable.ic_how_to_live));
        settingsItems.add(new SettingsItem(getString(R.string.how_to_livestream), R.drawable.ic_recorder_settings));
//        settingsItems.add(new SettingsItem(getString(R.string.personalized_ads_off), R.drawable.ic_noti_ads));
        settingsItems.add(new SettingsItem(getString(R.string.support_us_by_rating_our_app), R.drawable.ic_heart));
        settingsItems.add(new SettingsItem(getString(R.string.share_app_to_friends), R.drawable.ic_share_settings2));
        settingsItems.add(new SettingsItem(getString(R.string.available_storage_2_43gb) + " "+String.format("%.1f", getAvailableSizeExternal()) + " GB", R.drawable.ic_available_storage));
        settingsItems.add(new SettingsItem(getString(R.string.recording_cache_0_kb) + " "+String.format("%.1f MB",dirSize(new File(getBaseStorageDirectory()))*1f/(1024*1024)), R.drawable.ic_recording_cache));
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
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
//        System.out.println("thanhlv upgrade_to_pro onResumeonResumeonResumeonResume");
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
        billingClient = BillingClient.newBuilder(requireContext()).enablePendingPurchases().setListener((billingResult, list) -> {}).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                getPurchase();
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    billingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                            (billingResult1, list) -> {
                                if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK){
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
