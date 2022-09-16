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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class OptionChangeSpeedFragment extends DialogFragmentBase implements BasicAdapter.BasicAdapterListener {


    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    private static final String TAG = ProjectsFragment.class.getSimpleName();

    public static OptionChangeSpeedFragment newInstance(Bundle args) {
        OptionChangeSpeedFragment dialogSelectVideoSource = new OptionChangeSpeedFragment();
        dialogSelectVideoSource.setArguments(args);
        return dialogSelectVideoSource;
    }
    public ISelectVideoSourceListener callback = null;

    public OptionChangeSpeedFragment() {
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
        listPos.add("0.25x");
        listPos.add("0.5x");
        listPos.add("0.75x");
        listPos.add("1.0x");
        listPos.add("1.25x");
        listPos.add("1.5x");
        listPos.add("1.75x");
        listPos.add("2.0x");

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_position);
        BasicAdapter basicAdapter = new BasicAdapter(getContext(), listPos, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerView.setAdapter(basicAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);

    }

    private void processingAddText() {

//        new VideoUtil().addText(getActivity(), video_path, "startTime", "endTime", "", "", new VideoUtil.ITranscoding() {
//            @Override
//            public void onStartTranscoding(String outPath) {
//                buildDialog("compression...");
//            }
//
//            @Override
//            public void onFinishTranscoding(String code) {
//                if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
//            }
//
//            @Override
//            public void onUpdateProgressTranscoding(int progress) {
//
//            }
//        });

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
}
