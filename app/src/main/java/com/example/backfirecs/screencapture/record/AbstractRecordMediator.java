package com.example.backfirecs.screencapture.record;

import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.text.TextUtils;

/**
 * @author chace
 * @date   2019/9/19
 * 抽象中介者
 *
 */
public abstract class AbstractRecordMediator {

    protected MediaFormat mVideoMediaFormat = DefaultMediaFormat.getDefaultVideoFormat();//获取视频默认的MediaFormat

    protected MediaFormat mAudioMediaFormat = DefaultMediaFormat.getDefaultAudioFormat();//获取音频默认的MediaFormat

    protected boolean mMuxerStart = false;//mMediaMuxer是否启动

    protected MediaRecorder mScreenRecorder = new ScreenRecorder();

    protected MediaRecorder mAudeiRecorder = new TBAudioRecorder();

    protected final String DEFAULTPATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/backfirecs.mp4";//生成MP4文件的默认路径

    protected String mSavePath;

    protected MediaMuxer mMediaMuxer;

    protected boolean mRecording = false;

    protected boolean mScreenRecordMuxerStartReady = false;//ScreenRecord是否准备好开启MediaMuxer;

    protected boolean mAudioRecordMuxerStartReady = false;//AudioRecord是否准备好开启MediaMuxer;

    protected boolean mScreenRecordMuxerStopReady = false;//ScreenRecord是否准备好停止MediaMuxer;

    protected boolean mAudioRecordMuxerStopReady = false;//AudioRecord是否准备好停止MediaMuxer;

    public AbstractRecordMediator(String path,MediaFormat _videoMediaFormat,MediaFormat _audioMediaFormat){
        if(_videoMediaFormat!=null){

            this.mVideoMediaFormat = _videoMediaFormat;
        }

        if(_audioMediaFormat!=null){

            this.mAudioMediaFormat = _audioMediaFormat;

        }

        if(!TextUtils.isEmpty(path)){
            mSavePath = path;
        }else{
            mSavePath = DEFAULTPATH;
        }


    }


    public abstract void startRecord();


    public abstract void stopRecord();


    public abstract void config();

    public abstract boolean isRecording();

}
