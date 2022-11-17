package com.atsoft.screenrecord.ui.fragments;

import static com.atsoft.screenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_YOUTUBE;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.adapter.PhotoAdapter;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;
import com.atsoft.screenrecord.model.PhotoModel;
import com.atsoft.screenrecord.utils.AdsUtil;
import com.atsoft.screenrecord.utils.OnSingleClickListener;

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
    boolean isFirstTime = true;
    private FragmentManager mFragmentManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mFragmentManager = getParentFragmentManager();
    }
    View mViewRoot;
    private AdsUtil mAdManager;
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mViewRoot != null) return mViewRoot;
        mViewRoot = inflater.inflate(R.layout.fragment_facebook_live_streaming, container, false);
        RelativeLayout mAdview = mViewRoot.findViewById(R.id.adView);
        mAdManager = new AdsUtil(getContext(), mAdview);
        isFirstTime  = SettingManager2.getFirstTimeLiveStreamYoutube(requireContext());
        SettingManager2.setFirstTimeLiveStreamYoutube(requireContext(), false);
        return mViewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mAdManager != null) mAdManager.loadBanner();
        viewPager2 = view.findViewById(R.id.view_pager_img_tutorial);
        circleIndicator3 = view.findViewById(R.id.circle_indicator);
        btnContinue = view.findViewById(R.id.btn_continue_);
        imgBack = view.findViewById(R.id.tv_btn_skip);
        TextView tvTitle = view.findViewById(R.id.title_name);
        tvTitle.setText(getString(R.string.youtube_livestreaming));
        TextView tvDes = view.findViewById(R.id.tv_decs);
        tvDes.setText(getString(R.string.description_yt_live));

        photoAdapter = new PhotoAdapter(getContext(), getListPhoto());
        viewPager2.setAdapter(photoAdapter);
        circleIndicator3.setViewPager(viewPager2);

        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });

        viewPager2.setPageTransformer(compositePageTransformer);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                i = position;
                if (i == getListPhoto().size() - 1){
                    if (!isFirstTime)
                        btnContinue.setText(getString(R.string.done_));
                } else {
                    btnContinue.setText(getString(R.string.continue_));
                }
            }
        });
        btnContinue.setOnClickListener(v -> {
            i = i + 1;
            viewPager2.setCurrentItem(i);
            if (i == getListPhoto().size() - 1)
                if (!isFirstTime) {
                    btnContinue.setText(getString(R.string.done_));
                }

            if (i == getListPhoto().size()) {
                if (isFirstTime) {
                    RTMPLiveAddressFragment fragment = new RTMPLiveAddressFragment();
                    fragment.setSocialType(SOCIAL_TYPE_YOUTUBE);
                    mFragmentManager.beginTransaction()
                            .replace(R.id.frame_layout_fragment, fragment)
                            .addToBackStack("")
                            .commit();
                } else {
                    mFragmentManager.popBackStack();
                }
                i = 0;
            }
        });
        imgBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
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