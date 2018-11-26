package com.wangc.openglesdemo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * autour: wangc
 * date: 2018/11/26 16:52
 * 获取音频原始数据
*/
public class AudioRecordActivity extends AppCompatActivity {
    private static final String TAG = "wangc";
    private AudioRecordUtil audioRecordUtil;
    private File mAudioRecordFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);



        audioRecordUtil = new AudioRecordUtil();
        audioRecordUtil.setOnRecordListrner(new AudioRecordUtil.onRecordListrner() {
            @Override
            public void recordByte(byte[] audioSize, int size) {
                //得到原始音频数据
                Log.e(TAG,""+audioSize);
                //保存录音文件
                saveFile(audioSize,size);
            }
        });

        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordUtil.startRecord();
            }
        });

        findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecordUtil.stopRecord();
            }
        });

        findViewById(R.id.btn_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player(v);
            }
        });

    }

    /**
     * 保存录音文件
     * @param audioSize
     * @param size
     */
    private void saveFile(byte[] audioSize, int size) {

        //创建录音文件
        mAudioRecordFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/OpenGlEs/" + System.currentTimeMillis() + ".pcm");
        if (!mAudioRecordFile.getParentFile().exists())
            mAudioRecordFile.getParentFile().mkdirs();

        try {
            mAudioRecordFile.createNewFile();
            //创建文件输出流
            FileOutputStream mFileOutputStream = new FileOutputStream(mAudioRecordFile);
            mFileOutputStream.write(audioSize, 0, size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 播放声音
     * @param view
     */
    public void player(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                doPlay(mAudioRecordFile);
            }
        }).start();
    }

    private void doPlay(File audioFile) {
        if(audioFile !=null){
            Log.i("Tag8","go there");
            //配置播放器
            //音乐类型，扬声器播放
            int streamType= AudioManager.STREAM_MUSIC;
            //录音时采用的采样频率，所以播放时同样的采样频率
            int sampleRate=44100;
            //单声道，和录音时设置的一样
            int channelConfig=AudioFormat.CHANNEL_OUT_MONO;
            //录音时使用16bit，所以播放时同样采用该方式
            int audioFormat=AudioFormat.ENCODING_PCM_16BIT;
            //流模式
            int mode= AudioTrack.MODE_STREAM;

            //计算最小buffer大小
            int minBufferSize=AudioTrack.getMinBufferSize(sampleRate,channelConfig,audioFormat);

            //构造AudioTrack  不能小于AudioTrack的最低要求，也不能小于我们每次读的大小
            AudioTrack audioTrack=new AudioTrack(streamType,sampleRate,channelConfig,audioFormat,
                    Math.max(minBufferSize,2048),mode);

            //从文件流读数据
            FileInputStream inputStream=null;
            try{
                //循环读数据，写到播放器去播放
                inputStream=new FileInputStream(audioFile);

                //循环读数据，写到播放器去播放
                int read;
                //只要没读完，循环播放
                while ((read=inputStream.read(new byte[2048]))>0){
                    Log.i("Tag8","read:"+read);
                    int ret=audioTrack.write(new byte[2048],0,read);
                    //检查write的返回值，处理错误
                    switch (ret){
                        case AudioTrack.ERROR_INVALID_OPERATION:
                        case AudioTrack.ERROR_BAD_VALUE:
                        case AudioManager.ERROR_DEAD_OBJECT:
                            playFail();
                            return;
                        default:
                            break;
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
                //读取失败
                playFail();
            }finally {
                //关闭文件输入流
                if(inputStream !=null){
                    closeStream(inputStream);
                }
                //播放器释放
                resetQuietly(audioTrack);
            }
        }
    }

    /**
     * 播放失败
     */
    private void playFail() {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                tv_stream_msg.setText("播放失败");
//            }
//        });
        Log.e(TAG,"播放失败");
    }


    /**
     * 关闭输入流
     * @param inputStream
     */
    private void closeStream(FileInputStream inputStream){
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetQuietly(AudioTrack audioTrack) {
        try{
            audioTrack.stop();
            audioTrack.release();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
