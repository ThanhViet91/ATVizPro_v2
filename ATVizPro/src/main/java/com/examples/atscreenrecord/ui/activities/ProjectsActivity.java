package com.examples.atscreenrecord.ui.activities;

import static com.examples.atscreenrecord.ui.activities.MainActivity.REQUEST_SHOW_PROJECTS_DEFAULT;
import static com.examples.atscreenrecord.ui.activities.MainActivity.REQUEST_VIDEO_FOR_COMMENTARY;
import static com.examples.atscreenrecord.ui.activities.MainActivity.REQUEST_VIDEO_FOR_REACT_CAM;
import static com.examples.atscreenrecord.ui.activities.MainActivity.REQUEST_VIDEO_FOR_VIDEO_EDIT;
import static com.examples.atscreenrecord.ui.utils.MyUtils.hideStatusBar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
    private TextView btn_cancel, tv_nodata;
    private VideoProjectsAdapter mAdapter;
    private final String VIDEO_PATH_KEY = "video-file-path";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_video_projects);
        hideStatusBar(this);
        initViews();

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        reloadData();
        if (videoList == null || videoList.size() == 0) {
            tv_nodata.setVisibility(View.VISIBLE);
        } else {
            tv_nodata.setVisibility(View.GONE);
        }
        AdView mAdView = findViewById(R.id.adView);
        AdUtil.createBannerAdmob(this, mAdView);
    }

    private void initViews() {
        recyclerView = (RecyclerView) findViewById(R.id.list_videos);
        btn_cancel = findViewById(R.id.tv_btn_cancel_projects);
        tv_nodata = findViewById(R.id.tvEmpty);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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

        // Create a new {@link ArrayAdapter} of earthquakes: gắn cái datalist vào layout
        readData();
        mAdapter = new VideoProjectsAdapter(
                this, videoList_revert);
        mAdapter.setVideoProjectsListener(this);
        // Set the mAdapter on the {@link ListView}
        // so the list can be populated in the user interface
        recyclerView.setAdapter(mAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    private ArrayList<VideoModel> videoList = new ArrayList<VideoModel>();
    private ArrayList<VideoModel> videoList_revert = new ArrayList<VideoModel>();

    private void reloadData() {
        videoList.clear();
        videoList_revert.clear();
        readData();
        mAdapter.reloadData(videoList_revert);
    }

    private void readData() {
        listFilesForFolder(new File(MyUtils.getBaseStorageDirectory()));
        if (videoList != null && videoList.size() > 0) {
            for (int i = videoList.size() - 1; i >= 0; i--) {
                videoList_revert.add(videoList.get(i));
            }
        }
    }

    public void listFilesForFolder(final File folder) {
        int i = 0;
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                if (fileEntry.getName().contains(".mp4"))
                    videoList.add(new VideoModel(i, fileEntry.getName().replace(".mp4", ""), 0, 0, fileEntry.getAbsolutePath(), getDuration(fileEntry.getAbsolutePath())));
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
        int code = 0;
        if (getIntent() != null) code = getIntent().getIntExtra("key_from_code", 0);
        Bundle bundle = new Bundle();
        bundle.putString(VIDEO_PATH_KEY, path);
        Intent intent;
        switch (code) {
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
            Toast.makeText(this, "The video is deleted!", Toast.LENGTH_SHORT).show();
        }
        else {
            //...
//            Toast.makeText(this, "Error!! Can't delete this video!", Toast.LENGTH_SHORT).show();
        }
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
