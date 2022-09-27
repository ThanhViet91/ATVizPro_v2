package com.examples.atscreenrecord.ui.utils;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.examples.atscreenrecord.R;
import com.google.android.material.textfield.TextInputLayout;

public class DialogHelper {

    private static DialogHelper mInstance = null;

    private DialogHelper() {

    }

    public static DialogHelper getInstance(){
        if (mInstance == null) {
            synchronized (DialogHelper.class) {
                mInstance = new DialogHelper();
            }
        }
        return mInstance;
    }


    public void showRenameDialog(Context context, String videoName) {
        if (!videoName.equals("")) {
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.layout_rename);
            dialog.setTitle("Properties");
            final TextInputLayout tilEditext = (TextInputLayout) dialog.findViewById(R.id.tilRename);

            final boolean isValid = true;

            final EditText editText = dialog.findViewById(R.id.edRename);
            editText.setText(videoName);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (MyUtils.isValidFilenameSynctax(s.toString())) {
                        tilEditext.setError("A filename cannot contain any of the following charactor: \\/\":*<>| is not n");
                    } else {
                        tilEditext.setError("");
                    }
                }
            });
            Button btnOk = (Button) dialog.findViewById(R.id.rename_btn_ok);
            // if button is clicked, close the custom dialog
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newTitle = editText.getText().toString() + ".mp4";
//                    if (!TextUtils.equals(video.getTitle(), newTitle)) {
//                        try {
//                            renameFile(video, newTitle);
//                            mVideoAdapter.notifyDataSetChanged();
//                            dialog.dismiss();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            tilEditext.setError(e.getMessage());
//                        }
//                    } else
//                        dialog.dismiss();

                }
            });

            Button btnCancel = (Button) dialog.findViewById(R.id.rename_btn_cancel);
            // if button is clicked, close the custom dialog
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }
}