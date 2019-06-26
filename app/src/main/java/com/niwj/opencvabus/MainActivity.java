package com.niwj.opencvabus;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    /**
     * 展示原图和对比图
     */
    private ImageView iv1, iv2;


    /**
     * 原图和对比图
     */
    private Bitmap bmp1, bmp2;


    /**
     * 拍摄原图、拍摄对比图、对比、自动对比按钮
     */
    private Button pz1, pz2, db, zddb;


    /**
     * 显示相似度（完全相同值为1）
     */
    private TextView tv;


    /**
     * CV相机
     */
    private CameraBridgeViewBase mCVCamera;

    /**
     * 加载OpenCV的回调
     */
    private BaseLoaderCallback mLoaderCallback;

    /**
     * 拍照状态 0:不拍照 ，1:拍原图，2:拍对比图，3:拍对比图并自动对比
     */
    private int isTakePhoto = 0;

    /**
     * 用于定时执行图片对比
     */
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        /**
         * 初始化
         */
        iv1 = findViewById(R.id.iv1);
        iv2 = findViewById(R.id.iv2);
        pz1 = findViewById(R.id.pz1);
        pz2 = findViewById(R.id.pz2);
        db = findViewById(R.id.db);
        zddb = findViewById(R.id.zddb);
        tv = findViewById(R.id.tv);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg); // 自动拍照并对比
                isTakePhoto = 3;
            }
        };


        View.OnClickListener btnOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.pz1: // 拍原图
                        isTakePhoto = 1;
                        break;
                    case R.id.pz2: // 拍对比图
                        isTakePhoto = 2;
                        break;
                    case R.id.db:
                        hist();
                        break;
                    case R.id.zddb: // 拍对比图并自动对比
                        isTakePhoto = 3;
                        break;
                }
            }
        };
        // 设置点击事件
        pz1.setOnClickListener(btnOnClick);
        pz2.setOnClickListener(btnOnClick);
        db.setOnClickListener(btnOnClick);
        zddb.setOnClickListener(btnOnClick);


//        初始化CV相机
        mCVCamera = findViewById(R.id.cv);
        mCVCamera.setVisibility(CameraBridgeViewBase.VISIBLE);

//        设置相机监听
        mCVCamera.setCvCameraViewListener(this);
        mLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        mCVCamera.enableView();
                        break;
                    default:
                        break;
                }
            }
        };

    }

    private void hist() { // 对比算法会耗时，导致页面卡顿，所以新开线程进行对比
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 对比
                final double target = HistUtils.comPareHist(bmp1, bmp2);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 将相似度显示在左上角
                        tv.setText("相似度：" + target);
                    }
                });
            }
        }).start();
    }


    //    相机启动
    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    //    相机销毁
    @Override
    public void onCameraViewStopped() {

    }

    //    相机工作时调用，参数是相机每一帧的图像，实时对比就在这个方法中进行
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {//相机拍摄每一帧的图像，都在此处理
        // 获取相机中的图像
        final Mat rgba = inputFrame.rgba();
        if (isTakePhoto != 0) { // 记录拍照状态
            final int who = isTakePhoto; // 重置拍照状态
            isTakePhoto = 0; // 要把Mat对象转换成Bitmap对象，需要创建一个宽高相同的Bitmap对象昨晚参数
            final Bitmap bmp = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.RGB_565); // 记录要展示图片的ImageView
            ImageView iv = null; // Mat >>> Bitmap
            Utils.matToBitmap(rgba, bmp);
            if (who == 1) { // 展示原图
                iv = iv1;
                bmp1 = bmp;
            } else if (who == 2) { // 展示对比图
                iv = iv2;
                bmp2 = bmp;
            } else { // 展示对比图
                iv = iv2;
                bmp2 = bmp; // 对比
                hist(); // 每隔0.5秒对比一次
                handler.sendEmptyMessageDelayed(1, 500);
            } // 记录要展示图片的ImageView
            final ImageView image = iv;
            runOnUiThread(new Runnable() {
                @Override
                public void run() { // 展示拍到的图片
                    image.setImageBitmap(bmp);
                    if (bmp1 != null) { // 如果原图已经拍好了，那么可以进行自动对比，将自动对比按钮设置为可用
                        zddb.setEnabled(true);
                        if (bmp2 != null) { // 如果原图和对比图都已经拍好了，那么可以进行对比，将对比按钮设置为可用
                            db.setEnabled(true);
                        }
                    }
                }
            });
        } // 将每一帧的图像展示在界面上


        return inputFrame.rgba();
    }

    @Override
    protected void onResume() {
        // 界面加载完成的时候向OpenCV的连接回调发送连接成功的信号
        if (OpenCVLoader.initDebug()) {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 销毁OpenCV相机
        if (mCVCamera != null)
            mCVCamera.disableView();
    }
}
