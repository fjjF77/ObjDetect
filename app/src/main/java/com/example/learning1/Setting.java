package com.example.learning1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;

import java.io.Serializable;
import java.util.Objects;

public class Setting extends PreferenceActivity {
    //https://www.cnblogs.com/lenve/p/4770537.html
    //https://blog.csdn.net/nihaoqiulinhe/article/details/15341685
    //https://blog.csdn.net/weixin_31767183/article/details/77748421

    private user_config cfg=new user_config();

    private static String SetLog="SetLog";
    private static String PREFER_NAME="setting";
    private static int SettingResultCode=0;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(SetLog,"进入Setting类");
        getPreferenceManager().setSharedPreferencesName(PREFER_NAME);
        // 所的的值将会自动保存到SharePreferences
        addPreferencesFromResource(R.xml.setting);
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(this);

        String obj_kind=sp.getString("OBJ_LIST","all");
        String model_kind=sp.getString("MODEL_LIST","CNN"); //"yolov5"
        String use_gpu=sp.getString("DEVICE_LIST","cpu");
        Log.d(SetLog,"准备输出cfg成员变量 "+ obj_kind+" "+model_kind);
        cfg.setObj(obj_kind);
        cfg.setModel(model_kind);
        cfg.setUse_gpu( Objects.equals(use_gpu, "gpu") );

        Intent data=new Intent();
        Bundle b=new Bundle();
        b.putSerializable("cfg",(Serializable) this.cfg);
        data.putExtras(b);
        setResult(SettingResultCode,data);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference){
        SharedPreferences sp = preference.getSharedPreferences();
        this.cfg.setObj(sp.getString("OBJ_LIST","all"));
        this.cfg.setModel(sp.getString("MODEL_LIST","yolov5"));
        Log.d(SetLog,cfg.getObj().toString()+" "+cfg.getModel().toString());
        return true;
    }

    public void onCancel(View view)
    {
        Intent intent = new Intent(this , MainActivity.class);
        startActivity(intent);
    }

}