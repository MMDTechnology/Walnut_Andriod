package maojian.android.walnut;

/**
 * Created by android on 5/8/16.
 */

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CameraSurfacePreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public File mRecVedioPath;
    public File mRecAudioFile;

    // 0表示后置，1表示前置
    private int cameraPosition = 1;

    private boolean bool;

    private int hour = 0;
    private int minute = 0;     //计时专用
    private int second = 0;
    private MediaRecorder mediaRecorder;


    public CameraSurfacePreview(Context context) {
        super(context);
        //mCamera = camera;

        //mCamera = Camera.open();
        //Log.e("abc","debugdebug2 "+mCamera+" 33 "+camera);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {

        mRecVedioPath = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/walnut/video/temp/");
        if (!mRecVedioPath.exists()) {
            mRecVedioPath.mkdirs();
        }




        mCamera = Camera.open();



        Camera.Parameters p = mCamera.getParameters();

        List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPreviewSizes();

        for(int i=0;i<sizeList.size();i++) {
            Log.e("abc", "supported size: " + sizeList.get(i).width+"  "+sizeList.get(i).height);
        }

        //p.setPreviewFrameRate(5); // 每秒5帧
        //p.setPictureFormat(ImageFormat.JPEG);// 设置照片的输出格式
        //p.set("jpeg-quality", 85);// 照片质量

        Camera.Size previewSize = mCamera.new Size((int) getWidth()/9*16, (int) getWidth());
        Log.d("Dennis", "surfaceCreated() is called"+previewSize.width+" "+ previewSize.height);
        p.setPreviewSize(previewSize.width, previewSize.height);
        p.setPictureSize(previewSize.width, previewSize.height);
        mCamera.setParameters(p);

        try {
            // Open the Camera in preview mode
            //Log.e("abc","debugging "+mCamera);
            //if(mCamera ==null) mCamera = Camera.open();
            Log.e("abc","debugdebug00 "+mCamera);
            Camera.Parameters param = mCamera.getParameters();
            if(this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
                //如果是竖屏
                Log.e("abc","debugging portrait "+mCamera);
                param.set("orientation", "portrait");
                //在2.2以上可以使用
                mCamera.setDisplayOrientation(90);
            }else{
                Log.e("abc","debugging landscape "+mCamera);
                param.set("orientation", "landscape");
                //在2.2以上可以使用
                mCamera.setDisplayOrientation(0);
            }
            //mCamera = Camera.open();
            mCamera.setParameters(param);
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.d("Dennis", "Error setting camera preview: " + e.getMessage());
        }

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        Log.d("Dennis", "surfaceChanged() is called");

        try {
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("Dennis", "Error starting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        Log.d("Dennis", "surfaceDestroyed() is called");
    }

    public void cameraSwitch(){

        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数

        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息
            if (cameraPosition == 1) {
                // 现在是后置，变更为前置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    /**
                     * 记得释放camera，方便其他应用调用
                     */
                    releaseCamera();
                    // 打开当前选中的摄像头
                    mCamera = Camera.open(i);
                    // 通过surfaceview显示取景画面
                    //surfaceCreated(mHolder);
                    setStartPreview(mCamera,mHolder);
                    cameraPosition = 0;
                    break;
                }
            } else {
                // 现在是前置， 变更为后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    /**
                     * 记得释放camera，方便其他应用调用
                     */
                    releaseCamera();
                    mCamera = Camera.open(i);
                    //surfaceCreated(mHolder);
                    setStartPreview(mCamera,mHolder);
                    cameraPosition = 1;
                    break;
                }
            }

        }
    }

    private void setStartPreview(Camera camera,SurfaceHolder holder){
        try {
            Camera.Parameters param = mCamera.getParameters();
            if(this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
                //如果是竖屏
                Log.e("abc", "debugging portrait " + mCamera);
                //param.set("orientation", "portrait");
                //在2.2以上可以使用
                mCamera.setDisplayOrientation(90);
            }else{
                Log.e("abc", "debugging landscape " + mCamera);
                //param.set("orientation", "landscape");
                //在2.2以上可以使用
                mCamera.setDisplayOrientation(0);
            }
            //mCamera = Camera.open();
            mCamera.setParameters(param);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Log.e("abc", "Error starting camera preview: " + e.getMessage());
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();// 停掉原来摄像头的预览
            mCamera.release();
            mCamera = null;
        }
    }

    public void takePicture(PictureCallback imageCallback) {
        Log.e("abc","debugging camera0");
        mCamera.takePicture(null, null, imageCallback);
    }

    public void takeVideo() {
        Log.e("abc", "debugging camera0");
        //mCamera.takePicture(null, null, imageCallback);

//        mCamera.stopPreview();
//        mCamera.release();
//        mCamera = null;

        second = 0;
        minute = 0;
        hour = 0;
        bool = true;
        if(null==mediaRecorder){
            mediaRecorder = new MediaRecorder();
        }else {
            mediaRecorder.reset();
        }
        //表面设置显示记录媒体（视频）的预览




        mCamera.unlock();
        //给Recorder设置Camera对象，保证录像跟预览的方向保持一致
        mediaRecorder.setCamera(mCamera);
        //mediaRecorder.setOrientationHint(90);

        mediaRecorder.setPreviewDisplay(mHolder.getSurface());
        //开始捕捉和编码数据到setOutputFile（指定的文件）

        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        //设置用于录制的音源
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //设置在录制过程中产生的输出文件的格式
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //设置视频编码器，用于录制
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //设置audio的编码格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        //设置要捕获的视频的宽度和高度
        mediaRecorder.setVideoSize(1280, 720);
        //设置要捕获的视频帧速率
        mediaRecorder.setVideoFrameRate(5);

        try {
            mRecAudioFile = File.createTempFile("Vedio", ".mp4",
                    mRecVedioPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());

        try {
            mediaRecorder.prepare();



            handler.postDelayed(task, 1000);
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e("videodebug", "=====开始录制视频=====");

    }

    public void takeVideodone(PictureCallback imageCallback) {

        mediaRecorder.stop();

        mediaRecorder.release();
        mediaRecorder = null;

        mCamera.takePicture(null, null, imageCallback);

        Log.e("videodebug","takeVideodone");

        //return mRecAudioFile.getAbsolutePath();

    }

    private Handler handler = new Handler();
    private Runnable task = new Runnable() {
        public void run() {
            if (bool) {
                handler.postDelayed(this, 1000);
                second++;
                if (second >= 60) {
                    minute++;
                    second = second % 60;
                }
                if (minute >= 60) {
                    hour++;
                    minute = minute % 60;
                }

            }
        }
    };

}
