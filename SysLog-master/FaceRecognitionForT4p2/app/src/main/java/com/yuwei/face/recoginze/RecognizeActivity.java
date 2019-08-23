package com.yuwei.face.recoginze;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.VersionInfo;
import com.yuwei.face.R;
import com.yuwei.face.callback.ReceiveYuvFrameCallBack;
import com.yuwei.face.faceserver.CompareResult;
import com.yuwei.face.faceserver.FaceServer;
import com.yuwei.face.model.FacePreviewInfo;
import com.yuwei.face.play.CHReceiveServices;
import com.yuwei.face.service.FaceService;
import com.yuwei.face.util.ConfigUtil;
import com.yuwei.face.util.Constant;
import com.yuwei.face.util.ImageUtil;
import com.yuwei.face.util.NetWorkUtils;
import com.yuwei.face.util.NotifyUtil;
import com.yuwei.face.util.StorageList;
import com.yuwei.face.util.YuweiFaceUtil;
import com.yuwei.face.util.face.FaceHelper;
import com.yuwei.face.util.face.FaceListener;
import com.yuwei.face.util.face.RequestFeatureStatus;
import com.yuwei.face.widget.LoadingView;
import com.yuwei.sdk.YwSdkManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
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

public class RecognizeActivity extends Activity implements ReceiveYuvFrameCallBack, Callback {

