package com.examples.atvizpro.model;

public class SettingsItem {
    private String content;
    private int resourceId;

    public SettingsItem(String content, int resourceId) {
        this.content = content;
        this.resourceId = resourceId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }
}
