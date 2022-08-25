package com.examples.atvizpro.ui.activities;

import static com.examples.atvizpro.ui.utils.MyUtils.hideStatusBar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.examples.atvizpro.R;
import com.examples.atvizpro.adapter.VideoOptionAdapter;
import com.examples.atvizpro.ui.VideoEditorView;
import com.examples.atvizpro.ui.VideoStreamListener;
import com.examples.atvizpro.ui.fragments.IOptionFragmentListener;
import com.examples.atvizpro.ui.fragments.OptionAddImageFragment;
import com.examples.atvizpro.ui.fragments.OptionAddTextFragment;
import com.examples.atvizpro.ui.fragments.OptionChangeSpeedFragment;
import com.examples.atvizpro.ui.fragments.OptionTrimFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class VideoEditorActivity extends AppCompatActivity implements VideoOptionAdapter.VideoOptionListener, IOptionFragmentListener, VideoStreamListener {

    static final String VIDEO_PATH_KEY = "video-file-path";
    private ProgressDialog mProgressDialog;
    private VideoEditorView videoEditorView;
    private String pathOriginalVideo = "";
    private RecyclerView recyclerView;

    private ArrayList<String> videoOptions = new ArrayList<>();
    private VideoOptionAdapter mAdapter;

    private LottieAnimationView animationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(this);
        setContentView(R.layout.video_editor_layout);
        videoEditorView = findViewById(R.id.trimmer_view);
        Bundle bd = getIntent().getExtras();
        if (bd != null) pathOriginalVideo = bd.getString(VIDEO_PATH_KEY);
        videoEditorView.setOnEditVideoListener(this);
        videoEditorView.initVideoByURI(Uri.parse(pathOriginalVideo));

        videoOptions.add("Trim");
        videoOptions.add("Music");
        videoOptions.add("Speed");
        videoOptions.add("Text");
        videoOptions.add("Image");
        videoOptions.add("Merge");
        recyclerView = findViewById(R.id.recycler_view);
        mAdapter = new VideoOptionAdapter(this, videoOptions, this);
        // Set the mAdapter on the {@link ListView}
        // so the list can be populated in the user interface
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        animationView = findViewById(R.id.animation_view);
        animationView.setVisibility(View.GONE);

    }

    @Override
    public void onPause() {
        super.onPause();
        videoEditorView.onVideoPause();
        videoEditorView.setRestoreState(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoEditorView.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    String cacheAudioFilePath;

    @SuppressLint("SimpleDateFormat")
    public String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }



    private ProgressDialog buildDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "", msg);
        }
        mProgressDialog.setMessage(msg);
        return mProgressDialog;
    }

    @Override
    public void onClickItem(String text) {

        if (!text.equals("")) {
            showOptionFragment(text);
        }
    }


    private void showOptionFragment(String opt) {
        Bundle bundle = new Bundle();

        bundle.putLong("video_duration", videoEditorView.getVideoDuration());
        bundle.putString("video_path", pathOriginalVideo);
        switch (opt) {
            case "Trim":

                OptionTrimFragment.newInstance(this, bundle).show(getSupportFragmentManager(), "");
                break;
            case "Text":

                OptionAddTextFragment.newInstance(bundle).show(getSupportFragmentManager(), "");
                break;

            case "Speed":

                OptionChangeSpeedFragment.newInstance(bundle).show(getSupportFragmentManager(), "");
                break;

            case "Image":

                OptionAddImageFragment.newInstance(bundle).show(getSupportFragmentManager(), "");
                break;
        }

    }

    @Override
    public void onClickClose() {
    }

    @Override
    public void onClickDone() {

        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation();
    }

    @Override
    public void onClickNext() {

    }
    @Override
    public void onFinishProcess() {

        animationView.pauseAnimation();
        animationView.setVisibility(View.GONE);
    }


    @Override
    public void onStartRecord() {

    }

    @Override
    public void onStopRecord() {

    }

    @Override
    public void onDeleteRecord() {

    }

    @Override
    public void onCancel() {
        finish();
    }

}