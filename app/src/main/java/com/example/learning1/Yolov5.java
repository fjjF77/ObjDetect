package com.example.learning1;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Yolov5 extends Model{
    private YoloV5Ncnn yolov5ncnn;
    private boolean use_gpu;
    private int detect_flag;

    public Yolov5(Context context,user_config cfg) throws IOException {
        super(context,cfg);
        use_gpu=cfg.getUse_gpu();
        yolov5ncnn = new YoloV5Ncnn();
        if(cfg.getObj().equals("all")) detect_flag=0;
        else detect_flag=1;

        boolean ret_init = yolov5ncnn.Init(context.getResources().getAssets(),detect_flag);
        if (!ret_init)
        {
            Log.e("obj_find_test", "yolov5ncnn Init failed");
        }
    }

    @Override
    public Tuple2<Boolean,Objects> finding(Bitmap bitmap){
        YoloV5Ncnn.Obj[] objects = yolov5ncnn.Detect(bitmap, use_gpu,detect_flag);

        List<String> classes=new ArrayList<>();
        List<ansRect> loac=new ArrayList<>();
        List<Float> probs=new ArrayList<>();
        for (YoloV5Ncnn.Obj object : objects) {
            classes.add(object.label);
            loac.add(new ansRect(object.x, object.y, object.h, object.w));
            probs.add(object.prob);
        }
        if(classes.size()==0){
            Objects ans=null;
            return new Tuple2<Boolean,Objects>(false,ans);
        }
        else{
            Objects ans=new Objects(classes,loac,probs,bitmap);
            return new Tuple2<Boolean,Objects>(true,ans);
        }
    }
}
