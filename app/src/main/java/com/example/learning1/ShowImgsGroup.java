package com.example.learning1;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Scroller;

import java.util.ArrayList;

public class ShowImgsGroup extends ViewGroup {
    //https://blog.csdn.net/zz51233273/article/details/107912256
    private static String obj_find_test="obj_find_test";
    private ArrayList<ImageView> imgs;
    private int onPageNum=3,ImgNum=0;
    private GestureDetector gestureDetector;
    private Scroller mScroller=new Scroller(getContext()); //用于平滑过渡
    private int nowImgNum=0;  //当前显示哪一张图片

    public ShowImgsGroup(Context context){
        super(context);
        init();
    }
    public ShowImgsGroup(Context context,AttributeSet attrs){
        super(context,attrs);
        init();
    }
    public ShowImgsGroup(Context context,AttributeSet attrs,int defStyle){
        super(context,attrs,defStyle);
        init();
    }
    public ShowImgsGroup(Context context, int onPageNum){
        super(context);
        this.onPageNum=onPageNum;
        init();
    }

    private void init(){
        gestureDetector=new GestureDetector(new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                    float distanceX, float distanceY) {
                //X方向滑动，view跟着滑动
                scrollBy((int)distanceX/onPageNum,0);
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });
    }

    public void addImg(Bitmap bitmap){
        ImageView view=new ImageView(getContext());
        view.setImageBitmap(bitmap);
        view.setScaleType(ImageView.ScaleType.FIT_XY);
        view.bringToFront();
        view.setVisibility(View.VISIBLE);
        this.addView(view);
        ImgNum++;
        Log.d(obj_find_test,"图片展示栏添加一张图片");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b){
        int ChildNum=getChildCount();
        Log.d(obj_find_test,"ChildNum is "+String.valueOf(ChildNum));
        int paddingPic=10;
        for(int i=0;i<ChildNum;i+=onPageNum){
            for(int j=0;j<onPageNum;j++){
                if(i+j>=ChildNum) break;
                Log.d(obj_find_test,"正展示第"+String.valueOf(i)+"组"+"第"+String.valueOf(j)+"张图片");
                View childView=getChildAt(i+j);
                //layout 相对于父布局的位置
                childView.layout(i/onPageNum*getWidth()+j*getWidth()/onPageNum+paddingPic ,0 ,i/onPageNum*getWidth()+(j+1)*getWidth()/onPageNum-paddingPic ,getHeight());
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (getChildCount()==0) return false;
        //将触摸事件传递手势识别器
        float lastPosX=0f;
        gestureDetector.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastPosX=event.getX();
                break;
            case MotionEvent.ACTION_UP: //删除
                //lastPosX=event.getX();
                move();
                /*if(nowImgNum>0 && nowImgNum<=ImgNum){
                    View v=getChildAt(nowImgNum);
                    if(v!=null){
                        this.removeView(v);
                        ImgNum--;
                        if(nowImgNum>ImgNum) nowImgNum=ImgNum;
                    }
                }*/
                break;
            case MotionEvent.ACTION_MOVE:
                if(lastPosX<event.getX()){  //手指向右滑
                    nowImgNum=(getScrollX()+getWidth()/(3*onPageNum))/(getWidth()/onPageNum);
                }else{      //手指向左滑
                    nowImgNum=(getScrollX()+2*getWidth()/(3*onPageNum))/(getWidth()/onPageNum);
                }
                if(nowImgNum>=ImgNum)nowImgNum=ImgNum-1;
                if(nowImgNum<0) nowImgNum=0;
                break;
        }
        return true;
    }

    //移动页面
    private void move(){
        mScroller.startScroll(getScrollX(),0,nowImgNum*getWidth()/onPageNum-getScrollX(),0);
        invalidate();   //使用invalidate会执行回调方法computeScroll
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            postInvalidateDelayed(10);
        }
    }
}
