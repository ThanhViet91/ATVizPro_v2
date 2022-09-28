package com.takusemba.rtmppublisher;

import static com.takusemba.rtmppublisher.helper.MyUtils.DEBUG;

import android.media.projection.MediaProjection;
import android.util.Log;

import androidx.annotation.NonNull;

public class RtmpPublisherSSL implements Publisher {

    private static final String TAG = RtmpPublisherSSL.class.getSimpleName();

    private Streamer streamer;

    private String url;
    private int width;
    private int height;
    private int audioBitrate;
    private int videoBitrate;
    private int density;
    private PublisherListener listener;

    RtmpPublisherSSL(
            String url,
            int width,
            int height,
            int audioBitrate,
            int videoBitrate,
            int density,
            @NonNull MediaProjection mediaProjection,
            PublisherListener listener) {

        this.url = url;
        this.width = width;
        this.height = height;
        this.audioBitrate = audioBitrate;
        this.videoBitrate = videoBitrate;
        this.density = density;
        this.streamer = new Streamer(mediaProjection, listener);
        streamer.open(url, width, height);
    }

    @Override
    public void startPublishing() {
        if(DEBUG) Log.i(TAG, "startPublishing: called (clicked)");
        streamer.startStreamingSSL(width, height, audioBitrate, videoBitrate, density);
    }

    @Override
    public void stopPublishing() {
        if (streamer.isStreaming()) {
            streamer.stopStreaming();
        }
    }

    @Override
    public void closePublishing() {
        if (streamer.isStreaming()) {
            streamer.disconnectStreaming();
        }
    }

    @Override
    public boolean isPublishing() {
        return streamer.isStreaming();
    }

}
