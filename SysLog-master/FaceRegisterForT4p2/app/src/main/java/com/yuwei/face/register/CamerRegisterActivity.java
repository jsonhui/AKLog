package com.yuwei.face.register;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.VersionInfo;
import com.yuwei.face.camera.util.CameraHelper;
import com.yuwei.face.camera.util.CameraListener;
import com.yuwei.face.faceserver.FaceServer;
import com.yuwei.face.http.MsgBean;
import com.yuwei.face.http.OkHttpManager;
import com.yuwei.face.http.callback.IView;
import com.yuwei.face.model.FacePreviewInfo;
import com.yuwei.face.register.R;
import com.yuwei.face.register.view.MyDialog;
import com.yuwei.face.util.ConfigUtil;
import com.yuwei.face.util.Constant;
import com.yuwei.face.util.NetWorkUtils;
import com.yuwei.face.util.StorageList;
import com.yuwei.face.util.YuweiFaceUtil;
import com.yuwei.face.util.face.FaceHelperCamera;
import com.yuwei.face.util.face.FaceListener;
import com.yuwei.face.widget.LoadingView;
import com.yuwei.sdk.certificate.DataBase;
import com.yuwei.sdk.entity.DriverInfo;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.ViewTreeObserver;
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

public class CamerRegisterActivity extends Activity implements ViewTreeObserver.OnGlobalLayoutListener, IView {

	private final String TAG = "CamerRegisterActivity";
	private TextureView mDisplaySurface;
	private ImageView mScann_line, mFaceLabel;
	private static final int MAX_DETECT_NUM = 10;
	private final int MSG_UPDATE_UI_MSG = 1;
	private final int MSG_START_HIDE_TIMER_MSG = 2;
	private final int MSG_SHOW_TEXT = 3;
	private final int MSG_REGISTER_FACE_SUCCESS = 4;// 人脸注册结果
	private final int MSG_REGISTER_FACE_FAIL = 5;// 人脸注册失败
	private final int MSG_BEGIN_RECOGNIZE = 6;// 开始人脸识别
	private final int MSG_ENGINE_ACTIVE_NEED_NETWORK = 100;// 引擎激活需网络通畅
	private final int MSG_ENGINE_ACTIVE_SHOW_LOADING = 200;// 引擎激活显示等待
	public static final int MSG_DO_DETACT = 8;
	private FaceHelperCamera faceHelper;
	private LoadingView waitView;
	private Integer cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;

	/**
	 * 注册人脸状态码，准备注册
	 */
	private static final int REGISTER_STATUS_READY = 0;
	/**
	 * 注册人脸状态码，注册中
	 */
	private static final int REGISTER_STATUS_PROCESSING = 1;
	/**
	 * 注册人脸状态码，注册结束（无论成功失败）
	 */
	private static final int REGISTER_STATUS_DONE = 2;

	private int registerStatus = REGISTER_STATUS_DONE;

	private final Object bmpPersonLocker = new Object();
	private CameraHelper cameraHelper;
	private Camera.Size previewSize;

	private FaceEngine faceEngine;
	private int afCode = -1;
	private Toast toast = null;

	// private T4SDK mT4SDK;
	private String driverCode;
	private static final int rotation = 3;
	private Disposable disposable;
	private OkHttpManager okHttpManager;

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
			case MSG_REGISTER_FACE_FAIL:
				playVoice(getString(R.string.register_failed));
				break;
			case MSG_REGISTER_FACE_SUCCESS:
				playVoice(getString(R.string.register_success));
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camera_register_main);
		mDisplaySurface = (TextureView) findViewById(R.id.display_surface);
		mScann_line = (ImageView) findViewById(R.id.scann_line);
		mFaceLabel = (ImageView) findViewById(R.id.face_label);
		// 扫描线 动画
		Animation animation = AnimationUtils.loadAnimation(CamerRegisterActivity.this, R.anim.anim_fp_entry);
		mScann_line.setAnimation(animation);
		waitView = (LoadingView) findViewById(R.id.wait_loading);
		findViewById(R.id.go_register_face_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!YuweiFaceUtil.isEngineActivated(CamerRegisterActivity.this)) {
					showToast(getString(R.string.active_failed));
					return;
				}
				// registerFaceInfo(driverCode);
				recordPerson();
			}
		});
		findViewById(R.id.go_remove_face_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int deletedFeatureCount = FaceServer.getInstance().clearAllFaces(CamerRegisterActivity.this);
				Log.i(TAG, "====>>> remove_face--deletedFeatureCount:" + deletedFeatureCount);
				if (deletedFeatureCount > 0) {
					playVoice(getString(R.string.delete_success));
				} else {
					playVoice(getString(R.string.no_face_date));
				}
			}
		});
		findViewById(R.id.export_register_data_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!StorageList.getInstance(CamerRegisterActivity.this).getExternalStorageState()) {
					playVoice(getString(R.string.no_tf_card));
					return;
				}
				String filePath = StorageList.getInstance(CamerRegisterActivity.this).getExternalStorage()
						+ "/DriverFace/";
				// Log.i(TAG, "====>>>
				// exportDriverInfoToTFCard--filePath:"+filePath);
				int exportSuccess = FaceServer.getInstance().exportDriverRegisterToTFCard(filePath);
				Log.i(TAG, "====>>> exportDriverInfoToTFCard--exportSuccess:" + exportSuccess);
				switch (exportSuccess) {
				case 0:
					playVoice(getString(R.string.export_failed));
					break;
				case 1:
					playVoice(getString(R.string.no_register_date));
					break;
				case 2:
					playVoice(getString(R.string.export_success));
					break;
				}
			}
		});

		// mT4SDK = new T4SDK(getApplicationContext());
		// mT4SDK.getCardManager().setIccardStatusListen(this);
		initData();
		okHttpManager = new OkHttpManager(this);
	}

