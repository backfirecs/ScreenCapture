package com.example.backfirecs.screencapture.record;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * @author chace
 * @date   2016/9/19
 * 屏幕录制器
 *
 */
public class ScreenRecorder extends MediaRecorder{

    private static final String LOG_TAG = ScreenRecorder.class.getSimpleName();

    private Surface mSurface;

    private VirtualDisplay mVirtualDisplay;

    private MediaProjection mMediaProjection;

    public ScreenRecorder() {
        super();
    }

    @Override
    public void run() {
        super.run();
        while(!mQuit.get()){
          //  int input_index = mMediaCodec.dequeueInputBuffer(-1);
         //   Log.d(LOG_TAG,"input_index "+input_index);
            //获取MediaCodec的Output缓冲池的数据
            int index = mMediaCodec.dequeueOutputBuffer(mBufferInfo,MediaDefaultConfig.VIDEO_TIMEOUT_US);

            if(index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                //为MediaMuxer添加VideoTrack
                resetOutputFormat();

            }else if(index == MediaCodec.INFO_TRY_AGAIN_LATER){
                //没有拿到数据，10ms后再去获取
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }else if(index>0){


                if (!mMuxerStart) {
                    throw new IllegalStateException("MediaMuxer dose not call addTrack(format) ");
                }
                //将数据写入MediaMuxer
                encodeToTrack(index);
                //释放MediaCodec的OutputBuffer
                mMediaCodec.releaseOutputBuffer(index, false);

            }


        }

        release();

    }

    @Override
    public void startRecord() {
        start();
    }

    @Override
    public void stopRecord() {

        mQuit.set(true);

    }

    @Override
    public void config(MediaFormat mediaFormat) throws IOException {

        if(mediaFormat==null){
            throw new NullPointerException("video MediaFormat is null");
        }

        mVideoMediaFormat = mediaFormat;
        //初始化编码器
        prepareEncoder(mediaFormat);
        //初始化VirtualDisplay
        prepareVirtualDisPlay();


    }
    /**
     *
     * 初始化VirtualDisPlay
     *
     *
     */
    private void prepareVirtualDisPlay(){
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen_record",
                mVideoMediaFormat.getInteger(MediaFormat.KEY_WIDTH),mVideoMediaFormat.getInteger(MediaFormat.KEY_HEIGHT),
                1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,mSurface,null,null);


    }

    /**
     *
     *
     * 初始化MediaCodec
     *
     */
    private void prepareEncoder(MediaFormat mediaFormat) throws IOException {
        String mimeType = mVideoMediaFormat.getString(MediaFormat.KEY_MIME);
        mMediaCodec = MediaCodec.createEncoderByType(mimeType);
        mMediaCodec.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        mSurface = mMediaCodec.createInputSurface();
        mMediaCodec.start();
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

        if(mediaMuxer == null){
            throw new NullPointerException("MediaMuxer is null");
        }

        mMediaMuxer = mediaMuxer;
    }

    @Override
    public void release() {
        if(mMediaCodec!=null){
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }

        if(mVirtualDisplay!=null){

            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }

        if(mMediaProjection!=null){
            mMediaProjection = null;
        }

        if(mMuxerListener!=null){
           mMuxerListener.stopMuxer(RECORD_SCREEN);
           mMediaMuxer = null;
        }
    }

    @Override
    public void resetOutputFormat() {
        if(mMuxerStart){

            throw new IllegalStateException("output format already changed");

        }

        MediaFormat format = mMediaCodec.getOutputFormat();
        mTackIndex = mMediaMuxer.addTrack(format);
       // Log.d(LOG_TAG,"screen tack_index is "+mTackIndex);
        mMuxerStart = mMuxerListener.startMuxer(RECORD_SCREEN);

    }

    /**
     *
     *
     * 将MediaCodec编码过后的byte流写入MediaMuxer
     *
     */
    @Override
    public void encodeToTrack(int index) {

        ByteBuffer enData = mMediaCodec.getOutputBuffer(index);
      //  Log.d(LOG_TAG, "BufferInfo flag is "+mBufferInfo.flags+" size is "+mBufferInfo.size);
        if((mBufferInfo.flags&MediaCodec.BUFFER_FLAG_CODEC_CONFIG)!=0){

            mBufferInfo.size = 0;
        }

        if(mBufferInfo.size==0){

            enData = null;

        }

        if(enData!=null){
            //mBufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
            enData.position(mBufferInfo.offset);
            enData.limit(mBufferInfo.size + mBufferInfo.offset);
            mBufferInfo.presentationTimeUs = getPTSUs();
            prevOutputPTSUs = mBufferInfo.presentationTimeUs;
            mMediaMuxer.writeSampleData(mTackIndex, enData, mBufferInfo);
           // Log.d(LOG_TAG, "mTackIndex is "+mTackIndex+" screen data is writing to mediamuxer...");
        }

    }

    public void setMediaProjection(MediaProjection _mediaProjection){

        if(_mediaProjection == null){

            throw new NullPointerException("mediaProjection is null");
        }

        this.mMediaProjection = _mediaProjection;
    }
}
