package com.yuwei.face.register;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.yuwei.face.faceserver.FaceServer;
import com.yuwei.face.http.MsgBean;
import com.yuwei.face.http.OkHttpManager;
import com.yuwei.face.http.callback.IView;
import com.yuwei.face.play.CHReceiveServices;
import com.yuwei.face.register.view.MyDialog;
import com.yuwei.face.util.Constant;
import com.yuwei.face.util.ImageUtil;
import com.yuwei.face.util.NetWorkUtils;
import com.yuwei.face.util.StorageList;
import com.yuwei.face.util.YuweiFaceUtil;
import com.yuwei.face.widget.LoadingView;
import com.yuwei.sdk.certificate.DataBase;
import com.yuwei.sdk.entity.DriverInfo;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RegisterActivity extends Activity implements Callback, IView {

    private final String TAG = "RegisterActivity";
    private CHReceiveServices mChReceiveServices;
    private SurfaceView mDisplaySurface;
    private ImageView mScann_line,mFaceLabel;
    private final int MSG_SOCKET_TIME_OUT = 1;
    private final int MSG_FINISH = 2;
    private final int MSG_SHOW_TEXT = 3;
    private final int MSG_BEGIN_RECOGNIZE = 6;
    public static final int MSG_DO_DETACT = 8;
    public static final int MSG_DATA = 200;
    private final Object bmpPersonLocker = new Object();
    private boolean isStart = true;
    private int activeCode = -1;
    private Toast toast = null;
    private final int MSG_ENGINE_ACTIVE_NEED_NETWORK = 100;// 引擎激活需网络通畅
	private final int MSG_ENGINE_ACTIVE_SHOW_LOADING = 101;// 引擎激活显示等待
	private LoadingView waitView;
    private Thread autoPersonThread;
    private Bitmap mPersonBitmap;
    private OkHttpManager okHttpManager;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SOCKET_TIME_OUT:
                	playVoice(getString(R.string.va_connect_failed_tips));
                	if (mHandler != null){
                        mHandler.sendEmptyMessageDelayed(MSG_SOCKET_TIME_OUT,30000);
                    }
                    break;
                case MSG_FINISH:
                    finish();
                    break;
                case MSG_SHOW_TEXT:
                    String text = (String) msg.obj;
                    showNotify(text);
                    break;
                case MSG_BEGIN_RECOGNIZE:
                    mFaceLabel.setVisibility(View.GONE);
                    autoPersonRecognize();
                    break;
                case MSG_DATA:
                    break;
                case MSG_ENGINE_ACTIVE_NEED_NETWORK:
    				waitView.showSuccess("");
    				playVoice(getString(R.string.net_active_failed));
    				break;
    			case MSG_ENGINE_ACTIVE_SHOW_LOADING:
    				waitView.startAnimation(getString(R.string.wait));
    				break;
                default:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.recognize_main);
        initView();
        initData();
        findViewById(R.id.go_register_face_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if (!YuweiFaceUtil.isEngineActivated(RegisterActivity.this)) {
					showToast(getString(R.string.active_failed));
					return;
				}
                if (mPersonBitmap==null) {
                    return;
                }
                recordPerson();
            }
        });
        findViewById(R.id.go_remove_face_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int deletedFeatureCount = FaceServer.getInstance().clearAllFaces(RegisterActivity.this);
                Log.i(TAG, "====>>> remove_face--deletedFeatureCount:"+deletedFeatureCount);
                if (deletedFeatureCount > 0) {
                	playVoice(getString(R.string.delete_success));
                } else{
                	playVoice(getString(R.string.no_face_date));
                }
            }
        });
        findViewById(R.id.export_register_data_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if(!StorageList.getInstance(RegisterActivity.this).getExternalStorageState()){
            		playVoice(getString(R.string.no_tf_card));
        			return;
        		}
            	String filePath = StorageList.getInstance(RegisterActivity.this).getExternalStorage() + "/DriverFace/";
                int exportSuccess = FaceServer.getInstance().exportDriverRegisterToTFCard(filePath);
                Log.i(TAG, "====>>> exportDriverInfoToTFCard--exportSuccess:"+exportSuccess);
                switch(exportSuccess){
                case 0:
                	playVoice(getString(R.string.no_face_date));
                	break;
                case 1:
                	playVoice(getString(R.string.no_face_date));
                	break;	
                case 2:
                	playVoice(getString(R.string.export_success));
                	break;
                }
            }
        });
    }
    
    private void initView(){
        mDisplaySurface = (SurfaceView) findViewById(R.id.display_surface);
        mDisplaySurface.getHolder().addCallback(this);
        mScann_line = (ImageView) findViewById(R.id.scann_line);
        mFaceLabel = (ImageView) findViewById(R.id.face_label);
        // 扫描线 动画
        Animation animation = AnimationUtils.loadAnimation(RegisterActivity.this, R.anim.anim_fp_entry);
        mScann_line.setAnimation(animation);
        waitView = (LoadingView) findViewById(R.id.wait_loading);
    }
    
    private void initData(){
        okHttpManager = new OkHttpManager(this);
    	if(!YuweiFaceUtil.isEngineActivated(RegisterActivity.this)){
			mHandler.sendEmptyMessage(MSG_ENGINE_ACTIVE_SHOW_LOADING);
			NetWorkUtils.isNetWorkAvailableOfGet("https://www.baidu.com", new Comparable<Boolean>() {

			    @Override
			    public int compareTo(Boolean available) {
			        if (available) {
			            // TODO 设备访问Internet正常
			        	activeEngine();
			        } else {
			            // TODO 设备无法访问Internet
			        	mHandler.sendEmptyMessage(MSG_ENGINE_ACTIVE_NEED_NETWORK);	
			        }	
					return 0;		        
			    }
			});
		} else {
			FaceServer.getInstance().init(RegisterActivity.this);
		}
    }

    public void recordPerson() {
    	MyDialog registerdialog = new MyDialog(this, R.style.yuwei_dialog);
    	final EditText editText = new EditText(this);
		editText.setTextSize(40);
		editText.setBackgroundResource(R.drawable.yuwei_edit_clickbutton);
		editText.setInputType(InputType.TYPE_CLASS_NUMBER);
    	registerdialog.setTitle(getString(R.string.person_tip)).setView(editText)
    	.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub				
						final String name = editText.getText().toString();
                        synchronized (bmpPersonLocker) {
                            Log.d(TAG, "====>>>recordPerson " + name);
                            if (name.length() == 0) {
                                mHandler.obtainMessage(MSG_SHOW_TEXT, getString(R.string.person_tip)).sendToTarget();
                                return;
                            }
                            recogizePerson(name);
                        }
                        dialog.dismiss();
					}
				}).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
		if (registerdialog != null){
			registerdialog.showMe();
		}
    	
    }
    private void initSurface(){
        mChReceiveServices = CHReceiveServices.getInstance();
        mChReceiveServices.initFaceRecoginzeCamera(mDisplaySurface.getHolder().getSurface());
    }

    private void recogizePerson(final String name) {
        Log.d(TAG, "====>>> do register");
        if (mPersonBitmap==null) {
        	return;
        }
        final Bitmap recordPic = mPersonBitmap;
        Bitmap bitmap = recordPic.copy(Bitmap.Config.ARGB_8888, true);
        //NV21宽度必须为4的倍数,高度为2的倍数
        bitmap = ImageUtil.alignBitmapForNv21(bitmap);

        final int width1 = bitmap.getWidth();
        final int height1 = bitmap.getHeight();
        //bitmap转NV21
        final byte[] nv21 = ImageUtil.bitmapToNv21(bitmap, width1, height1);
        Log.i(TAG, "====>>> do register--width1:"+width1+"--height1:"+height1);
        if (nv21 != null) {
                Observable.create(new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(ObservableEmitter<Boolean> emitter) {
                        boolean success = FaceServer.getInstance().register(RegisterActivity.this, nv21.clone(), width1, height1, name );
                        Log.i(TAG, "====>>> do register--success:"+success);
                        emitter.onNext(success);
                    }
                })
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Boolean>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Boolean success) {
                            	if (success && mHandler != null){
                                    getPlateNo(name);
                                	mHandler.sendEmptyMessageDelayed(MSG_FINISH, 1000);
                                	if(mChReceiveServices!=null)
                                        mChReceiveServices.onStop();
                                }
                                String result = success ? getString(R.string.register_success) : getString(R.string.register_failed);
                                playVoice(result);
                            }

                            @Override
                            public void onError(Throwable e) {
                            	playVoice(getString(R.string.register_failed));
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mHandler != null){
            mHandler.sendEmptyMessageDelayed(MSG_BEGIN_RECOGNIZE,2000);
            mHandler.sendEmptyMessageDelayed(MSG_SOCKET_TIME_OUT,30000);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        FaceServer.getInstance().unInit();
    }

    public void autoPersonRecognize() {
        autoPersonThread = new Thread() {
            @Override
            public void run() {
                super.run();
                while (!interrupted()&&isStart) {
                    try {
                            synchronized (bmpPersonLocker) {
                                mPersonBitmap = mChReceiveServices.getFramePersonPlayer().getFrame();
                                if(mHandler != null && mPersonBitmap != null)
                                	mHandler.removeMessages(MSG_SOCKET_TIME_OUT);
                        }
                        sleep(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        autoPersonThread.start();
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.i(TAG,"-----------onPause-------------");
        mHandler.removeMessages(MSG_SOCKET_TIME_OUT);
        mHandler.removeMessages(MSG_FINISH);
        mHandler.removeMessages(MSG_DATA);
        if(mChReceiveServices!=null){
            mChReceiveServices.onStop();
        }
        isStart = false;
//        finish();
    }
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    	Log.i(TAG,"-----------onStop-------------");
    	if(mChReceiveServices!=null){
            mChReceiveServices.onDestroy();
        }
    }

    private void showNotify(String content){
        YuweiFaceUtil.showTopMessage(RegisterActivity.this,content);
    }

    public void return_home(View view) {
        this.finish();
    }

    /**
     * 激活引擎
     *
     * @param
     */
    public void activeEngine() {
   
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                FaceEngine faceEngine = new FaceEngine();
                activeCode = faceEngine.active(RegisterActivity.this, Constant.APP_ID, Constant.SDK_KEY);
                Log.i(TAG,"-----------activeEngine-----------activeCode: "+activeCode);
                emitter.onNext(activeCode);
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .timeout(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                    	waitView.showSuccess("");
                        if (activeCode == ErrorInfo.MOK) {
                        	FaceServer.getInstance().init(RegisterActivity.this);
                        	playVoice(getString(R.string.active_success));
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {  
                        } else {
                        	playVoice(getString(R.string.active_failed));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    	waitView.showSuccess("");
                    	playVoice(getString(R.string.active_failed));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void showToast(String s) {
        if (toast == null) {
            toast = Toast.makeText(this, s, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 50);
            LinearLayout layout = (LinearLayout) toast.getView();
            TextView tv = (TextView) layout.getChildAt(0);
            tv.setTextSize(35);
            toast.show();
        } else {
            LinearLayout layout = (LinearLayout) toast.getView();
            TextView tv = (TextView) layout.getChildAt(0);
            tv.setTextSize(35);
            toast.setText(s);
            toast.show();
        }
    }
    
    private long currentTime = 0;
	private void playVoice(String text) {
		long time = System.currentTimeMillis();
		if (time - currentTime < 2000) {
			return;
		}
		// if (mT4SDK != null) {
		// mT4SDK.startPlayTts(text);
		// } else {
		showToast(text);
		// NotifyUtil.showTopMessage(getApplicationContext(), text);
		// }
		currentTime = time;
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		initSurface();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		
	}

    // 获取车牌信息
    private void getPlateNo(String driverCode) {
        DriverInfo info = DataBase.getDriverLisenceDB().FetchCertificateFromDBTo(driverCode);
        if (info != null) {
            uploadFaceInfo(driverCode, info.getDriverVehPlate());
        }
    }

    //上传人脸信息（平台）
    private void uploadFaceInfo(String driverCode, String driverPlateNo) {
        String url = Constant.HTTP_URL + "/face";
        // 要上传的图片路径
        File imgFile = new File(Constant.ROOT_PATH + Constant.SAVE_IMG_DIR + File.separator
                + driverCode + Constant.IMG_SUFFIX);
        File featureFile = new File(Constant.ROOT_PATH + Constant.SAVE_FEATURE_DIR + File.separator
                + driverCode + Constant.FEATURE_SUFFIX);
        String imgPath = Constant.ROOT_PATH + Constant.SAVE_IMG_DIR + File.separator + driverCode
                + Constant.IMG_SUFFIX;
        String featurePath = Constant.ROOT_PATH + Constant.SAVE_FEATURE_DIR + File.separator
                + driverCode + Constant.FEATURE_SUFFIX;
        if (imgFile.exists() && featureFile.exists()) {// 判断是否有此文件
            Map<String, Object> map = new HashMap<>();
            map.put("driver_code", driverCode);
            map.put("driver_pic", YuweiFaceUtil.imageToBase64(imgPath));
            map.put("car_number", driverPlateNo);
//			map.put("face_data", YuweiFaceUtil.imageToBase64(featurePath));
            okHttpManager.uploadFile(url, map);
        } else {
            Log.i(TAG, "文件不存在");
//			playVoice("文件不存在");
        }
    }

    @Override
    public void onSuccess(Object o, int tag) {
        // TODO Auto-generated method stub
        switch (tag) {
            case 1:
                MsgBean msg = (MsgBean) o;
                Log.i(TAG, "msg--: " + o);
                Log.i(TAG, "msg--status: " + msg.status);
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
        }
    }

    @Override
    public void onFailed(Exception e, int tag) {
        // TODO Auto-generated method stub
    }
}
