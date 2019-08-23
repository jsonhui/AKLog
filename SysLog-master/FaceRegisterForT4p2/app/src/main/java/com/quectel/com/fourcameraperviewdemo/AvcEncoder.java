package com.quectel.com.fourcameraperviewdemo;

import android.annotation.SuppressLint;

/**
 * Created by wangjian on 2019-5-27.
 */
public class AvcEncoder {
    private final static String TAG = "AvcEncoder";
    private byte[] mdata;

    @SuppressLint("NewApi")
    public AvcEncoder(int width, int height, int cameraId) {
        mdata = new byte[height * width * 3 / 2];
        getBuff(mdata, cameraId);
    }

    public int getBuff(byte[] mdata, int num) {
        int buffAlloc = 0;
        buffAlloc = startAllocChannel(mdata, num);
        return buffAlloc;
    }

    public int getBufSize(int num) {
        int index = 0;
        index = getBufAndSize(num);
        return index;
    }

    public native int getBufAndSize(int num);

    public native int startAllocChannel(byte[] buffer, int num);

}
