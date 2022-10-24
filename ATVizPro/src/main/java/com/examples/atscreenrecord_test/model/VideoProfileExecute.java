package com.examples.atscreenrecord_test.model;

import java.io.Serializable;

public class VideoProfileExecute implements Serializable {
    private String originalVideoPath;
    private String overlayVideoPath;
    private long startTime;
    private long endTime;
    private int camSize;
    private int posX;
    private int posY;
    private boolean isMuteAudioOriginal = false;
    private boolean isMuteAudioOverlay = false;
    private int type;

    public VideoProfileExecute(int type, String originalVideoPath, String overlayVideoPath,
                               long startTime, long endTime, int camSize,
                               int posX, int posY,
                               boolean isMuteAudioOriginal, boolean isMuteAudioOverlay) {
        this.type = type;
        this.originalVideoPath = originalVideoPath;
        this.overlayVideoPath = overlayVideoPath;
        this.startTime = startTime;
        this.endTime = endTime;
        this.camSize = camSize;
        this.posX = posX;
        this.posY = posY;
        this.isMuteAudioOriginal = isMuteAudioOriginal;
        this.isMuteAudioOverlay = isMuteAudioOverlay;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getOriginalVideoPath() {
        return originalVideoPath;
    }

    public void setOriginalVideoPath(String originalVideoPath) {
        this.originalVideoPath = originalVideoPath;
    }

    public String getOverlayVideoPath() {
        return overlayVideoPath;
    }

    public void setOverlayVideoPath(String overlayVideoPath) {
        this.overlayVideoPath = overlayVideoPath;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getCamSize() {
        return camSize;
    }

    public void setCamSize(int camSize) {
        this.camSize = camSize;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public boolean isMuteAudioOriginal() {
        return isMuteAudioOriginal;
    }

    public void setMuteAudioOriginal(boolean muteAudioOriginal) {
        isMuteAudioOriginal = muteAudioOriginal;
    }

    public boolean isMuteAudioOverlay() {
        return isMuteAudioOverlay;
    }

    public void setMuteAudioOverlay(boolean muteAudioOverlay) {
        isMuteAudioOverlay = muteAudioOverlay;
    }
}
