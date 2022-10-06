package com.examples.atscreenrecord.ui.activities;

import static com.examples.atscreenrecord.ui.activities.MainActivity.REQUEST_SHOW_PROJECTS_DEFAULT;
import static com.examples.atscreenrecord.ui.activities.MainActivity.REQUEST_VIDEO_FOR_COMMENTARY;
import static com.examples.atscreenrecord.ui.activities.MainActivity.REQUEST_VIDEO_FOR_REACT_CAM;
import static com.examples.atscreenrecord.ui.activities.MainActivity.REQUEST_VIDEO_FOR_VIDEO_EDIT;
import static com.examples.atscreenrecord.ui.utils.MyUtils.hideStatusBar;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.examples.atscreenrecord.BuildConfig;
import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.adapter.VideoProjectsAdapter;
import com.examples.atscreenrecord.model.VideoModel;
import com.examples.atscreenrecord.ui.utils.DialogHelper;
import com.examples.atscreenrecord.ui.utils.MyUtils;
import com.examples.atscreenrecord.utils.AdUtil;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;

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
    private int from_code = 0;
    final static int APP_STORAGE_ACCESS_REQUEST_CODE = 501; // Any value

    private RelativeLayout rootView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_video_projects);
        rootView = findViewById(R.id.root_container);
        hideStatusBar(this);
        if (getIntent() != null) from_code = getIntent().getIntExtra("key_from_code", 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
            } else {
                initViews();
            }
        } else {
            initViews();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == APP_STORAGE_ACCESS_REQUEST_CODE)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager())
                {
                    // Permission granted. Now resume your workflow.
                    MyUtils.showSnackBarNotification(rootView, "Please grant all permissions to access files.", Snackbar.LENGTH_LONG);
                }
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadData();
        System.out.println("thanhlv onResumekkkkkkkkkkkkk");
        if (videoList.size() == 0) {
            toggleView(tv_noData, View.VISIBLE);
        } else {
            toggleView(tv_noData, View.GONE);
        }
        AdView mAdView = findViewById(R.id.adView);
        AdUtil.createBannerAdmob(this, mAdView);
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
        if (from_code == REQUEST_SHOW_PROJECTS_DEFAULT) {
            toggleView(tv_cancel, View.GONE);
            toggleView(btn_back, View.VISIBLE);
        } else {
            toggleView(tv_cancel, View.VISIBLE);
            toggleView(btn_back, View.GONE);
            toggleView(tv_select, View.GONE);
        }
        tv_cancel.setOnClickListener(view -> {
            if (from_code == REQUEST_SHOW_PROJECTS_DEFAULT) {
                for (VideoModel video : videoList) video.setSelected(false);
                mAdapter.setSelectable(false);
                toggleView(btn_back, View.VISIBLE);
                toggleView(tv_cancel, View.GONE);
                modeSelect = 0;
                tv_select.setText(getString(R.string.select));
                toggleView(findViewById(R.id.option), View.GONE);
            } else
                onBackPressed();
        });
        btn_back.setOnClickListener(view -> onBackPressed());
        tv_select.setOnClickListener(view -> {
            modeSelect++;
            handleSelectButton();
        });
        btn_delete.setOnClickListener(view -> handleDeleteButton());
        btn_rename.setOnClickListener(view -> handleRenameButton(getVideoListSelected().get(0)));
        final SwipeRefreshLayout srl = findViewById(R.id.swipeLayout);
        srl.setOnRefreshListener(() -> {
            reloadData();
            srl.setRefreshing(false);
        });
        readData();
        mAdapter = new VideoProjectsAdapter(this, videoList);
        mAdapter.setVideoProjectsListener(this);
        recyclerView.setAdapter(mAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
    }
    VideoModel videoModelOld;
    private void handleRenameButton(VideoModel oldVideo) {
        videoModelOld = new VideoModel();
        videoModelOld.setPath(oldVideo.getPath());
        videoModelOld.setId(oldVideo.getId());
        videoModelOld.setName(oldVideo.getName());
        videoModelOld.setSelected(oldVideo.isSelected());
        videoModelOld.setDuration(oldVideo.getDuration());
        System.out.println("thanhlv rename file " + videoModelOld.getPath() + " /// " + videoModelOld.getName());
        String oldPath = videoModelOld.getPath();
        String oldName = videoModelOld.getName();
        DialogHelper.getInstance(new DialogHelper.IDialogHelper() {
            @Override
            public void onClickOK(String result) {
//                File file = new File(oldVideo.getPath());
//                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{file.getName()},
//                        (s, uri)  -> {
//                            rename(uri, result);
//                        });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (VideoModel videoModel : videoList)
                            if (videoModel.getCompare().equals(videoModelOld.getCompare())) {
                                System.out.println("thanhlv replace "+videoModelOld.getPath());
                                videoModel.setPath(oldPath.replace(oldName, result));
                                videoModel.setName(result);
                                System.out.println("thanhlv replace "+videoModel.getPath());
                                mAdapter.updateData(videoList);
                                break;
                            }
                        for (VideoModel videoModel : videoList)
                            System.out.println("thanhlv lisssss = " + videoModel.getPath());
                    }
                }, 1000);

            }
            @Override
            public void onClickCancel(String result) {}
        }).showRenameDialog(this, oldVideo);
    }

    private void handleDeleteButton() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Video")
                .setMessage("Are you sure you want to delete video?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteVideos(getVideoListSelected()))
                .show();
    }

    public void deleteVideos(ArrayList<VideoModel> listSelected) {
        for (VideoModel video : listSelected) {
            File file = new File(video.getPath());
            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{file.getName()},
                    (s, uri) -> {
                        ContentResolver contentResolver = getApplicationContext().getContentResolver();
                        try {
                            //delete object using resolver
                            contentResolver.delete(uri, null, null);
                            videoList.remove(video);
                            runOnUiThread(() -> mAdapter.updateData(videoList));
                            Toast.makeText(getApplicationContext(), "The video is deleted!", Toast.LENGTH_SHORT).show();
                        } catch (SecurityException e) {
                            delete(e, loginResultHandler, uri, contentResolver);
                        }
                    });
        }
    }

    public ArrayList<VideoModel> getVideoListSelected() {
        ArrayList<VideoModel> list = new ArrayList<>();
        for (VideoModel item : videoList)
            if (item.isSelected()) list.add(0, item);
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

    private void reloadData() {
        videoList_temp.clear();
        videoList_temp = new ArrayList<>(videoList);
        videoList.clear();
        readData();
        processingData();
        mAdapter.updateData(videoList);
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

    private void readData() {
        listFilesForFolder(new File(MyUtils.getBaseStorageDirectory()));
    }

    public void listFilesForFolder(final File folder) {
        int i = 0;
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                if (fileEntry.getName().contains(".mp4"))
                    videoList.add(0, new VideoModel(i, fileEntry.getName().replace(".mp4", ""),
                            fileEntry.getAbsolutePath(), getDuration(fileEntry.getAbsolutePath())));
                i++;
            }
        }
    }

    public String getDuration(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, Uri.parse(path));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        retriever.release();
        long timeInMs = Long.parseLong(time);
        return parseLongToTime(timeInMs);
    }

    @SuppressLint("DefaultLocale")
    public String parseLongToTime(long durationInMillis) {
        long second = (durationInMillis / 1000) % 60;
        long minute = (durationInMillis / (1000 * 60)) % 60;
        long hour = (durationInMillis / (1000 * 60 * 60)) % 24;
        if (hour == 0) return String.format("%02d:%02d", minute, second);
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    @Override
    public void onSelected(String path) {
        if (path.equals("longClick")) {
            if (from_code == REQUEST_SHOW_PROJECTS_DEFAULT) {
                mAdapter.setSelectable(true);
            } else {
                return;
            }
        }
        if (mAdapter.getSelectable()) {
            checkNumberSelected();
            return;
        }
        Bundle bundle = new Bundle();
        String VIDEO_PATH_KEY = "video-file-path";
        bundle.putString(VIDEO_PATH_KEY, path);
        Intent intent;
        switch (from_code) {
            case REQUEST_VIDEO_FOR_REACT_CAM:
                intent = new Intent(this, CompressBeforeReactCamActivity.class);
                break;
            case REQUEST_VIDEO_FOR_VIDEO_EDIT:
                intent = new Intent(this, VideoEditorActivity.class);
                break;
            case REQUEST_VIDEO_FOR_COMMENTARY:
                intent = new Intent(this, CommentaryActivity.class);
                break;
            case REQUEST_SHOW_PROJECTS_DEFAULT:
            default:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
                intent.setDataAndType(Uri.parse(path), "video/*");
        }
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private final ActivityResultLauncher<IntentSenderRequest> loginResultHandler = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
        // handle intent result here
        if (result.getResultCode() == RESULT_OK) {
            Toast.makeText(this, "The video is deleted! " + result.getData(), Toast.LENGTH_SHORT).show();
        }
    });

    public void delete(SecurityException e, ActivityResultLauncher<IntentSenderRequest> launcher, Uri uri, ContentResolver contentResolver) {
        PendingIntent pendingIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ArrayList<Uri> collection = new ArrayList<>();
            collection.add(uri);
            pendingIntent = MediaStore.createDeleteRequest(contentResolver, collection);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //if exception is recoverable then again send delete request using intent
            if (e instanceof RecoverableSecurityException) {
                RecoverableSecurityException exception = (RecoverableSecurityException) e;
                pendingIntent = exception.getUserAction().getActionIntent();
            }
        }
        if (pendingIntent != null) {
            IntentSender sender = pendingIntent.getIntentSender();
            IntentSenderRequest request = new IntentSenderRequest.Builder(sender).build();
            launcher.launch(request);
        }
    }

    public void rename(Uri uri, String rename) {

        //create content values with new name and update
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, rename);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ContentResolver contentResolver = getApplicationContext().getContentResolver();
            contentResolver.update(uri, contentValues, null);
        }
    }
}
