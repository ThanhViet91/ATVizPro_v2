package com.examples.atscreenrecord.model;

public class VideoModel {
    private int id;
    private String name;
    private String path;
    private String duration;
    private boolean selected;

    public VideoModel() {
    }

    public VideoModel(int id, String name, String path, String duration) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.duration = duration;
        this.selected = false;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
