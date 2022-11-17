package com.atsoft.screenrecord.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.atsoft.screenrecord.App;
import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;
import com.atsoft.screenrecord.ui.activities.MainActivity;
import com.atsoft.screenrecord.utils.AdsUtil;
import com.atsoft.screenrecord.utils.OnSingleClickListener;

import org.jetbrains.annotations.NotNull;

public class LiveStreamingFragment extends Fragment {

    ImageView imgBack, imgFacebook, imgYoutube, imgTwitch;
    public static final int SOCIAL_TYPE_YOUTUBE = 1;
    public static final int SOCIAL_TYPE_FACEBOOK = 2;
    public static final int SOCIAL_TYPE_TWITCH = 3;
    private MainActivity mParentActivity = null;
    private App mApplication;
    private FragmentManager mFragmentManager;
    private AdsUtil mAdManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mParentActivity = (MainActivity) context;
        this.mApplication = (App) context.getApplicationContext();
        mFragmentManager = getParentFragmentManager();
    }

    View mViewRoot;
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mViewRoot != null) return mViewRoot;
        mViewRoot = inflater.inflate(R.layout.fragment_live_streaming, container, false);
        RelativeLayout mAdView = mViewRoot.findViewById(R.id.adView);
        mAdManager = new AdsUtil(getContext(), mAdView);
        return mViewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imgBack = view.findViewById(R.id.img_btn_back_header);
        imgFacebook = view.findViewById(R.id.img_facebook_livestreaming);
        imgTwitch = view.findViewById(R.id.img_twitch_livestreaming);
        imgYoutube = view.findViewById(R.id.img_youtube_livestreaming);

        imgBack.setOnClickListener(v -> mFragmentManager.popBackStack());
        imgYoutube.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                handleSocialLive(SOCIAL_TYPE_YOUTUBE);
            }
        });
        imgFacebook.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                handleSocialLive(SOCIAL_TYPE_FACEBOOK);
            }
        });
        imgTwitch.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                handleSocialLive(SOCIAL_TYPE_TWITCH);
            }
        });
        if (mAdManager != null) mAdManager.loadBanner();
    }


    private void handleSocialLive(int type) {
        SettingManager2.setLiveStreamType(requireContext(), type);
        mParentActivity.updateIconService();
        if (isFirstTimeReach(type)) {
            showTutorialScreenLiveStream(type);
            return;
        }
        RTMPLiveAddressFragment fragment = new RTMPLiveAddressFragment();
        fragment.setSocialType(type);
        mFragmentManager.beginTransaction()
                .replace(R.id.frame_layout_fragment, fragment)
                .addToBackStack("")
                .commit();
    }
    private boolean isFirstTimeReach(int type) {
        if (type == SOCIAL_TYPE_YOUTUBE)
            return SettingManager2.getFirstTimeLiveStreamYoutube(requireContext());
        if (type == SOCIAL_TYPE_FACEBOOK)
            return SettingManager2.getFirstTimeLiveStreamFacebook(requireContext());
        if (type == SOCIAL_TYPE_TWITCH)
            return SettingManager2.getFirstTimeLiveStreamTwitch(requireContext());
        return false;
    }

    private void showTutorialScreenLiveStream(int type) {
        Fragment fragment = null;
        if (type == SOCIAL_TYPE_YOUTUBE) fragment = new GuidelineYoutubeLiveStreamingFragment();
        if (type == SOCIAL_TYPE_FACEBOOK) fragment = new GuidelineFacebookLiveStreamingFragment();
        if (type == SOCIAL_TYPE_TWITCH) fragment = new GuidelineTwitchLiveStreamingFragment();
        if (fragment != null)
            mFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_fragment, fragment)
                    .addToBackStack("")
                    .commit();
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