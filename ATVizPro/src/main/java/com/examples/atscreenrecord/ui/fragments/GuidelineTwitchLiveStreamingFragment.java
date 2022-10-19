package com.examples.atscreenrecord.ui.fragments;

import static com.examples.atscreenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_TWITCH;

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

import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.adapter.PhotoAdapter;
import com.examples.atscreenrecord.controllers.settings.SettingManager2;
import com.examples.atscreenrecord.model.PhotoModel;
import com.examples.atscreenrecord.utils.AdsUtil;
import com.examples.atscreenrecord.utils.OnSingleClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class GuidelineTwitchLiveStreamingFragment extends Fragment {
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

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View mViewRoot = inflater.inflate(R.layout.fragment_facebook_live_streaming, container, false);
        isFirstTime  = SettingManager2.getFirstTimeLiveStreamTwitch(requireContext());
        SettingManager2.setFirstTimeLiveStreamTwitch(requireContext(), false);
        return mViewRoot;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager2 = view.findViewById(R.id.view_pager_img_tutorial);
        circleIndicator3 = view.findViewById(R.id.circle_indicator);
        btnContinue = view.findViewById(R.id.btn_continue_);
        imgBack = view.findViewById(R.id.tv_btn_skip);
        TextView tvTitle = view.findViewById(R.id.title_name);
        tvTitle.setText(getString(R.string.twitch_livestreaming));
        TextView tvDes = view.findViewById(R.id.tv_decs);
        tvDes.setText(getString(R.string.description_tw_live));

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
                    fragment.setSocialType(SOCIAL_TYPE_TWITCH);
                    mFragmentManager.beginTransaction()
                            .replace(R.id.frame_layout_fragment, fragment)
                            .addToBackStack("")
                            .commit();
                } else {
                    mFragmentManager.popBackStack();
                }
//                    SettingManager2.setFirstTimeLiveStreamTwitch(requireContext(), false);
                i = 0;
            }
        });
        imgBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mFragmentManager.popBackStack();
            }
        });
        RelativeLayout mAdview = view.findViewById(R.id.adView);
        new AdsUtil(getContext(), mAdview).loadBanner();

    }


    public List<PhotoModel> getListPhoto() {
        List<PhotoModel> mListPhoto;
        mListPhoto = new ArrayList<>();
        mListPhoto.add(new PhotoModel(R.drawable.bg_page_twitch_1));
        mListPhoto.add(new PhotoModel(R.drawable.bg_page_twitch_2));
        return mListPhoto;
    }
}