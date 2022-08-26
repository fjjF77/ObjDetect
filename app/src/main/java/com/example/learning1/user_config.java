package com.example.learning1;

import java.io.Serializable;

public class user_config implements Serializable {
    private String obj,model;
    private Boolean use_gpu;

    public user_config(){
       obj="all";
       model="yolov5";
       use_gpu=false;
    }

    public String getObj() {
        return obj;
    }
    public void setObj(String obj) {
        this.obj = obj;
    }

    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }

    public Boolean getUse_gpu() { return use_gpu; }
    public void setUse_gpu(Boolean use_gpu) { this.use_gpu = use_gpu; }


}
