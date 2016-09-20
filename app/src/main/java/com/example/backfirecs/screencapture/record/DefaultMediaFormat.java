package com.example.backfirecs.screencapture.record;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;


/**
 * @author chace
 * @date   2019/9/19
 * 获取默认MediaFormat
 *
 */
public class DefaultMediaFormat{

    public static MediaFormat getDefaultVideoFormat(){

        MediaFormat format = MediaFormat.createVideoFormat(MediaDefaultConfig.VIDEO_MIME_TYPE, MediaDefaultConfig.VIDEO_WIDTH, MediaDefaultConfig.VIDEO_HEIGHT);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, MediaDefaultConfig.VIDEO_BIT_RATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, MediaDefaultConfig.VIDEO_FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, MediaDefaultConfig.VIDEO_IFRAME_INTERVAL);


        return format;
    }

    public static MediaFormat getDefaultAudioFormat(){

        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, MediaDefaultConfig.AUDIO_MIME_TYPE);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, MediaDefaultConfig.AUDIO_SIMPLE_RATE);
        format.setInteger(MediaFormat.KEY_BIT_RATE, MediaDefaultConfig.AUDIO_BIT_RATE);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, MediaDefaultConfig.AUDIO_CHANNEL_COUNT);
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, MediaDefaultConfig.AUDIO_MAX_INPUT_SIZE);


        return format;

    }

}
