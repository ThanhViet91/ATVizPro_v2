package com.atsoft.screenrecord.ui.fragments;

import static com.atsoft.screenrecord.ui.activities.MainActivity.REQUEST_VIDEO_FOR_COMMENTARY;
import static com.atsoft.screenrecord.ui.activities.MainActivity.REQUEST_VIDEO_FOR_REACT_CAM;
import static com.atsoft.screenrecord.ui.activities.MainActivity.REQUEST_VIDEO_FOR_VIDEO_EDIT;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.ui.activities.MainActivity;
import com.atsoft.screenrecord.utils.OnSingleClickListener;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

public class DialogSelectVideoSource extends DialogFragmentBase {


    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    private static final String TAG = DialogSelectVideoSource.class.getSimpleName();

    public static DialogSelectVideoSource newInstance(Bundle args) {
        try {
            DialogSelectVideoSource dialogSelectVideoSource = new DialogSelectVideoSource();
            dialogSelectVideoSource.setArguments(args);
            return dialogSelectVideoSource;
        } catch (InstantiationException e) {
            throw new InstantiationException("Unable to instantiate fragment: make sure class name exists, " +
                    "is public, and has an empty constructor that is public", e);
        }
    }
    public ISelectVideoSourceListener callback;

    public DialogSelectVideoSource() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (ISelectVideoSourceListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context
                    + " must implement LogoutUser");
        }
    }

    public DialogSelectVideoSource(ISelectVideoSourceListener callback) {
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

        ImageView btn_close = view.findViewById(R.id.img_btn_close);
        btn_close.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                dismiss();
            }
        });
        RelativeLayout buttonSelectVideoProjects = view.findViewById(R.id.ln_select_my_recordings);
        RelativeLayout buttonSelectVideoCameraRoll = view.findViewById(R.id.ln_select_video_camera_roll);
        buttonSelectVideoCameraRoll.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (callback != null) callback.onClickCameraRoll();
                dismiss();
            }
        });

        buttonSelectVideoProjects.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (callback != null) callback.onClickMyRecordings();
                dismiss();
            }
        });

        handlerArgs(view);

    }

    private void handlerArgs(View view) {

        if (getArguments() != null) {

            ImageView ava = view.findViewById(R.id.img_ava_option);
            TextView title = view.findViewById(R.id.tv_title_option);
            TextView des = view.findViewById(R.id.tv_decs_popup);
            switch (getArguments().getInt(ARG_PARAM1)) {
                case REQUEST_VIDEO_FOR_REACT_CAM:
                    ava.setImageResource(R.drawable.ic_react_cam_svg);
                    title.setText(R.string.react_cam);
                    des.setText(R.string.react_to_videos_from_camera_roll);
                    break;
                case REQUEST_VIDEO_FOR_VIDEO_EDIT:
                    ava.setImageResource(R.drawable.ic_edit_video);
                    title.setText(R.string.video_editor);
                    des.setText(R.string.edit_video_from_my_recordings_or_camera_roll);
                    break;
                case REQUEST_VIDEO_FOR_COMMENTARY:
                    ava.setImageResource(R.drawable.ic_microphone_2);
                    title.setText(R.string.commentary);
                    des.setText(R.string.add_commentary_to_video_from_my_recordings_or_camera_roll);
                    break;
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


}
