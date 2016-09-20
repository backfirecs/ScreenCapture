package com.example.backfirecs.screencapture.record;

import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.util.Log;

import java.io.IOException;

/**
 * @author chace
 * @date   2016/9/19
 * 具体中介者，协调ScreenRecord与AudioRecord的动作
 *
 */
public class RecordMediator extends AbstractRecordMediator implements MediaRecorder.MuxerListener{

    private static final String LOG_TAG  = RecordMediator.class.getSimpleName();

    public RecordMediator(String path,MediaFormat _videoMediaFormat, MediaFormat _audioMediaFormat,MediaProjection mediaProjection) {

        super(path, _videoMediaFormat, _audioMediaFormat);
        mScreenRecorder.setMuxerListener(this);
        ((ScreenRecorder)mScreenRecorder).setMediaProjection(mediaProjection);
        mAudeiRecorder.setMuxerListener(this);
    }




    /**
     * 开始录制，注意此时只开启音频录制，屏幕录制要等AudioRecord准
     * 备好开启MediaMuxer后在开启,因为如果同时开启的话一开始录制
     * 的画面会无法送入MediaMuxer中，暂时还不知道原因
     *
     *
     */
    @Override
    public void startRecord() {
        //启动音频录制
        mAudeiRecorder.startRecord();



        mRecording = true;
    }

    @Override
    public void stopRecord() {

        mAudeiRecorder.stopRecord();
        mScreenRecorder.stopRecord();

        mRecording = false;
    }

    @Override
    public void config() {
        //初始化MediaMuxer
        try {
            //初始化MediaMuxer,设置输出格式为mp4
            mMediaMuxer = new MediaMuxer(mSavePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mScreenRecorder.setMediaMuxer(mMediaMuxer);
            //配置ScreenRecorder
            mScreenRecorder.config(mVideoMediaFormat);

            mAudeiRecorder.setMediaMuxer(mMediaMuxer);
            //配置AudioRecorder
            mAudeiRecorder.config(mAudioMediaFormat);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean isRecording() {
        return mRecording;
    }


    /**
     * 在这个方法中启动MediaMuxer,注意要ScreenRecorder与AudioRecorder
     * 都准备好开启MediaMuxer(即Track都添加进了MediaMuxer)才能开启
     *
     *
     */
    @Override
    public boolean startMuxer(int type) {
        if(type == MediaRecorder.RECORD_SCREEN){
            mScreenRecordMuxerStartReady = true;

        }else if(type == MediaRecorder.RECORD_AUDIO){
            mAudioRecordMuxerStartReady = true;
            mScreenRecorder.startRecord();
        }
        if(mScreenRecordMuxerStartReady&&mAudioRecordMuxerStartReady){
            mMediaMuxer.start();
            mScreenRecorder.startMuxer(true);
            mAudeiRecorder.startMuxer(true);
            Log.d(LOG_TAG,"muxer is starting...");
            return true;
        }

       return false;

    }

    /**
     * 在这个方法中停止并释放MediaMuxer,注意要ScreenRecorder与AudioRecorder
     * 都准备好停止MediaMuxer才能停止
     *
     *
     */
    @Override
    public boolean stopMuxer(int type) {
        if(type == MediaRecorder.RECORD_SCREEN){
            mScreenRecordMuxerStopReady = true;
        }else if(type == MediaRecorder.RECORD_AUDIO){
            mAudioRecordMuxerStopReady = true;
        }
        if(mAudioRecordMuxerStopReady&&mScreenRecordMuxerStopReady){
            mMediaMuxer.stop();
            mMediaMuxer.release();
            Log.d(LOG_TAG, "muxer is stop...");
        }

        return true;
    }


}
