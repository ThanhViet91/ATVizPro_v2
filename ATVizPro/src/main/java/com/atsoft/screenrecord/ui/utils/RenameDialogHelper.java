package com.atsoft.screenrecord.ui.utils;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.model.VideoModel;
import com.google.android.material.textfield.TextInputLayout;
import java.io.File;
import java.io.IOException;

public class RenameDialogHelper {

    public RenameDialogHelper(IDialogHelper callback) {
        this.mCallback = callback;
    }

    public interface IDialogHelper {
        void onClickOK(String result);
        void onClickCancel(String result);
    }

    private final IDialogHelper mCallback;

    public void showRenameDialog(Context context, VideoModel oldVideo) {

            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.layout_rename);
            dialog.setTitle("Rename");
            final TextInputLayout tilEdittext = dialog.findViewById(R.id.tilRename);

            final EditText editText = dialog.findViewById(R.id.edRename);
            editText.setText(oldVideo.getName());
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
                        tilEdittext.setError("A filename cannot contain any of the following character: \\/\":*<>|%");
                    } else {
                        tilEdittext.setError("");
                    }
                }
            });
            Button btnOk = dialog.findViewById(R.id.rename_btn_ok);
            // if button is clicked, close the custom dialog
            btnOk.setOnClickListener(v -> {
                String newTitle = editText.getText().toString().trim();
                if (!oldVideo.getName().equals(newTitle)) {
                    try {
                        renameFile(oldVideo.getPath(), newTitle);
                        editText.setText(newTitle);
                        mCallback.onClickOK(newTitle);
                        dialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                        tilEdittext.setError(e.getMessage());
                    }
                } else
                    dialog.dismiss();

            });

            Button btnCancel = dialog.findViewById(R.id.rename_btn_cancel);
            // if button is clicked, close the custom dialog
            btnCancel.setOnClickListener(v -> {
                mCallback.onClickCancel("cancel");
                dialog.dismiss();
            });
            dialog.show();

    }

    public void renameFile(final String videoPath, final String newNames) throws Exception {
        String newName = newNames.trim();
        if (TextUtils.isEmpty(newName)) throw new Exception("File name cannot be empty.");
        if (MyUtils.isValidFilenameSynctax(newName)){
            System.out.println("thanhlv A filename cannot contain any of the following character:");
            throw new Exception("A filename cannot contain any of the following character: \\/\":*<>|%");
        }

        File file = new File(videoPath);

        final File fileWithNewName = new File(file.getParentFile(), newName + ".mp4");
        if (fileWithNewName.exists()) {
            System.out.println("thanhlv This filename is exists. Please choose another name");
            throw new IOException("This filename is exists. Please choose another name");
        }

        // Rename file (or directory)
        boolean success = file.renameTo(fileWithNewName);

        if (!success) {
            // File was not successfully renamed
            throw new Exception("Cannot rename this video. This video file might not available.");
        } else {
            System.out.println("thanhlv rename successsssssssss " + fileWithNewName.getAbsolutePath());
        }
    }
}