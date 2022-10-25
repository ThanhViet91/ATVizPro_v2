package com.examples.atscreenrecord_test.ui.fragments;

import android.app.Activity;
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

import com.examples.atscreenrecord_test.R;
import com.examples.atscreenrecord_test.adapter.PhotoAdapter;
import com.examples.atscreenrecord_test.controllers.settings.SettingManager2;
import com.examples.atscreenrecord_test.model.PhotoModel;
import com.examples.atscreenrecord_test.ui.activities.MainActivity;
import com.examples.atscreenrecord_test.utils.AdsUtil;
import com.examples.atscreenrecord_test.utils.OnSingleClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class SubscriptionFragment extends Fragment {

    private Activity mParentActivity = null;
    private FragmentManager mFragmentManager;

    public SubscriptionFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mParentActivity = (MainActivity) context;
        mFragmentManager = getParentFragmentManager();

    }

    private View mViewRoot;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.ss_layout, container, false);
        return mViewRoot;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView btn_dismiss = view.findViewById(R.id.img_btn_back_header);
        btn_dismiss.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mFragmentManager.popBackStack();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}