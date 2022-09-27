package com.examples.atscreenrecord.model;


public class VideoModel {
    private int id;
    private String name;
    private String thumb;
    private String duration;
    private boolean selected;

    public VideoModel() {
    }

    public VideoModel(int id, String name, String thumb, String duration) {
        this.id = id;
        this.name = name;
        this.thumb = thumb;
        this.duration = duration;
        this.selected = false;
    }

    public String getCompare() {
        return name + thumb + duration;
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

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
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
