package com.quectel.com.fourcameraperviewdemo;

public class MainActivity {
    public static final int MAX_NUM = 1;
    public boolean[] lockList = new boolean[MAX_NUM];
    public static int pre_Width = 1280;
    public static int pre_Height = 720;
    public static int vid_Width = 1280;
    public static int vid_Height = 720;
  
    public void onCreate() {
    	setPreviewSize(pre_Width, pre_Height);
        setVideoSize(vid_Width, vid_Height);
        qcarcamQueryInputs(0, MAX_NUM);
        qcarcamOpenInitStart();
        initFrameCopyLock(lockList,MAX_NUM);      
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("mmqcar_ais_moudle_jni");
    }

    //mode 0: 1080p , 720p ,  720p
    //     1: 720p, 1080p, 720p
    //     2: 720p, 720p, 720p, 720p
    public native int qcarcamQueryInputs(int mode, int input_num);

    public native void qcarcamOpenInitStart();

    public native void qcarcamStopCloseRelease();

    public native void setSaveFrameMaxNum(int maxNum);

    public native void setPreviewSize(int width, int height);

    public native void setVideoSize(int width, int height);

    public native void initFrameCopyLock(boolean[] lockList,int num);


    public void onDestroy() {
        qcarcamStopCloseRelease();
    }
    
}
