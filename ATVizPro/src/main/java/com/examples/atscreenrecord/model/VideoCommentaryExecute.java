package com.examples.atscreenrecord.model;

import java.io.Serializable;

public class VideoCommentaryExecute implements Serializable {
    private String originalVideoPath;
    private String audioOverlayPath;
    private boolean isMuteAudioOriginal = false;
    private boolean isMuteAudioOverlay = false;

    public VideoCommentaryExecute(String originalVideoPath, String audioOverlayPath) {
        this.originalVideoPath = originalVideoPath;
        this.audioOverlayPath = audioOverlayPath;
    }

    public String getOriginalVideoPath() {
        return originalVideoPath;
    }

    public void setOriginalVideoPath(String originalVideoPath) {
        this.originalVideoPath = originalVideoPath;
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
