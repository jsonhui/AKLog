package com.yuwei.face.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.VersionInfo;
import com.yuwei.camera.occlusion.AlarmFlowDialog;
import com.yuwei.camera.occlusion.CameraOcclusionTools;
import com.yuwei.camera.occlusion.CameraOcclusionTools.ResultCallBack;
import com.yuwei.camera.occlusion.OcclusionResultModel;
import com.yuwei.face.R;
import com.yuwei.face.callback.VideoConnStatusCallBack;
import com.yuwei.face.faceserver.CompareResult;
import com.yuwei.face.faceserver.FaceServer;
import com.yuwei.face.model.FacePreviewInfo;
import com.yuwei.face.play.CHReceiveServices;
import com.yuwei.face.play.H264FrameDecoder;
import com.yuwei.face.socket.GossClient;
import com.yuwei.face.socket.Message.GossPrefix;
import com.yuwei.face.socket.Message.MediaData;
import com.yuwei.face.socket.Message.Rtpdatas;
import com.yuwei.face.util.ConfigUtil;
import com.yuwei.face.util.ImageUtil;
import com.yuwei.face.util.face.FaceHelper;
import com.yuwei.face.util.face.FaceListener;
import com.yuwei.face.util.face.RequestFeatureStatus;
import com.yuwei.sdk.YwSdkManager;
import com.yw.tts.Tts;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FaceService extends Service implements VideoConnStatusCallBack {

    private static FaceService mFaceService = null;
    private List<MediaData> ChQueue = null;
    private GossClient mGossClient;
    private H264FrameDecoder framePersonPlayer;
    private H264FrameDecoder framePersonPlayer2;
    private H264FrameDecoder framePersonPlayer3;
    public static final String TAG = "FaceService";
    private boolean isgorun = false; // 继续运行
    private Thread checkThread = null;
    public long totalRecv = 0;
    public long startTime;
    MediaData curMediaData;
    public static final int VIDEO_CHANNEL_NUM_ONE = 1;
    public static final int VIDEO_CHANNEL_NUM_TWO = 2;
    public static final int VIDEO_CHANNEL_NUM_THREE = 3;
    private Thread autoPersonThread;
    private Bitmap mPersonBitmap;
    private final Object bmpPersonLocker = new Object();
    private boolean isStart = true;
    public static final int MSG_DO_DETACT = 8;
    private FaceHelper faceHelper;
    private static final int MAX_DETECT_NUM = 10;
    public static final int MSG_SEND_OREDER_MSG = 100;
    private FaceEngine faceEngine;
    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
    private List<CompareResult> compareResultList;
    private static final float SIMILAR_THRESHOLD = 0.6F;
    private int afCode = -1;
    private int warnCount = 0;
    //    private int faceNum = 0;
    private AlertDialog dialog;
    private final String action = "action.face.recognize.facenum";
    private boolean isSocketConnected = false;
    private Toast toast = null;
    private CameraOcclusionTools mCameraOcclusionTools;
    private List<OcclusionResultModel> mOcclusionResultList = new ArrayList<>();
    private int oneOcclusionCount = 0;
    private int twoOcclusionCount = 0;
    private int threeOcclusionCount = 0;
    private Tts mAlarmTts;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case MSG_DO_DETACT:
                    synchronized (bmpPersonLocker) {
                        if (mPersonBitmap != null) {
                            recogizePerson();
                        }
                        recognitionOcclusion();
                    }
                    break;

                case MSG_SEND_OREDER_MSG:
                    if (YwSdkManager.getInstants(mFaceService) != null)
                        YwSdkManager.getInstants(mFaceService).sendCmd((byte[]) msg.obj);
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mFaceService = this;
        autoPersonRecognize();
        initEngine();
        mCameraOcclusionTools = new CameraOcclusionTools(getApplicationContext());
        ChQueue = new ArrayList<MediaData>();
        mAlarmTts = new Tts(getApplicationContext());
        framePersonPlayer = new H264FrameDecoder();
////        framePersonPlayer2 = new H264FrameDecoder();
//        framePersonPlayer3 = new H264FrameDecoder();
    }

    private void initSocket() {
        isSocketConnected = true;
        new Thread() {
            @Override
            public void run() {
                try {
                    if (mGossClient == null) {
                        mGossClient = new GossClient();
                        mGossClient.setmVideoConnStatusCallBack(mFaceService);
                        mGossClient.startConnectAndLogin();
                    } else {
                        mGossClient.stopRealTimeVideo();
                        mGossClient.close();
                        mGossClient = new GossClient();
                        mGossClient.setmVideoConnStatusCallBack(mFaceService);
                        mGossClient.startConnectAndLogin();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.run();
            }
        }.start();
    }

    public void releaseSocket() {
        isSocketConnected = false;
        new Thread() {
            @Override
            public void run() {
                if (mGossClient != null) {
                    mGossClient.stopRealTimeVideo();
                    try {
                        sleep(500);
                        mGossClient.close();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                super.run();
            }
        }.start();
//		if (framePersonPlayer != null) {
//			framePersonPlayer.release();
//			framePersonPlayer = null;
//		}
//		isgorun = false;
//		ClearData();
//		checkThread = null;
    }

    private void initEngine() {
        faceEngine = new FaceEngine();
        afCode = faceEngine.init(this, FaceEngine.ASF_DETECT_MODE_IMAGE, FaceEngine.ASF_OP_0_ONLY, 16, 10,
                FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT);
        VersionInfo versionInfo = new VersionInfo();
        faceEngine.getVersion(versionInfo);
        Log.i(TAG, "initEngine:  init: " + afCode + "  version:" + versionInfo);
    }

    public void autoPersonRecognize() {
        autoPersonThread = new Thread() {
            @Override
            public void run() {
                super.run();
                Log.i(TAG, "====>>> autoPersonRecognize--isStart---" + isStart);
                while (!interrupted() && isStart) {
                    try {
                        sleep(15000);
                        synchronized (bmpPersonLocker) {
                            Log.i(TAG, "====>>> getFrame per 15s---init");
                            if (afCode != 0)
                                initEngine();

                            if (CHReceiveServices.getChReceiveServices() == null) {
                                initSocket();
                                sleep(3000);
                                mOcclusionResultList.clear();
//                                SparseArray<Bitmap> bitList = framePersonPlayer.getFrame();
                                mPersonBitmap = framePersonPlayer.getFrame();
                                if (mPersonBitmap != null) {
                                    mOcclusionResultList.add(new OcclusionResultModel(mPersonBitmap, 1));
                                }
//                                if(twoBitmap!=null){
//                                    mOcclusionResultList.add(new OcclusionResultModel(twoBitmap, 2));
//                                }
//                                if(threeBitmap!=null){
//                                    mOcclusionResultList.add(new OcclusionResultModel(threeBitmap, 3));
//                                }

//                                for (int i = 0; i < bitList.size(); i++) {
//                                    int key = bitList.keyAt(i);
//                                    if (bitList.get(key) != null) {
//                                        mOcclusionResultList.add(new OcclusionResultModel(bitList.get(key), key));
//                                    }
//                                }
//								if (mPersonBitmap == null) {
//									Log.i(TAG, "====>>>mPersonBitmap is null---");
//								} else {
                                mHandler.sendEmptyMessage(MSG_DO_DETACT);
//								}
                                releaseSocket();
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        autoPersonThread.start();
    }

    private void initFaceHelper() {

        if (faceHelper == null && afCode == 0) {
            if (mPersonBitmap == null) {
                return;
            }
            faceHelper = new FaceHelper.Builder().faceEngine(faceEngine).frThreadNum(MAX_DETECT_NUM)
                    .previewWidth(mPersonBitmap.getWidth()).previewHeight(mPersonBitmap.getHeight())
                    .faceListener(faceListener).currentTrackId(ConfigUtil.getTrackId(this.getApplicationContext()))
                    .build();
        }

    }

    private void recognitionOcclusion() {

        for (OcclusionResultModel bitmapData : mOcclusionResultList) {
            mCameraOcclusionTools.GetPalette(bitmapData.bitmap, bitmapData.channel, new ResultCallBack() {

                @Override
                public void isOcclusion(boolean isOcclusion, int channel) {
                    // TODO Auto-generated method stub
                    Log.i(TAG, "recogizeOcclusion  channel=" + channel + "  isOcclusion==>" + isOcclusion);
//                    totalCount++;
//                    if (totalCount >= 6) {
//                        oneOcclusionCount = 0;
//                        twoOcclusionCount = 0;
//                        threeOcclusionCount = 0;
//                    }
                    switch (channel) {
                        case VIDEO_CHANNEL_NUM_ONE:
                            if (isOcclusion) {
                                oneOcclusionCount++;
                                if (oneOcclusionCount >= 2) {
                                    oneOcclusionCount = 0;
                                    alarmTtsTips(1);
                                }
                            } else {
                                oneOcclusionCount = 0;
                            }
                            break;
                        case VIDEO_CHANNEL_NUM_TWO:
                            if (isOcclusion) {
                                twoOcclusionCount++;
                                if (twoOcclusionCount >= 2) {
                                    alarmTtsTips(2);
                                }
                            } else {
                                twoOcclusionCount = 0;
                            }
                            break;
                        case VIDEO_CHANNEL_NUM_THREE:
                            if (isOcclusion) {
                                threeOcclusionCount++;
                                if (threeOcclusionCount >= 2) {
                                    alarmTtsTips(3);
                                }
                            } else {
                                threeOcclusionCount = 0;
                            }

                            break;
                    }
                }
            });
        }


    }

    private void alarmTtsTips(int channel) {
        String tips = getResources().getString(R.string.camera_str) + channel + getResources().getString(R.string.occlusion_str);
        showToast(tips);
        if (mAlarmTts != null) {
            mAlarmTts.play(tips);
        }
    }

    private void recogizePerson() {
        Log.d(TAG, "====>>> do recogizePerson");
        if (mPersonBitmap == null) {
            return;
        }
        initFaceHelper();
        Bitmap bitmap = mPersonBitmap.copy(Bitmap.Config.ARGB_8888, true);

        // NV21宽度必须为4的倍数,高度为2的倍数
        bitmap = ImageUtil.alignBitmapForNv21(bitmap);

        if (bitmap == null) {
            return;
        }
        final int width1 = bitmap.getWidth();
        final int height1 = bitmap.getHeight();
        // bitmap转NV21
        final byte[] nv21 = ImageUtil.bitmapToNv21(bitmap, width1, height1);
        Log.i(TAG, "====>>> do recogizePerson--width1:" + width1 + "--height1:" + height1);
        if (nv21 != null && afCode == 0) {

            List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21.clone());
            Log.i(TAG, "recogizePerson: facePreviewInfoList.size() = " + facePreviewInfoList.size());
            if (facePreviewInfoList != null && facePreviewInfoList.size() > 0) {

                Intent intent = new Intent();
                intent.setAction(action);
                intent.putExtra("face_num", facePreviewInfoList.size());
                sendBroadcast(intent);
                for (int i = 0; i < facePreviewInfoList.size(); i++) {
                    Log.i(TAG, "requestFaceFeature--getTrackId:" + facePreviewInfoList.get(i).getTrackId());
                    faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), width1, height1,
                            FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
                }
            }
        }
    }

    final FaceListener faceListener = new FaceListener() {
        @Override
        public void onFail(Exception e) {
            Log.e(TAG, "onFail: " + e.getMessage());
            initEngine();
        }

        // 请求FR的回调
        @Override
        public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId) {
//            //FR成功
            if (faceFeature != null) {
                Log.i(TAG, "onPreview: fr end = " + System.currentTimeMillis() + " trackId = " + requestId);
                searchFace(faceFeature, requestId);

            }
        }
    };

    /**
     * @param frFace
     * @param requestId
     */
    private void searchFace(final FaceFeature frFace, final Integer requestId) {
        Observable.create(new ObservableOnSubscribe<CompareResult>() {
            @Override
            public void subscribe(ObservableEmitter<CompareResult> emitter) {
                Log.i(TAG, "subscribe: fr search start = " + System.currentTimeMillis() + " trackId = " + requestId);
                CompareResult compareResult = FaceServer.getInstance().getTopOfFaceLib(frFace);
                Log.i(TAG, "subscribe: fr search end = " + System.currentTimeMillis() + " trackId = " + requestId
                        + "  name =" + compareResult.getUserName());
                if (compareResult == null) {
                    emitter.onError(null);
                } else {
                    emitter.onNext(compareResult);
                }
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CompareResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CompareResult compareResult) {
                        if (compareResult == null || compareResult.getUserName() == null) {
                            return;
                        }

                        Log.i(TAG, "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = "
                                + requestId + "  similar = " + compareResult.getSimilar());
                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
                            warnCount = 0;
                            int inspectDriver = 0;
//                        	dismissWarnToast();
                            String driver_code = Settings.System.getString(mFaceService.getContentResolver(),
                                    "drivercode");
                            Log.i(TAG, "showDriverInfo--driver_code: " + driver_code);
                            if (driver_code != null && !driver_code.equals(compareResult.getUserName())) {
//                        		NotifyUtil.showTopMessage(getApplicationContext(), "司机顶班运营!");
                                showToast(getString(R.string.str_driver_stand_in));
                                inspectDriver = 4;
                            } else {
                                inspectDriver = 2;
                            }
                            byte[] data = GetReport0xD3(inspectDriver, inspectDriver == 2 ? 1 : 0xFF);
                            Message m = new Message();
                            m.what = MSG_SEND_OREDER_MSG;
                            m.obj = data;
                            if (mHandler != null)
                                mHandler.sendMessage(m);
                        } else {
                            warnCount++;
                            if (warnCount >= 2) {
                                warnCount = 0;
//                        		NotifyUtil.showTopMessage(getApplicationContext(), "司机不是当前系统内人员!");
                                showToast(getString(R.string.str_driver_not_insystem));
                                byte[] data = GetReport0xD3(2, 0);
                                Message m = new Message();
                                m.what = MSG_SEND_OREDER_MSG;
                                m.obj = data;
                                if (mHandler != null)
                                    mHandler.sendMessage(m);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private byte[] GetReport0xD3(int eventId, int result) {
        byte[] cmdData = new byte[8];
        cmdData[0] = (byte) 0xAA;
        cmdData[1] = 0x05;
        cmdData[2] = 0x55;
        cmdData[3] = (byte) 0xD3;
        cmdData[4] = 0x00;
        cmdData[5] = (byte) eventId;
        cmdData[6] = (byte) result;
        byte check = 0x00;
        for (int i = 0; i < cmdData.length - 1; i++) {
            check ^= cmdData[i];
        }
        cmdData[7] = check;
        return cmdData;
    }

    private void dismissWarnToast() {

        new Thread() {
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
            }

            ;
        }.start();
    }

    public static FaceService getFaceService() {
        return mFaceService;
    }

    public void AddRtpData(MediaData _data) {
        if (checkThread != null) {
            ChQueue.add(_data);
        }
    }

    public boolean isStart() {
        return isgorun;
    }

    private void onStart() {
        isgorun = true;
        try {
            checkThread = new Thread(checkGossMessageQueue);
            checkThread.setPriority(Thread.MAX_PRIORITY);
            checkThread.start();
        } catch (Exception e) {
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        DoExit();
        // faceHelper中可能会有FR耗时操作仍在执行，加锁防止crash
        if (faceHelper != null) {
            synchronized (faceHelper) {
                unInitEngine();
            }
            ConfigUtil.setTrackId(this, faceHelper.getCurrentTrackId());
            faceHelper.release();
        } else {
            unInitEngine();
        }
    }

    private void unInitEngine() {

        if (afCode == 0) {
            afCode = faceEngine.unInit();
            Log.i(TAG, "unInitEngine: " + afCode);
        }
    }

    public void DoExit() {
        isStart = false;
        new Thread() {
            @Override
            public void run() {
                if (mGossClient != null) {
                    mGossClient.stopRealTimeVideo();
                    mGossClient.close();
                }
                super.run();
            }
        }.start();
        if (framePersonPlayer != null) {
            framePersonPlayer.release();
            framePersonPlayer = null;
        }
        if (framePersonPlayer2 != null) {
            framePersonPlayer2.release();
            framePersonPlayer2 = null;
        }
        if (framePersonPlayer3 != null) {
            framePersonPlayer3.release();
            framePersonPlayer3 = null;
        }
        isgorun = false;
        ClearData();
        checkThread = null;
        mFaceService = null;
    }

    /**
     * add md to curMediaData
     *
     * @param md
     */
    private void addData(MediaData md) {
        if (curMediaData == null)
            return;
        byte d1[] = curMediaData.getRtpdata();
        byte d2[] = md.getRtpdata(); // 需减去前15字节的rtsp head
        byte ndate[] = new byte[d1.length + d2.length - 15];
        System.arraycopy(d1, 0, ndate, 0, d1.length);
        System.arraycopy(d2, 15, ndate, d1.length, d2.length - 15);
        curMediaData.setRtpdata(ndate);
    }

    /**
     * 累积完整包
     *
     * @param md
     * @return true：累积完成，false：未完成
     */
    private boolean addFrame(MediaData md) {
        if (md == null)
            return false;
        if (md.getMediaType() == 2) {
            curMediaData = md;
            return true;// 如果是音频数据则不需要分包直接处理
        }
        switch (md.getGossPrefix().getFenbao_flag()) {
            case GossPrefix.PACKAGE_HEAD:
                curMediaData = md;
                return false;
            case GossPrefix.PACKAGE_MID:
                addData(md);
                return false;
            case GossPrefix.PACKAGE_END:
                addData(md);
                return true;
            case GossPrefix.PACKAGE_SINGLE:
                curMediaData = md;
                return true;
            default:
                // do nothing
        }
        return false;
    }

    Runnable checkGossMessageQueue = new Runnable() {
        @SuppressLint("NewApi")
        @Override
        public void run() {
            Rtpdatas rtpdatas = new Rtpdatas();
            while (isgorun) {
                try {
                    int s = ChQueue.size();
                    if (s > 0) {
                        if (ChQueue.get(0) == null) {
                            ChQueue.remove(0);
                            continue;
                        }
                        MediaData md = ChQueue.get(0);
                        if (addFrame(md)) {
                            rtpdatas.fromBinaryData(curMediaData.getRtpdata());
                            if (rtpdatas.getB47() == 1) {// 视频
//                                if (md.getChNum() == VIDEO_CHANNEL_NUM_THREE){
////									framePersonPlayer.addData(rtpdatas.getMdata());
////                                    framePersonPlayer
////                                            .addData(new ChannelFrameData(md.getChNum(), rtpdatas.getMdata()));
//                                    framePersonPlayer3.addData(rtpdatas.getMdata());
//                                }
//                                else if(md.getChNum() == VIDEO_CHANNEL_NUM_TWO){
//                                    framePersonPlayer2.addData(rtpdatas.getMdata());
//                                }
                                if (md.getChNum() == VIDEO_CHANNEL_NUM_ONE) {
                                    framePersonPlayer.addData(rtpdatas.getMdata());
                                }

                            } else if (rtpdatas.getB47() == 2) {// 音频
                                // Log.d(TAG,"---->>audio format:" + rtpdatas.getMformat());
                            }
                        }
                        ChQueue.remove(0);
                    } else {
                        Thread.sleep(5);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    DoExit();
                    break;
                }
            }
        }
    };

    public void AddMediaPackage(MediaData _data) {
        if (!isStart()) {
            onStart();
        }
        AddRtpData(_data);
    }

    public void ClearData() {
        if (checkThread != null)
            ChQueue.clear();
    }

    @Override
    public void onSessionConnect() {
        Log.i(TAG, "faceService--onSessionConnect------------");
        if (framePersonPlayer != null) {
            new Thread() {
                @Override
                public void run() {
                    if (mGossClient != null) {
//						mGossClient.playRealTimeVideo(VIDEO_CHANNEL_NUM_THREE);
                        mGossClient.showAllChannelVideo();
                    }
                    super.run();
                }
            }.start();
            framePersonPlayer.initDecoder();
        }
    }

    @Override
    public void onSessionDisConnect() {
        Log.i(TAG, "faceService--onSessionDisConnect------------");
    }

    public boolean isConnected() {
        return isSocketConnected;
    }

    private void showToast(String s) {
        if (toast == null) {
            toast = Toast.makeText(this, s, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 50);
        } else {
            toast.setText(s);
        }
        LinearLayout layout = (LinearLayout) toast.getView();
        TextView tv = (TextView) layout.getChildAt(0);
        tv.setTextSize(35);
        toast.show();
    }
}
