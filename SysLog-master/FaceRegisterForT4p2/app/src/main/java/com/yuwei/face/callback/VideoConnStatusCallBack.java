package com.yuwei.face.callback;

/**
 * 视频连着状态回调接口
 */
public interface VideoConnStatusCallBack {
    public void onSessionConnect();//Socket连接上
    public void onSessionDisConnect();//Socket断开连接
}
