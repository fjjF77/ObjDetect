package com.example.learning1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Model {
    public Model(Context context,user_config cfg){
    }

    public Tuple2<Boolean,Objects> finding(Bitmap bitmap){
        return new Tuple2<Boolean,Objects>(false,null);
    }

    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    public static Bitmap imageScale(Bitmap bitmap, int src_w, int src_h, int dst_w, int dst_h) {
        float scale_w = ((float) dst_w) / src_w;
        float scale_h = ((float) dst_h) / src_h;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,
                true);
        return dstbmp;
    }
}
