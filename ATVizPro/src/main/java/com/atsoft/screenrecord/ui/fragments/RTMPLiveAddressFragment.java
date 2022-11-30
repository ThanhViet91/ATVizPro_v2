package com.atsoft.screenrecord.ui.fragments;

import static com.atsoft.screenrecord.Core.isConnected;
import static com.atsoft.screenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_FACEBOOK;
import static com.atsoft.screenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_TWITCH;
import static com.atsoft.screenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_YOUTUBE;
import static com.atsoft.screenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_CONNECTED;
import static com.atsoft.screenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_CONNECTION_FAILED;
import static com.atsoft.screenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_CONNECTION_STARTED;
import static com.atsoft.screenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_ERROR;
import static com.atsoft.screenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_STREAM_STOPPED;
import static com.atsoft.screenrecord.ui.utils.MyUtils.KEY_MESSAGE;
import static com.atsoft.screenrecord.ui.utils.MyUtils.hideSoftInput;
import static com.atsoft.screenrecord.ui.utils.MyUtils.isMyServiceRunning;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.atsoft.screenrecord.App;
import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;
import com.atsoft.screenrecord.ui.activities.MainActivity;
import com.atsoft.screenrecord.ui.services.ControllerService;
import com.atsoft.screenrecord.ui.services.recording.RecordingService;
import com.atsoft.screenrecord.ui.utils.MyUtils;
import com.atsoft.screenrecord.utils.AdsUtil;
import com.google.android.material.snackbar.Snackbar;
import com.takusemba.rtmppublisher.helper.StreamProfile;

import org.jetbrains.annotations.NotNull;

public class RTMPLiveAddressFragment extends Fragment {

    private ImageView btnClearRTMP;
    private ImageView btnClearStreamKey;
    private EditText edtRTMPAddress, edtStreamKey;
    private TextView tvStartLiveStream;
    private TextView tvPasteRTMPAddress;
    private TextView tvPasteStreamKey;
    private StreamingReceiver mStreamReceiver = null;
    private MainActivity mParentActivity = null;
    private App mApplication;
    private FragmentManager mFragmentManager;
    private boolean isGoToHome = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mParentActivity = (MainActivity) context;
        this.mApplication = (App) context.getApplicationContext();
        mFragmentManager = getParentFragmentManager();
        if (mStreamReceiver == null) {
            registerSyncServiceReceiver();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unRegisterSyncServiceReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isGoToHome) {
            mParentActivity.removeAllFragment();
            isGoToHome = false;
        }
    }

    private void unRegisterSyncServiceReceiver() {
        if (mStreamReceiver != null) {
            mApplication.unregisterReceiver(mStreamReceiver);
        }
    }

    private int type;
    private String mRTMP = "", mStreamKey = "";

    public void setSocialType(int type) {
        this.type = type;
    }

    View mViewRoot;
    private AdsUtil mAdManager;
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mViewRoot != null) return mViewRoot;
        mViewRoot = inflater.inflate(R.layout.fragment_rtmplive_address, container, false);
        RelativeLayout mAdview = mViewRoot.findViewById(R.id.adView);
        mAdManager = new AdsUtil(getContext(), mAdview);
        return mViewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mAdManager != null) mAdManager.loadBanner();
        ImageView imgBack = view.findViewById(R.id.img_back_rtmp);
        btnClearRTMP = view.findViewById(R.id.img_clear_rtmp);
        btnClearStreamKey = view.findViewById(R.id.img_clear_stream_key);
        tvStartLiveStream = view.findViewById(R.id.tv_start_livestreaming);
        edtRTMPAddress = view.findViewById(R.id.edt_rtmp_address);
        edtStreamKey = view.findViewById(R.id.edt_stream_key);
        tvPasteRTMPAddress = view.findViewById(R.id.tv_paste_rtmp_address);
        tvPasteStreamKey = view.findViewById(R.id.tv_paste_stream_key);
        TextView tvTutorial = view.findViewById(R.id.tv_tutorial);
        checkRestoreData();

