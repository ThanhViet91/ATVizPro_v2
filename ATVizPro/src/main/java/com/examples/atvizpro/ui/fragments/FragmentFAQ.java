package com.examples.atvizpro.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.examples.atvizpro.R;
import com.examples.atvizpro.adapter.FAQAdapter;
import com.examples.atvizpro.model.FAQItem;
import com.examples.atvizpro.utils.AdUtil;
import com.google.android.gms.ads.AdView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FragmentFAQ extends Fragment {

    RecyclerView recyclerView;
    ArrayList<FAQItem> mFAQs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View mViewRoot = inflater.inflate(R.layout.fragment_faq, container, false);
        mFAQs.add(new FAQItem("Sound is not working", "Before starting your screen recording,make sure your phone is not in slient mode and the volume is on. " +
                "Some apps do not allow the sound to be recorded, and it is a limitation we cannot bypass.", false));
        mFAQs.add(new FAQItem("How do I turn on the microphone?", "Make sure you long press (or 3D touch it available) the record button, " +
                "so a menu will appear to turn on the microphone.", false));
        mFAQs.add(new FAQItem("I want to Face Cam while I screen record", "Currently, it is not possible to Face Cam and record your screen at the same time. " +
                "However, you can add a Face Cam reaction after you finish your recording. Just open the video recording in our app and select “ Face Cam”.", false));
        mFAQs.add(new FAQItem("My Video is saved to camera roll and I want to save it on the app", "Just make sure you long press (or 3D touch if available) " +
                "the record button on the control center. Then, select Record it! as the destination app.", false));
        mFAQs.add(new FAQItem("Error while recording or recording won’t stop", "Somethings the recording engine gets stuck due to a memory bug on some IOS devices." +
                " Try restarting you divice and close all other apps.", false));
        mFAQs.add(new FAQItem("My record button is greyed out", "Make sure Screen Recording is Not Restricted with Parental Control: " +
                "Open Settings and Tap on General, Tap on Restrictions. You need to enter your Restrictions passcode. " +
                "Then scroll and look if Screen Recorder is restricted. If so, turn it off.", false));
        mFAQs.add(new FAQItem("I still have a question", "Contact us at support@atsoft.io so we can help you.", false));

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
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().popBackStack();
            }
        });

        AdView mAdView = view.findViewById(R.id.adView);
        AdUtil.createBannerAdmob(getContext(), mAdView);
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
