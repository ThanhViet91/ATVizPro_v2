package com.examples.atscreenrecord_test.model;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

public class VideoModel {
    private String name;
    private String path;
    private String duration;
    private boolean selected;
    private long lastModified;

    public VideoModel() {
    }

    public VideoModel(String name, String path, String duration, long lastModified) {
        this.name = name;
        this.path = path;
        this.duration = duration;
        this.selected = false;
        this.lastModified = lastModified;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getCompare() {
        return name + path + duration;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public long getDurationMs(Context context) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, Uri.parse(path));
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
            long timeInMs = Long.parseLong(time);
            return timeInMs;
    }
}
