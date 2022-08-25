package com.examples.atvizpro.ui.fragments;

import android.app.ProgressDialog;
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

import com.examples.atvizpro.R;
import com.examples.atvizpro.adapter.BasicAdapter;
import com.examples.atvizpro.adapter.PhotoAdapter;
import com.examples.atvizpro.adapter.StickerAdapter;
import com.examples.atvizpro.model.PhotoModel;
import com.examples.atvizpro.utils.VideoUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OptionAddImageFragment extends DialogFragmentBase implements BasicAdapter.BasicAdapterListener, StickerAdapter.StickerAdapterListener {


    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    private static final String TAG = ProjectsFragment.class.getSimpleName();

    public static OptionAddImageFragment newInstance(Bundle args) {
        OptionAddImageFragment dialogSelectVideoSource = new OptionAddImageFragment();
        dialogSelectVideoSource.setArguments(args);
        return dialogSelectVideoSource;
    }
    public CallbackFragment callback = null;

    public OptionAddImageFragment() {
    }
    @Override
    public int getLayout() {
        return R.layout.option_image_fragment;
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

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processingAddText();
            }
        });


        ArrayList<String> listPos = new ArrayList<>();
        listPos.add("TopLeft");
        listPos.add("TopRight");
        listPos.add("Center");
        listPos.add("BottomLeft");
        listPos.add("BottomRight");
        listPos.add("CenterLeft");
        listPos.add("CenterBottom");
        listPos.add("CenterRight");

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        BasicAdapter basicAdapter = new BasicAdapter(getContext(), listPos, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerView.setAdapter(basicAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);


        RecyclerView recyclerView2 = view.findViewById(R.id.recycler_view_image);
        StickerAdapter stickerAdapter = new StickerAdapter(getContext(), getListSticker(), this);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerView2.setAdapter(stickerAdapter);
        recyclerView2.setLayoutManager(linearLayoutManager2);

    }

    private List<PhotoModel> getListSticker() {

        List<PhotoModel> listSticker;
        listSticker = new ArrayList<>();
        listSticker.add(new PhotoModel(R.drawable.facebook_slider1));
        listSticker.add(new PhotoModel(R.drawable.bg_page_fb_2));
        listSticker.add(new PhotoModel(R.drawable.bg_page_fb_2));
        listSticker.add(new PhotoModel(R.drawable.bg_page_fb_2));
        listSticker.add(new PhotoModel(R.drawable.bg_page_fb_2));
        listSticker.add(new PhotoModel(R.drawable.bg_page_fb_3));


        return listSticker;
    }

    private void processingAddText() {

        new VideoUtil().addText(getActivity(), video_path, "startTime", "endTime", "", "", new VideoUtil.ITranscoding() {
            @Override
            public void onStartTranscoding(String outPath) {
                buildDialog("compression...");
            }

            @Override
            public void onFinishTranscoding(String code) {
                if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
            }

            @Override
            public void onUpdateProgressTranscoding(int progress) {

            }
        });

    }

    private ProgressDialog mProgressDialog;
    private ProgressDialog buildDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(getContext(), "", msg);
        }
        mProgressDialog.setMessage(msg);
        return mProgressDialog;
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

    }

    @Override
    public void onClickStickerItem(String text) {

    }
}
