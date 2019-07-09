package com.yuwei.playmv;

import android.content.Context;
import android.util.Log;

import com.yuwei.playmv.utils.StorageUtils;
import com.yuwei.playmv.utils.Util;

import java.util.ArrayList;

public class OperationVideo implements IoperationVideo {
    private static final String TAG = "Jason";
    private static volatile OperationVideo instance;

    private OperationVideo() {
    }

    public static OperationVideo getInstance() {
        if (instance != null) {
            synchronized (OperationVideo.class) {
                if (instance != null) {
                    instance = new OperationVideo();
                }
            }
        }
        return instance;
    }

    @Override
    public boolean play(Context context) {
        ArrayList<StorageUtils.Volume> volumes = Util.operatorSDCard(context);
        if (volumes.size() == 0) {
            return false;
        }


        return false;
    }

    @Override
    public void stop() {

    }
}
