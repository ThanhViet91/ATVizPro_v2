package com.examples.atvizpro.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.examples.atvizpro.App;
import com.examples.atvizpro.R;
import com.examples.atvizpro.ui.activities.MainActivity;

import org.jetbrains.annotations.NotNull;

public class LiveStreamingFragment extends Fragment {

    ImageView imgBack, imgFacebook, imgYoutube, imgTwitch;

    private MainActivity mParentActivity = null;
    private App mApplication;
    private FragmentManager mFragmentManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mParentActivity = (MainActivity) context;
        this.mApplication = (App) context.getApplicationContext();
        mFragmentManager = getParentFragmentManager();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View mViewRoot = inflater.inflate(R.layout.fragment_live_streaming, container, false);
        return mViewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imgBack = view.findViewById(R.id.img_btn_back_header);
        imgFacebook = view.findViewById(R.id.img_facebook_livestreaming);
        imgTwitch = view.findViewById(R.id.img_twitch_livestreaming);
        imgYoutube = view.findViewById(R.id.img_youtube_livestreaming);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentManager.popBackStack();
            }
        });
        imgYoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout_fragment, new YoutubeLiveStreamingFragment())
                        .addToBackStack("")
                        .commit();
            }
        });
        imgFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout_fragment, new FacebookLiveStreamingFragment())
                        .addToBackStack("")
                        .commit();
            }
        });
        imgTwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout_fragment, new TwitchLiveStreamingFragment())
                        .addToBackStack("")
                        .commit();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}