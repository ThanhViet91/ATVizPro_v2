package com.examples.atvizpro.ui.fragments;

import static com.examples.atvizpro.ui.utils.MyUtils.getAvailableSizeExternal;
import static com.examples.atvizpro.ui.utils.MyUtils.getCacheSize;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.examples.atvizpro.App;
import com.examples.atvizpro.R;
import com.examples.atvizpro.adapter.FAQAdapter;
import com.examples.atvizpro.adapter.SettingsAdapter;
import com.examples.atvizpro.controllers.settings.SettingManager2;
import com.examples.atvizpro.model.FAQItem;
import com.examples.atvizpro.model.SettingsItem;
import com.examples.atvizpro.ui.activities.MainActivity;
import com.examples.atvizpro.ui.services.ExecuteService;
import com.examples.atvizpro.utils.AdUtil;
import com.google.android.gms.ads.AdView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class FragmentSettings extends Fragment implements SettingsAdapter.SettingsListener {

    RecyclerView recyclerView;
    ArrayList<SettingsItem> settingsItems = new ArrayList<>();
    private FragmentManager mFragmentManager;

    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View mViewRoot = inflater.inflate(R.layout.fragment_setting, container, false);
        settingsItems.clear();
        settingsItems.add(new SettingsItem(getString(R.string.upgrade_to_pro), R.drawable.ic_crown));
        settingsItems.add(new SettingsItem(getString(R.string.how_to_record_your_screen), R.drawable.ic_recorder_settings));
        settingsItems.add(new SettingsItem(getString(R.string.how_to_livestream), R.drawable.ic_recorder_settings));
//        settingsItems.add(new SettingsItem(getString(R.string.restore_purchase), R.drawable.ic_restore));
//        settingsItems.add(new SettingsItem(getString(R.string.personalized_ads_off), R.drawable.ic_noti_ads));
        settingsItems.add(new SettingsItem(getString(R.string.available_storage_2_43gb) + " "+String.format("%.1f", getAvailableSizeExternal()) + " GB", R.drawable.ic_available_storage));
        settingsItems.add(new SettingsItem(getString(R.string.recording_cache_0_kb) + " "+String.format("%.1f",getCacheSize()) + " MB", R.drawable.ic_recording_cache));
        settingsItems.add(new SettingsItem(getString(R.string.support_us_by_rating_our_app), R.drawable.ic_heart));
        settingsItems.add(new SettingsItem(getString(R.string.contact_us), R.drawable.ic_letter));


        return mViewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view);
        SettingsAdapter adapter = new SettingsAdapter(getContext(), settingsItems);
        adapter.setListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        ImageView btn_back = view.findViewById(R.id.img_btn_back_header);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().popBackStack();
            }
        });

        AdView mAdView = view.findViewById(R.id.adView);
        AdUtil.createBannerAdmob(getContext(), mAdView);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mFragmentManager = getParentFragmentManager();
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
            System.out.println("thanhlv ddddddddddddddd");
            ((MainActivity) requireActivity()).showProductRemoveAds();
            SettingManager2.setRemoveAds(requireActivity().getApplicationContext(), true);
        }

        if (code.equals(getString(R.string.how_to_record_your_screen))) {
            System.out.println("thanhlv how_to_record_your_screen");
            mFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_fragment, new GuidelineScreenRecordFragment())
                    .addToBackStack("")
                    .commit();
        }

        if (code.equals(getString(R.string.contact_us))) {
            System.out.println("thanhlv kkkfsdfghsodfisdf");
            Intent intent = new Intent(getActivity(), ExecuteService.class);
            requireActivity().startService(intent);
        }


    }
}
