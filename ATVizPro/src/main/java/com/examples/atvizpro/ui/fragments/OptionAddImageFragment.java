package com.examples.atvizpro.ui.fragments;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.examples.atvizpro.OptiUtils;
import com.examples.atvizpro.R;
import com.examples.atvizpro.adapter.BasicAdapter;
import com.examples.atvizpro.adapter.StickerAdapter;
import com.examples.atvizpro.model.PhotoModel;
import com.examples.atvizpro.utils.VideoUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OptionAddImageFragment extends DialogFragmentBase implements BasicAdapter.BasicAdapterListener, StickerAdapter.StickerAdapterListener {


    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    private static final String TAG = ProjectsFragment.class.getSimpleName();

    public static OptionAddImageFragment newInstance(Bundle args) {
        OptionAddImageFragment dialogSelectVideoSource = new OptionAddImageFragment();
        dialogSelectVideoSource.setArguments(args);
        return dialogSelectVideoSource;
    }
    public ISelectVideoSourceListener callback = null;

    public OptionAddImageFragment() {
    }
    @Override
    public int getLayout() {
        return R.layout.option_image_fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    String video_path;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        video_path = getArguments() != null ? getArguments().getString("video_path", "") : "";

        ImageView btn_close = view.findViewById(R.id.iv_close);
        ImageView btn_done = view.findViewById(R.id.iv_done);

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processingAddImage();
            }
        });


        ArrayList<String> listPos = new ArrayList<>();
        listPos.add("TopLeft");
        listPos.add("TopRight");
        listPos.add("Center");
        listPos.add("BottomLeft");
        listPos.add("BottomRight");
        listPos.add("CenterLeft");
        listPos.add("CenterBottom");
        listPos.add("CenterRight");

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_position);
        BasicAdapter basicAdapter = new BasicAdapter(getContext(), listPos, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerView.setAdapter(basicAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);


        RecyclerView recyclerView2 = view.findViewById(R.id.recycler_view_image);
        StickerAdapter stickerAdapter = new StickerAdapter(getContext(), getListSticker(), this);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerView2.setAdapter(stickerAdapter);
        recyclerView2.setLayoutManager(linearLayoutManager2);

    }

    int[] resourceID = {R.drawable.sticker_1,
                        R.drawable.sticker_2,
                        R.drawable.sticker_3,
                        R.drawable.sticker_4,
                        R.drawable.sticker_5,
                        R.drawable.sticker_6,
                        R.drawable.sticker_7,
                        R.drawable.sticker_8,
                        R.drawable.sticker_9,
                        R.drawable.sticker_10,
                        R.drawable.sticker_11,
                        R.drawable.sticker_12,
                        R.drawable.sticker_13,
                        R.drawable.sticker_14,
                        R.drawable.sticker_15,
                        R.drawable.sticker_16,
                        R.drawable.sticker_17,
                        R.drawable.sticker_18,
                        R.drawable.sticker_19,
    };
    private List<PhotoModel> getListSticker() {

        List<PhotoModel> listSticker;
        listSticker = new ArrayList<>();

        for (int i = 1; i < 20; i ++){
            listSticker.add(new PhotoModel(resourceID[i-1]));
//            copyResourceToFile("sticker_"+i+".png", resourceID[i-1]);
            new OptiUtils().copyFileToInternalStorage(resourceID[i-1], "sticker_"+i, requireContext());
        }
        return listSticker;
    }

    public void copyResourceToFile(String resourceName, int id) {


        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();

        File file = new File(extStorageDirectory, resourceName);
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap bm = BitmapFactory.decodeResource( getResources(), R.drawable.sticker_1);
        bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        bm.recycle();
        try {
            if (outStream != null) {
                outStream.flush();
            }

            if (outStream != null) {
                outStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private void processingAddImage() {

        new VideoUtil().addImage(getActivity(), video_path, "/sdcard/thanh.png", "x=10:y=10",  new VideoUtil.ITranscoding() {
            @Override
            public void onStartTranscoding(String outPath) {
                buildDialog("compression...");
            }

            @Override
            public void onFinishTranscoding(String code) {
                if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
            }

            @Override
            public void onUpdateProgressTranscoding(int progress) {

            }
        });

    }

    private ProgressDialog mProgressDialog;
    private ProgressDialog buildDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(getContext(), "", msg);
        }
        mProgressDialog.setMessage(msg);
        return mProgressDialog;
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


    @Override
    public void onClickBasicItem(String text) {

    }

    @Override
    public void onClickStickerItem(String text) {

    }
}