	private final String TAG = "RecognizeActivity";
	private CHReceiveServices mChReceiveServices;
	private SurfaceView mDisplaySurface;
	private ImageView mScann_line, mFaceLabel;
	private static final int MAX_DETECT_NUM = 10;
	private final int MSG_SOCKET_TIME_OUT = 1;
	private final int MSG_FINISH = 2;
	private final int MSG_SHOW_TEXT = 3;
	private final int MSG_RECOGINE_FACE_SUCCESS = 4;// 人脸识别结果
	private final int MSG_RECOGINE_FACE_FAIL = 5;// 人脸识别失败
	private final int MSG_BEGIN_RECOGNIZE = 6;// 开始人脸识别
	public static final int MSG_DO_DETACT = 8;
	private FaceHelper faceHelper;
	private int isRegisterNum = 0;
	private static final float SIMILAR_THRESHOLD = 0.6F;
	private List<CompareResult> compareResultList;
	public static final int MSG_SEND_OREDER_MSG = 200;
	private final String action = "action.face.recognize.drivercode";
	private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
	private final Object bmpPersonLocker = new Object();
	private boolean isStart = true;
	private FaceEngine faceEngine;
	private int afCode = -1;
	private int activeCode = -1;
	private Toast toast = null;
	private final int MSG_ENGINE_ACTIVE_NEED_NETWORK = 100;// 引擎激活需网络通畅
	private final int MSG_ENGINE_ACTIVE_SHOW_LOADING = 101;// 引擎激活显示等待
	private LoadingView waitView;

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_SOCKET_TIME_OUT:
				playVoice(getString(R.string.va_connect_failed_tips));
				if (mHandler != null) {
					mHandler.sendEmptyMessageDelayed(MSG_SOCKET_TIME_OUT, 30000);
				}
				break;
			case MSG_FINISH:
				finish();
				break;
			case MSG_SHOW_TEXT:
				String text = (String) msg.obj;
				showNotify(text);
				break;
			case MSG_RECOGINE_FACE_FAIL:
				showNotify(getString(R.string.recognition_failed));
				break;
			case MSG_BEGIN_RECOGNIZE:
				mFaceLabel.setVisibility(View.GONE);
				initFaceHelper();
				isNeedRecoginzeFace = true;
				autoPersonRecognize();
				break;
			case MSG_SEND_OREDER_MSG:
				if (YwSdkManager.getInstants(RecognizeActivity.this) != null)
					YwSdkManager.getInstants(RecognizeActivity.this).sendCmd((byte[]) msg.obj);
				break;
			case MSG_DO_DETACT:
				synchronized (bmpPersonLocker) {
					if (mPersonBitmap == null)
						return;
					Log.d(TAG, "====>>>handle doRecognize");
					if (mHandler != null)
						mHandler.removeMessages(MSG_SOCKET_TIME_OUT);
					recogizePerson();
				}
				break;
			case MSG_ENGINE_ACTIVE_NEED_NETWORK:
				waitView.showSuccess("");
				showToast(getString(R.string.net_active_failed));
				break;
			case MSG_ENGINE_ACTIVE_SHOW_LOADING:
				waitView.startAnimation(getString(R.string.wait));
				break;
			default:
				break;
			}
		}

	};
	private Thread autoPersonThread;
	private Bitmap mPersonBitmap;

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
		findViewById(R.id.import_register_data_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!StorageList.getInstance(RecognizeActivity.this).getExternalStorageState()) {
					showToast(getString(R.string.no_tf_card));
					return;
				}
				String filePath = StorageList.getInstance(RecognizeActivity.this).getExternalStorage() + "/DriverFace/";
				int importSuccess = FaceServer.getInstance().copyDriverRegisterToLocal(filePath);
				Log.i(TAG, "====>>> exportDriverInfoToTFCard--exportSuccess:" + importSuccess);
				switch (importSuccess) {
				case 0:
					showToast(getString(R.string.no_register_date));
					break;
				case 1:
					showToast(getString(R.string.no_register_date));
					break;
				case 2:
					showToast(getString(R.string.import_success));
					FaceServer.getInstance().resetFaceList(RecognizeActivity.this);
					break;
				}
			}
		});
		
	}

	private void initView() {
		mDisplaySurface = (SurfaceView) findViewById(R.id.display_surface);
		mDisplaySurface.getHolder().addCallback(this);
		mScann_line = (ImageView) findViewById(R.id.scann_line);
		mFaceLabel = (ImageView) findViewById(R.id.face_label);
		// 扫描线 动画
		Animation animation = AnimationUtils.loadAnimation(RecognizeActivity.this, R.anim.anim_fp_entry);
		mScann_line.setAnimation(animation);
		waitView = (LoadingView) findViewById(R.id.wait_loading);
	}

	private void initData() {
		if (!YuweiFaceUtil.isEngineActivated(RecognizeActivity.this)) {
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
			initEngine();
			if (!FaceServer.getInstance().init(RecognizeActivity.this)) {
				FaceServer.getInstance().resetFaceList(RecognizeActivity.this);
			}
		}
		compareResultList = new ArrayList<>();
	}

	private void initFaceHelper() {

		if (faceHelper == null && afCode == 0) {
			if (mPersonBitmap == null) {
				return;
			}
			faceHelper = new FaceHelper.Builder().faceEngine(faceEngine).frThreadNum(MAX_DETECT_NUM)
					.previewWidth(mPersonBitmap.getWidth()).previewHeight(mPersonBitmap.getHeight())
					.faceListener(faceListener)
					.currentTrackId(ConfigUtil.getTrackId(RecognizeActivity.this.getApplicationContext())).build();
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

				for (int i = 0; i < facePreviewInfoList.size(); i++) {

					/**
					 * 对于每个人脸，若状态为空或者为失败，则请求FR（可根据需要添加其他判断以限制FR次数）， FR回传的人脸特征结果在
					 * {@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer)}
					 * 中回传
					 */
					if (requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == null
							|| requestFeatureStatusMap
									.get(facePreviewInfoList.get(i).getTrackId()) == RequestFeatureStatus.FAILED) {
						requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(),
								RequestFeatureStatus.SEARCHING);
						faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), width1, height1,
								FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
						// Log.i(TAG, "onPreview: fr start = " +
						// System.currentTimeMillis() + " trackId = " +
						// facePreviewInfoList.get(i).getTrackId());
					}
				}
			}
		}
	}

	final FaceListener faceListener = new FaceListener() {
		@Override
		public void onFail(Exception e) {
			Log.e(TAG, "onFail: " + e.getMessage());
		}

		// 请求FR的回调
		@Override
		public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId) {
			// //FR成功
			if (faceFeature != null) {
				Log.i(TAG, "onPreview: fr end = " + System.currentTimeMillis() + " trackId = " + requestId);
				searchFace(faceFeature, requestId);

			}
		}
	};

	/**
	 *
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
							requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
							faceHelper.addName(requestId, "VISITOR " + requestId);
							return;
						}

						Log.i(TAG, "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = "
								+ requestId + "  similar = " + compareResult.getSimilar());
						if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
							isRegisterNum = 0;
							playVoice(getString(R.string.recognition_success));
							Log.i(TAG, "face recoginize finish the driver_code is::::" + compareResult.getUserName());
							Intent intent = new Intent();
							intent.setAction(action);
							intent.putExtra("driver_code", compareResult.getUserName());
							sendBroadcast(intent);
							byte[] data = GetReport0xD3(1, 1);
							Message m = new Message();
							m.what = MSG_SEND_OREDER_MSG;
							m.obj = data;
							if (mHandler != null) {
								mHandler.sendMessage(m);
								mHandler.sendEmptyMessageDelayed(MSG_FINISH, 1500);
							}

							requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
							faceHelper.addName(requestId, compareResult.getUserName());
							// finish();

							// showToast(compareResult.getUserName());
							// Intent intent = new
							// Intent(RecognizeActivity.this,RfidDetectActivity.class);
							// intent.putExtra("name",
							// compareResult.getUserName());
							// startActivity(intent);
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
								// 对于多人脸搜索，假如最大显示数量为 MAX_DETECT_NUM
								// 且有新的人脸进入，则以队列的形式移除
								if (compareResultList.size() >= MAX_DETECT_NUM) {
									compareResultList.remove(0);
								}
								// 添加显示人员时，保存其trackId
								compareResult.setTrackId(requestId);
								compareResultList.add(compareResult);
							}
							requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
							faceHelper.addName(requestId, compareResult.getUserName());

						} else {
							isRegisterNum++;
							if (isRegisterNum > 2) {
								playVoice(getString(R.string.recognition_failed));
								isRegisterNum = 0;
							}

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

	@Override
	protected void onResume() {
		super.onResume();
		isStart = true;
		mFaceLabel.setVisibility(View.VISIBLE);
		if (mHandler != null) {
			mHandler.sendEmptyMessageDelayed(MSG_BEGIN_RECOGNIZE, 2000);
			mHandler.sendEmptyMessageDelayed(MSG_SOCKET_TIME_OUT, 30000);
		}
	}

	private void initSurface() {
		mChReceiveServices = CHReceiveServices.getInstance();
		mChReceiveServices.initFaceRecoginzeCamera(mDisplaySurface.getHolder().getSurface(), this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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

	public void autoPersonRecognize() {
		autoPersonThread = new Thread() {
			@Override
			public void run() {
				super.run();
				while (!interrupted() && isStart) {
					try {
						synchronized (bmpPersonLocker) {
							if (mChReceiveServices != null) {
								mPersonBitmap = mChReceiveServices.getFramePersonPlayer().getFrame();
							}
							mHandler.sendEmptyMessage(MSG_DO_DETACT);
						}
						sleep(5000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		autoPersonThread.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "-----------onPause-------------");
		isNeedRecoginzeFace = false;
		mHandler.removeMessages(MSG_SOCKET_TIME_OUT);
		mHandler.removeMessages(MSG_FINISH);
		mHandler.removeMessages(MSG_SEND_OREDER_MSG);
		if (mChReceiveServices != null) {
			mChReceiveServices.onStop();
		}
		isStart = false;
		// finish();
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

	private void showNotify(String content) {
		YuweiFaceUtil.showTopMessage(RecognizeActivity.this, content);
	}

	private boolean isNeedRecoginzeFace = false;

	@Override
	public void onYuvFrameCallBack(byte[] yuvData, int videoWidth, int videoHeight) {
		if (isNeedRecoginzeFace) {
			Log.i(TAG, "--------onYuvFrameCallBack yuvData.length-------" + yuvData.length);
		}
	}

	public void return_home(View view) {
		this.finish();
	}

	private void initEngine() {
		faceEngine = new FaceEngine();
		afCode = faceEngine.init(this, FaceEngine.ASF_DETECT_MODE_IMAGE, FaceEngine.ASF_OP_0_ONLY, 16, 10,
				FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT);
		VersionInfo versionInfo = new VersionInfo();
		faceEngine.getVersion(versionInfo);
		Log.i(TAG, "initEngine:  init: " + afCode + "  version:" + versionInfo);
	}

	private void unInitEngine() {

		if (afCode == 0) {
			afCode = faceEngine.unInit();
			Log.i(TAG, "unInitEngine: " + afCode);
		}
	}

	/**
	 * 激活引擎
	 *
	 */
	public void activeEngine() {
		Observable.create(new ObservableOnSubscribe<Integer>() {
			@Override
			public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
				FaceEngine faceEngine = new FaceEngine();
				activeCode = faceEngine.active(RecognizeActivity.this, Constant.APP_ID, Constant.SDK_KEY);
				emitter.onNext(activeCode);
				emitter.onComplete();
			}
		}).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Integer>() {
			@Override
			public void onSubscribe(Disposable d) {

			}

			@Override
			public void onNext(Integer activeCode) {
				waitView.showSuccess("");
				if (activeCode == ErrorInfo.MOK) {
					initEngine();
					if (!FaceServer.getInstance().init(RecognizeActivity.this)) {
						FaceServer.getInstance().resetFaceList(RecognizeActivity.this);
					}
					showToast(getString(R.string.active_success));
				} else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
				} else {
					showToast(getString(R.string.active_failed));
				}
			}

			@Override
			public void onError(Throwable e) {
				waitView.showSuccess("");
				showToast(getString(R.string.active_failed));
			}

			@Override
			public void onComplete() {

			}
		});
	}

	private void showToast(String s) {
		if (toast == null) {
			toast = Toast.makeText(RecognizeActivity.this, s, Toast.LENGTH_LONG);
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
		// showToast(text);
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
		Log.i(TAG, "====>>> surfaceCreated:");
		initSurface();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub

	}

}
