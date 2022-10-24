package com.examples.atscreenrecord_test.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.examples.atscreenrecord_test.R;
import com.examples.atscreenrecord_test.utils.OnSingleClickListener;
import com.examples.atscreenrecord_test.utils.VideoUtil;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import it.sephiroth.android.library.rangeseekbar.RangeSeekBar;

public class OptionTrimFragment extends DialogFragmentBase {


    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    private static final String TAG = OptionTrimFragment.class.getSimpleName();

    public static OptionTrimFragment newInstance(IOptionFragmentListener callback, Bundle args) {
        OptionTrimFragment dialogSelectVideoSource = new OptionTrimFragment(callback);
        dialogSelectVideoSource.setArguments(args);
        return dialogSelectVideoSource;
    }
    public IOptionFragmentListener callback = null;

    public OptionTrimFragment(IOptionFragmentListener callback) {
        this.callback =  callback;

    }
    @Override
    public int getLayout() {
        return R.layout.option_trim_fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    long videoDuration, startTime, endTime;
    TextView tvStartTime, tvEndTime;
    String video_path;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        videoDuration = getArguments() != null ? getArguments().getLong("video_duration", 0) : 0;
        video_path = getArguments() != null ? getArguments().getString("video_path", "") : "";

        ImageView btn_close = view.findViewById(R.id.iv_close);
        ImageView btn_done = view.findViewById(R.id.iv_done);
        tvStartTime = view.findViewById(R.id.actvStartTime);
        tvEndTime = view.findViewById(R.id.actvEndTime);

        tvStartTime.setText(secToTime(0));
        tvEndTime.setText(secToTime(videoDuration));
        btn_close.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                dismiss();
            }
        });
        btn_done.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                processingTrimming();
            }
        });

        RangeSeekBar rangeSlider = view.findViewById(R.id.sbrvVideoTrim);

        rangeSlider.setMax((int) videoDuration);
        rangeSlider.setProgress(0, (int) videoDuration);
        rangeSlider.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onProgressChanged(RangeSeekBar rangeSeekBar, int i, int i1, boolean b) {
                tvStartTime.setText(secToTime((long)i));
                tvEndTime.setText(secToTime((long)i1));

                startTime = i;
                endTime = i1;
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar rangeSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar rangeSeekBar) {

            }
        });


    }

    private void processingTrimming() {

        callback.onClickDone();

        VideoUtil.getInstance().trimVideo(getActivity(), video_path, startTime, endTime, new VideoUtil.ITranscoding() {
            @Override
            public void onStartTranscoding(String outPath) {
            }

            @Override
            public void onFinishTranscoding(String code) {
                dismiss();
                callback.onFinishProcess(code);
            }
        });

    }


    @SuppressLint("DefaultLocale")
    public String secToTime(long totalSeconds) {
        return String.format(
                "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(totalSeconds),
                TimeUnit.MILLISECONDS.toMinutes(totalSeconds) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalSeconds)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(totalSeconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalSeconds))
        );
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
