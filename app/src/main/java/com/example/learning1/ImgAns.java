package com.example.learning1;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

public class ImgAns implements Parcelable {
    private Tuple2<Boolean,Objects> ans;

    public ImgAns(Tuple2<Boolean,Objects> l){
        this.ans=l;
    }
    protected ImgAns(Parcel in) {
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBoolean(ans.getFirst());
        ans.getSecond().getAnsimg().writeToParcel(dest,0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ImgAns> CREATOR = new Creator<ImgAns>() {
        @Override
        public ImgAns createFromParcel(Parcel in) {
            return new ImgAns(in);
        }

        @Override
        public ImgAns[] newArray(int size) {
            return new ImgAns[size];
        }
    };
    public Tuple2<Boolean,Objects> getAns() {
        return ans;
    }
}
