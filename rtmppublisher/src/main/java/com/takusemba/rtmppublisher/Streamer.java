package com.takusemba.rtmppublisher;

import android.media.MediaCodec;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

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

    Streamer(@NonNull MediaProjection mediaProjection) {
        this.videoHandler = new VideoHandler(mediaProjection);
        this.audioHandler = new AudioHandler();
    }

    void open(String url, int width, int height) {
        if(DEBUG) Log.i(TAG, "open: "+url);

        srsFlvMuxer = new SrsFlvMuxer(new ConnectCheckerRtmp() {
            @Override
            public void onConnectionSuccessRtmp() {
            }

            @Override
            public void onConnectionFailedRtmp(@NonNull String reason) {
            }

            @Override
            public void onNewBitrateRtmp(long bitrate) {
            }

            @Override
            public void onDisconnectRtmp() {

            }

            @Override
            public void onAuthErrorRtmp() {

            }

            @Override
            public void onAuthSuccessRtmp() {

            }
        });
        srsFlvMuxer.setVideoResolution(width, height);
        srsFlvMuxer.start(url);

        srsFlvMuxer.setIsStereo(true);
        srsFlvMuxer.setSampleRate(44100);

    }

    void startStreamingSSL(int width, int height, int audioBitrate, int videoBitrate, int density) {
        int t = 0;
        while (!srsFlvMuxer.isConnected()){
            try {
                t+=100;
                Thread.sleep(100);
                if(t>5000)
                    break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (srsFlvMuxer.isConnected()) {
            if(DEBUG) Log.i(TAG, "start Streaming: connected");
            long startStreamingAt = System.currentTimeMillis();
            videoHandler.setOnVideoEncoderStateListener(this);
            audioHandler.setOnAudioEncoderStateListener(this);
            videoHandler.start(width, height, videoBitrate, startStreamingAt, density);
            audioHandler.start(audioBitrate, startStreamingAt);
        } else {
            Log.e(TAG, "startStreaming: failed coz muxer is not connected");
        }
    }

    void stopStreaming() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                srsFlvMuxer.stop();
                oldTimeStamp = 0L;
            }
        }, 1000);
        videoHandler.stop();
        audioHandler.stop();
    }

    boolean isStreaming() {
        return srsFlvMuxer.isConnected();
    }


    @Override
    public void onAudioDataEncoded(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
        fixTimeStamp(info);
        srsFlvMuxer.sendAudio(h264Buffer, info);

        System.out.println("thanhlv sendAudio "+info.size);
    }

    @Override
    public void onSpsPps(ByteBuffer sps, ByteBuffer pps) {
        srsFlvMuxer.setSpsPPs(sps, pps);
        System.out.println("thanhlv onSpsPps "+sps.toString());
    }

    @Override
    public void onVideoDataEncoded(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
        fixTimeStamp(info);
        srsFlvMuxer.sendVideo(h264Buffer, info);
        System.out.println("thanhlv sendVideo "+info.size);
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