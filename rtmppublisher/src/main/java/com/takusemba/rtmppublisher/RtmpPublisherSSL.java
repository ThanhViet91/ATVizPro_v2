package com.takusemba.rtmppublisher;

import static com.takusemba.rtmppublisher.helper.MyUtils.DEBUG;

import android.media.projection.MediaProjection;
import android.os.Handler;
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
            final String url,
            final int width,
            final int height,
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

    }

    @Override
    public void openPublishing(String url) {
                streamer.open(url, width, height);
    }

    @Override
    public void startPublishing() {
        if(DEBUG) Log.i(TAG, "startPublishing: called (clicked)");
        if (!streamer.isStreaming()) {
            streamer.open(url, width, height);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    streamer.startStreamingSSL(width, height, audioBitrate, videoBitrate, density);
                }
            }, 500);
        } else
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
