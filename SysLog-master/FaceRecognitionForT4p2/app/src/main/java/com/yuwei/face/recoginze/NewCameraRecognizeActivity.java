package com.yuwei.face.recoginze;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.VersionInfo;
import com.quectel.com.fourcameraperviewdemo.AvcEncoder;
import com.quectel.com.fourcameraperviewdemo.MainActivity;
import com.quectel.com.fourcameraperviewdemo.SurfaceViewFragment;
import com.yuwei.face.R;
import com.yuwei.face.faceserver.CompareResult;
import com.yuwei.face.faceserver.FaceServer;
import com.yuwei.face.http.MsgBean;
import com.yuwei.face.http.OkHttpManager;
import com.yuwei.face.http.callback.IView;
import com.yuwei.face.model.FacePreviewInfo;
import com.yuwei.face.util.ConfigUtil;
import com.yuwei.face.util.Constant;
import com.yuwei.face.util.NetWorkUtils;
import com.yuwei.face.util.StorageList;
import com.yuwei.face.util.YuweiFaceUtil;
import com.yuwei.face.util.face.FaceHelper;
import com.yuwei.face.util.face.FaceListener;
import com.yuwei.face.util.face.RequestFeatureStatus;
import com.yuwei.face.widget.LoadingView;
import com.yuwei.sdk.certificate.DataBase;
import com.yuwei.sdk.entity.DriverInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
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

public class NewCameraRecognizeActivity extends Activity implements ViewTreeObserver.OnGlobalLayoutListener, IView {

	private final String TAG = "NewCameraRecognizeActivity";
	private FrameLayout mDisplaySurface;
	private ImageView mScann_line;
	private static final int MAX_DETECT_NUM = 10;
	private final int MSG_START_HIDE_TIMER_MSG = 2;
	private final int MSG_SHOW_TEXT = 3;
	private final int MSG_RECOGINE_FACE_SUCCESS = 4;// 人脸识别结果
	private final int MSG_RECOGINE_FACE_FAIL = 5;// 人脸识别失败
	public static final int MSG_DO_DETACT = 8;
	private final int MSG_ENGINE_ACTIVE_NEED_NETWORK = 100;// 引擎激活需网络通畅
	private final int MSG_ENGINE_ACTIVE_SHOW_LOADING = 200;// 引擎激活显示等待
	private FaceHelper faceHelper;
	private static final float SIMILAR_THRESHOLD = 0.6F;
	private List<CompareResult> compareResultList;
	private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<Integer, Integer>();

