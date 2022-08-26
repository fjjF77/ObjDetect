package com.example.learning1;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;
import org.pytorch.MemoryFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class ObjectModel_1 extends Model{
    Module model = null;
    ImgNetClass ImgClassName;
    int min_h=100,min_w=100;

    public ObjectModel_1(Context context,user_config cfg) throws IOException {
        super(context,cfg);
        if(cfg.getModel().equals("CNN") && cfg.getObj().equals("mask")) {
            Toast.makeText(context, "目前暂不支持该模型检测口罩，已切换成检测ImageNet物体", Toast.LENGTH_SHORT).show();
        }

        //model = Module.load(assetFilePath( context, "resnet18_1.pt"));
        //model = Module.load("file:///android_assets/resnet18_1.pt"); //dlopen failed: library "libpytorch_jni.so" not found
        model=LiteModuleLoader.load(assetFilePath(context,"resnet18_1.ptl"));
        ImgClassName=new ImgNetClass();
    }

    @Override
    public Tuple2<Boolean,Objects> finding(Bitmap bitmap){
        int img_h,img_w;
        img_h=bitmap.getHeight();
        img_w=bitmap.getWidth();

        // 图像金字塔+滑动窗口
        List<Integer> win_h_lst = new ArrayList<>(),win_w_lst = new ArrayList<>();
        float ratio=2.0f;
        int h=img_h,w=img_w;
        while(h>min_h) {
            win_h_lst.add(h);
            h = (int)((float)h/ratio);
        }
        while(w>min_w) {
            win_w_lst.add(w);
            w = (int)((float)w/ratio);
        }

        int step=100;
        int maxId = 0;
        float maxx=0;
        ansRect maxLoac=null;
        //匿名Comparator实现
        Comparator<ans_object> idComparator = new Comparator<ans_object>(){
            @Override
            public int compare(ans_object a1, ans_object a2) {
                return (int) (a2.prob - a1.prob); //从大到小排序应该是
            }
        };
        Queue<ans_object> maybe_anses=new PriorityQueue<>(idComparator);

        for(int win_h : win_h_lst){
            for(int win_w : win_w_lst){
                for(int crop_h=0;crop_h<=img_h-win_h;crop_h+=step){
                    for(int crop_w=0;crop_w<img_w-win_w;crop_w+=step){
                        Log.d("obj_find_test","滑动窗口位置:("+String.valueOf(crop_h)+","+String.valueOf(crop_w)+
                                ") 宽高:("+String.valueOf(win_w)+","+String.valueOf(win_h)+")");
                        Bitmap bitmap_temp=Bitmap.createBitmap(bitmap,crop_w,crop_h,win_w,win_h);
                        bitmap_temp=imageScale(bitmap_temp,win_w,win_h,min_w,min_h);
                        Tensor data_temp=TensorImageUtils.bitmapToFloat32Tensor(bitmap_temp,
                                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB, MemoryFormat.CHANNELS_LAST);
                        // resnet18预测
                        Tuple2<Integer,Float> pred_temp=predict(data_temp);
                        if(pred_temp.getSecond()>0.6f){
                            ans_object maybe_ans=new ans_object( ImgClassName.getStr(pred_temp.getFirst()),
                                    pred_temp.getSecond(),new ansRect(crop_w,crop_h,win_h,win_w));
                            maybe_anses.add(maybe_ans);
                        }

                        if(maxx<pred_temp.getSecond()){
                            maxx=pred_temp.getSecond();
                            maxId=pred_temp.getFirst();
                            maxLoac=new ansRect(crop_w,crop_h,win_h,win_w);
                        }
                    }
                }
            }
        }

        ans_object maybe_ans=new ans_object( ImgClassName.getStr(maxId),maxx,maxLoac);
        maybe_anses.add(maybe_ans);

        Objects true_ans=NMS(maybe_anses,bitmap);
        if(true_ans.getClasses().size()==0){
            return new Tuple2<Boolean,Objects>(false,null);
        }
        else{
            return new Tuple2<Boolean,Objects>(true,true_ans);
        }
    }

    private Tuple2<Integer, Float> predict(Tensor inputTensor){
        // resnet18预测
        Tensor outputTensor=model.forward(IValue.from(inputTensor)).toTensor();
        float[] scores = outputTensor.getDataAsFloatArray();
        float maxScore = -Float.MAX_VALUE;
        int maxScoreIdx = -1;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > maxScore) {
                maxScore = scores[i];
                maxScoreIdx = i;
            }
        }

        Tuple2<Integer, Float> ans=new Tuple2<>(maxScoreIdx,maxScore);
        if(ans.getSecond()==null) Log.d("obj_find_test","predict函数出现了一个空对象");
        return ans;
    }

    private Objects NMS(Queue<ans_object> anses,Bitmap img){ //非极大抑制
        float min_IOU=0.5f;
        List<String> classnames=new ArrayList<>();
        List<Float> probs=new ArrayList<>();
        List<ansRect> loacs=new ArrayList<>();

        while(anses.size()>0){
            ans_object cur_max=anses.poll();
            classnames.add(cur_max.name);
            probs.add(cur_max.prob);
            loacs.add(cur_max.loac);

            Iterator<ans_object> iter=anses.iterator();
            while(iter.hasNext()){
                ans_object aobj=iter.next();
                Float aobj_IOU=IOU(cur_max.loac,aobj.loac);
                if(aobj_IOU>min_IOU){
                    iter.remove();
                }
            }

            /*for(ans_object aobj:anses){ //遍历里删除元素会报错
                //java遍历优先队列 只能保证堆顶元素为最值，其他元素顺序不定
                Float aobj_IOU=IOU(cur_max.loac,aobj.loac);
                if(aobj_IOU>min_IOU){
                    anses.remove(aobj);
                }
            }*/
        }
        Objects true_ans=new Objects(classnames,loacs,probs,img);
        return true_ans;
    }

    private Float IOU(ansRect l1,ansRect l2){
        float S_jiao;
        if(l2.x>l1.x2() || l2.y>l1.y2() || l2.x2()<l1.x || l2.y2()<l1.y){
            S_jiao=0f;
        }
        else{
            float x1,x2,y1,y2;
            x1=Math.max(l1.x,l2.x);
            y1=Math.max(l1.y,l2.y);
            x2=Math.max(l1.x2(),l2.x2());
            y2=Math.max(l1.y2(),l2.y2());
            S_jiao=(x2-x1)*(y2-y1);
        }
        float S=Math.min(l1.s(),l2.s());
        return S_jiao/S;
    }

    private class ans_object{
        public String name;
        public Float prob;
        public ansRect loac;
        private ans_object(String name,Float prob,ansRect loac){
            this.name=name;
            this.prob=prob;
            this.loac=loac;
        }
    }
}


