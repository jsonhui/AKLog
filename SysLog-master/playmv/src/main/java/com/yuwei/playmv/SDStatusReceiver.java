package com.yuwei.playmv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SDStatusReceiver extends BroadcastReceiver {

    private static final String TAG = "Jason";

    @Override
    public void onReceive(Context context, Intent intent) {
        //判断收到的到底是什么广播
        String action = intent.getAction();
        if ("android.intent.action.MEDIA_MOUNTED".equals(action)) {
            Log.e(TAG, "SD卡可用");
        } else if ("android.intent.action.MEDIA_UNMOUNTED".equals(action)) {
            Log.e(TAG, "SD卡不可用");
        }
    }
}
