package com.examples.atvizpro.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.examples.atvizpro.R;
import com.examples.atvizpro.adapter.FAQAdapter;
import com.examples.atvizpro.adapter.SettingsAdapter;
import com.examples.atvizpro.model.FAQItem;
import com.examples.atvizpro.model.SettingsItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FragmentSettings extends Fragment {

    RecyclerView recyclerView;
    ArrayList<SettingsItem> settingsItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View mViewRoot = inflater.inflate(R.layout.fragment_setting, container, false);
        settingsItems.add(new SettingsItem(getString(R.string.how_to_record_your_screen), R.drawable.ic_recorder_settings));
        settingsItems.add(new SettingsItem(getString(R.string.upgrade_to_pro), R.drawable.ic_crown));
        settingsItems.add(new SettingsItem(getString(R.string.restore_purchase), R.drawable.ic_restore));
        settingsItems.add(new SettingsItem(getString(R.string.personalized_ads_off), R.drawable.ic_noti_ads));
        settingsItems.add(new SettingsItem(getString(R.string.available_storage_2_43gb), R.drawable.ic_available_storage));
        settingsItems.add(new SettingsItem(getString(R.string.recording_cache_0_kb), R.drawable.ic_recording_cache));
        settingsItems.add(new SettingsItem(getString(R.string.support_us_by_rating_our_app), R.drawable.ic_heart));
        settingsItems.add(new SettingsItem(getString(R.string.contact_us), R.drawable.ic_letter));


        return mViewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view);
        SettingsAdapter adapter = new SettingsAdapter(getContext(), settingsItems);
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
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}
