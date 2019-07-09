package com.yuwei.playmv.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class Util {

    /* 获取SD卡是否可用 */
    public static ArrayList<StorageUtils.Volume> operatorSDCard(Context context) {
        ArrayList<StorageUtils.Volume> datas = new ArrayList<>();
        ArrayList<StorageUtils.Volume> list_volume = StorageUtils.getVolume(context);
        for (int i = 0; i < list_volume.size(); i++) {
            if (list_volume.get(i).isRemovable() && "mounted".equals(list_volume.get(i).getState())) {
                datas.add(list_volume.get(i));
            }
        }
        return datas;
    }
}
