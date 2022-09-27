package com.examples.atscreenrecord.ui.fragments;

import static com.examples.atscreenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_FACEBOOK;
import static com.examples.atscreenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_TWITCH;
import static com.examples.atscreenrecord.ui.fragments.LiveStreamingFragment.SOCIAL_TYPE_YOUTUBE;
import static com.examples.atscreenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_CONNECTION_DISCONNECTED;
import static com.examples.atscreenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_CONNECTION_FAILED;
import static com.examples.atscreenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_CONNECTION_STARTED;
import static com.examples.atscreenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_ERROR;
import static com.examples.atscreenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_REQUEST_START;
import static com.examples.atscreenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_REQUEST_STOP;
import static com.examples.atscreenrecord.ui.services.streaming.StreamingService.NOTIFY_MSG_STREAM_STOPPED;
import static com.examples.atscreenrecord.ui.utils.MyUtils.hideSoftInput;
import static com.examples.atscreenrecord.ui.utils.MyUtils.isMyServiceRunning;

import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.examples.atscreenrecord.App;
import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.ui.activities.MainActivity;
import com.examples.atscreenrecord.ui.services.ControllerService;
import com.examples.atscreenrecord.ui.services.recording.RecordingService;
import com.examples.atscreenrecord.ui.services.streaming.StreamingService;
import com.examples.atscreenrecord.ui.utils.MyUtils;
import com.google.android.material.snackbar.Snackbar;
import com.takusemba.rtmppublisher.helper.StreamProfile;

import org.jetbrains.annotations.NotNull;

public class RTMPLiveAddressFragment extends Fragment {

    ImageView imgBack;
    TextView tvStartLiveStream;
    EditText edtRTMPAddress, edtStreamKey;
    TextView tvPasteRTMPAddress, tvPasteStreamKey, tvTutorial;


    String mUrl;
    private StreamingReceiver mStreamReceiver = null;


    private MainActivity mParentActivity = null;
    private App mApplication;
    private FragmentManager mFragmentManager;

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

    private int type;

    public void setSocialType(int type) {
        this.type = type;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View mViewRoot = inflater.inflate(R.layout.fragment_rtmplive_address, container, false);
        return mViewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgBack = view.findViewById(R.id.img_back_rtmp);
        tvStartLiveStream = view.findViewById(R.id.tv_start_livestreaming);
        edtRTMPAddress = view.findViewById(R.id.edt_rtmp_address);
        edtStreamKey = view.findViewById(R.id.edt_stream_key);
        tvPasteRTMPAddress = view.findViewById(R.id.tv_paste_rtmp_address);
        tvPasteStreamKey = view.findViewById(R.id.tv_paste_stream_key);
        tvTutorial = view.findViewById(R.id.tv_tutorial);
        tvStartLiveStream.setAlpha(0.5f);

//        edtRTMPAddress.setText("rtmp://live.skysoft.us/live/");
//        edtStreamKey.setText("test");

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftInput(requireActivity());
                mFragmentManager.popBackStack();

            }
        });
        tvStartLiveStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftInput(requireActivity());
                mParentActivity.mMode = MyUtils.MODE_STREAMING;
                mUrl = fixRTMPAddress(edtRTMPAddress.getText().toString()) + edtStreamKey.getText().toString();
//                if (!MyUtils.isValidStreamUrlFormat(mUrl)) {
//                    MyUtils.showSnackBarNotification(view, "Wrong stream url format (ex: rtmp://127.192.123.1/live/stream)", Snackbar.LENGTH_LONG);
//                    edtRTMPAddress.requestFocus();
//                } else {
                if (isMyServiceRunning(requireContext(), RecordingService.class)) {
                    MyUtils.showSnackBarNotification(view, "You are in RECORDING Mode. Please close Recording controller", Snackbar.LENGTH_LONG);
                    return;
                }
                mParentActivity.mMode = MyUtils.MODE_STREAMING;
                StreamProfile mStreamProfile = new StreamProfile("", mUrl, "");
                mParentActivity.setStreamProfile(mStreamProfile);

                if (isMyServiceRunning(requireContext(), ControllerService.class)) {
                    MyUtils.showSnackBarNotification(view, "Streaming service is running!", Snackbar.LENGTH_LONG);
                    mParentActivity.notifyUpdateStreamProfile();
                } else
                    mParentActivity.shouldStartControllerService();
                tvStartLiveStream.setEnabled(false);
                edtRTMPAddress.setEnabled(false);
                edtStreamKey.setEnabled(false);

                MyUtils.showSnackBarNotification(view, "Stream connected!", Snackbar.LENGTH_LONG);
            }
