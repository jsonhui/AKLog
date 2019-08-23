package com.yuwei.face.http.callback;

public interface CallBackPro {
	void onSuccess(Object o);

	void onFailed(Exception e);

	void onProgressBar(Long i);
}
