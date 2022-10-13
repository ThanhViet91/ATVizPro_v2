package com.takusemba.rtmppublisher;

import android.media.MediaCodec;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.serenegiant.utils.UIThreadHelper;

import net.ossrs.rtmp.ConnectCheckerRtmp;
import net.ossrs.rtmp.SrsFlvMuxer;

import java.nio.ByteBuffer;


class Streamer
        implements AudioHandler.OnAudioEncoderStateListener, VideoHandler.OnVideoEncoderStateListener {

    private static final String TAG = "Streamer_chienpm_log";

    private static final boolean DEBUG = false;

    private VideoHandler videoHandler;
    private AudioHandler audioHandler;

    private SrsFlvMuxer srsFlvMuxer;
    private PublisherListener listener = null;

    Streamer(@NonNull MediaProjection mediaProjection, PublisherListener listener) {
        this.videoHandler = new VideoHandler(mediaProjection);
        this.audioHandler = new AudioHandler();
        this.listener = listener;
    }

    void open(String url, int width, int height) {
        if (DEBUG) Log.i(TAG, "open: " + url);

        if (listener != null) listener.onStarted();
        srsFlvMuxer = new SrsFlvMuxer(new ConnectCheckerRtmp() {
            @Override
            public void onConnectionSuccessRtmp() {
                System.out.println("thanhlv onConnectionSuccessRtmp");
                if (listener != null) listener.onConnected();
            }

            @Override
            public void onConnectionFailedRtmp(@NonNull String reason) {
                System.out.println("thanhlv onConnectionFailedRtmp " + reason);
                if (listener != null) listener.onFailedToConnect();
            }

            @Override
            public void onNewBitrateRtmp(long bitrate) {
                System.out.println("thanhlv onNewBitrateRtmp");
            }

            @Override
            public void onDisconnectRtmp() {
                System.out.println("thanhlv onDisconnectRtmp");
                if (listener != null) listener.onDisconnected();
            }

            @Override
            public void onAuthErrorRtmp() {
                System.out.println("thanhlv onAuthErrorRtmp");
            }

            @Override
            public void onAuthSuccessRtmp() {
                System.out.println("thanhlv onAuthSuccessRtmp");
            }
        });
        srsFlvMuxer.setVideoResolution(width, height);
        srsFlvMuxer.start(url);
        srsFlvMuxer.setIsStereo(true);
        srsFlvMuxer.setSampleRate(44100);

    }

    void startStreamingSSL(int width, int height, int audioBitrate, int videoBitrate, int density) {
        int t = 0;
        while (!srsFlvMuxer.isConnected()) {
            try {
                t += 100;
                Thread.sleep(100);
                if (t > 5000)
                    break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (srsFlvMuxer.isConnected()) {
            if (DEBUG) Log.i(TAG, "start Streaming: connected");
            long startStreamingAt = System.currentTimeMillis();
            videoHandler.setOnVideoEncoderStateListener(this);
            audioHandler.setOnAudioEncoderStateListener(this);
            videoHandler.start(width, height, videoBitrate, startStreamingAt, density);
            audioHandler.start(audioBitrate, startStreamingAt);

        } else {
            Log.e(TAG, "startStreaming: failed coz muxer is not connected");
            System.out.println("startStreaming: failed coz muxer is not connected");
        }
    }

    void disconnectStreaming() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                srsFlvMuxer.stop();
                oldTimeStamp = 0L;
            }
        }, 1000);
    }

    void stopStreaming() {
        videoHandler.stop();
        audioHandler.stop();
        if (listener != null) listener.onStopped();
    }

    boolean isStreaming() {
        return srsFlvMuxer.isConnected();
    }


    @Override
    public void onAudioDataEncoded(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
        fixTimeStamp(info);
        srsFlvMuxer.sendAudio(h264Buffer, info);

        System.out.println("thanhlv sendAudio " + info.size);
    }

    @Override
    public void onSpsPps(ByteBuffer sps, ByteBuffer pps) {
        srsFlvMuxer.setSpsPPs(sps, pps);
        System.out.println("thanhlv onSpsPps " + sps.toString());
    }

    @Override
    public void onVideoDataEncoded(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
        fixTimeStamp(info);
        srsFlvMuxer.sendVideo(h264Buffer, info);
        System.out.println("thanhlv sendVideo " + info.size);
    }

    long oldTimeStamp = 0L;

    protected void fixTimeStamp(MediaCodec.BufferInfo info) {
        if (oldTimeStamp > info.presentationTimeUs) {
            info.presentationTimeUs = oldTimeStamp;
        } else {
            oldTimeStamp = info.presentationTimeUs;
        }
    }
}