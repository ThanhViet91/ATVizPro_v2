package com.examples.atvizpro.ui.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.examples.atvizpro.Core;
import com.examples.atvizpro.R;
import com.examples.atvizpro.adapter.VideoSettingsAdapter;
import com.examples.atvizpro.model.VideoProperties;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DialogSelectVideoSource extends DialogFragmentBase {

    static DialogSelectVideoSource newInstance(CallbackFragment callBack) {
        return new DialogSelectVideoSource(callBack);
    }
    public CallbackFragment callback = null;

    public DialogSelectVideoSource(CallbackFragment callback) {
        this.callback = callback;
    }
    @Override
    public int getLayout() {
        return R.layout.dialog_select_video_source;
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout buttonSelectVideo = view.findViewById(R.id.ln_select_video);
        buttonSelectVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null) callback.onClick();
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
