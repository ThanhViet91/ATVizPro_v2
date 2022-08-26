package com.examples.atvizpro.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.examples.atvizpro.R;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DialogSelectVideoSource2 extends DialogFragmentBase {

    static DialogSelectVideoSource2 newInstance(ISelectVideoSourceListener callBack) {
        return new DialogSelectVideoSource2(callBack);
    }
    public ISelectVideoSourceListener callback = null;

    public DialogSelectVideoSource2(ISelectVideoSourceListener callback) {
        this.callback = callback;
    }
    @Override
    public int getLayout() {
        return R.layout.dialog_select_video_source2;
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Objects.requireNonNull(getDialog()).setCanceledOnTouchOutside(true);
        LinearLayout buttonSelectVideo = view.findViewById(R.id.ln_select_video);
        buttonSelectVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null) callback.onClickCameraRoll();
                dismiss();
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
                getDialog().getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}
