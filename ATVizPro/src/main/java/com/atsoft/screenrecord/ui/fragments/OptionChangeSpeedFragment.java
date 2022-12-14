package com.atsoft.screenrecord.ui.fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.adapter.BasicAdapter;
import com.atsoft.screenrecord.ui.utils.MyUtils;
import com.atsoft.screenrecord.utils.OnSingleClickListener;
import com.atsoft.screenrecord.utils.FFmpegUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class OptionChangeSpeedFragment extends DialogFragmentBase implements BasicAdapter.BasicAdapterListener {


    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    private static final String TAG = OptionChangeSpeedFragment.class.getSimpleName();

    public IOptionFragmentListener mCallback;

    public static OptionChangeSpeedFragment newInstance(IOptionFragmentListener callback, Bundle args) {
        OptionChangeSpeedFragment dialogSelectVideoSource = new OptionChangeSpeedFragment(callback);
        dialogSelectVideoSource.setArguments(args);
        return dialogSelectVideoSource;
    }

    public OptionChangeSpeedFragment(IOptionFragmentListener callback) {
        mCallback = callback;
    }
    @Override
    public int getLayout() {
        return R.layout.option_speed_fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    String video_path;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        video_path = getArguments() != null ? getArguments().getString("video_path", "") : "";

        ImageView btn_close = view.findViewById(R.id.iv_close);
        ImageView btn_done = view.findViewById(R.id.iv_done);

        btn_close.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                dismiss();
            }
        });
        btn_done.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                processingChangeSpeed();
            }
        });


        ArrayList<String> listPos = new ArrayList<>();
        listPos.add("0.25x");
        listPos.add("0.5x");
        listPos.add("0.75x");
        listPos.add("1.25x");
        listPos.add("1.5x");
        listPos.add("2.0x");
        listPos.add("2.5x");
        listPos.add("3.0x");
        listPos.add("4.0x");
        listPos.add("5.0x");

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_position);
        BasicAdapter basicAdapter = new BasicAdapter(getContext(), listPos, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerView.setAdapter(basicAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);

    }

    String speed_selected = "1.0";

    private void processingChangeSpeed() {
        if (MyUtils.getDurationMs(requireContext(), video_path)/Float.parseFloat(speed_selected) < 500) {
            Toast.makeText(requireContext(), "Video is too short!", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }
        mCallback.onClickDone();
        dismiss();
        FFmpegUtil.getInstance().changeSpeed(video_path, speed_selected,  new FFmpegUtil.ITranscoding() {
            @Override
            public void onStartTranscoding(String outPath) {

            }

            @Override
            public void onFinishTranscoding(String code) {
                if (!code.equals(""))
                    mCallback.onFinishProcess(code);
            }
        });
    }

    @Override
    public void updateUI() {
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            if (getDialog() != null && getDialog().getWindow() != null) {
                getDialog().getWindow()
                        .setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                getDialog().getWindow().setGravity(Gravity.BOTTOM);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onClickBasicItem(String text) {
        speed_selected = text.substring(0, text.length()-1);
    }
}
