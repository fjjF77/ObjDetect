package com.example.learning1;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;

public class FindThread extends Thread {  //https://zhuanlan.zhihu.com/p/55372840
    Model model1;
    Bitmap bitmap;
    public Handler mhandler;
    private user_config cfg;

    public FindThread(Context context, Handler mhandler, user_config cfg, Bitmap img){
        super();
        this.mhandler=mhandler;
        this.bitmap=img;
        this.cfg=cfg;
        try{
            if(cfg.getModel().equals("CNN"))
                this.model1=new ObjectModel_1(context,cfg);
            else if(cfg.getModel().equals("yolov5"))
                this.model1=new Yolov5(context,cfg);
        }catch (IOException e){
            Log.d("obj_find_test","文件读取错误");
        }
    }

    public void run(){
        /* Looper.prepare();
        myLooper=Looper.myLooper();
        Looper.loop(); */

        //目标检测代码
        //检测完毕，发送结果
        Tuple2<Boolean,Objects> tuple=model1.finding(bitmap);
        ImgAns ans=new ImgAns(tuple);
        Message msg = Message.obtain();
        Bundle b=new Bundle();
        if(tuple.getFirst()){
            msg.arg1=1;
            b.putParcelable("bitmaps", (Parcelable) ans);
        }
        else{
            msg.arg1=0;
        }
        msg.setData(b);
        mhandler.sendMessage(msg);
    }

}
