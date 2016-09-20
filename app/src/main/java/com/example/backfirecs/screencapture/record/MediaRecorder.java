package com.example.backfirecs.screencapture.record;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chace
 * @date   2016/9/19
 * 抽象录制器
 *
 */
public abstract class MediaRecorder extends Thread{

    protected MediaFormat mVideoMediaFormat;

    protected MediaFormat mAudioMediaFormat;

    protected MuxerListener mMuxerListener;

    protected MediaCodec mMediaCodec;

    protected MediaMuxer mMediaMuxer;

    protected AtomicBoolean mQuit = new AtomicBoolean(false);//退出录制的标志位

    protected MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    protected boolean mMuxerStart = false;

    protected int mTackIndex = -1;

    public static final int RECORD_SCREEN = 1;

    public static final int RECORD_AUDIO = 2;

    public MediaRecorder(){

    }

    public abstract void startRecord();

    public abstract void stopRecord();

    public abstract void config(MediaFormat mediaFormat) throws IOException;

    public abstract void startMuxer(boolean muxerStart);

    public abstract void setMuxerListener(MuxerListener listener);

    public abstract void setMediaMuxer(MediaMuxer mediaMuxer);

    public abstract void release();

    public abstract void encodeToTrack(int index);

    public abstract void resetOutputFormat();


    protected long prevOutputPTSUs = 0;
    /**
     * 获取当前的时间戳
     *
     *
     *
     */
    protected long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        // presentationTimeUs should be monotonic
        // otherwise muxer fail to write
        if (result < prevOutputPTSUs)
            result = (prevOutputPTSUs - result) + result;
        return result;

    }

    public interface MuxerListener{
        public boolean startMuxer(int type);

        public boolean stopMuxer(int type);
    }

}
