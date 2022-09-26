package com.examples.atscreenrecord.model;


public class VideoModel {
    private int id;
    private String name;
    private long size;
    private long created;
    private String thumb;
    private String duration;

    public VideoModel() {
    }

    public VideoModel(int id, String name, long size, long created, String thumb, String duration) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.created = created;
        this.thumb = thumb;
        this.duration = duration;
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
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
}