//            }
        });
        tvPasteStreamKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) mApplication.getSystemService(Context.CLIPBOARD_SERVICE);
                try {
                    CharSequence textToPaste = clipboard.getPrimaryClip().getItemAt(0).getText();
                    if (!textToPaste.equals("")) {
                        edtStreamKey.setText(textToPaste);
                        edtStreamKey.setSelection(textToPaste.length());
                    }
                } catch (Exception e) {
                }
            }
        });
        tvPasteRTMPAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) mApplication.getSystemService(Context.CLIPBOARD_SERVICE);
                try {
                    CharSequence textToPaste = clipboard.getPrimaryClip().getItemAt(0).getText();
                    if (!textToPaste.equals("")) {
                        edtRTMPAddress.setText(textToPaste);
                        edtRTMPAddress.setSelection(textToPaste.length());
                    }
                } catch (Exception e) {
                }
            }
        });

        tvTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftInput(requireActivity());
                Fragment fragment = null;
                if (type == SOCIAL_TYPE_YOUTUBE) fragment = new GuidelineYoutubeLiveStreamingFragment();
                if (type == SOCIAL_TYPE_FACEBOOK) fragment = new GuidelineFacebookLiveStreamingFragment();
                if (type == SOCIAL_TYPE_TWITCH) fragment = new GuidelineTwitchLiveStreamingFragment();
                if (fragment != null) {
                    mFragmentManager.beginTransaction()
                            .replace(R.id.frame_layout_fragment, fragment)
                            .addToBackStack("")
                            .commit();
                }
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
                } else {
                    tvPasteRTMPAddress.setVisibility(View.GONE);
                }
                if (MyUtils.isValidStreamUrlFormat(fixRTMPAddress(charSequence.toString()) + edtStreamKey.getText().toString())) {
                    tvStartLiveStream.setAlpha(1f);
                    tvStartLiveStream.setClickable(true);
                } else {
                    tvStartLiveStream.setAlpha(0.5f);
                    tvStartLiveStream.setClickable(false);
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
                } else {
                    tvPasteStreamKey.setVisibility(View.GONE);
                }
                if (MyUtils.isValidStreamUrlFormat(fixRTMPAddress(edtRTMPAddress.getText().toString()) + charSequence)) {
                    tvStartLiveStream.setAlpha(1f);
                    tvStartLiveStream.setClickable(true);
                } else {
                    tvStartLiveStream.setAlpha(0.5f);
                    tvStartLiveStream.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        LinearLayout rootView = view.findViewById(R.id.root_container);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftInput(requireActivity());
            }
        });
    }

    public String fixRTMPAddress(String rtmp) {
        if (!rtmp.endsWith("/")) return rtmp + "/";
        return rtmp;
    }

    private void registerSyncServiceReceiver() {
        mStreamReceiver = new StreamingReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyUtils.ACTION_NOTIFY_FROM_STREAM_SERVICE);
        mApplication.registerReceiver(mStreamReceiver, intentFilter);
    }


    //Receiver
    private class StreamingReceiver extends BroadcastReceiver {
        private boolean isStarted = false;

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
//            if(DEBUG) Log.i(TAG, "onReceive: "+action);
            if (!TextUtils.isEmpty(action) &&
                    MyUtils.ACTION_NOTIFY_FROM_STREAM_SERVICE.equals(action)) {

                String notify_msg = intent.getStringExtra(StreamingService.KEY_NOTIFY_MSG);
                if (TextUtils.isEmpty(notify_msg))
                    return;
                switch (notify_msg) {
                    case NOTIFY_MSG_CONNECTION_STARTED:
                        edtRTMPAddress.setEnabled(false);
                        edtStreamKey.setEnabled(false);
//                        appendLog("Streaming started");
//                        appendLog("Streaming ...");
                        isStarted = true;
                        break;

                    case NOTIFY_MSG_CONNECTION_FAILED:
//                        appendLog("Connection to server failed");
                        break;

                    case NOTIFY_MSG_CONNECTION_DISCONNECTED:
//                        appendLog("Connection disconnected");
                        break;

                    case NOTIFY_MSG_STREAM_STOPPED:
//                        appendLog("Streaming stopped");
//                        isTested = true;
                        break;

                    case NOTIFY_MSG_ERROR:
//                        appendLog("Sorry, an error occurs!");

                        break;
                    case NOTIFY_MSG_REQUEST_STOP:
//                        appendLog("Requesting stop stream...");
//                        isTested = false;

                        edtRTMPAddress.setEnabled(true);
                        edtStreamKey.setEnabled(true);

                        break;
                    case NOTIFY_MSG_REQUEST_START:
//                        appendLog("Requesting start stream...");

                        new Thread(new Runnable() {
                            int i = 0;

                            @Override
                            public void run() {
                                while (!isStarted) {
                                    if (i > 5000) {
                                        //failed
//                                        appendLog("Cannot stream to server. Try later..");
                                        onStop();
                                        break;
                                    }
                                    try {
                                        i += 1000;
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();

                        break;
                    default:
//                        appendLog(notify_msg);
                }
            }
        }
    }

}