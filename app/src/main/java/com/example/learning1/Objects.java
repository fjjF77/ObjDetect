package com.example.learning1;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.List;

public class Objects{
    private final List<String> Classes;
    private final List<ansRect> loactions;
    private final List<Float> probs;
    private final Bitmap ansimg;

    public Objects(List<String> Classes, List<ansRect> loactions, List<Float> probs, Bitmap ansimg){
        this.Classes=Classes;
        this.loactions=loactions;
        this.probs=probs;
        this.ansimg=draw(ansimg);
    }
    public Objects(List<String> Classes, List<ansRect> loactions, Bitmap ansimg){
        this.Classes=Classes;
        this.loactions=loactions;
        probs=null;
        this.ansimg=draw(ansimg); }

    public List<String> getClasses() {
        return Classes;
    }
    public String getClasses(int i) {
        return Classes.get(i);
    }
    public List<ansRect> getLoactions() {
        return loactions;
    }
    public ansRect getLoactions(int i){
        return loactions.get(i);
    }
    public List<Float> getProbs() {
        return probs;
    }
    public Float getProbs(int i) {
        return probs.get(i);
    }
    public Bitmap getAnsimg() {
        return ansimg;
    }

    @SuppressLint("DefaultLocale")
    private Bitmap draw(Bitmap img){
        Canvas canvas=new Canvas(img);

        final int[] colors = new int[] {
            Color.rgb( 54,  67, 244),
            Color.rgb( 99,  30, 233),
            Color.rgb(176,  39, 156),
            Color.rgb(183,  58, 103),
            Color.rgb(181,  81,  63),
            Color.rgb(243, 150,  33),
            Color.rgb(244, 169,   3),
            Color.rgb(212, 188,   0),
            Color.rgb(136, 150,   0),
            Color.rgb( 80, 175,  76),
            Color.rgb( 74, 195, 139),
            Color.rgb( 57, 220, 205),
            Color.rgb( 59, 235, 255),
            Color.rgb(  7, 193, 255),
            Color.rgb(  0, 152, 255),
            Color.rgb( 34,  87, 255),
            Color.rgb( 72,  85, 121),
            Color.rgb(158, 158, 158),
            Color.rgb(139, 125,  96)
        };

        Paint paint=new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        Paint textbgpaint = new Paint();
        textbgpaint.setColor(Color.WHITE);
        textbgpaint.setStyle(Paint.Style.FILL);

        Paint textpaint = new Paint();
        textpaint.setColor(Color.BLACK);
        textpaint.setTextSize(26);
        textpaint.setTextAlign(Paint.Align.LEFT);

        for (int i = 0; i < Classes.size(); i++)
        {
            paint.setColor(colors[i % 19]);

            canvas.drawRect(loactions.get(i).x, loactions.get(i).y, loactions.get(i).x + loactions.get(i).w, loactions.get(i).y + loactions.get(i).h, paint);

            // draw filled text inside image
            {
                String text;
                if(probs!=null)  text = Classes.get(i) + " = " + String.format("%.1f", probs.get(i) * 100) + "%";
                else text = Classes.get(i);

                float text_width = textpaint.measureText(text);
                float text_height = - textpaint.ascent() + textpaint.descent();

                float x = loactions.get(i).x;
                ansRect[] objects;
                float y = loactions.get(i).y - text_height;
                if (y < 0)
                    y = 0;
                if (x + text_width > img.getWidth())
                    x = img.getWidth() - text_width;

                canvas.drawRect(x, y, x + text_width, y + text_height, textbgpaint);

                canvas.drawText(text, x, y - textpaint.ascent(), textpaint);
            }
        }

        return img;
    }
}
