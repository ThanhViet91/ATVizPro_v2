package com.examples.atscreenrecord_test.ui.activities;

import static com.examples.atscreenrecord_test.ui.activities.MainActivity.KEY_FROM_FUNCTION;
import static com.examples.atscreenrecord_test.ui.activities.MainActivity.KEY_PATH_VIDEO;
import static com.examples.atscreenrecord_test.ui.activities.MainActivity.KEY_VIDEO_NAME;
import static com.examples.atscreenrecord_test.ui.activities.MainActivity.REQUEST_SHOW_PROJECTS_DEFAULT;
import static com.examples.atscreenrecord_test.ui.activities.MainActivity.REQUEST_VIDEO_FOR_COMMENTARY;
import static com.examples.atscreenrecord_test.ui.activities.MainActivity.REQUEST_VIDEO_FOR_REACT_CAM;
import static com.examples.atscreenrecord_test.ui.activities.MainActivity.REQUEST_VIDEO_FOR_VIDEO_EDIT;
import static com.examples.atscreenrecord_test.ui.activities.PrepareVideoActivity.VIDEO_PATH_KEY;
import static com.examples.atscreenrecord_test.ui.utils.MyUtils.ACTION_FOR_COMMENTARY;
import static com.examples.atscreenrecord_test.ui.utils.MyUtils.ACTION_FOR_REACT;
import static com.examples.atscreenrecord_test.ui.utils.MyUtils.ACTION_GO_TO_EDIT;
import static com.examples.atscreenrecord_test.ui.utils.MyUtils.ACTION_GO_TO_PLAY;
import static com.examples.atscreenrecord_test.ui.utils.MyUtils.hideStatusBar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.examples.atscreenrecord_test.R;
import com.examples.atscreenrecord_test.adapter.VideoProjectsAdapter;
import com.examples.atscreenrecord_test.controllers.settings.SettingManager2;
import com.examples.atscreenrecord_test.model.VideoModel;
import com.examples.atscreenrecord_test.ui.fragments.SubscriptionFragment;
import com.examples.atscreenrecord_test.ui.utils.DialogHelper;
import com.examples.atscreenrecord_test.ui.utils.MyUtils;
import com.examples.atscreenrecord_test.utils.AdsUtil;
import com.examples.atscreenrecord_test.utils.DisplayUtil;
import com.examples.atscreenrecord_test.utils.OnSingleClickListener;
import com.examples.atscreenrecord_test.utils.StorageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class ProjectsActivity extends AppCompatActivity implements VideoProjectsAdapter.VideoProjectsListener {
    // TODO: Rename parameter arguments, choose names that match
    private TextView tv_cancel, tv_noData, tv_select, tv_selected;
    private ImageView btn_back;
    private ImageView btn_rename;
    private VideoProjectsAdapter mAdapter;
    private final ArrayList<VideoModel> videoList = new ArrayList<>();
    private ArrayList<VideoModel> videoList_temp = new ArrayList<>();
    private int fromFunction = 0;
    private String navigateTo = "";
    String videoPath, videoName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_video_projects);
        hideStatusBar(this);
        if (getIntent() != null) {
            fromFunction = getIntent().getIntExtra(KEY_FROM_FUNCTION, 0);

            if (getIntent().getAction() != null) {
                videoPath = getIntent().getStringExtra(KEY_PATH_VIDEO);
                if (ACTION_GO_TO_EDIT.equals(getIntent().getAction())) {
                    if (requireSubscription(videoPath)) return;
                    gotoEditVideo(videoPath);
                }
                if (ACTION_GO_TO_PLAY.equals(getIntent().getAction())) {
                    gotoPlayVideoDetail(this, videoPath);
                }
            }

        }
        fetchData();
        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoList.size() == 0) {
            toggleView(tv_noData, View.VISIBLE);
        } else {
            toggleView(tv_noData, View.GONE);
        }
        RelativeLayout mAdView = findViewById(R.id.adView);
        AdsUtil mAdManager = new AdsUtil(this, mAdView);
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

    private void initViews() {
        RecyclerView recyclerView = findViewById(R.id.list_videos);
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
        } else {
            toggleView(tv_cancel, View.VISIBLE);
            toggleView(btn_back, View.GONE);
            toggleView(tv_select, View.GONE);
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
                handleRenameButton(getVideoListSelected().get(0));
            }
        });

        if (totalVideos == 0 && fromFunction == REQUEST_SHOW_PROJECTS_DEFAULT) {
            toggleView(tv_cancel, View.GONE);
            toggleView(btn_back, View.VISIBLE);
            toggleView(tv_select, View.GONE);
        }
        mAdapter = new VideoProjectsAdapter(this, videoList);
        mAdapter.setVideoProjectsListener(this);
        mAdapter.setHasStableIds(true);

        recyclerView.setAdapter(mAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, calculateSpanCount());
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    private int calculateSpanCount() {
        float screenWidth = DisplayUtil.getDeviceWidthDpi();
        return (int) screenWidth / 180 + 1;
    }

    private VideoModel videoModelOld;

    private void handleRenameButton(VideoModel oldVideo) {
        videoModelOld = new VideoModel();
        videoModelOld.setPath(oldVideo.getPath());
        videoModelOld.setName(oldVideo.getName());
        videoModelOld.setSelected(oldVideo.isSelected());
        videoModelOld.setDuration(oldVideo.getDuration());
        String oldPath = videoModelOld.getPath();
        String oldName = videoModelOld.getName();
        DialogHelper.getInstance(new DialogHelper.IDialogHelper() {
            @Override
            public void onClickOK(String result) {
                new Handler().postDelayed(() -> {
                    for (VideoModel videoModel : videoList)
                        if (videoModel.getCompare().equals(videoModelOld.getCompare())) {
                            videoModel.setPath(oldPath.replace(oldName, result));
                            videoModel.setName(result);
                            mAdapter.updateData(videoList);
                            break;
                        }
                }, 200);

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
                .show();
    }

    private void deleteVideos(ArrayList<VideoModel> listSelected) {
        for (VideoModel video : listSelected) {
            if (StorageUtil.deleteFile(video.getPath())) videoList.remove(video);
        }
        runOnUiThread(() -> {
            if (mAdapter != null) mAdapter.updateData(videoList);
        });
    }

    private int countVideoSelected = 0;

    private ArrayList<VideoModel> getVideoListSelected() {
        countVideoSelected = 0;
        ArrayList<VideoModel> list = new ArrayList<>();
        for (VideoModel item : videoList)
            if (item.isSelected()) {
                list.add(0, item);
                countVideoSelected++;
            }
        return list;
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
        readData();
        processingData();
        if (mAdapter != null) mAdapter.updateData(videoList);
    }

    private void processingData() {
        for (VideoModel videoOld : videoList_temp) {
            for (VideoModel videoNew : videoList) {
                if (videoOld.getCompare().equals(videoNew.getCompare())) {
                    videoNew.setSelected(videoOld.isSelected());
                    break;
                }
            }
        }
    }

    private int totalVideos = 0;

    private void readData() {
        totalVideos = 0;
        listFilesForFolder(new File(MyUtils.getBaseStorageDirectory()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && videoList.size() > 1) {
            videoList.sort((t0, t1) -> t0.getLastModified() > t1.getLastModified() ? -1 : 0);
        }
    }

    public void listFilesForFolder(final File folder) {
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                if (fileEntry.getName().contains(".mp4"))
                    videoList.add(0,
                            new VideoModel(fileEntry.getName().replace(".mp4", ""),
                                    fileEntry.getAbsolutePath(),
                                    MyUtils.getDurationTime(this, fileEntry.getAbsolutePath()),
                                    fileEntry.lastModified())
                    );
                totalVideos++;
            }
        }
    }

    @Override
    public void onSelected(VideoModel video) {
        if (video == null) {
            if (fromFunction == REQUEST_SHOW_PROJECTS_DEFAULT) {
                mAdapter.setSelectable(true);
            } else {
                return;
            }
        }
        if (mAdapter.getSelectable()) {
            checkNumberSelected();
            return;
        }

        if (video != null) {
            if (fromFunction != REQUEST_SHOW_PROJECTS_DEFAULT && requireSubscription(video.getPath())) return;
            switch (fromFunction) {
                case REQUEST_VIDEO_FOR_REACT_CAM:
                    gotoPrepareVideo(video.getPath(), ACTION_FOR_REACT);
                    break;
                case REQUEST_VIDEO_FOR_VIDEO_EDIT:
                    gotoEditVideo(video.getPath());
                    break;
                case REQUEST_VIDEO_FOR_COMMENTARY:
                    gotoPrepareVideo(video.getPath(), ACTION_FOR_COMMENTARY);
                    break;
                case REQUEST_SHOW_PROJECTS_DEFAULT:
                default:
                    gotoPlayVideoDetail(this, video.getPath());
            }
        }
    }

    public boolean requireSubscription(String path) {
        if ( !SettingManager2.isProApp(this) && MyUtils.getDurationMs(this, path) > 60000){
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.frame_layout_fragment, new SubscriptionFragment())
                    .addToBackStack("")
                    .commit();
            return true;
        }
        return false;
    }

    public void gotoEditVideo(String path) {
        Intent intent = new Intent(this, VideoEditorActivity.class);
        intent.setAction(MyUtils.ACTION_FOR_EDIT);
        intent.putExtra(VIDEO_PATH_KEY, path);
        startActivity(intent);
    }

    public void gotoPrepareVideo(String path, String action) {
        Intent intent = new Intent(this, PrepareVideoActivity.class);
        intent.setAction(action);
        intent.putExtra(VIDEO_PATH_KEY, path);
        startActivity(intent);
    }

    public static void gotoPlayVideoDetail(Context context, String path) {
        if (path.equals("")) return;
        String name = new File(path).getName().replace(".mp4", "");
        Intent intent = new Intent(context, PlayVideoDetailActivity.class);
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
