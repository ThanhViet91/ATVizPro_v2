package com.examples.atscreenrecord_test.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.examples.atscreenrecord_test.AppConfigs;
import com.examples.atscreenrecord_test.R;
import com.examples.atscreenrecord_test.adapter.FAQAdapter;
import com.examples.atscreenrecord_test.model.FAQItem;
import com.examples.atscreenrecord_test.utils.AdsUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FragmentFAQ extends Fragment {

    RecyclerView recyclerView;
    ArrayList<FAQItem> mFAQs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View mViewRoot = inflater.inflate(R.layout.fragment_faq, container, false);

        mFAQs.add(new FAQItem(getString(R.string.question_1), getString(R.string.answer_1), false));
        mFAQs.add(new FAQItem(getString(R.string.question_2), getString(R.string.answer_2), false));
        mFAQs.add(new FAQItem(getString(R.string.question_3), getString(R.string.answer_3), false));
        mFAQs.add(new FAQItem(getString(R.string.question_4), getString(R.string.answer_4), false));
        mFAQs.add(new FAQItem(getString(R.string.question_5), getString(R.string.answer_5), false));
        mFAQs.add(new FAQItem(getString(R.string.question_6), getString(R.string.answer_6), false));
        mFAQs.add(new FAQItem(getString(R.string.question_8), getString(R.string.answer_8), false));
        mFAQs.add(new FAQItem(getString(R.string.question_7), String.format(getString(R.string.answer_7), AppConfigs.getInstance().getConfigModel().getFeedbackEmail()), false));
        return mViewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_position);
        FAQAdapter adapter = new FAQAdapter(getContext(), mFAQs);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        ImageView btn_back = view.findViewById(R.id.img_btn_back_header);
        btn_back.setOnClickListener(view1 -> getParentFragmentManager().popBackStack());

        RelativeLayout mAdView = view.findViewById(R.id.adView);
        new AdsUtil(getContext(), mAdView).loadBanner();
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