/*	private void registerFaceInfo(String name) {
		synchronized (bmpPersonLocker) {
			if (name == null || name.length() == 0) {
				playVoice("请插入IC卡");
				return;
			}
			register();
		}
	}*/

	private void recordPerson() {

		MyDialog registerdialog = new MyDialog(this, R.style.yuwei_dialog);
		final EditText nameEt = new EditText(this);

		nameEt.setInputType(InputType.TYPE_CLASS_NUMBER);
		nameEt.setBackgroundResource(R.drawable.yuwei_edit_clickbutton);
		registerdialog.setTitle(getString(R.string.person_tip)).setView(nameEt)
				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						final String name = nameEt.getText().toString();

						synchronized (bmpPersonLocker) {

							Log.d(TAG, "====>>>recordPerson " + name);

							if (name.length() == 0) {

								mHandler.obtainMessage(MSG_SHOW_TEXT, getString(R.string.person_tip)).sendToTarget();

								return;

							}
							if (!TextUtils.isEmpty(name)) {
								driverCode = name.trim();
							}
							register();

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
		if (registerdialog != null)
			registerdialog.showMe();
	}

	private void initData() {

		if (!YuweiFaceUtil.isEngineActivated(CamerRegisterActivity.this)) {
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
			FaceServer.getInstance().init(CamerRegisterActivity.this);
		}
		mDisplaySurface.getViewTreeObserver().addOnGlobalLayoutListener(CamerRegisterActivity.this);
	}

	public void register() {
		Log.i(TAG, "register_change-Status--registerStatus:" + registerStatus);
		if (registerStatus == REGISTER_STATUS_DONE) {
			registerStatus = REGISTER_STATUS_READY;
		}
	}

	private void initCamera() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		final FaceListener faceListener = new FaceListener() {
			@Override
			public void onFail(Exception e) {
				Log.e(TAG, "onFail: " + e.getMessage());
			}

			// 请求FR的回调
			@Override
			public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId) {

			}

		};

		CameraListener cameraListener = new CameraListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
				previewSize = camera.getParameters().getPreviewSize();
				faceHelper = new FaceHelperCamera.Builder().faceEngine(faceEngine).frThreadNum(MAX_DETECT_NUM)
						.previewSize(previewSize).faceListener(faceListener)
						.currentTrackId(ConfigUtil.getTrackId(CamerRegisterActivity.this.getApplicationContext()))
						.build();
			}

			@Override
			public void onPreview(final byte[] nv21, Camera camera) {
				List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21);

				if (registerStatus == REGISTER_STATUS_READY && facePreviewInfoList != null
						&& facePreviewInfoList.size() > 0) {
					Log.e(TAG, "====>>> start--register-facePreviewInfoList.size():" + facePreviewInfoList.size());
					registerStatus = REGISTER_STATUS_PROCESSING;
					Observable.create(new ObservableOnSubscribe<Boolean>() {
						@Override
						public void subscribe(ObservableEmitter<Boolean> emitter) {
							Log.i(TAG, "====>>> start--register:");
							boolean success = FaceServer.getInstance().register(CamerRegisterActivity.this,
									nv21.clone(), previewSize.width, previewSize.height, driverCode);
							Log.i(TAG, "====>>> register--success:" + success);
							// if(success)
							// playVoice("注册成功");
							emitter.onNext(success);
							emitter.onComplete();
						}
					}).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
							.subscribe(new Observer<Boolean>() {
								@Override
								public void onSubscribe(Disposable d) {

								}

								@Override
								public void onNext(Boolean success) {
									Log.i(TAG, "====>>> register222--success:" + success);
									if (success){
										getPlateNo(driverCode);//信息上传平台
										mHandler.sendEmptyMessage(MSG_REGISTER_FACE_SUCCESS);
									} else {
										mHandler.sendEmptyMessage(MSG_REGISTER_FACE_FAIL);
									}
									registerStatus = REGISTER_STATUS_DONE;
								}

								@Override
								public void onError(Throwable e) {
									mHandler.sendEmptyMessage(MSG_REGISTER_FACE_FAIL);
									registerStatus = REGISTER_STATUS_DONE;
								}

								@Override
								public void onComplete() {

								}
							});
				}
			}

			@Override
			public void onCameraClosed() {
				Log.i(TAG, "onCameraClosed: ");
			}

			@Override
			public void onCameraError(Exception e) {
				Log.i(TAG, "onCameraError: " + e.getMessage());
			}

			@Override
			public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
				Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
			}
		};

		cameraHelper = new CameraHelper.Builder()
				.previewViewSize(new Point(mDisplaySurface.getMeasuredWidth(), mDisplaySurface.getMeasuredHeight()))
				.rotation(rotation)
				.specificCameraId(cameraID != null ? cameraID : Camera.CameraInfo.CAMERA_FACING_FRONT).isMirror(false)
				.previewOn(mDisplaySurface).cameraListener(cameraListener).build();
		cameraHelper.init();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (cameraHelper != null) {
			cameraHelper.release();
			cameraHelper = null;
		}
		if (faceHelper != null) {
			synchronized (faceHelper) {
				unInitEngine();
			}
			ConfigUtil.setTrackId(this, faceHelper.getCurrentTrackId());
			faceHelper.release();
		} else {
			unInitEngine();
		}
		FaceServer.getInstance().unInit();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "-----------onPause-------------");
	}

	private void showNotify(String content) {
		YuweiFaceUtil.showTopMessage(CamerRegisterActivity.this, content);
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
	 */
	public void activeEngine() {
		Log.i(TAG, "start_activeEngine:");

		Observable.create(new ObservableOnSubscribe<Integer>() {
			@Override
			public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
				FaceEngine faceEngine = new FaceEngine();
				int activeCode = faceEngine.active(CamerRegisterActivity.this, Constant.APP_ID, Constant.SDK_KEY);
				Log.i(TAG, "active_activeCode:" + activeCode);
				emitter.onNext(activeCode);
				emitter.onComplete();
			}

		}).timeout(2, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Observer<Integer>() {
					@Override
					public void onSubscribe(Disposable d) {
						disposable = d;
					}

					@Override
					public void onNext(Integer activeCode) {
						waitView.showSuccess("");
						if (activeCode == ErrorInfo.MOK) {
							showToast(getString(R.string.active_success));
							// NotifyUtil.showTopMessage(getApplicationContext(),
							// getString(R.string.active_success));
							Log.i(TAG, "active_success");
							initEngine();
							FaceServer.getInstance().init(CamerRegisterActivity.this);
						} else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
							// showToast(getString(R.string.already_activated));
							Log.i(TAG, "already_active_success");
						} else {
							showToast(getString(R.string.active_failed));
							Log.i(TAG, "already_active_failed");
						}
					}

					@Override
					public void onError(Throwable e) {
						Log.i(TAG, "activeEngine--onError: " + e);
						waitView.showSuccess("");
						showToast(getString(R.string.active_failed));
						if (disposable != null && !disposable.isDisposed()) {
							disposable.dispose();
						}
					}

					@Override
					public void onComplete() {
						Log.i(TAG, "activeEngine--onComplete: ");
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

	/*
	 * @Override public void onIccardAttached(boolean attach) { // TODO
	 * Auto-generated method stub Log.i("pppppp", "attach====>" + attach); if
	 * (attach) { Map<String, byte[]> result = mT4SDK.getIccardInfo(); if
	 * (result != null) { DriverInfo info = new DriverInfo(); if
	 * (result.containsKey("driver_name")) { String drivername = new
	 * String(result.get("driver_name")); if (drivername != null &&
	 * drivername.trim().length() > 0) info.setDriverName(drivername); } if
	 * (result.containsKey("driver_certifity")) { String certificate = new
	 * String(result.get("driver_certifity")); if (certificate != null &&
	 * certificate.trim().length() > 0)
	 * info.setDriverVehCertificate(certificate); } if
	 * (result.containsKey("driver_code")) { String driverc = new
	 * String(result.get("driver_code")); if (driverc != null) { Log.i("pppppp",
	 * "driverCode===>" + driverc.trim()); driverCode = driverc.trim(); } } } }
	 * else { driverCode = ""; } }
	 */

	@Override
	public void onGlobalLayout() {
		mDisplaySurface.getViewTreeObserver().removeOnGlobalLayoutListener(this);
		initCamera();
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
