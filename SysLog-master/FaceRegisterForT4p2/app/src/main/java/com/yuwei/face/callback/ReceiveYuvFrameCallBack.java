package com.yuwei.face.callback;

public interface ReceiveYuvFrameCallBack {
    public void onYuvFrameCallBack(byte[] yuvData, int videoWidth, int videoHeight);
}
