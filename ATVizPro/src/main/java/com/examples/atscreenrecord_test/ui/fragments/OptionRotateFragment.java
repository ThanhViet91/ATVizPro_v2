package com.examples.atscreenrecord_test.ui.fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.examples.atscreenrecord_test.R;
import com.examples.atscreenrecord_test.adapter.Sticker2Adapter;
import com.examples.atscreenrecord_test.model.PhotoModel;
import com.examples.atscreenrecord_test.utils.FFmpegUtil;
import com.examples.atscreenrecord_test.utils.OnSingleClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OptionRotateFragment extends DialogFragmentBase implements Sticker2Adapter.StickerAdapterListener {


    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    private static final String TAG = OptionRotateFragment.class.getSimpleName();

    public IOptionFragmentListener mCallback;

    public static OptionRotateFragment newInstance(IOptionFragmentListener callback, Bundle args) {
        OptionRotateFragment dialogSelectVideoSource = new OptionRotateFragment(callback);
        dialogSelectVideoSource.setArguments(args);
        return dialogSelectVideoSource;
    }

    public OptionRotateFragment(IOptionFragmentListener callback) {
        mCallback = callback;
    }
    @Override
    public int getLayout() {
        return R.layout.option_rotate_fragment;
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
                processingRotate(selected);
            }
        });


        ArrayList<String> listPos = new ArrayList<>();
        listPos.add("0.25x");
        listPos.add("0.5x");
        listPos.add("0.75x");
        listPos.add("1.0x");
        listPos.add("1.25x");
        listPos.add("1.5x");
        listPos.add("1.75x");
        listPos.add("2.0x");

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_image);
        Sticker2Adapter stickerAdapter = new Sticker2Adapter(getContext(), getListSticker(), this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerView.setAdapter(stickerAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);

    }


    private void processingRotate(int pos) {
        dismiss();
        mCallback.onClickDone();
        if (pos == 0)
        FFmpegUtil.getInstance().flipVertical(video_path, new FFmpegUtil.ITranscoding() {
            @Override
            public void onStartTranscoding(String outPath) {

            }

            @Override
            public void onFinishTranscoding(String code) {
                if (!code.equals(""))
                    mCallback.onFinishProcess(code);
            }
        });
        if (pos == 1)
            FFmpegUtil.getInstance().flipHorizontal(video_path, new FFmpegUtil.ITranscoding() {
                @Override
                public void onStartTranscoding(String outPath) {

                }

                @Override
                public void onFinishTranscoding(String code) {
                    if (!code.equals(""))
                        mCallback.onFinishProcess(code);
                }
            });
        if (pos == 2)
            FFmpegUtil.getInstance().rotate(video_path, " -vf transpose=2,transpose=2", new FFmpegUtil.ITranscoding() {
                @Override
                public void onStartTranscoding(String outPath) {

                }

                @Override
                public void onFinishTranscoding(String code) {
                    if (!code.equals(""))
                        mCallback.onFinishProcess(code);
                }
            });
        if (pos == 3)
            FFmpegUtil.getInstance().rotate(video_path, " -vf transpose=1", new FFmpegUtil.ITranscoding() {
                @Override
                public void onStartTranscoding(String outPath) {

                }

                @Override
                public void onFinishTranscoding(String code) {
                    if (!code.equals(""))
                        mCallback.onFinishProcess(code);
                }
            });
        if (pos == 4)
            FFmpegUtil.getInstance().rotate(video_path, " -vf transpose=2", new FFmpegUtil.ITranscoding() {
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
    private List<PhotoModel> getListSticker() {
        List<PhotoModel> listSticker;
        listSticker = new ArrayList<>();
        for (int i = 0; i < 5; i ++){
            listSticker.add(new PhotoModel(resourceID[i]));
        }
        return listSticker;
    }
    int[] resourceID = {R.drawable.ic_flip_ver,
            R.drawable.ic_flip_hor,
            R.drawable.ic_clock_180,
            R.drawable.ic_clock_90,
            R.drawable.ic_clock_90_,
    };


    private int selected = 0;
    @Override
    public void onClickStickerItem(int pos) {
        selected = pos;
    }
}
