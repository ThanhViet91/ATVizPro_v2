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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
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

import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.adapter.VideoProjectsAdapter;
import com.examples.atscreenrecord.model.VideoModel;
import com.examples.atscreenrecord.ui.utils.MyUtils;
import com.examples.atscreenrecord.utils.AdUtil;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class ProjectsActivity extends AppCompatActivity implements VideoProjectsAdapter.VideoProjectsListener {
    // TODO: Rename parameter arguments, choose names that match

    private static final String TAG = ProjectsActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private TextView btn_cancel, tv_nodata, tv_select, tv_selected;
    private ImageView btn_back, btn_rename, btn_delete;
    private VideoProjectsAdapter mAdapter;
    private final String VIDEO_PATH_KEY = "video-file-path";
    private ArrayList<VideoModel> videoList = new ArrayList<VideoModel>();
    private ArrayList<VideoModel> videoList_temp = new ArrayList<VideoModel>();
    private ArrayList<VideoModel> videoList_selected = new ArrayList<VideoModel>();
    private int from_code = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_video_projects);
        hideStatusBar(this);

        if (getIntent() != null) from_code = getIntent().getIntExtra("key_from_code", 0);

        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadData();
        if (videoList == null || videoList.size() == 0) {
            toggleView(tv_nodata, View.VISIBLE);
        } else {
            toggleView(tv_nodata, View.GONE);
        }
        AdView mAdView = findViewById(R.id.adView);
        AdUtil.createBannerAdmob(this, mAdView);
    }

    public void toggleView(View view, int type) {
        if (view != null) {
            view.setVisibility(type);
        }
    }

    private void initViews() {
        recyclerView = (RecyclerView) findViewById(R.id.list_videos);
        btn_cancel = findViewById(R.id.tv_btn_cancel_projects);
        tv_select = findViewById(R.id.tv_btn_select);
        btn_back = findViewById(R.id.img_btn_back_header);
        btn_delete = findViewById(R.id.img_btn_delete);
        btn_rename = findViewById(R.id.img_btn_rename);
        tv_selected = findViewById(R.id.tv_selected);

        toggleView(findViewById(R.id.option), View.GONE);
        tv_select.setText("Select");

        if (from_code == REQUEST_SHOW_PROJECTS_DEFAULT) {
            toggleView(btn_cancel, View.GONE);
            toggleView(btn_back, View.VISIBLE);
        } else {
            toggleView(btn_cancel, View.VISIBLE);
            toggleView(btn_back, View.GONE);
            toggleView(tv_select, View.GONE);
        }

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (from_code == REQUEST_SHOW_PROJECTS_DEFAULT) {
                    for (VideoModel video : videoList) video.setSelected(false);
                    mAdapter.setSelectable(false);
                    toggleView(btn_back, View.VISIBLE);
                    toggleView(btn_cancel, View.GONE);
                    modeSelect = 0;
                    tv_select.setText(getString(R.string.select));
                    toggleView(findViewById(R.id.option), View.GONE);
                } else {
                    onBackPressed();
                }
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tv_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                modeSelect++;
                handleSelectButton();
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleDeleteButton();
            }
        });

        btn_rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                handleRenameButton();
            }
        });

        final SwipeRefreshLayout srl = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadData();
                srl.setRefreshing(false);
            }
        });

        readData();
        mAdapter = new VideoProjectsAdapter(
                this, videoList);
        mAdapter.setVideoProjectsListener(this);
        // Set the mAdapter on the {@link ListView}
        // so the list can be populated in the user interface
        recyclerView.setAdapter(mAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    private void handleDeleteButton() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Video")
                .setMessage("Are you sure you want to delete video?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        deleteVideos(getVideoListSelected());
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                    }
                });
    }

    public void deleteVideos(ArrayList<VideoModel> listSelected) {
        for (VideoModel video : listSelected) {
            File file = new File(video.getThumb());
            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{file.getName()},
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String s, Uri uri) {
                            ContentResolver contentResolver = getApplicationContext().getContentResolver();
                            try {
                                //delete object using resolver
                                contentResolver.delete(uri, null, null);
                                videoList.remove(video);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.updateData(videoList);
                                    }
                                });
                                Toast.makeText(getApplicationContext(), "The video is deleted!", Toast.LENGTH_SHORT).show();
                            } catch (SecurityException e) {
                                delete(e, loginResultHandler, uri, contentResolver);
                            }
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

    int modeSelect = 0;
    private void handleSelectButton() {
        toggleView(btn_back, View.GONE);
        toggleView(btn_cancel, View.VISIBLE);
        toggleView(findViewById(R.id.option), View.VISIBLE);
        if (modeSelect == 1) {
            tv_select.setText(getString(R.string.select_all));
            mAdapter.setSelectable(true);
            checkNumberSelected();
        }
        if (modeSelect == 2) {
            for (VideoModel video : videoList) video.setSelected(true);
            mAdapter.updateData(videoList);
            tv_select.setText(getString(R.string.deselect_all));
            checkNumberSelected();

        }
        if (modeSelect == 3) {
            for (VideoModel video : videoList) video.setSelected(false);
            mAdapter.updateData(videoList);
            tv_select.setText(getString(R.string.select_all));
            modeSelect = 1;
            checkNumberSelected();
        }
    }

    private void checkNumberSelected() {
        if (getVideoListSelected().size() == 0) {
            toggleView(tv_selected, View.GONE);
            toggleView(btn_rename, View.GONE);
            toggleView(btn_delete, View.GONE);
            return;
        }
        toggleView(tv_selected, View.VISIBLE);
        toggleView(btn_delete, View.VISIBLE);
        if (getVideoListSelected().size() == 1) {
            toggleView(btn_rename, View.VISIBLE);
        } else {
            if (getVideoListSelected().size() == videoList.size()) {
                modeSelect = 2;
                tv_select.setText(getString(R.string.deselect_all));
            } else {
                modeSelect = 1;
                tv_select.setText(getString(R.string.select_all));
            }
            toggleView(btn_rename, View.GONE);
        }
        tv_selected.setText(getVideoListSelected().size() + " item selected");
    }

    private void reloadData() {
        videoList_temp.clear();
        videoList_temp = new ArrayList<>(videoList);
        videoList.clear();
        readData();
        processingData();
        mAdapter.reloadData(videoList);
    }

    private void processingData() {
        for (VideoModel videoOld: videoList_temp) {
            for (VideoModel videoNew : videoList) {
                if (videoOld.getCompare().equals(videoNew.getCompare())){
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
                    videoList.add(0, new VideoModel(i, fileEntry.getName().replace(".mp4", ""), fileEntry.getAbsolutePath(), getDuration(fileEntry.getAbsolutePath())));
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
        if (from_code != 0) {
            checkNumberSelected();
            return;
        }
        Bundle bundle = new Bundle();
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

    @Override
    public void onDeleteFile(SecurityException e, Uri uri, ContentResolver contentResolver) {
        delete(e, loginResultHandler, uri, contentResolver);
    }

    private ActivityResultLauncher<IntentSenderRequest> loginResultHandler = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
        // handle intent result here
        if (result.getResultCode() == RESULT_OK) {
            Toast.makeText(this, "The video is deleted! "+result.getData(), Toast.LENGTH_SHORT).show();
        } /*else {
            Toast.makeText(this, "Error!! Can't delete this video! " + result.getResultCode(), Toast.LENGTH_SHORT).show();
        }*/
    });

    boolean isDeleleteNomal;

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
}
