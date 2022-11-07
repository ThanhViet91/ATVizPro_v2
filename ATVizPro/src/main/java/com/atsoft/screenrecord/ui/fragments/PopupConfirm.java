package com.atsoft.screenrecord.ui.fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.atsoft.screenrecord.R;

import org.jetbrains.annotations.NotNull;

public class PopupConfirm extends DialogFragmentBase {

    private static final String TAG = PopupConfirm.class.getSimpleName();
    public static final String KEY_TITLE = "KEY_TITLE";
    public static final String KEY_POSITIVE = "KEY_POSITIVE";
    public static final String KEY_NEGATIVE = "KEY_NEGATIVE";

    public static PopupConfirm newInstance(IConfirmPopupListener callBack, Bundle args) {
        PopupConfirm dialogSelectVideoSource = new PopupConfirm(callBack);
        dialogSelectVideoSource.setArguments(args);
        return dialogSelectVideoSource;
    }

    public IConfirmPopupListener callback;

    public PopupConfirm(IConfirmPopupListener callback) {
        this.callback = callback;
    }

    @Override
    public int getLayout() {
        return R.layout.custom_popup_layout;
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handlerArgs(view);
    }

    private void handlerArgs(View view) {

        if (getArguments() != null) {

            String title = getArguments().getString(KEY_TITLE);
            String pos = getArguments().getString(KEY_POSITIVE);
            String neg = getArguments().getString(KEY_NEGATIVE);
//            ImageView ava = view.findViewById(R.id.icon_popup);
            TextView tvTitle = view.findViewById(R.id.title_popup);
            tvTitle.setText(title);
            if (pos == null || neg == null) {
                //mode single positive
                LinearLayout layout = view.findViewById(R.id.ln_action_dual_mode);
                layout.setVisibility(View.GONE);
                TextView tvOK = view.findViewById(R.id.positive_single);
                tvOK.setVisibility(View.VISIBLE);
                tvOK.setOnClickListener(v -> {
                    callback.onClickPositiveButton();
                    dismiss();
                });
            } else {
                TextView tvPositive = view.findViewById(R.id.tv_positive);
                tvPositive.setText(pos);
                TextView tvNegative = view.findViewById(R.id.tv_negative);
                tvNegative.setText(neg);
                tvPositive.setOnClickListener(v -> {
                    callback.onClickPositiveButton();
                    dismiss();
                });
                tvNegative.setOnClickListener(v -> {
                    callback.onClickNegativeButton();
                    dismiss();
                });
            }
        }
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
                        .setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                getDialog().getWindow().setGravity(Gravity.CENTER);
                getDialog().setCanceledOnTouchOutside(true);
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
