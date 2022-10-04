package com.examples.atscreenrecord.ui.fragments;

import static com.examples.atscreenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_YOUTUBE;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.examples.atscreenrecord.App;
import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.adapter.PhotoAdapter;
import com.examples.atscreenrecord.controllers.settings.SettingManager2;
import com.examples.atscreenrecord.model.PhotoModel;
import com.examples.atscreenrecord.ui.activities.MainActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class GuidelineYoutubeLiveStreamingFragment extends Fragment {

    ViewPager2 viewPager2;
    CircleIndicator3 circleIndicator3;
    PhotoAdapter photoAdapter;
    TextView btnContinue;
    ImageView imgBack;
    int i = 0;

    private Activity mParentActivity = null;
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

        View mViewRoot = inflater.inflate(R.layout.fragment_youtube_live_streaming, container, false);
        return mViewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager2 = view.findViewById(R.id.view_pager_img_youtube);
        circleIndicator3 = view.findViewById(R.id.circle_indicator_youtube);
        btnContinue = view.findViewById(R.id.btn_continue_youtube_livestreaming);
        imgBack = view.findViewById(R.id.img_back_yt_slider);

        photoAdapter = new PhotoAdapter(getContext(), getListPhoto());
        viewPager2.setAdapter(photoAdapter);
        circleIndicator3.setViewPager(viewPager2);

        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            }
        });

        viewPager2.setPageTransformer(compositePageTransformer);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                i = position;
                if (i == getListPhoto().size() - 1){
                    if (!SettingManager2.getFirstTimeLiveStreamYoutube(requireContext()))
                        btnContinue.setText(getString(R.string.done_));
                } else {
                    btnContinue.setText(getString(R.string.continue_));
                }
            }
        });
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = i + 1;
                viewPager2.setCurrentItem(i);
                if (i == getListPhoto().size() - 1)
                    if (!SettingManager2.getFirstTimeLiveStreamYoutube(requireContext())) {
                        btnContinue.setText(getString(R.string.done_));
                    }

                if (i == getListPhoto().size()) {
                    if (SettingManager2.getFirstTimeLiveStreamYoutube(requireContext())) {
                        RTMPLiveAddressFragment fragment = new RTMPLiveAddressFragment();
                        fragment.setSocialType(SOCIAL_TYPE_YOUTUBE);
                        mFragmentManager.beginTransaction()
                                .replace(R.id.frame_layout_fragment, fragment)
                                .addToBackStack("")
                                .commit();
                    } else {
                        mFragmentManager.popBackStack();
                    }
                    SettingManager2.setFirstTimeLiveStreamYoutube(requireContext(), false);
                    i = 0;
                }
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentManager.popBackStack();
            }
        });

    }

    public List<PhotoModel> getListPhoto() {
        List<PhotoModel> mListPhoto;
        mListPhoto = new ArrayList<>();
        mListPhoto.add(new PhotoModel(R.drawable.bg_page_yt_1));
        mListPhoto.add(new PhotoModel(R.drawable.bg_page_yt_2));
        return mListPhoto;
    }
}