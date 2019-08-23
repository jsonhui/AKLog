package com.yuwei.face.http;

import java.util.Map;

import com.yuwei.face.http.callback.CallBack;
import com.yuwei.face.http.callback.CallBackPro;
import com.yuwei.face.http.callback.IView;

public class OkHttpManager {
	private IView iv;

	public OkHttpManager(IView iv) {
		this.iv = iv;
	}

	public void uploadFile(String url, Map<String, Object> paramsMap) {
		OkHttpUtils.uploadFile(url, paramsMap, new CallBack() {
			@Override
			public void onSuccess(Object o) {
				iv.onSuccess(o, 1);
			}

			@Override
			public void onFailed(Exception e) {
				iv.onFailed(e, 1);
			}
		});
	}

	public void updateFile(String url, Map<String, Object> paramsMap) {
		OkHttpUtils.updateFile(url, paramsMap, new CallBackPro() {
			@Override
			public void onSuccess(Object o) {
				iv.onSuccess(o, 2);
			}

			@Override
			public void onFailed(Exception e) {
				iv.onFailed(e, 2);
			}

			@Override
			public void onProgressBar(Long i) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void sendFaceFile(String url, Map<String, Object> paramsMap) {
		OkHttpUtils.uploadFile(url, paramsMap, new CallBack() {
			@Override
			public void onSuccess(Object o) {
				iv.onSuccess(o, 3);
			}

			@Override
			public void onFailed(Exception e) {
				iv.onFailed(e, 3);
			}
		});
	}

	public void deleteFaceInfo(String url, Map<String, String> paramsMap) {
		OkHttpUtils.get(url, paramsMap, new CallBack() {
			@Override
			public void onSuccess(Object o) {
				iv.onSuccess(o, 4);
			}

			@Override
			public void onFailed(Exception e) {
				iv.onFailed(e, 4);
			}
		});
	}

	public void getFaceInfo(String url, Map<String, String> paramsMap) {
		OkHttpUtils.get(url, paramsMap, new CallBack() {
			@Override
			public void onSuccess(Object o) {
				iv.onSuccess(o, 5);
			}

			@Override
			public void onFailed(Exception e) {
				iv.onFailed(e, 5);
			}
		});
	}

	// 防止内存泄漏
	public void detatch() {
		if (iv != null) {
			iv = null;
		}
	}
}
