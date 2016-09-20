package com.example.backfirecs.screencapture;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.backfirecs.screencapture.record.RecordMediator;


/**
 * @author chace
 * @Date 2016/9/19
 * 1.主要实现了android5.0 及以上版本的屏幕采集功能，并且同步录制MIC的音频与视频合成最后生成一个MP4
 * 文件
 *
 * 2.使用到了sd卡写权限与录音权限，这两个权限在6.0及以上版本都算是危险权限，所以有针对它们做动态权限
 * 的处理以兼容6.0级以上版本
 *
 * 3.主要实现方式是通过MediaProjection配合VirtualDisplay采集屏幕，AudioRecord采集MIC的音频，通过
 * MediaCodec编码后将数据写入MediaMuxer，由MediaMuxer来生成MP4文件。
 *
 *4.屏幕采集类ScreenRecord与音频采集类TBAudioRecord两个类因为需要混合的关系耦合比较大，所以录制模块
 * 采用了中介者模式，具体中介者为RecordMediator
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private Button mRecordBtu;

    private MediaProjectionManager mediaProjectionManager;

    private RecordMediator mRecordMediator;

    private MediaProjection mediaProjection;

    private static final int WRITE_EXTERNAL_PERMISSION_REQUEST_CODE = 1515;

    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1516;

    private static final int RECORD_AUDIO_AND_WRITE_EXTERNAL_PERMISSION_REQUEST_CODE = 100;

    private static final int SCREEN_CAPTURE_REQUEST_CODE = 15;

    private boolean mAudioRecordEnable = false;

    private boolean mWriteExternalEnable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        bindListener();
    }

    private void bindListener() {

        mRecordBtu.setOnClickListener(this);

    }

    private void init() {

        mRecordBtu = (Button) findViewById(R.id.record_btu);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.record_btu:
                if(mRecordMediator!=null&&mRecordMediator.isRecording()){
                    stopRecord();
                    mRecordBtu.setText(getText(R.string.record_start));
                }else{

                    startRecord();
                    mRecordBtu.setText(getText(R.string.record_end));

                }

                break;


        }
    }

    /**
     *调用系统的屏幕录制接口与调用相机的方法相似，通过getSystemService()获取到MediaProjectionManager
     * 之后通过获取到MediaProjectionManager的createScreenCaptureIntent()方法获取到Intent,通过这个
     * Intent去隐式的调用询问用户是否允许屏幕录制的Activity.
     */
    private void startRecord() {

        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent intent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(intent,SCREEN_CAPTURE_REQUEST_CODE);

    }

    private void stopRecord(){
       mRecordMediator.stopRecord();
      //  screenRecorder2.quit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode!=SCREEN_CAPTURE_REQUEST_CODE){
            return;
        }

        if(resultCode!=RESULT_OK){
            return;
        }


        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);

        if(mediaProjection == null){

            return;
        }

        //判断当前设备的android版本是否是6.0及以上版本
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            //检查是否具有sd卡写权限
            mWriteExternalEnable = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            //检查是否具有录音权限
            mAudioRecordEnable = (ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);

            //如果有录音权限，没有sd卡写权限就请求sd卡写权限
            if(!mWriteExternalEnable&&mAudioRecordEnable){

                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_EXTERNAL_PERMISSION_REQUEST_CODE);
                return;
            }

            //如果有sd卡写权限，没有录音权限就请求录音权限
            if(!mAudioRecordEnable&&mWriteExternalEnable){

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_REQUEST_CODE);
                return;
            }

            //如果都没有就都去请求
            if(!mAudioRecordEnable&&!mWriteExternalEnable){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},RECORD_AUDIO_AND_WRITE_EXTERNAL_PERMISSION_REQUEST_CODE);
                return;
            }

            if(!mWriteExternalEnable||!mAudioRecordEnable){
                return ;
            }


        }
        //如果不是6.0及以上版本或者权限都已拥有就直接开始屏幕录制
        mRecordMediator = new RecordMediator(null,null,null,mediaProjection);
        mRecordMediator.config();
        mRecordMediator.startRecord();

    }


    /**
     *请求权限的结果会在这个方法返回
     *
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == WRITE_EXTERNAL_PERMISSION_REQUEST_CODE&&grantResults.length>1){
            int grantResult = grantResults[0];
            mWriteExternalEnable = (grantResult == PackageManager.PERMISSION_GRANTED);


        }
        if(requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE&&grantResults.length>1){
            int grantResult = grantResults[0];
            mAudioRecordEnable = (grantResult == PackageManager.PERMISSION_GRANTED);

        }

        if(requestCode == RECORD_AUDIO_AND_WRITE_EXTERNAL_PERMISSION_REQUEST_CODE&&grantResults.length>1){
            int grantResult1 = grantResults[0];
            int grantResult2 = grantResults[1];
            mWriteExternalEnable = (grantResult1 == PackageManager.PERMISSION_GRANTED);
            mAudioRecordEnable = (grantResult2 == PackageManager.PERMISSION_GRANTED);
        }
        //权限全部请求到后就可以开始录制屏幕了
        if(mWriteExternalEnable&&mAudioRecordEnable){
            mRecordMediator = new RecordMediator(null,null,null,mediaProjection);
            mRecordMediator.config();
            mRecordMediator.startRecord();
        }
    }
}
