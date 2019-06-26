package com.niwj.opencvabus;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * Created by Administrator on 2019/6/25.
 */
public class ONE {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
 //用来调用OpenCV库文件,必须添加

    public static void main(String args[]) {
        Mat anima = Imgcodecs.imread("F:/Androidworkplace/openCVABUS/app/src/main/res/mipmap-xhdpi/sample1.jpg", Imgcodecs.IMREAD_UNCHANGED);  //获取原图
        Range range = new Range(250, 800);
        Rect rect = new Rect(804, 129, 874, 652);
        Mat ROI = new Mat(anima, rect);
        Mat ROI1 = new Mat(anima, range);

        Imgcodecs.imwrite("F:/Androidworkplace/openCVABUS/app/src/main/res/mipmap-xhdpi/sample1_1.jpg", ROI);
        Imgcodecs.imwrite("F:/Androidworkplace/openCVABUS/app/src/main/res/mipmap-xhdpi/sample1_2.jpg", ROI1);
    }

}
