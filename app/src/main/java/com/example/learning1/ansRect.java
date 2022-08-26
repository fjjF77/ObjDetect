package com.example.learning1;

import androidx.annotation.NonNull;

public class ansRect {
    float x,y,h,w;
    public ansRect(float x,float y,float h,float w){
        this.x=x;
        this.y=y;
        this.h=h;
        this.w=w;
    }

    public float x2(){
        return x+w;
    }
    public float y2(){
        return y+h;
    }
    public float s(){
        return h*w;
    }

    @NonNull
    public String toString(){
        return "("+String.valueOf(x)+","+String.valueOf(y)+" ,"+String.valueOf(h)+","+String.valueOf(w)+")";
    }
}
