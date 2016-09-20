package com.example.backfirecs.screencapture.record;

import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
/**
 * @author chace
 * @date   2016/9/19
 * 音频录制器
 *使用AudioRecord去录制音频
 */
public class TBAudioRecorder extends MediaRecorder{


    private static final String LOG_TAG = TBAudioRecorder.class.getSimpleName();

    private long mPresentationTimeStamp = 0;

    private AudioRecord mAudioRecord;

    private static long mAudioBytesReceived = 0; // 接收到的音频数据 用来设置录音起始时间的

    private long mAudioStartTime = 0;

    private boolean mIsEncoding = false;

    private boolean mRecording = false;

    public TBAudioRecorder(){

            //初始化AudioRecord
            mAudioRecord = new AudioRecord(MediaDefaultConfig.AUDIO_SOURCE,MediaDefaultConfig.AUDIO_SIMPLE_RATE,
                    MediaDefaultConfig.AUDIO_CHANNEL_CONFIG,MediaDefaultConfig.AUDIO_FORMAT,MediaDefaultConfig.AUDIO_BUFFERSIZEINBYTE);


    }

    @Override
    public void startRecord() {

        mRecording = true;
        start();

    }

    @Override
    public void stopRecord() {

        mQuit.set(true);
        mRecording = false;
    }

    @Override
    public void run() {

        super.run();
        mAudioRecord.startRecording();
        byte buffer[] = new byte[MediaDefaultConfig.AUDIO_BUFFERSIZEINBYTE/4];

        while(!mQuit.get()){
            mAudioRecord.read(buffer, 0,
                    MediaDefaultConfig.AUDIO_BUFFERSIZEINBYTE / 4);
            mPresentationTimeStamp = System.nanoTime();
            //将编码过后的数据写入MediaMuxer
            encodeToTrack(-1);
            //将录制的音频源数据送入MediaCodec编码
            encodeToMediaCodec(buffer.clone(),mPresentationTimeStamp);
        }
        mAudioRecord.stop();

        release();
    }

    @Override
    public void config(MediaFormat mediaFormat) throws IOException {
        if(mediaFormat!=null){
            this.mAudioMediaFormat = mediaFormat;
        }
        prepareEncoder();

    }


    private void prepareEncoder() throws IOException {

        mAudioBytesReceived = 0;
        mBufferInfo = new MediaCodec.BufferInfo();
        mMediaCodec = MediaCodec.createEncoderByType(mAudioMediaFormat.getString(MediaFormat.KEY_MIME));
        mMediaCodec.configure(mAudioMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
        mIsEncoding = true;
    }

    /**
     *
     *
     * 将录制的音频源数据送入MediaCodec编码
     *
     */
    private void encodeToMediaCodec(byte[] buffer,long presentationTimeStamp){
        if (mAudioBytesReceived == 0) {

            mAudioStartTime = presentationTimeStamp;

        }
        mAudioBytesReceived += buffer.length;

        if (mMediaCodec != null) {
            int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
            ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(buffer);
                long presentationTimeUs = (presentationTimeStamp - mAudioStartTime) / 1000;
                // Log.d("hsk","presentationTimeUs--"+presentationTimeUs);
                if (!mIsEncoding) {
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0,
                            buffer.length, presentationTimeUs,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    // closeEncoder(mAudioCodec, mAudioBufferInfo,
                    // mAudioTrackIndex);
                    // closeMuxer();
                    // encodingService.shutdown();
                    //closeEncode();
                } else {

                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0,
                            buffer.length, presentationTimeUs, 0);
                //   Log.d(LOG_TAG, "audio data is encoding...");
                }
				/*
				 * mMediaCodec.queueInputBuffer(inputBufferIndex, 0,
				 * this_buffer.length, presentationTimeStamp, 0);
				 */

            }

        }

    }

    @Override
    public void startMuxer(boolean muxerStart) {
        mMuxerStart = muxerStart;
    }

    @Override
    public void setMuxerListener(MuxerListener listener) {
        mMuxerListener = listener;
    }

    @Override
    public void setMediaMuxer(MediaMuxer mediaMuxer) {
        mMediaMuxer = mediaMuxer;
    }

    @Override
    public void release() {
       // mMediaCodec.queueInputBuffer(inputBufferIndex, 0,
         //       this_buffer.length, presentationTimeUs,
         //       MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        if(mMediaCodec!=null){
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
        if(mMuxerListener!=null){
            mMuxerListener.stopMuxer(RECORD_AUDIO);
            mMediaMuxer = null;
        }
    }
    /**
     *
     *
     * 将编码过后的数据写入MediaMuxer
     *
     */
    @Override
    public void encodeToTrack(int index) {

        ByteBuffer[] encoderOutputBuffers = mMediaCodec.getOutputBuffers();
        while(mRecording) {
            int encoderIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo,
                    100);
            if (encoderIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                if(mTackIndex == -1){
                    mTackIndex = mMediaMuxer.addTrack(mMediaCodec.getOutputFormat());
                    Log.d(LOG_TAG, "Audio tack_index is " + mTackIndex);
                    if (mMuxerListener != null) {
                        mMuxerStart = mMuxerListener.startMuxer(RECORD_AUDIO);
                    }
                }


            } else if (encoderIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break;
            } else if (encoderIndex > 0) {
                if (mMuxerStart) {
                    ByteBuffer enData = encoderOutputBuffers[encoderIndex];
                    if((mBufferInfo.flags&MediaCodec.BUFFER_FLAG_CODEC_CONFIG)!=0){
                        mBufferInfo.size = 0;
                    }
                    if(mBufferInfo.size == 0){
                        enData = null;
                    }
                    if(enData!=null){
                        enData.position(mBufferInfo.offset);
                        enData.limit(mBufferInfo.size + mBufferInfo.offset);
                        mBufferInfo.presentationTimeUs = getPTSUs();
                        prevOutputPTSUs = mBufferInfo.presentationTimeUs;
                        mMediaMuxer.writeSampleData(mTackIndex, enData, mBufferInfo);
                        Log.d(LOG_TAG,"audio data is writing to mediamuxer...");
                    }

                    mMediaCodec.releaseOutputBuffer(encoderIndex, false);
                    // 退出循环
                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {

                        break;

                    }
                }


                //mMediaCodec.releaseOutputBuffer(index,false);
            }
        }




    }

    @Override
    public void resetOutputFormat() {

    }

}
