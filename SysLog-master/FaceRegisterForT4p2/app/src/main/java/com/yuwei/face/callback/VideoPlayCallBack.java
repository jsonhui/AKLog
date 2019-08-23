package com.yuwei.face.callback;

import com.yuwei.face.socket.Message.MediaInfo;

import java.util.List;

/**
 * 视频播放控制回调接口
 */
public interface VideoPlayCallBack {
    void onPlayRealVideoCallBack(byte result);//播放实时视频回调
    void onPollingPlayRealVideoCallBack(boolean result);//开启轮循播放结果回调
    void onClosePollingPlayRealVideoCallBack(boolean result);//关闭轮循播放结果回调
    void onReceiveHistoryVideoRecords(List<MediaInfo> videoList); //查询历史视频回调接口
    void onStartPlayHistoryVideo(boolean result);//播放历史视频的回调
    void onPausePlayHistoryVideo(boolean result);//暂停播放历史视频的回调
    void onStopPlayHistoryVideo(boolean result);//停止播放历史视频的回调
    void onPlayCompleteCallBack(boolean result);//视频播放完成回调
    void onHistoryVideoSpeed(byte mode, byte speed, boolean result);
    void onStopRealVideoCallBack(boolean result);
}
