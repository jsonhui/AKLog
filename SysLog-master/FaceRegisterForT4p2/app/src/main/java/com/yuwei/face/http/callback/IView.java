package com.yuwei.face.http.callback;

public interface IView {
	void onSuccess(Object o, int tag);

	void onFailed(Exception e, int tag);
}
