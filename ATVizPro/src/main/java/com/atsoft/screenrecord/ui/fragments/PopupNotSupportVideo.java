package com.atsoft.screenrecord.ui.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
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

public class PopupNotSupportVideo extends DialogFragmentBase {


    public static PopupNotSupportVideo newInstance(IConfirmPopupListener callBack) {
        return new PopupNotSupportVideo(callBack);
    }

    public IConfirmPopupListener callback;

    public PopupNotSupportVideo(IConfirmPopupListener callback) {
        this.callback = callback;
    }

    @Override
    public int getLayout() {
        return R.layout.custom_popup_not_support_layout;
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

    @Override
    public void onResume() {
        super.onResume();
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, android.view.KeyEvent event) {
                if ((keyCode ==  android.view.KeyEvent.KEYCODE_BACK))
                {
                    //Hide your keyboard here!!!
                    updateUI();

                    if (callback != null) callback.onClickPositiveButton();
                    dismissAllowingStateLoss();
                    return true; // pretend we've processed it
                }
                else
                    return false; // pass on to be processed as normal
            }
        });
    }

    private void handlerArgs(View view) {

        TextView tvOK = view.findViewById(R.id.positive_single);
        tvOK.setVisibility(View.VISIBLE);
        tvOK.setOnClickListener(v -> {
            if (callback != null)
            callback.onClickPositiveButton();
            dismiss();
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
                        .setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                getDialog().getWindow().setGravity(Gravity.CENTER);
                getDialog().setCanceledOnTouchOutside(false);
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
