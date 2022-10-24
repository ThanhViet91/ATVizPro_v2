package com.examples.atscreenrecord_test.ui.fragments;

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

import com.examples.atscreenrecord_test.Core;
import com.examples.atscreenrecord_test.R;
import com.examples.atscreenrecord_test.adapter.VideoSettingsAdapter;
import com.examples.atscreenrecord_test.controllers.settings.SettingManager2;
import com.examples.atscreenrecord_test.model.VideoProperties;
import com.examples.atscreenrecord_test.utils.OnSingleClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DialogBitrate extends DialogFragmentBase {
    private ArrayList<VideoProperties> mBitrates;
    public IVideoSettingListener callback;
    public DialogBitrate(IVideoSettingListener callback) {
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
        RecyclerView recyclerView = view.findViewById(R.id.rc_item);
        TextView title = view.findViewById(R.id.title_box);
        title.setText(getResources().getString(R.string.bitrate));
        btn_back.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                updateUI();
                dismiss();
            }
        });
        initChecked();
        VideoSettingsAdapter adapter = new VideoSettingsAdapter(getContext(), mBitrates, 2);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void updateUI() {
        SettingManager2.setVideoBitrate(requireContext(), Core.bitrate);
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

        mBitrates = new ArrayList<>();
        mBitrates.add(new VideoProperties("16Mbps", false));
        mBitrates.add(new VideoProperties("12Mbps", false));
        mBitrates.add(new VideoProperties("10Mbps", false));
        mBitrates.add(new VideoProperties("8Mbps", false));
        mBitrates.add(new VideoProperties("6Mbps", false));
        mBitrates.add(new VideoProperties("4Mbps", false));
        mBitrates.add(new VideoProperties("2Mbps", false));

        String bitrate = SettingManager2.getVideoBitrate(requireContext());
        for (VideoProperties selected: mBitrates) {
            selected.setCheck(selected.getValue().contains(bitrate));
        }
    }

}
