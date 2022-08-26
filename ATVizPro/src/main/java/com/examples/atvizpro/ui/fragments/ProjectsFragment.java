package com.examples.atvizpro.ui.fragments;


import static com.examples.atvizpro.ui.activities.MainActivity.REQUEST_VIDEO_FOR_COMMENTARY;
import static com.examples.atvizpro.ui.activities.MainActivity.REQUEST_VIDEO_FOR_REACT_CAM;
import static com.examples.atvizpro.ui.activities.MainActivity.REQUEST_VIDEO_FOR_VIDEO_EDIT;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.examples.atvizpro.R;
import com.examples.atvizpro.adapter.VideoProjectsAdapter;
import com.examples.atvizpro.model.VideoModel;
import com.examples.atvizpro.ui.activities.CommentaryActivity;
import com.examples.atvizpro.ui.activities.CompressBeforeReactCamActivity;
import com.examples.atvizpro.ui.activities.VideoEditorActivity;
import com.examples.atvizpro.ui.utils.MyUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProjectsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProjectsFragment extends Fragment implements VideoProjectsAdapter.VideoProjectsListener {
    // TODO: Rename parameter arguments, choose names that match

    private static final String TAG = ProjectsFragment.class.getSimpleName();
    private View mViewRoot;
    private Menu mMenu;
    RecyclerView recyclerView;
    TextView mTvEmpty;
    private VideoProjectsAdapter mAdapter;
    private Object mSync = new Object();
    private final String VIDEO_PATH_KEY = "video-file-path";

    public ProjectsFragment() {
        // Required empty public constructor
    }

    public static ProjectsFragment newInstance(Bundle args) {
        ProjectsFragment fragment = new ProjectsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        mViewRoot = inflater.inflate(R.layout.fragment_video_projects, container, false);
        return mViewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initViews() {
        recyclerView = (RecyclerView) mViewRoot.findViewById(R.id.list_videos);
        mTvEmpty = mViewRoot.findViewById(R.id.tvEmpty);

        final SwipeRefreshLayout srl = (SwipeRefreshLayout) mViewRoot.findViewById(R.id.swipeLayout);
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
                getActivity(), videoList);
        mAdapter.setVideoProjectsListener(this);
        // Set the mAdapter on the {@link ListView}
        // so the list can be populated in the user interface
        recyclerView.setAdapter(mAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);


    }

    private ArrayList<VideoModel> videoList = new ArrayList<VideoModel>();
    private void reloadData() {
        videoList.clear();
        readData();
        mAdapter.reloadData(videoList);
    }

    private void readData() {
        listFilesForFolder(new File(MyUtils.getBaseStorageDirectory()));

    }
    public void listFilesForFolder(final File folder) {
        int i = 0;
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                if (fileEntry.getName().contains(".mp4"))
                    videoList.add(new VideoModel(i, fileEntry.getName(), 0, 0, fileEntry.getAbsolutePath()));
                i++;
            }
        }
    }


    @Override
    public void onSelected(String path) {
        int code = 0;
        if (getArguments() != null) code = getArguments().getInt("key_from_code");
        Bundle bundle = new Bundle();
        bundle.putString(VIDEO_PATH_KEY, path);
        Intent intent;
        switch (code) {
            case REQUEST_VIDEO_FOR_REACT_CAM:
                intent = new Intent(getActivity(), CompressBeforeReactCamActivity.class);
                break;
            case REQUEST_VIDEO_FOR_VIDEO_EDIT:
                intent = new Intent(getActivity(), VideoEditorActivity.class);
                break;
            case REQUEST_VIDEO_FOR_COMMENTARY:
                intent = new Intent(getActivity(), CommentaryActivity.class);
                break;
            default:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
                intent.setDataAndType(Uri.parse(path), "video/*");
        }

        intent.putExtras(bundle);
        startActivity(intent);
    }
}
