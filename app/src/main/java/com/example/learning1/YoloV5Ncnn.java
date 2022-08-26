package com.example.learning1;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

public class YoloV5Ncnn {

    public native boolean Init(AssetManager mgr,int flag);

    public class Obj
    {
        public float x;
        public float y;
        public float w;
        public float h;
        public String label;
        public float prob;
    }

    public native Obj[] Detect(Bitmap bitmap, boolean use_gpu, int flag);

    static {
        System.loadLibrary("yolov5ncnn");
    }
}