//        edtRTMPAddress.setText("rtmp://live.skysoft.us/live/");
//        edtStreamKey.setText("test");

        imgBack.setOnClickListener(v -> {
            hideSoftInput(requireActivity());
            new Handler().postDelayed(() -> mFragmentManager.popBackStack(), 600);
        });

        tvStartLiveStream.setOnClickListener(v -> {
            hideSoftInput(requireActivity());
            saveData(edtRTMPAddress.getText().toString(), edtStreamKey.getText().toString());
            if (!SettingManager2.isProApp(requireContext())) {
                mFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout_fragment, new SubscriptionFragment(this::doStartAlready))
                        .addToBackStack("")
                        .commit();
                return;
            }
            doStartAlready();
        });

        tvPasteStreamKey.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) mApplication.getSystemService(Context.CLIPBOARD_SERVICE);
            try {
                CharSequence textToPaste = clipboard.getPrimaryClip().getItemAt(0).getText();
                if (!textToPaste.equals("")) {
                    edtStreamKey.setText(textToPaste);
                    edtStreamKey.requestFocus();
                    edtStreamKey.setSelection(textToPaste.length());
                }
            } catch (Exception ignored) {
            }
        });
        tvPasteRTMPAddress.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) mApplication.getSystemService(Context.CLIPBOARD_SERVICE);
            try {
                CharSequence textToPaste = clipboard.getPrimaryClip().getItemAt(0).getText();
                if (!textToPaste.equals("")) {
                    edtRTMPAddress.setText(textToPaste);
                    edtRTMPAddress.requestFocus();
                    edtRTMPAddress.setSelection(textToPaste.length());
                }
            } catch (Exception ignored) {
            }
        });
        tvTutorial.setOnClickListener(v -> {
            hideSoftInput(requireActivity());
            Fragment fragment = null;
            if (type == SOCIAL_TYPE_YOUTUBE) fragment = new GuidelineYoutubeLiveStreamingFragment();
            if (type == SOCIAL_TYPE_FACEBOOK)
                fragment = new GuidelineFacebookLiveStreamingFragment();
            if (type == SOCIAL_TYPE_TWITCH) fragment = new GuidelineTwitchLiveStreamingFragment();
            if (fragment != null) {
                mFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout_fragment, fragment)
                        .addToBackStack("")
                        .commit();
            }
        });
        edtRTMPAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    tvPasteRTMPAddress.setVisibility(View.VISIBLE);
                    btnClearRTMP.setVisibility(View.GONE);
                } else {
                    tvPasteRTMPAddress.setVisibility(View.GONE);
                    btnClearRTMP.setVisibility(View.VISIBLE);
                }
                if (MyUtils.isValidStreamUrlFormat(fixRTMPAddress(charSequence.toString()) + edtStreamKey.getText().toString())) {
                    tvStartLiveStream.setAlpha(1f);
                    tvStartLiveStream.setEnabled(true);
                } else {
                    tvStartLiveStream.setAlpha(0.5f);
                    tvStartLiveStream.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        edtStreamKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    tvPasteStreamKey.setVisibility(View.VISIBLE);
                    btnClearStreamKey.setVisibility(View.GONE);
                } else {
                    tvPasteStreamKey.setVisibility(View.GONE);
                    btnClearStreamKey.setVisibility(View.VISIBLE);
                }
                if (MyUtils.isValidStreamUrlFormat(fixRTMPAddress(edtRTMPAddress.getText().toString()) + charSequence)) {
                    tvStartLiveStream.setAlpha(1f);
                    tvStartLiveStream.setEnabled(true);
                } else {
                    tvStartLiveStream.setAlpha(0.5f);
                    tvStartLiveStream.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        btnClearRTMP.setOnClickListener(v -> {
            edtRTMPAddress.setText("");
            edtRTMPAddress.requestFocus();
        });
        btnClearStreamKey.setOnClickListener(v -> {
            edtStreamKey.setText("");
            edtStreamKey.requestFocus();
        });
        RelativeLayout rootView = view.findViewById(R.id.root_container);
        rootView.setOnClickListener(view1 -> hideSoftInput(requireActivity()));
    }

    public void doStartAlready() {
        mParentActivity.mMode = MyUtils.MODE_STREAMING;
        String mUrl = fixRTMPAddress(edtRTMPAddress.getText().toString()) + edtStreamKey.getText().toString();
        if (isMyServiceRunning(requireContext(), RecordingService.class)) {
            MyUtils.showSnackBarNotification(edtRTMPAddress, "You are in RECORDING Mode. Please close Recording controller", Snackbar.LENGTH_LONG);
            return;
        }
        StreamProfile mStreamProfile = new StreamProfile("", mUrl, "");
        mParentActivity.setStreamProfile(mStreamProfile);

        if (isMyServiceRunning(requireContext(), ControllerService.class)) {
            if (!isConnected && SettingManager2.getLiveStreamType(requireContext()) == type) {
                saveData(edtRTMPAddress.getText().toString(), edtStreamKey.getText().toString());
                mParentActivity.notifyUpdateStreamProfile(mUrl);
            } else {
                MyUtils.showSnackBarNotification(edtRTMPAddress, String.format("Livestream on %s is running!",
                        parseType(SettingManager2.getLiveStreamType(requireContext()))), Snackbar.LENGTH_SHORT);
            }
        } else {
            saveData(edtRTMPAddress.getText().toString(), edtStreamKey.getText().toString());
            mParentActivity.shouldStartControllerService();
        }
    }

    private String parseType(int type) {
        if (type == 2) return "Facebook";
        if (type == 3) return "Twitch";
        return "Youtube";
    }

    private ProgressDialog mProgressDialog;

    private void buildDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(getContext(), "", "Connecting...");
        }
        mProgressDialog.setMessage("Connecting...");
    }

    private void saveData(String rtmp, String streamkey) {
        if (type == SOCIAL_TYPE_YOUTUBE) {
            SettingManager2.setRTMPYoutube(requireContext(), rtmp);
            SettingManager2.setStreamKeyYoutube(requireContext(), streamkey);
        }
        if (type == SOCIAL_TYPE_FACEBOOK) {
            SettingManager2.setRTMPFacebook(requireContext(), rtmp);
            SettingManager2.setStreamKeyFacebook(requireContext(), streamkey);
        }
        if (type == SOCIAL_TYPE_TWITCH) {
            SettingManager2.setRTMPTwitch(requireContext(), rtmp);
            SettingManager2.setStreamKeyTwitch(requireContext(), streamkey);
        }
    }

    public void fillData(String mRTMP, String mStreamKey) {
        if (mRTMP.equals("") || mStreamKey.equals("")) {
            tvStartLiveStream.setAlpha(0.5f);
            tvStartLiveStream.setEnabled(false);
            edtRTMPAddress.setText("");
            edtStreamKey.setText("");
            tvPasteRTMPAddress.setVisibility(View.VISIBLE);
            btnClearRTMP.setVisibility(View.GONE);
            tvPasteStreamKey.setVisibility(View.VISIBLE);
            btnClearStreamKey.setVisibility(View.GONE);
            edtRTMPAddress.requestFocus();
        } else {
            edtRTMPAddress.setText(mRTMP);
            edtStreamKey.setText(mStreamKey);
            tvPasteRTMPAddress.setVisibility(View.GONE);
            btnClearRTMP.setVisibility(View.VISIBLE);
            tvPasteStreamKey.setVisibility(View.GONE);
            btnClearStreamKey.setVisibility(View.VISIBLE);
            tvStartLiveStream.setAlpha(1f);
            tvStartLiveStream.setEnabled(true);
        }
        if (isMyServiceRunning(requireContext(), ControllerService.class)) {
            if (isConnected && SettingManager2.getLiveStreamType(requireContext()) == type) {
                edtRTMPAddress.setEnabled(false);
                edtStreamKey.setEnabled(false);
                btnClearRTMP.setEnabled(false);
                btnClearRTMP.setVisibility(View.GONE);
                btnClearStreamKey.setVisibility(View.GONE);
                btnClearStreamKey.setEnabled(false);
                tvStartLiveStream.setEnabled(false);
            }
        }
    }

    private void checkRestoreData() {
        if (type == SOCIAL_TYPE_YOUTUBE) {
            mRTMP = SettingManager2.getRTMPYoutube(requireContext());
            mStreamKey = SettingManager2.getStreamKeyYoutube(requireContext());
        }
        if (type == SOCIAL_TYPE_FACEBOOK) {
            mRTMP = SettingManager2.getRTMPFacebook(requireContext());
            mStreamKey = SettingManager2.getStreamKeyFacebook(requireContext());
        }
        if (type == SOCIAL_TYPE_TWITCH) {
            mRTMP = SettingManager2.getRTMPTwitch(requireContext());
            mStreamKey = SettingManager2.getStreamKeyTwitch(requireContext());
        }
        fillData(mRTMP, mStreamKey);
    }

    public String fixRTMPAddress(String rtmp) {
        if (!rtmp.endsWith("/")) return rtmp + "/";
        return rtmp;
    }

    private void registerSyncServiceReceiver() {
        mStreamReceiver = new StreamingReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyUtils.ACTION_SEND_MESSAGE_FROM_SERVICE);
        mApplication.registerReceiver(mStreamReceiver, intentFilter);
    }


    //Receiver
    private class StreamingReceiver extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (!TextUtils.isEmpty(action) &&
                    MyUtils.ACTION_SEND_MESSAGE_FROM_SERVICE.equals(action)) {

                String notify_msg = intent.getStringExtra(KEY_MESSAGE);
                if (TextUtils.isEmpty(notify_msg))
                    return;
                switch (notify_msg) {
                    case NOTIFY_MSG_CONNECTION_STARTED:
                        buildDialog();
                        tvStartLiveStream.setEnabled(false);
                        edtRTMPAddress.setEnabled(false);
                        edtStreamKey.setEnabled(false);
                        btnClearRTMP.setVisibility(View.GONE);
                        btnClearStreamKey.setVisibility(View.GONE);
                        btnClearRTMP.setEnabled(false);
                        btnClearStreamKey.setEnabled(false);
                        break;
                    case NOTIFY_MSG_CONNECTED:
                        if (mProgressDialog != null) mProgressDialog.dismiss();
                        isGoToHome = true;
                        mParentActivity.removeAllFragment();
//                        mParentActivity.updateUILivestreamHome(false);
                        mParentActivity.updateService();
                        break;

                    case NOTIFY_MSG_CONNECTION_FAILED:
                        if (mProgressDialog != null) mProgressDialog.dismiss();
                        edtRTMPAddress.setEnabled(true);
                        edtStreamKey.setEnabled(true);
                        btnClearRTMP.setVisibility(View.VISIBLE);
                        btnClearStreamKey.setVisibility(View.VISIBLE);
                        btnClearRTMP.setEnabled(true);
                        btnClearStreamKey.setEnabled(true);
                        tvStartLiveStream.setEnabled(true);
                        edtRTMPAddress.requestFocus();
                        edtRTMPAddress.setSelection(edtRTMPAddress.getText().length());
                        break;

                    case NOTIFY_MSG_STREAM_STOPPED:
                    case NOTIFY_MSG_ERROR:
                        break;

                    default:
                }
            }
        }
    }

}