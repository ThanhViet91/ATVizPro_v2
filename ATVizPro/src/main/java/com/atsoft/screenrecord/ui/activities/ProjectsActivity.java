package com.atsoft.screenrecord.ui.activities;

import static com.atsoft.screenrecord.ui.activities.MainActivity.KEY_FROM_FUNCTION;
import static com.atsoft.screenrecord.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.atsoft.screenrecord.ui.activities.MainActivity.KEY_VIDEO_NAME;
import static com.atsoft.screenrecord.ui.activities.MainActivity.REQUEST_SHOW_PROJECTS_DEFAULT;
import static com.atsoft.screenrecord.ui.activities.MainActivity.REQUEST_VIDEO_FOR_COMMENTARY;
import static com.atsoft.screenrecord.ui.activities.MainActivity.REQUEST_VIDEO_FOR_REACT_CAM;
import static com.atsoft.screenrecord.ui.activities.MainActivity.REQUEST_VIDEO_FOR_VIDEO_EDIT;
import static com.atsoft.screenrecord.ui.activities.PopUpResultVideoTranslucentActivity.afterAdd;
import static com.atsoft.screenrecord.ui.activities.PopUpResultVideoTranslucentActivity.afterDelete;
import static com.atsoft.screenrecord.ui.activities.VideoEditorActivity.finishEdit;
import static com.atsoft.screenrecord.ui.services.recording.RecordingService.NOTIFY_MSG_RECORDING_DONE;
import static com.atsoft.screenrecord.ui.utils.MyUtils.ACTION_GO_TO_EDIT;
import static com.atsoft.screenrecord.ui.utils.MyUtils.ACTION_GO_TO_PLAY;
import static com.atsoft.screenrecord.ui.utils.MyUtils.KEY_MESSAGE;
import static com.atsoft.screenrecord.ui.utils.MyUtils.hideStatusBar;
import static com.atsoft.screenrecord.ui.utils.MyUtils.isMyServiceRunning;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.adapter.VideoProjectsAdapter;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;
import com.atsoft.screenrecord.model.VideoModel;
import com.atsoft.screenrecord.ui.fragments.SubscriptionFragment;
import com.atsoft.screenrecord.ui.services.recording.RecordingService;
import com.atsoft.screenrecord.ui.utils.MyUtils;
import com.atsoft.screenrecord.ui.utils.RenameDialogHelper;
import com.atsoft.screenrecord.utils.AdsUtil;
import com.atsoft.screenrecord.utils.DisplayUtil;
import com.atsoft.screenrecord.utils.OnSingleClickListener;
import com.atsoft.screenrecord.utils.StorageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class ProjectsActivity extends AppCompatActivity implements VideoProjectsAdapter.VideoProjectsListener {
    // TODO: Rename parameter arguments, choose names that match
    private TextView tv_cancel, tv_noData, tv_select, tv_selected;
    private ImageView btn_back;
    private ImageView btn_rename;
    public VideoProjectsAdapter mAdapter;
    public ArrayList<VideoModel> videoList = new ArrayList<>();
    public ArrayList<VideoModel> videoList_temp = new ArrayList<>();
    private int fromFunction = 0;
    private AdsUtil mAdManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_video_projects);
        hideStatusBar(this);

        if (getIntent() != null) {
            fromFunction = getIntent().getIntExtra(KEY_FROM_FUNCTION, 0);
            if (getIntent().getAction() != null) {
                selectedVideoPath = getIntent().getStringExtra(KEY_PATH_VIDEO);
                if (ACTION_GO_TO_EDIT.equals(getIntent().getAction())) {
                    if (!requireSubscription(selectedVideoPath)) gotoEditVideo(selectedVideoPath);
                }
                if (ACTION_GO_TO_PLAY.equals(getIntent().getAction())) {
                    gotoPlayVideoDetail(this, selectedVideoPath, ACTION_GO_TO_PLAY);
                }
            }
        }

        fetchData();
        initViews();
    }


    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action) && MyUtils.ACTION_SEND_MESSAGE_FROM_SERVICE.equals(action)) {
                String notify_msg = intent.getStringExtra(KEY_MESSAGE);
                if (TextUtils.isEmpty(notify_msg))
                    return;
                if (NOTIFY_MSG_RECORDING_DONE.equals(notify_msg)) {
                    fetchData();
                }
            }
        }
    };

    private void registerSyncServiceReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyUtils.ACTION_SEND_MESSAGE_FROM_SERVICE);
        registerReceiver(mMessageReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerSyncServiceReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (finishEdit || afterDelete || afterAdd) {
            fetchData();
            finishEdit = false;
            afterDelete = false;
            afterAdd = false;
        }
        if (videoList.size() == 0) {
            toggleView(tv_noData, View.VISIBLE);
        } else {
            toggleView(tv_noData, View.GONE);
        }
        if (videoList.size() == 0 && fromFunction == REQUEST_SHOW_PROJECTS_DEFAULT) {
            toggleView(tv_select, View.GONE);
        }
        mAdManager.loadBanner();
        if (mAdapter != null && mAdapter.getSelectable()) {
            checkNumberSelected();
        }
    }

    public void toggleView(View view, int type) {
        if (view != null) {
            view.setVisibility(type);
        }
    }

    RecyclerView recyclerView;

    private void initViews() {
        RelativeLayout mAdView = findViewById(R.id.adView);
        mAdManager = new AdsUtil(this, mAdView);
        recyclerView = findViewById(R.id.list_videos);
        tv_cancel = findViewById(R.id.tv_btn_cancel_projects);
        tv_noData = findViewById(R.id.tvEmpty);
        tv_select = findViewById(R.id.tv_btn_select);
        btn_back = findViewById(R.id.img_btn_back_header);
        ImageView btn_delete = findViewById(R.id.img_btn_delete);
        btn_rename = findViewById(R.id.img_btn_rename);
        tv_selected = findViewById(R.id.tv_selected);

        toggleView(findViewById(R.id.option), View.GONE);
        tv_select.setText(getString(R.string.select));
        if (fromFunction == REQUEST_SHOW_PROJECTS_DEFAULT) {
            toggleView(tv_cancel, View.GONE);
            toggleView(btn_back, View.VISIBLE);
            toggleView(tv_select, View.VISIBLE);
        } else {
            toggleView(tv_cancel, View.VISIBLE);
            toggleView(btn_back, View.GONE);
            toggleView(tv_select, View.GONE);
        }
        if (ACTION_GO_TO_PLAY.equals(getIntent().getAction())) {
            toggleView(tv_cancel, View.GONE);
            toggleView(btn_back, View.VISIBLE);
        }
        tv_cancel.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (fromFunction == REQUEST_SHOW_PROJECTS_DEFAULT) {
                    for (VideoModel video : videoList) video.setSelected(false);
                    mAdapter.setSelectable(false);
                    toggleView(btn_back, View.VISIBLE);
                    toggleView(tv_cancel, View.GONE);
                    modeSelect = 0;
                    tv_select.setText(getString(R.string.select));
                    toggleView(findViewById(R.id.option), View.GONE);
                } else
                    onBackPressed();
            }
        });
        btn_back.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                onBackPressed();
            }
        });
        tv_select.setOnClickListener(v -> {
            modeSelect++;
            handleSelectButton();
        });
        btn_delete.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                handleDeleteButton();
            }
        });
        btn_rename.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                VideoModel videoRename = getVideoListSelected().get(0);
                handleRenameButton(videoRename);
            }
        });

        mAdapter = new VideoProjectsAdapter(this, videoList);
        mAdapter.setVideoProjectsListener(this);
