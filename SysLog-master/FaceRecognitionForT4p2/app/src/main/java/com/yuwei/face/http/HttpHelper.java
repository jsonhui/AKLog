package com.yuwei.face.http;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

public class HttpHelper {
	public static int DEFAULT_TIMEOUT = 5;
	private OkHttpClient okHttpClient;

	private HttpHelper() {
		OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
		builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
		okHttpClient = builder.build();
	}

	public static HttpHelper getInstance() {
		return HttpHelperHolder.instance;
	}

	public OkHttpClient getOkHttpClient() {
		return okHttpClient;
	}

	private static class HttpHelperHolder {
		private static HttpHelper instance = new HttpHelper();
	}
}
