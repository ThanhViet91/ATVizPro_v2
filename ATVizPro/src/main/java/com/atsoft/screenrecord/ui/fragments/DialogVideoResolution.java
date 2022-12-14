package com.atsoft.screenrecord.ui.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atsoft.screenrecord.Core;
import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.adapter.VideoSettingsAdapter;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;
import com.atsoft.screenrecord.model.VideoProperties;
import com.atsoft.screenrecord.utils.OnSingleClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DialogVideoResolution extends DialogFragmentBase{

    RecyclerView recyclerView;
    ArrayList<VideoProperties> mVideoResolutions;
    public IVideoSettingListener callback;
    public DialogVideoResolution(IVideoSettingListener callback) {
        this.callback = callback;
    }

    @Override
    public int getLayout() {
        return R.layout.dialog_settings_video_properties;
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView btn_back = view.findViewById(R.id.img_btn_back_header);
        recyclerView = view.findViewById(R.id.rc_item);

        TextView title = view.findViewById(R.id.title_box);
        title.setText(getResources().getString(R.string.video_resolution));

        btn_back.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                updateUI();
                dismiss();
            }
        });

        initChecked();

        VideoSettingsAdapter adapter = new VideoSettingsAdapter(getContext(), mVideoResolutions, 1);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void updateUI() {
        SettingManager2.setVideoResolution(requireContext(), Core.resolution);
        callback.onClick();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initChecked() {

        mVideoResolutions = new ArrayList<>();
        mVideoResolutions.add(new VideoProperties("1080p (FHD)", false));
        mVideoResolutions.add(new VideoProperties("720p (HD)", false));
        mVideoResolutions.add(new VideoProperties("480p (SD)", false));
        mVideoResolutions.add(new VideoProperties("360p (SD)", false));

        String resolution = SettingManager2.getVideoResolution(requireContext());
        for (VideoProperties selected: mVideoResolutions) {
            selected.setCheck(selected.getValue().contains(resolution));
        }
    }
}