//        mAdapter.setHasStableIds(true);
        recyclerView.setAdapter(mAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, calculateSpanCount());
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    private int calculateSpanCount() {
        float screenWidth = DisplayUtil.getDeviceWidthDpi();
        return (int) screenWidth / 180 + 1;
    }

    private VideoModel videoModelOld;
    private String oldPath;
    private String oldName;

    private void handleRenameButton(VideoModel oldVideo) {
        videoModelOld = oldVideo;
        new RenameDialogHelper(new RenameDialogHelper.IDialogHelper() {
            @Override
            public void onClickOK(String result) {
                oldPath = videoModelOld.getPath();
                oldName = videoModelOld.getName();
                for (VideoModel videoModel : videoList) {
                    if (videoModel.getPath().equals(videoModelOld.getPath())) {
                        videoModel.setPath(oldPath.replace(oldName, result));
                        videoModel.setName(result);
                        mAdapter.updateData(videoList);
                        break;
                    }
                }
            }

            @Override
            public void onClickCancel(String result) {
            }
        }).showRenameDialog(this, oldVideo);
    }

    private void handleDeleteButton() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Video")
                .setMessage(String.format("Do you want to delete %s", countVideoSelected == 1 ? "video?" : "videos?"))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteVideos(getVideoListSelected()))
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                })
                .show();
    }

    private void deleteVideos(ArrayList<VideoModel> listSelected) {
        for (VideoModel video : listSelected) {
            if (StorageUtil.deleteFile(video.getPath())) videoList.remove(video);
        }

        if (mAdapter != null) {
            mAdapter.updateData(videoList);
        }
        if (videoList.size() == 0) {
            SettingManager2.setNumberEditFile(this, 0);
            SettingManager2.setNumberRecordingFile(this, 0);
            SettingManager2.setNumberReactFile(this, 0);
            SettingManager2.setNumberCommentaryFile(this, 0);
            toggleView(tv_cancel, View.GONE);
            toggleView(btn_back, View.VISIBLE);
            toggleView(tv_select, View.GONE);
            toggleView(tv_noData, View.VISIBLE);
        }
        toggleView(findViewById(R.id.option), View.GONE);

    }

    private int countVideoSelected = 0;

    ArrayList<VideoModel> listSelected = new ArrayList<>();

    private ArrayList<VideoModel> getVideoListSelected() {
        countVideoSelected = 0;
        listSelected.clear();
        for (VideoModel item : videoList)
            if (item.isSelected()) {
                listSelected.add(0, item);
                countVideoSelected++;
            }
        return listSelected;
    }

    private int modeSelect = 0;

    private void handleSelectButton() {
        toggleView(btn_back, View.GONE);
        toggleView(tv_cancel, View.VISIBLE);
        if (modeSelect == 1) {
            tv_select.setText(getString(R.string.select_all));
            mAdapter.setSelectable(true);
        }
        if (modeSelect == 2) {
            for (VideoModel video : videoList) video.setSelected(true);
            mAdapter.updateData(videoList);
            tv_select.setText(getString(R.string.deselect_all));
        }
        if (modeSelect == 3) {
            for (VideoModel video : videoList) video.setSelected(false);
            mAdapter.updateData(videoList);
            tv_select.setText(getString(R.string.select_all));
            modeSelect = 1;
        }
        checkNumberSelected();
    }

    @SuppressLint("DefaultLocale")
    private void checkNumberSelected() {
        if (getVideoListSelected().size() == 0) {
            toggleView(findViewById(R.id.option), View.GONE);
            return;
        }
        toggleView(findViewById(R.id.option), View.VISIBLE);
        toggleView(tv_cancel, View.VISIBLE);
        toggleView(btn_back, View.GONE);
        if (getVideoListSelected().size() == 1) {
            toggleView(btn_rename, View.VISIBLE);
        } else {
            toggleView(btn_rename, View.GONE);
        }
        if (getVideoListSelected().size() == videoList.size()) {
            modeSelect = 2;
            tv_select.setText(getString(R.string.deselect_all));
        } else {
            modeSelect = 1;
            tv_select.setText(getString(R.string.select_all));
        }
        tv_selected.setText(String.format("%d item selected", getVideoListSelected().size()));
    }

    private void fetchData() {
        videoList_temp.clear();
        videoList_temp = new ArrayList<>(videoList);
        videoList.clear();
        isRecording = isMyServiceRunning(getApplicationContext(), RecordingService.class);
        readData();
        processingData();
        if (mAdapter != null) {
            mAdapter.updateData(videoList);
        }
    }

    private void processingData() {
        for (VideoModel videoOld : videoList_temp) {
            for (VideoModel videoNew : videoList) {
                if (videoOld.getPath().equals(videoNew.getPath())) {
                    videoNew.setSelected(videoOld.isSelected());
                    break;
                }
            }
        }
    }

    private int totalVideos = 0;

    private void readData() {
        totalVideos = 0;
        retriever = new MediaMetadataRetriever();
        listFilesForFolder(new File(MyUtils.getBaseStorageDirectory()));
        retriever.release();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && videoList.size() > 1) {
            videoList.sort((t0, t1) -> t0.getLastModified() > t1.getLastModified() ? -1 : 0);
        }
    }

    Boolean isRecording = false;
    MediaMetadataRetriever retriever;
    public boolean isVideo(String path) {
        try {

            retriever.setDataSource(this, Uri.fromFile(new File(path)));
            String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);

            if (hasVideo == null) {
                // file video loi thi xoa
                if (isRecording) {
                    return false;
                }
                StorageUtil.deleteFile(path);
            } else {
                if (hasVideo.equals("yes")) { // neu la file video
                    return true;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    public void listFilesForFolder(final File folder) {
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                String path = fileEntry.getAbsolutePath();
                if (isVideo(path)) {
                    videoList.add(0,
                            new VideoModel(StorageUtil.getFileNameWithoutExtension(path),
                                    path,
                                    MyUtils.getDurationTime(this, path),
                                    fileEntry.lastModified())
                    );
                    totalVideos++;
                }
            }
        }
        if (totalVideos == 0 && fromFunction == REQUEST_SHOW_PROJECTS_DEFAULT) {
            toggleView(tv_cancel, View.GONE);
            toggleView(btn_back, View.VISIBLE);
            toggleView(tv_select, View.GONE);
        }
    }
    String selectedVideoPath = "";
    @Override
    public void onSelected(VideoModel video) {
        if (mAdapter.getSelectable()) {
            checkNumberSelected();
            return;
        }

        if (video != null) {
            selectedVideoPath = video.getPath();
            if (!MyUtils.isVideo(this, selectedVideoPath)) return;

            if (fromFunction != REQUEST_SHOW_PROJECTS_DEFAULT && requireSubscription(video.getPath()))
                return;
            switch (fromFunction) {
                case REQUEST_VIDEO_FOR_REACT_CAM:
                    gotoReactCam(video.getPath());
                    break;
                case REQUEST_VIDEO_FOR_VIDEO_EDIT:
                    gotoEditVideo(video.getPath());
                    break;
                case REQUEST_VIDEO_FOR_COMMENTARY:
                    gotoCommentary(video.getPath());
                    break;
                case REQUEST_SHOW_PROJECTS_DEFAULT:
                default:
                    gotoPlayVideoDetail(this, video.getPath(), "");
            }
        }
    }

    public boolean requireSubscription(String path) {
        if (!SettingManager2.isProApp(this) && MyUtils.getDurationMs(this, path) > 60000) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.frame_layout_fragment, new SubscriptionFragment(() -> runOnUiThread(this::continueTask)))
                    .addToBackStack("")
                    .commit();
            return true;
        }
        return false;
    }

    private void continueTask() {
        if (mAdManager != null) mAdManager.loadBanner();
        if (selectedVideoPath.equals("")) return;
        switch (fromFunction) {
            case REQUEST_VIDEO_FOR_REACT_CAM:
                gotoReactCam(selectedVideoPath);
                break;
            case REQUEST_VIDEO_FOR_VIDEO_EDIT:
                gotoEditVideo(selectedVideoPath);
                break;
            case REQUEST_VIDEO_FOR_COMMENTARY:
                gotoCommentary(selectedVideoPath);
                break;
        }
    }

    public void gotoEditVideo(String path) {
        Intent intent = new Intent(this, VideoEditorActivity.class);
        intent.setAction(MyUtils.ACTION_FOR_EDIT);
        intent.putExtra(KEY_PATH_VIDEO, path);
        startActivity(intent);
    }

    private void gotoCommentary(String path) {
        Intent intent = new Intent(this, CommentaryActivity.class);
        intent.putExtra(KEY_PATH_VIDEO, path);
        startActivity(intent);
    }

    private void gotoReactCam(String path) {
        Intent intent = new Intent(this, ReactCamActivity.class);
        intent.putExtra(KEY_PATH_VIDEO, path);
        startActivity(intent);
    }

    public static void gotoPlayVideoDetail(Context context, String path, String action) {
        if (path.equals("")) return;
        String name = StorageUtil.getFileNameWithoutExtension(path);
        Intent intent = new Intent(context, PlayVideoDetailActivity.class);
        intent.setAction(action);
        intent.putExtra(KEY_PATH_VIDEO, path);
        intent.putExtra(KEY_VIDEO_NAME, name);
        context.startActivity(intent);
    }

//    public void delete(SecurityException e, ActivityResultLauncher<IntentSenderRequest> launcher, Uri uri, ContentResolver contentResolver) {
//        PendingIntent pendingIntent = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            ArrayList<Uri> collection = new ArrayList<>();
//            collection.add(uri);
//            pendingIntent = MediaStore.createDeleteRequest(contentResolver, collection);
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            //if exception is recoverable then again send delete request using intent
//            if (e instanceof RecoverableSecurityException) {
//                RecoverableSecurityException exception = (RecoverableSecurityException) e;
//                pendingIntent = exception.getUserAction().getActionIntent();
//            }
//        }
//        if (pendingIntent != null) {
//            IntentSender sender = pendingIntent.getIntentSender();
//            IntentSenderRequest request = new IntentSenderRequest.Builder(sender).build();
//            launcher.launch(request);
//        }
//    }
//
//    public void rename(Uri uri, String rename) {
//        //create content values with new name and update
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, rename);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            ContentResolver contentResolver = getApplicationContext().getContentResolver();
//            contentResolver.update(uri, contentValues, null);
//        }
//    }
}