	private FaceEngine faceEngine;
	private int afCode = -1;
	private Toast toast = null;
	private final String action = "action.face.recognize.drivercode";
	private String faceResult = "";
	private LoadingView waitView;
	private OkHttpManager okHttpManager;
	private MainActivity main;
	private AvcEncoder avcEncoder;
	private boolean isPreview = true;
	private int camera_id = 0;

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_START_HIDE_TIMER_MSG:
				finish();
				break;
			case MSG_SHOW_TEXT:
				String text = (String) msg.obj;
				showNotify(text);
				break;
			case MSG_RECOGINE_FACE_FAIL:
				playVoice(getString(R.string.recognition_failed));
				break;
			case MSG_RECOGINE_FACE_SUCCESS:
				// showToast(faceResult);
				playVoice(getString(R.string.recognition_success));
				mHandler.sendEmptyMessageDelayed(MSG_START_HIDE_TIMER_MSG, 1500);
				break;
			case MSG_ENGINE_ACTIVE_NEED_NETWORK:
				waitView.showSuccess("");
				showToast(getString(R.string.net_active_failed));
				break;
			case MSG_ENGINE_ACTIVE_SHOW_LOADING:
				waitView.startAnimation(getString(R.string.wait));
			default:
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.face_recognize_main);
		main = new MainActivity();
		main.onCreate();
		initView();
		initData();
		findViewById(R.id.import_register_data_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!StorageList.getInstance(NewCameraRecognizeActivity.this).getExternalStorageState()) {
					playVoice(getString(R.string.no_tf_card));
					return;
				}
				String filePath = StorageList.getInstance(NewCameraRecognizeActivity.this).getExternalStorage()
						+ "/DriverFace/";
				int importSuccess = FaceServer.getInstance().copyDriverRegisterToLocal(filePath);
				Log.i(TAG, "====>>> exportDriverInfoToTFCard--exportSuccess:" + importSuccess);
				switch (importSuccess) {
				case 0:
					playVoice(getString(R.string.import_failed));
					break;
				case 1:
					playVoice(getString(R.string.no_register_date));
					break;
				case 2:
					playVoice(getString(R.string.import_success));
					FaceServer.getInstance().resetFaceList(NewCameraRecognizeActivity.this);
					break;
				}
			}
		});
	}
	
	private void initView() {
		mDisplaySurface = (FrameLayout) findViewById(R.id.display_surface);
		getFragmentManager().beginTransaction().replace(R.id.display_surface, SurfaceViewFragment.newInstance(0)).commit();
		mScann_line = (ImageView) findViewById(R.id.scann_line);
		// 扫描线 动画
		Animation animation = AnimationUtils.loadAnimation(NewCameraRecognizeActivity.this, R.anim.anim_fp_entry);
		mScann_line.setAnimation(animation);
		waitView = (LoadingView) findViewById(R.id.wait_loading);
		mDisplaySurface.getViewTreeObserver().addOnGlobalLayoutListener(NewCameraRecognizeActivity.this);
	}

	private void initData() {
		compareResultList = new ArrayList<CompareResult>();
		avcEncoder = new AvcEncoder(MainActivity.vid_Width, MainActivity.vid_Height, camera_id);
		okHttpManager = new OkHttpManager(this);
		isUpdateFaceInfo(); //查询该车牌的平台信息是否有更新
		if (!YuweiFaceUtil.isEngineActivated(NewCameraRecognizeActivity.this)) {
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
			if (!FaceServer.getInstance().init(NewCameraRecognizeActivity.this)) {
				FaceServer.getInstance().resetFaceList(NewCameraRecognizeActivity.this);
			}
		}	
	}
	
	private void initPreviewThread() {
		faceHelper = new FaceHelper.Builder().faceEngine(faceEngine).frThreadNum(MAX_DETECT_NUM)
				.previewWidth(MainActivity.pre_Width).previewHeight(MainActivity.pre_Height).faceListener(faceListener)
				.currentTrackId(ConfigUtil.getTrackId(NewCameraRecognizeActivity.this.getApplicationContext())).build();
		PreviewThread previewThread = new PreviewThread();
		previewThread.start();
	}
	
	final FaceListener faceListener = new FaceListener() {
		@Override
		public void onFail(Exception e) {
			Log.e(TAG, "onFail: " + e.getMessage());
		}

		// 请求FR的回调
		@Override
		public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId) {
			// FR成功
			if (faceFeature != null) {
				searchFace(faceFeature, requestId);
			}
			// FR 失败
			else {
				requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
			}
		}
	};
	
	class PreviewThread extends Thread {
		private byte[] mdata;
		private int oldIndex = 0;

		public PreviewThread() {
			mdata = new byte[MainActivity.vid_Height * MainActivity.pre_Width * 3 / 2];
			avcEncoder.getBuff(mdata, camera_id);
		}

		@Override
		public void run() {
			// Log.i(TAG, "camera_id=" + camera_id);
			while (isPreview) {
				int index = avcEncoder.getBufSize(camera_id);
				if (index <= oldIndex) {
					oldIndex = index;
				} else {
					oldIndex = index;					
//					Log.i(TAG, mdata[0] + " " + mdata[1] + " " + mdata[2]);
					if (mdata != null) {
						List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(mdata);
						clearLeftFace(facePreviewInfoList);
						Log.e(TAG, "====>>> start--register-facePreviewInfoList.size():" + facePreviewInfoList.size());
						if (facePreviewInfoList != null && facePreviewInfoList.size() > 0) {

							for (int i = 0; i < facePreviewInfoList.size(); i++) {
								/**
								 * 对于每个人脸，若状态为空或者为失败，则请求FR（可根据需要添加其他判断以限制FR次数），
								 * FR回传的人脸特征结果在
								 * {@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer)}
								 * 中回传
								 */
								if (requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == null
										|| requestFeatureStatusMap
												.get(facePreviewInfoList.get(i).getTrackId()) == RequestFeatureStatus.FAILED) {
									requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(),
											RequestFeatureStatus.SEARCHING);
									faceHelper.requestFaceFeature(mdata, facePreviewInfoList.get(i).getFaceInfo(),
											MainActivity.pre_Width, MainActivity.pre_Height, FaceEngine.CP_PAF_NV21,
											facePreviewInfoList.get(i).getTrackId());
								}
							}
						}
					} else {
						Log.i(TAG + "Rondo", "data null");
					}
				}
			}
		}
	}

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
				faceResult = compareResult.getUserName();
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
							Log.i(TAG, "searchFace failed");
							requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
							faceHelper.addName(requestId, "VISITOR " + requestId);
							return;
						}

						Log.i(TAG, "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = "
								+ requestId + "  similar = " + compareResult.getSimilar());
						if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
							mHandler.sendEmptyMessage(MSG_RECOGINE_FACE_SUCCESS);
							Log.i(TAG, "face recoginize finish the driver_code is::::" + compareResult.getUserName());
							Intent intent = new Intent();
							intent.setAction(action);
							intent.putExtra("driver_code", compareResult.getUserName());
							sendBroadcast(intent);

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
							// showToast("请确认是否已注册");
							// playVoice("人脸识别失败，请确认是否注册");
							mHandler.sendEmptyMessage(MSG_RECOGINE_FACE_FAIL);
							requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
							faceHelper.addName(requestId, "VISITOR " + requestId);
						}
					}

					@Override
					public void onError(Throwable e) {
						requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
						// playVoice("人脸识别失败");
					}

					@Override
					public void onComplete() {

					}
				});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		main.onDestroy();
		isPreview = false;
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

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "-----------onPause-------------");
		mHandler.removeMessages(MSG_START_HIDE_TIMER_MSG);
	}

	private void showNotify(String content) {
		YuweiFaceUtil.showTopMessage(NewCameraRecognizeActivity.this, content);
	}

	public void return_home(View view) {
		this.finish();
	}

	private void initEngine() {
		faceEngine = new FaceEngine();
		afCode = faceEngine.init(this, FaceEngine.ASF_DETECT_MODE_VIDEO, FaceEngine.ASF_OP_0_HIGHER_EXT, 16, 10,
				FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT);
		VersionInfo versionInfo = new VersionInfo();
		faceEngine.getVersion(versionInfo);
		Log.i(TAG, "initEngine:  init: " + afCode + "  version:" + versionInfo);
		if (afCode != ErrorInfo.MOK) {
			showToast(getString(R.string.init_failed));
			// NotifyUtil.showTopMessage(getApplicationContext(),
			// getString(R.string.init_failed));
		} else {
			if (faceHelper != null) {
				faceHelper.setFaceEngine(faceEngine);
			}
		}
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
	 * @param view
	 */
	public void activeEngine() {

		Observable.create(new ObservableOnSubscribe<Integer>() {
			@Override
			public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
				FaceEngine faceEngine = new FaceEngine();
				int activeCode = faceEngine.active(NewCameraRecognizeActivity.this, Constant.APP_ID, Constant.SDK_KEY);
				emitter.onNext(activeCode);
				emitter.onComplete();
			}
		}).timeout(2, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Observer<Integer>() {
					@Override
					public void onSubscribe(Disposable d) {

					}

					@Override
					public void onNext(Integer activeCode) {
						waitView.showSuccess("");
						if (activeCode == ErrorInfo.MOK) {
							initEngine();
							if (!FaceServer.getInstance().init(NewCameraRecognizeActivity.this)) {
								FaceServer.getInstance().resetFaceList(NewCameraRecognizeActivity.this);
							}
							showToast(getString(R.string.active_success));
							// NotifyUtil.showTopMessage(getApplicationContext(),
							// getString(R.string.active_success));
						} else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {

						} else {
							showToast(getString(R.string.active_failed));
						}
					}

					@Override
					public void onError(Throwable e) {
						Log.i(TAG, "activeEngine--onError: " + e);
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
			toast = Toast.makeText(NewCameraRecognizeActivity.this, s, Toast.LENGTH_LONG);
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

	private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
		Set<Integer> keySet = requestFeatureStatusMap.keySet();
		if (compareResultList != null) {
			for (int i = compareResultList.size() - 1; i >= 0; i--) {
				if (!keySet.contains(compareResultList.get(i).getTrackId())) {
					compareResultList.remove(i);
				}
			}
		}
		if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
			requestFeatureStatusMap.clear();
			return;
		}

		for (Integer integer : keySet) {
			boolean contained = false;
			for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
				if (facePreviewInfo.getTrackId() == integer) {
					contained = true;
					break;
				}
			}
			if (!contained) {
				requestFeatureStatusMap.remove(integer);
			}
		}

	}

	@Override
	public void onGlobalLayout() {
		// TODO Auto-generated method stub
		mDisplaySurface.getViewTreeObserver().removeOnGlobalLayoutListener(this);
		// initEngine();
		initPreviewThread();
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

	// 根据driverCode获取车牌号
	private void isUpdateFaceInfo() {
		String driverCode = Settings.System.getString(this.getContentResolver(), Constant.DRIVER_CODE);
		Log.i(TAG, "updataFaceInfo--driverCode: " + driverCode);
		DriverInfo info = DataBase.getDriverLisenceDB().FetchCertificateFromDBTo(driverCode);
		if (info != null) {
			updataFaceInfo(info.getDriverVehPlate());
		}
	}

	// 更新本地人脸信息（平台）
	private void updataFaceInfo(String car_number) {
		String url = Constant.HTTP_URL + File.separator + car_number + "/car";
		String datetime = YuweiFaceUtil.getCurrTime();
		Log.i(TAG, "updataFaceInfo--datatime: " + datetime);
		Map<String, Object> map = new HashMap<>();
		map.put("datetime", datetime);
		okHttpManager.updateFile(url, map);
	}

	// 查询人脸信息（平台）
	private void getFaceInfo(String driverCode, String car_number) {
		String url = Constant.HTTP_URL + File.separator + car_number + "/car";
		Map<String, String> map = new HashMap<>();
		map.put("driver_code", driverCode);
		okHttpManager.getFaceInfo(url, map);
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
