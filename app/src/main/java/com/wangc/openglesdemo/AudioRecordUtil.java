package com.wangc.openglesdemo;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Created by wangc on 2018/11/26
 * E-MAIL:274281610@QQ.COM
 */
public class AudioRecordUtil {
    private AudioRecord audioRecord;
    private int bufferSizeInBytes;
    private boolean start = false;
    public int size = 0;

    public void setOnRecordListrner(AudioRecordUtil.onRecordListrner onRecordListrner) {
        this.onRecordListrner = onRecordListrner;
    }

    private onRecordListrner onRecordListrner;

    public AudioRecordUtil(){
        bufferSizeInBytes = AudioRecord.getMinBufferSize(44100,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSizeInBytes);
    }

    public void startRecord(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                audioRecord.startRecording();
                start = true;
                while (start){
                    byte[] audiodata = new byte[bufferSizeInBytes];
                    size =  audioRecord.read(audiodata,0,bufferSizeInBytes);
                    if (onRecordListrner != null){
                        onRecordListrner.recordByte(audiodata,size);
                    }
                }

                if (audioRecord != null){
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                }
            }
        }).start();
    }

    public void stopRecord(){
        start = false;
    }

    public interface onRecordListrner{
        void recordByte(byte[] audioSize,int size);
    }
}
