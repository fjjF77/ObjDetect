package com.example.learning1;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class FindObject extends AppCompatActivity {
    private static String obj_find_test="obj_find_test";
    public static final int PICK_PHOTO=50;
    public static final int TAKE_CAMERA=51;

    private user_config cfg;

    private FrameLayout ImageFrame;
    private ImageView ImageSelect,FindingImg;
    private ShowImgsGroup ImgGroup;
    private TextView PutImgMeg,SettingText;
    private Button TakePhoto_bt,SelectPhoto_bt,FindObj_bt,SaveImg_bt;

    private Uri CameraImgUri;
    private String ImagePath;
    private boolean HasImg=false,HasdetectedImg=false;
    private float[] anim_lastPosition=new float[2];
    private int ShowImg_w,ShowImg_h;
    private float ShowImg_x,ShowImg_y;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_object);
        //https://blog.csdn.net/jj120522/article/details/8467810
        ImageFrame=(FrameLayout) findViewById(R.id.ImageFrame);
        ImageSelect=(ImageView) findViewById(R.id.ImageSelect);
        FindingImg=(ImageView) findViewById(R.id.FindingImg);
        //PutImgMeg=(TextView) findViewById(R.id.PutImgMeg);
        TakePhoto_bt=(Button) findViewById(R.id.TakePhoto_bt);
        SelectPhoto_bt=(Button) findViewById(R.id.SelectPhoto_bt);
        FindObj_bt=(Button) findViewById(R.id.FindObj_bt);
        //SettingText=(TextView) findViewById(R.id.setting_text);
        SaveImg_bt=(Button) findViewById(R.id.Saveimg_bt);

        ImgGroup=(ShowImgsGroup) findViewById(R.id.ImgGroup);
        RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
        params.addRule(RelativeLayout.BELOW,R.id.ButtonGrid);
        params.topMargin=50;
        ImgGroup.setBackgroundColor(getResources().getColor(R.color.aliceblue));
        ImgGroup.setLayoutParams(params);

        Onclick onclick=new Onclick();
        TakePhoto_bt.setOnClickListener(onclick);
        SelectPhoto_bt.setOnClickListener(onclick);
        FindObj_bt.setOnClickListener(onclick);
        SaveImg_bt.setOnClickListener(onclick);
        Log.d(obj_find_test,"进入目标检测界面");

        Intent setting_data=this.getIntent();
        Bundle b=setting_data.getExtras();
        cfg=(user_config) b.getSerializable("cfg");
        Log.d(obj_find_test,"获得设置 "+cfg.getObj().toString()+" "+cfg.getModel().toString());
        String device;
        if(cfg.getUse_gpu()) device="gpu";
        else device="cpu";
        String textview_msg="检测目标:"+cfg.getObj()+" 模型:"+cfg.getModel()+" 设备:"+device;
        SettingText.setText(textview_msg);
    }

    class Onclick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.TakePhoto_bt:
                    take_photo(v);
                    break;
                case R.id.SelectPhoto_bt:
                    Log.d(obj_find_test,"按钮选择图片");
                    select_fig(v);
                    break;
                case R.id.FindObj_bt:
                   find_object(v);
                   break;
                case R.id.Saveimg_bt:
                    save_image(v);
                    break;
                default:
                    break;
            }
        }
    }
    // https://blog.csdn.net/qianfeifeio/article/details/93474391
    public void take_photo(View view)
    {
        Log.d(obj_find_test,"正在拍照");
        // 创建File对象，用于存储拍照后的图片
        //存放在手机SD卡的应用关联缓存目录下
        File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
        //从Android 6.0系统开始，读写SD卡被列为了危险权限，如果将图片存放在SD卡的任何其他目录，都要进行运行时权限处理才行，而使用应用关联 目录则可以跳过这一步
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /* 7.0系统开始，直接使用本地真实路径的Uri被认为是不安全的，会抛 出一个FileUriExposedException异常。
           而FileProvider则是一种特殊的内容提供器，它使用了和内容提供器类似的机制来对数据进行保护，
           可以选择性地将封装过的Uri共享给外部，从而提高了 应用的安全性 */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //大于等于版本24（7.0）的场合
            //https://juejin.cn/post/7009204672225345549
            CameraImgUri = FileProvider.getUriForFile(FindObject.this, "com.example.learning1.fileprovider", outputImage);
        } else {
            //小于android 版本7.0（24）的场合
            CameraImgUri = Uri.fromFile(outputImage);
        }

        //启动相机程序
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, CameraImgUri);
        startActivityForResult(intent, TAKE_CAMERA);
    }

    public void select_fig(View view) //https://cloud.tencent.com/developer/article/1888341
    {
        if (ContextCompat.checkSelfPermission(FindObject.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(FindObject.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            Log.d(obj_find_test,"写权限关闭，正在申请权限，申请结果为："+ContextCompat.checkSelfPermission(FindObject.this, Manifest.permission.WRITE_EXTERNAL_STORAGE));
        }
        if (ContextCompat.checkSelfPermission(FindObject.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            Log.d(obj_find_test,"读权限关闭，正在申请权限，申请结果为："+ContextCompat.checkSelfPermission(FindObject.this, Manifest.permission.READ_EXTERNAL_STORAGE));
            ActivityCompat.requestPermissions(FindObject.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},2);
        }

        Log.d(obj_find_test,"已有读取相册权限，正启动相册");
        //执行启动相册的方法
        openAlbum();
    }

    public void openAlbum()
    {
        //打开相册
        Log.d(obj_find_test,"正在打开相册");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        Log.d(obj_find_test,"获取全部图片");
        Log.d(obj_find_test,"准备启动该活动");
        startActivityForResult(intent, PICK_PHOTO); //凑合着用吧，底下那些替代方案我也不会
        /*ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Log.d(obj_find_test,"进入onActivityResult函数");
                        int resultCode=result.getResultCode();
                        Intent data=result.getData();
                        Log.d(obj_find_test,"判断");
                        if(resultCode==RESULT_OK && data!=null){
                            Toast.makeText(FindObject.this,"获取Uri",Toast.LENGTH_SHORT).show();
                            if(Build.VERSION.SDK_INT>=19) handImage(data);
                            else handImageLow(data);
                        }
                    }
                });
        intentActivityResultLauncher.launch(intent);
        @RequiresApi(api=Build.VERSION_CODES.KITKAT)
        private void handImage(Intent data){
            String path=null;
            Uri uri=data.getData();
        }
        */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return; //Handle error
        if (requestCode == PICK_PHOTO) {
            if (Build.VERSION.SDK_INT >= 19) {
                // 4.4及以上系统使用这个方法处理图片
                handleImageOnKitKat(data);
            } else {
                // 4.4以下系统使用这个方法处理图片
                handleImageBeforeKitKat(data);
            }
        }
        else if(requestCode == TAKE_CAMERA){
            try {
                // 将拍摄的照片显示出来
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(CameraImgUri));
                ImageSelect.setImageBitmap(bitmap);
                ImageSelect.bringToFront();
                HasImg=true;
                HasdetectedImg=false;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data)
    {
        ImagePath=null;
        Uri uri=data.getData();
        Log.d(obj_find_test,"高版本，uri: "+uri);
        if(DocumentsContract.isDocumentUri(this,uri)) { // 如果是document类型的Uri，则通过document id处理
            //https://blog.csdn.net/yancychas/article/details/76695136
            String document_id = DocumentsContract.getDocumentId(uri);
            Log.d(obj_find_test, "获得document类型的uri: " + document_id);
            Log.d(obj_find_test, "uri.getAuthority() :" + uri.getAuthority());
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) //从自带文件管理器中获取
            {
                String id = document_id.split(":")[1];
                // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                ImagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                Log.d(obj_find_test,"media: "+ImagePath);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content: //downloads/public_downloads"), Long.valueOf(document_id));
                ImagePath = getImagePath(contentUri, null);
                Log.d(obj_find_test, "downloads, " + Long.valueOf(document_id));
            } else if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                final String[] split = document_id.split(":");
                final String root = Environment.getExternalStorageDirectory().getParent().split("/")[1];
                // 应该是 /storage/15E6-071D/DCIM/002.jpg
                ImagePath = "/" + root + "/" + split[0] + "/" + split[1];  //无奈之举，以后需要改正
                Log.d(obj_find_test, "外部存储: " + ImagePath); //
            }
        }
        else if("content".equalsIgnoreCase(uri.getScheme()))  // 如果是content类型的Uri，则使用普通方式处理
        {
            Log.d(obj_find_test,"uri类型为content");
            ImagePath = getImagePath(uri, null);
        }
        else if("file".equalsIgnoreCase(uri.getScheme()))  // 如果是file类型的Uri，直接获取图片路径即可
        {
            Log.d(obj_find_test,"uri类型为file");
            ImagePath = uri.getPath();
        }
        else
            Log.d(obj_find_test,"找不到匹配的类型 "+uri.getScheme());

        displayImage(ImagePath);
    }
    private void handleImageBeforeKitKat(Intent data)
    {
        Uri uri=data.getData();
        Log.d(obj_find_test,"低版本，uri: "+uri);
        ImagePath = getImagePath(uri,null);
        displayImage(ImagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Log.d(obj_find_test,"列索引名称："+MediaStore.Images.Media.DATA);
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                Log.d(obj_find_test,"bitmap: "+bitmap);
                ImageSelect.setImageBitmap(bitmap);
                ImageSelect.bringToFront(); //图片置于最上层
                HasImg=true;
                HasdetectedImg=false;
            }catch(OutOfMemoryError e){
                Log.d(obj_find_test,"图片太大，内存不足");
            }catch (Exception e){
                Log.d(obj_find_test, String.valueOf(e));
            }
        } else {
            Toast.makeText(this, "获取相册图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void find_object(View view){
        if(!HasImg){
            Toast.makeText(this,"请先选择一张图片",Toast.LENGTH_SHORT).show();
            return ;
        }
        else{
            final boolean[] loupe_end = {false}; //放大镜停止徘徊
            //多线程执行目标检测代码
            @SuppressLint("HandlerLeak") Handler recvHandle=new Handler(){ // 子线程向主线程传值
                @SuppressLint("HandlerLeak")
                @Override
                public void handleMessage(Message msg){
                    super.handleMessage(msg);
                    if(msg.arg1!=0){
                        ImgAns ans=msg.getData().getParcelable("bitmaps");
                        Tuple2<Boolean,Objects> ans_imgs=ans.getAns();
                        loupe_end[0]=true;
                        Toast.makeText(FindObject.this,"检测完毕",Toast.LENGTH_SHORT).show();
                        Log.d(obj_find_test,"检测完毕，检测到以下物体:");
                        Objects obj_temp=ans_imgs.getSecond();
                        for(int i=0;i<obj_temp.getClasses().size();i++)
                        {
                            Log.d(obj_find_test,obj_temp.getClasses(i)+" "+obj_temp.getLoactions(i).toString()+" "+obj_temp.getProbs(i));
                        }
                        ImageSelect.setImageBitmap(obj_temp.getAnsimg());
                        ImgGroup.addImg(obj_temp.getAnsimg());
                    }
                    else{
                        loupe_end[0]=true;
                        Log.d(obj_find_test,"检测完毕，未检测到物体");
                        Toast.makeText(FindObject.this,"未检测到物体",Toast.LENGTH_SHORT).show();
                    }
                    HasdetectedImg=true;
                }
            };

            Bitmap img=((BitmapDrawable)ImageSelect.getDrawable()).getBitmap();
            img=imageScale(img,ImageSelect.getWidth(),ImageSelect.getHeight());
            FindThread find_thread=new FindThread(FindObject.this,recvHandle,cfg,img); //handle
            find_thread.start();

            //https://zhuanlan.zhihu.com/p/55372840
            //https://beluga.blog.csdn.net/article/details/81624442?spm=1001.2101.3001.6661.1&utm_medium=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-81624442-blog-117316660.pc_relevant_multi_platform_featuressortv2dupreplace&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-81624442-blog-117316660.pc_relevant_multi_platform_featuressortv2dupreplace&utm_relevant_index=1
            //主线程控制图片来回徘徊

            ShowImg_w=ImageFrame.getWidth()-FindingImg.getWidth();
            ShowImg_h=ImageFrame.getHeight()-FindingImg.getHeight();
            ShowImg_x=ImageSelect.getX();
            ShowImg_y=ImageSelect.getY();
            Log.d(obj_find_test,"放大镜基准坐标为:("+String.valueOf(ShowImg_x)+","+String.valueOf(ShowImg_y)+")");
            Log.d(obj_find_test,"展示图片宽高为:"+String.valueOf(ShowImg_w)+" "+String.valueOf(ShowImg_h));

            Log.d(obj_find_test,"FindingImg's parent is "+FindingImg.getParent().toString());
            FindingImg.setVisibility(View.VISIBLE);
            FindingImg.bringToFront();
            Log.d(obj_find_test,"等待图片显示状态: "+String.valueOf(FindingImg.isShown()));

            /*float[] lastPosition=new float[2];
            lastPosition[0]=ShowImg_x+(float)(Math.random()*ShowImg_w);
            lastPosition[1]=ShowImg_y+(float)(Math.random()*ShowImg_h);*/
            final Timer[] timer = {new Timer()};
            final TimerTask[] task = {new TimerTask() {
                int i = 1;

                @Override
                public void run() {
                    // 逻辑处理
                    if (!loupe_end[0]) { //System.currentTimeMillis() - last_time <= 10000
                        Log.d(obj_find_test, "执行第" + String.valueOf(i) + "次循环");
                        i += 1;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                doAnimation();
                                //https://blog.csdn.net/kaikevin01/article/details/78871197
                            }
                        });
                    }
                    else {
                        this.cancel();
                        timer[0].cancel();
                        timer[0] = null;
                        FindingImg.setVisibility(View.INVISIBLE);
                    }
                }
            }};
            timer[0].schedule(task[0], 0,2000);

            Log.d(obj_find_test,"正在检测目标");
        }
    }

    public static Bitmap imageScale(Bitmap bitmap, int dst_w, int dst_h) {
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        float scale_w = ((float) dst_w) / src_w;
        float scale_h = ((float) dst_h) / src_h;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,true);
        return dstbmp;
    }

    private void doAnimation(){
        Path mPath=new Path();
        float x2,y2,x3,y3;
        mPath.moveTo(anim_lastPosition[0],anim_lastPosition[1]);
        x2=ShowImg_x+(float)(Math.random()*ShowImg_w);
        y2=ShowImg_y+(float)(Math.random()*ShowImg_h);
        x3=ShowImg_x+(float)(Math.random()*ShowImg_w);
        y3=ShowImg_y+(float)(Math.random()*ShowImg_h);
        mPath.cubicTo(anim_lastPosition[0], anim_lastPosition[1], x2, y2, x3, y3);
        anim_lastPosition[0]=x3; //?
        anim_lastPosition[1]=y3;

        PathMeasure mPathMeasure = new PathMeasure(mPath,false);
        float len=mPathMeasure.getLength();
        float step=0.001f;
        float[] mCurrentPoint=new float[2];

        ValueAnimator animator=ValueAnimator.ofInt(0,1000);
        animator.setDuration(1000);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float dis=(int)animation.getAnimatedValue()*step*len;
                mPathMeasure.getPosTan(dis,mCurrentPoint,null);
                FindingImg.layout((int)mCurrentPoint[0],(int)mCurrentPoint[1],
                        (int)mCurrentPoint[0]+FindingImg.getWidth(),(int)mCurrentPoint[1]+FindingImg.getHeight());
            }
        });
        animator.start();
    }

    public void save_image(View view)
    {
        if(!HasdetectedImg){
            Toast.makeText(this,"请先检测一张图片",Toast.LENGTH_SHORT).show();
        }
        else{
            String path1 = null;
            String[] devide = ImagePath.split("/");
            int lenth = devide.length;
            path1 = devide[lenth-1];
            String path2 = path1.substring(0,path1.indexOf(".jpg"));
            path2 = path2 + "1.jpg";
            @SuppressLint("SdCardPath") String path = "/sdcard/Download/";
            path = path + path2;

            //String path="/sdcard/Download/100.jpg";
            String savePath;
            File filePic;
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                savePath = path;
            } else {
                Log.e("tag", "saveBitmap failure : sdcard not mounted");
                return;
            }
            try {
                filePic = new File(savePath);
                if (!filePic.exists()) {
                    filePic.getParentFile().mkdirs();
                    filePic.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(filePic);
                Bitmap img=((BitmapDrawable)ImageSelect.getDrawable()).getBitmap();
                img.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                Log.e("tag", "saveBitmap: " + e.getMessage());
                return;
            }
            Toast.makeText(this,"图片保存成功",Toast.LENGTH_SHORT).show();
            Log.i("tag", "saveBitmap success: " + filePic.getAbsolutePath());
        }

    }


    public void onCancel(View view)
    {
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }

}