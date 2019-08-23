package com.yuwei.face.register;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.VersionInfo;
import com.yuwei.face.faceserver.CompareResult;
import com.yuwei.face.model.FacePreviewInfo;
import com.yuwei.face.play.H264Player;
import com.yuwei.face.util.ConfigUtil;
import com.yuwei.face.util.Constant;
import com.yuwei.face.callback.ReceiveYuvFrameCallBack;
import com.yuwei.face.entity.VideoParam;
import com.yuwei.face.faceserver.FaceServer;
import com.yuwei.face.play.CHReceiveServices;
import com.yuwei.face.register.view.RegisterLeftPromptView;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.yuwei.face.R;
import com.yuwei.face.util.DrawHelper;
import com.yuwei.face.util.YuweiFaceUtil;
import com.yuwei.face.util.face.FaceHelper;
import com.yuwei.face.util.face.FaceListener;
import com.yuwei.face.util.face.RequestFeatureStatus;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RegisterActivity extends Activity implements ReceiveYuvFrameCallBack,View.OnClickListener{
    private final String TAG = "RegisterActivity";
//    private TextureView mTextureView;
//    private CameraManager mCameraManager;
    private CHReceiveServices mChReceiveServices;
    private SurfaceView mDisplaySurface;
    private ImageView mScann_line,mFaceLabel;
    private RegisterLeftPromptView mRegisterLeftPromptView;
    private Button mStepBtn;
    private String mDriverCode;
    private final int mCurCameraId = 0;

    private final int MSG_UPDATE_UI_MSG = 1;
    private final int MSG_START_HIDE_TIMER_MSG = 2;
    private final int MSG_SHOW_TEXT = 3;
    private final int MSG_FACE_RECORD_SUCCESS = 4;//注册人脸成功
    private final int MSG_FACE_RECORD_FAIL = 5;//注册人脸失败
    private final int MSG_FACE_EXIST = 6;//人脸已注册
    private final int MSG_BEGIN_REGISTER = 7;

    private VideoParam mDetectVideoParam;
    private HandlerThread mHandlerThread;
    private DetactHandler detactHandler;
    private boolean isNeedDetect = false;
    private boolean faceRecordComplete = false;
    private long lastClickTime = 0L;
    private static final int SEND_DATA_DELAY_TIME = 1000;  // 数据发送间隔

    private static final int MAX_DETECT_NUM = 10;
    private int afCode = -1;
    private Toast toast = null;
    private FaceEngine faceEngine;
    private FaceHelper faceHelper;
    private DrawHelper drawHelper;
    private List<CompareResult> compareResultList;
    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();
    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
    private static final int WAIT_LIVENESS_INTERVAL = 50;
    /**
     * 活体检测的开关
     */
    private boolean livenessDetect = false;
    private static final float SIMILAR_THRESHOLD = 0.8F;


    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_UI_MSG:
                    mFaceLabel.setVisibility(View.GONE);
                    break;
                case MSG_START_HIDE_TIMER_MSG:
                    finish();
                    break;
                case MSG_SHOW_TEXT:
                    String text = (String) msg.obj;
                    showNotify(text);
                    break;
                case MSG_FACE_RECORD_SUCCESS:
                    if(isNeedDetect)//如果正在监测人脸不更新界面
                        return;
                    mHandler.obtainMessage(MSG_SHOW_TEXT, "人脸录入完成").sendToTarget();
                    isNeedDetect = false;
                    faceRecordComplete = true;
//                    mRegisterLeftPromptView.setRegisterNotifyValue(1);
                    break;
                case MSG_FACE_RECORD_FAIL:
                    detactHandler.sendEmptyMessageDelayed(MSG_GET_BITMAP,100);
                    break;
                case MSG_FACE_EXIST:
                    showNotify("司机已存在，请直接登录");
                    break;
                case MSG_BEGIN_REGISTER:
//                    mRegisterLeftPromptView.setRegisterNotifyValue(1);
                    isNeedDetect = true;
                    mFaceLabel.setVisibility(View.GONE);
                    initFaceHelper();
                    initEngine();
                    break;
                default:
                    break;
            }
        }

    };
    private final int MSG_GET_BITMAP = 1;//从视频流中获取Bitmap
    private final int MSG_DETECT_FACE_FAIL = 2;//监测人脸失败
    private final int MSG_NO_DETECT_FACE = 3;//没监测到人脸
    private final int MSG_DETECT_FACE_SUCCESS = 4;//监测人脸成功

    public class DetactHandler extends Handler{

        public DetactHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_GET_BITMAP:
                    Log.i(TAG,"receive get bitmap msg");
//                    mDetectVideoParam = mCameraManager.getVideoParam();
                    if (mDetectVideoParam == null)
                        break;
                    byte[] videoData = mDetectVideoParam.getVideoData();
                    int previewWidth = mDetectVideoParam.getPreviewWidth();
                    int previewHeight = mDetectVideoParam.getPreviewHeight();
//                    FaceDetectManager.detectFace(videoData,previewWidth,previewHeight,mDetectListener);
                    break;
                case MSG_DETECT_FACE_FAIL:
                    detactHandler.sendEmptyMessageDelayed(MSG_GET_BITMAP,100);
                    break;
                case MSG_NO_DETECT_FACE:
                    detactHandler.sendEmptyMessageDelayed(MSG_GET_BITMAP,100);
                    break;
                case MSG_DETECT_FACE_SUCCESS:
//                    recordFace();
                    break;
                default:
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.face_register_main);
        //本地人脸库初始化
        FaceServer.getInstance().init(this);
        initData();
        initView();
    }
    private void initData(){
//        mCameraManager = CameraManager.getInstance();
//        if(mCameraManager == null)
//            mCameraManager = CameraManager.getInstance(this);
        Intent intent = getIntent();
        mDriverCode = intent.getStringExtra("driver_code");
        Log.i(TAG,"registerActivity::::::::::" + mDriverCode);
        mHandlerThread = new HandlerThread("faceRecognize");
        mHandlerThread.start();
        detactHandler = new DetactHandler(mHandlerThread.getLooper());
        mDetectVideoParam = new VideoParam();

        initEngine();
//        activeEngine(null);
    }

    private void initView(){
//        mTextureView = (TextureView) findViewById(R.id.face_texture);
        mDisplaySurface = (SurfaceView) findViewById(R.id.display_surface);
        mScann_line = (ImageView) findViewById(R.id.scann_line);
        mFaceLabel = (ImageView) findViewById(R.id.face_label);
        // 扫描线 动画
        Animation animation = AnimationUtils.loadAnimation(RegisterActivity.this, R.anim.anim_fp_entry);
        mScann_line.setAnimation(animation);
//        mRegisterLeftPromptView = (RegisterLeftPromptView)findViewById(R.id.register_notify_view);
        mStepBtn = (Button) findViewById(R.id.step_btn);
        mStepBtn.setOnClickListener(this);
    }

    private void initFaceHelper(){
        drawHelper = new DrawHelper(H264Player.VIDEO_WIDTH, H264Player.VIDEO_HEIGHT, H264Player.VIDEO_WIDTH, H264Player.VIDEO_HEIGHT, 0
                , 0, false);

        faceHelper = new FaceHelper.Builder()
                .faceEngine(faceEngine)
                .frThreadNum(MAX_DETECT_NUM)
                .previewWidth( H264Player.VIDEO_WIDTH)
                .previewHeight(H264Player.VIDEO_HEIGHT)
                .faceListener(faceListener)
                .currentTrackId(ConfigUtil.getTrackId(RegisterActivity.this.getApplicationContext()))
                .build();

    }
    final FaceListener faceListener = new FaceListener() {
        @Override
        public void onFail(Exception e) {
            Log.e(TAG, "onFail: " + e.getMessage());
        }

        //请求FR的回调
        @Override
        public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId) {
//            //FR成功
//            if (faceFeature != null) {
////                    Log.i(TAG, "onPreview: fr end = " + System.currentTimeMillis() + " trackId = " + requestId);
//
//                //不做活体检测的情况，直接搜索
//                if (!livenessDetect) {
//                    searchFace(faceFeature, requestId);
//                }
//                //活体检测通过，搜索特征
//                else if (livenessMap.get(requestId) != null && livenessMap.get(requestId) == LivenessInfo.ALIVE) {
//                    searchFace(faceFeature, requestId);
//                }
//                //活体检测未出结果，延迟100ms再执行该函数
//                else if (livenessMap.get(requestId) != null && livenessMap.get(requestId) == LivenessInfo.UNKNOWN) {
//                    getFeatureDelayedDisposables.add(Observable.timer(WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS)
//                            .subscribe(new Consumer<Long>() {
//                                @Override
//                                public void accept(Long aLong) {
//                                    onFaceFeatureInfoGet(faceFeature, requestId);
//                                }
//                            }));
//                }
//                //活体检测失败
//                else {
//                    requestFeatureStatusMap.put(requestId, RequestFeatureStatus.NOT_ALIVE);
//                }
//
//            }
//            //FR 失败
//            else {
//                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
//            }
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
//        mTextureView.setSurfaceTextureListener(this);
//        mCameraManager.setmCamerVideoDataInterface(this);
        mChReceiveServices = CHReceiveServices.getInstance();
        mChReceiveServices.initFaceRecoginzeCamera(mDisplaySurface.getHolder().getSurface(),this);

        if (mHandler != null)
            mHandler.sendEmptyMessageDelayed(MSG_BEGIN_REGISTER,5000);//5秒好开启人脸识别
    }
    /**
     * 初始化引擎
     */
    private void initEngine() {
        faceEngine = new FaceEngine();
        afCode = faceEngine.init(this, FaceEngine.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(this),
                16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_LIVENESS);
        VersionInfo versionInfo = new VersionInfo();
        faceEngine.getVersion(versionInfo);
        Log.i(TAG, "initEngine:  init: " + afCode + "  version:" + versionInfo);

        if (afCode != ErrorInfo.MOK) {
            Toast.makeText(this, getString(R.string.init_failed), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 激活引擎
     *
     * @param view
     */
    public void activeEngine(final View view) {

        if (view != null) {
            view.setClickable(false);
        }
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                FaceEngine faceEngine = new FaceEngine();
                int activeCode = faceEngine.active(RegisterActivity.this, Constant.APP_ID, Constant.SDK_KEY);
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            showToast(getString(R.string.active_success));
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            showToast(getString(R.string.already_activated));
                        } else {
                            showToast(getString(R.string.active_failed));
                        }

                        if (view != null) {
                            view.setClickable(true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 销毁引擎
     */
    private void unInitEngine() {

        if (afCode == ErrorInfo.MOK) {
            afCode = faceEngine.unInit();
            Log.i(TAG, "unInitEngine: " + afCode);
        }
    }

    private void showToast(String s) {
        if (toast == null) {
            toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            toast.setText(s);
            toast.show();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
//        if(mCameraManager != null){
//            mCameraManager.releaseCamera();
//        }
        mChReceiveServices.onStop();
        mHandler.removeMessages(MSG_UPDATE_UI_MSG);
        mHandler.removeMessages(MSG_START_HIDE_TIMER_MSG);
        faceRecordComplete = false;
//        mCameraManager.setmCamerVideoDataInterface(null);
    }

    @Override
    public void onClick(View v) {
        int viewId  = v.getId();
        switch (viewId){
            case R.id.step_btn:
//                if(faceRecordComplete) {
//                    Settings.System.putInt(this.getContentResolver(), Constant.FACE_REGISTER_ACTION,Constant.FACE_REGISTER_STATUS_IDLE);
                    mHandler.removeMessages(MSG_BEGIN_REGISTER);
                    isNeedDetect = false;
                    Log.i(TAG,"quit register face register");
                    finish();
//                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
//        FaceDetectManager.release();
        super.onDestroy();
        //faceHelper中可能会有FR耗时操作仍在执行，加锁防止crash
        if (faceHelper != null) {
            synchronized (faceHelper) {
                unInitEngine();
            }
            ConfigUtil.setTrackId(this, faceHelper.getCurrentTrackId());
            faceHelper.release();
        } else {
            unInitEngine();
        }
        if (getFeatureDelayedDisposables != null) {
            getFeatureDelayedDisposables.dispose();
            getFeatureDelayedDisposables.clear();
        }
        FaceServer.getInstance().unInit();
    }

    private void showNotify(String content){
        YuweiFaceUtil.showTopMessage(this,content);
    }


//    @Override
//    public void receiveVideoData(byte[] videoData, int previewWidth, int previewHeight) {
//        if(isNeedDetect) {
//            mDetectVideoParam.setVideoData(videoData);
//            mDetectVideoParam.setPreviewWidth(previewWidth);
//            mDetectVideoParam.setPreviewHeight(previewHeight);
//            FaceDetectManager.detectFace(videoData, previewWidth, previewHeight, mDetectListener);
//        }
//    }

    @Override
    public void onYuvFrameCallBack(byte[] nv21, int videoWidth, int videoHeight) {
        if(isNeedDetect) {
            Log.i(TAG,"--------onYuvFrameCallBack yuvData.length-------" + nv21.length);
//            if (System.currentTimeMillis() - lastClickTime <SEND_DATA_DELAY_TIME){
//                return;
//            }
//            lastClickTime = System.currentTimeMillis();
//            mDetectVideoParam.setVideoData(yuvData);
//            mDetectVideoParam.setPreviewWidth(videoWidth);
//            mDetectVideoParam.setPreviewHeight(videoHeight);
//            FaceDetectManager.detectFace(yuvData, videoWidth, videoHeight, mDetectListener);

//            if(faceHelper==null)return;
//            List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21);
//            if(facePreviewInfoList.size()>0){
//                Log.i(TAG,"--------检测到人脸数量-------"+facePreviewInfoList.size());
//            }
        }
    }

    private void searchFace(final FaceFeature frFace, final Integer requestId) {
        Observable
                .create(new ObservableOnSubscribe<CompareResult>() {
                    @Override
                    public void subscribe(ObservableEmitter<CompareResult> emitter) {
//                        Log.i(TAG, "subscribe: fr search start = " + System.currentTimeMillis() + " trackId = " + requestId);
                        CompareResult compareResult = FaceServer.getInstance().getTopOfFaceLib(frFace);
//                        Log.i(TAG, "subscribe: fr search end = " + System.currentTimeMillis() + " trackId = " + requestId);
                        if (compareResult == null) {
                            emitter.onError(null);
                        } else {
                            emitter.onNext(compareResult);
                        }
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CompareResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CompareResult compareResult) {
                        if (compareResult == null || compareResult.getUserName() == null) {
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            faceHelper.addName(requestId, "VISITOR " + requestId);
                            return;
                        }

//                        Log.i(TAG, "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = " + requestId + "  similar = " + compareResult.getSimilar());
                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
                            boolean isAdded = false;
                            if (compareResultList == null) {
                                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                                faceHelper.addName(requestId, "VISITOR " + requestId);
                                return;
                            }
                            for (CompareResult compareResult1 : compareResultList) {
                                if (compareResult1.getTrackId() == requestId) {
                                    isAdded = true;
                                    break;
                                }
                            }
                            if (!isAdded) {
                                //对于多人脸搜索，假如最大显示数量为 MAX_DETECT_NUM 且有新的人脸进入，则以队列的形式移除
                                if (compareResultList.size() >= MAX_DETECT_NUM) {
                                    compareResultList.remove(0);
//                                    adapter.notifyItemRemoved(0);
                                }
                                //添加显示人员时，保存其trackId
                                compareResult.setTrackId(requestId);
                                compareResultList.add(compareResult);
//                                adapter.notifyItemInserted(compareResultList.size() - 1);
                            }
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
                            faceHelper.addName(requestId, compareResult.getUserName());

                        } else {
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            faceHelper.addName(requestId, "VISITOR " + requestId);
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
}
