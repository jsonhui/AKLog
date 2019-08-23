package com.yuwei.face.callback;

/**
 * 摄像头状态回调
 */
public interface CameraStatusCallBack {
    public void onVideoServerStatusChanged(boolean connected);//视频服务器连接状态变化
    public void onV3PowerCallback(int result);//返回1：上电成功，2：下电成功， -1：上电失败，-2：下电失败
}
