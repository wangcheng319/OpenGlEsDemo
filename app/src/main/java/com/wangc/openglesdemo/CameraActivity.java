package com.wangc.openglesdemo;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * autour: wangc
 * date: 2018/11/26 15:10
 * Camera采集原始数据
*/
public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";
    private Camera mCamera;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        FrameLayout frameLayout = findViewById(R.id.fl);
        frameLayout.addView(new MyPreview(this));

        initCamera();

        //拍照
        findViewById(R.id.btn_take).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在捕获图片前进行自动对焦
                mCamera.autoFocus(new Camera.AutoFocusCallback() {

                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        // 从Camera捕获图片，获取一帧数据
                        mCamera.takePicture(null, null, mPicture);
                    }
                });
            }
        });
    }


    /**
     * 获取相机
     */
    private void initCamera() {
        try {
            mCamera = Camera.open();
            //这种方式每一帧都会创建一个新的buffer，进行存储帧数据，这样不断开辟和回收内存，GC会很频繁，效率很低（内存抖动）。
            mCamera.setPreviewCallback(previewCallback);
            //这种方式预先预定一个buffer，然后更换里面的内容，减小了内存抖动
//            mCamera.setPreviewCallbackWithBuffer(previewCallback);
        }catch (RuntimeException r){
            Log.e(TAG,"获取相机失败");
        }

    }

    /**
     * 预览回调
     */
    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            //这里获取相机采集的原始数据，对数据进行编解码都是从这里开始
            Log.e(TAG,"每一帧："+data);
        }
    };


    /**
     * 拍照回调
     */
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.e(TAG,""+data);
            // 获取Jpeg图片，并保存在sd卡上
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d("+++", "Error creating media file, check storage permissions: " );
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Log.e(TAG,"保存成功");
            } catch (FileNotFoundException e) {
                Log.d("+++", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("+++", "Error accessing file: " + e.getMessage());
            }
        }
    };

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }


    /**
     * 相机预览
     */
    private class MyPreview extends SurfaceView implements SurfaceHolder.Callback{

        private SurfaceHolder mHolder;

        public MyPreview(Context context) {
            super(context);

            mHolder = getHolder();
            mHolder.addCallback(this);

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.setDisplayOrientation(90);
                Camera.Parameters parameters = mCamera.getParameters();
                //设置预览数据格式
                parameters.setPreviewFormat(ImageFormat.NV21);
                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(TAG, "预览失败");
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    @Override
    protected void onDestroy() {
        // 回收Camera资源
        if(mCamera!=null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera=null;
        }
        super.onDestroy();
    }
}
