package com.example.learning1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.Serializable;

public class MainActivity extends Activity {
    private static int SettingResultCode=0;
    private Button setting_bt,findObj_bt;
    private user_config cfg=new user_config();

    private static String MainLog="MainLog";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setting_bt=(Button) findViewById(R.id.setting_bt);
        findObj_bt=(Button) findViewById(R.id.findObj_bt);
        Onclick onclick=new Onclick();
        setting_bt.setOnClickListener(onclick);
        findObj_bt.setOnClickListener(onclick);
    }

    class Onclick implements View.OnClickListener{
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.setting_bt:
                    show_setting();
                    Log.d(MainLog,"设置 "+cfg.getObj()+" "+cfg.getModel());
                    break;
                case R.id.findObj_bt:
                    find_object();
                    break;
            }
        }

    }

    public void show_setting()
    {
        Toast.makeText(MainActivity.this,"changing setting",Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(this, Setting.class);
        startActivityForResult(intent,SettingResultCode);
    }

    public void find_object()
    {
        Toast toast=Toast.makeText(MainActivity.this,"finding object",Toast.LENGTH_SHORT);
        toast.show();

        Intent intent=new Intent(this, FindObject.class);
        Bundle b=new Bundle();
        b.putSerializable("cfg",(Serializable) cfg);
        intent.putExtras(b);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        if(data!=null)
        {
            if(resultCode==SettingResultCode)
            {
                Bundle b=data.getExtras();
                cfg=(user_config) b.getSerializable("cfg");
                Log.d(MainLog,"get cfg: "+cfg.getObj()+" "+cfg.getModel());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}