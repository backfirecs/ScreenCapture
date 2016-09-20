package com.example.backfirecs.screencapture.record;


import android.media.*;

/**
 * @author chace
 * @date   2019/9/19
 * 默认MediaFormat的一些配置
 *
 */
public class MediaDefaultConfig {


    public static final String VIDEO_MIME_TYPE = "video/avc"; // H.264 编码

    public static final int VIDEO_FRAME_RATE = 30; // 帧率

    public static final int VIDEO_IFRAME_INTERVAL = 10; //  I帧间隔

    public static final int VIDEO_TIMEOUT_US = 10000;

    public static final int VIDEO_BIT_RATE = 6000000;//码率

    public static final int VIDEO_HEIGHT = 720;//分辨率高度

    public static final int VIDEO_WIDTH = 1280;//分辨率宽度

    public static final int AUDIO_SOURCE = android.media.MediaRecorder.AudioSource.MIC;//音频采集来源

    public static final int AUDIO_SIMPLE_RATE = 44100;//采样率

    public static final int AUDIO_BIT_RATE = 128000;//码率

    public static final int AUDIO_CHANNEL_COUNT = 1;//声道

    public static final int AUDIO_MAX_INPUT_SIZE = 16384;

    public static final int AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;

    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    public static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";

    public static final int  AUDIO_BUFFERSIZEINBYTE = AudioRecord.getMinBufferSize(AUDIO_SIMPLE_RATE,AUDIO_CHANNEL_CONFIG,AUDIO_FORMAT);


}
