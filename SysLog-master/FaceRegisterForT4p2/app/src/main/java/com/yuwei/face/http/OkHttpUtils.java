package com.yuwei.face.http;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.yuwei.face.http.callback.CallBack;
import com.yuwei.face.http.callback.CallBackPro;
import com.yuwei.face.util.Constant;

import android.os.Handler;
import android.util.Log;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpUtils {
	private static String TAG = "OkHttp";
	private static Handler handler = new Handler();

	// 上传人脸信息&上传照片进行识别
	public static void uploadFile(String url, Map<String, Object> paramsMap, final CallBack callBack) {
		MultipartBody.Builder multipartBody = new MultipartBody.Builder();
		// form 表单上传
		multipartBody.setType(MultipartBody.FORM);
		// 拼接参数
		for (String key : paramsMap.keySet()) {
			Object object = paramsMap.get(key);
			if (object instanceof String) {
				multipartBody.addFormDataPart(key, object.toString());
			} else if (object instanceof File) {
				File file = (File) object;
				multipartBody.addFormDataPart(key, file.getName(),
						MultipartBody.create(MediaType.parse("multipart/form-data"), file));
			}
		}
		RequestBody requestBody = multipartBody.build();
		Log.i("OkHttp", "url: " + url);
		OkHttpClient okHttpClient = HttpHelper.getInstance().getOkHttpClient();
		Request request = new Request.Builder().url(url).post(requestBody).build();
		okHttpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, final IOException e) {
				Log.i(TAG, "onFailure--e: " + e);
				handler.post(new Runnable() {
					@Override
					public void run() {
						callBack.onFailed(e);
					}
				});
			}

			@Override
			public void onResponse(Call call, final Response response) throws IOException {
				Log.i(TAG, "response: " + response);
				final String str = response.body().string();
				Log.i(TAG, "str: " + str);
				// 解析
				final MsgBean msgBean = new Gson().fromJson(str, MsgBean.class);
				handler.post(new Runnable() {
					@Override
					public void run() {
						callBack.onSuccess(msgBean);
					}
				});
			}
		});
	}

	// 根据drivercode获取司机信息
	public static void get(String url, Map<String, String> map, final CallBack callBack) {
		// 对url和参数做拼接处理
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(url);

		if (map != null) {
			// 判断是否存在? if中是存在
			if (stringBuffer.indexOf("?") != -1) {
				// 判断?是否在最后一位 if中是不在最后一位
				if (stringBuffer.indexOf("?") != stringBuffer.length() - 1) {
					stringBuffer.append("&");
				}
			} else {
				stringBuffer.append("?");
			}
			for (Map.Entry<String, String> entry : map.entrySet()) {
				stringBuffer.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
			}
			// 判断是否存在& if中是存在
			if (stringBuffer.indexOf("&") != -1) {
				stringBuffer.deleteCharAt(stringBuffer.lastIndexOf("&"));
			}
		}
		Log.i("OkHttp", "url:" + stringBuffer.toString());
		OkHttpClient okHttpClient = HttpHelper.getInstance().getOkHttpClient();
		final Request request = new Request.Builder().get().url(stringBuffer.toString()).build();
		okHttpClient.newCall(request).enqueue(new Callback() {
			// 请求失败
			@Override
			public void onFailure(Call call, final IOException e) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						callBack.onFailed(e);
					}
				});
			}

			// 请求成功
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				Log.i(TAG, "response: " + response);
				String result = response.body().string();
				Log.i(TAG, "result: " + result);
				// 解析
				final MsgBean msgBean = new Gson().fromJson(result, MsgBean.class);
				handler.post(new Runnable() {
					@Override
					public void run() {
						callBack.onSuccess(msgBean);
					}
				});
				if (msgBean.data != null) {
					String jsonStr = new Gson().toJson(msgBean.data);
					dataToUrls(jsonStr);
				}
			}
		});
	}

	// 删除人脸信息
	public static void get(String url, final CallBack callBack) {
		OkHttpClient okHttpClient = HttpHelper.getInstance().getOkHttpClient();
		final Request request = new Request.Builder().get().url(url).build();
		okHttpClient.newCall(request).enqueue(new Callback() {
			// 请求失败
			@Override
			public void onFailure(Call call, final IOException e) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						callBack.onFailed(e);
					}
				});
			}

			// 请求成功
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				Log.i(TAG, "response: " + response);
				callBack.onSuccess(response);
			}
		});
	}

	// 根据车牌号检测人脸数据是否有更新
	public static void updateFile(String url, Map<String, Object> paramsMap, final CallBackPro callBack) {
		String jsonStr = new Gson().toJson(paramsMap);
		MediaType jsonType = MediaType.parse("application/json; charset=utf-8");
		RequestBody requestBody = RequestBody.create(jsonType, jsonStr);
		Log.i(TAG, "url: " + url + "  jsonStr: " + jsonStr);
		OkHttpClient okHttpClient = HttpHelper.getInstance().getOkHttpClient();
		Request request = new Request.Builder().url(url).post(requestBody).build();
		okHttpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, final IOException e) {
				Log.i(TAG, "onFailure--e: " + e);
				handler.post(new Runnable() {
					@Override
					public void run() {
						callBack.onFailed(e);
					}
				});
			}

			@Override
			public void onResponse(Call call, final Response response) throws IOException {
				Log.i(TAG, "response: " + response);
				final String str = response.body().string();
				Log.i(TAG, "str: " + str);
				// 解析
				final MsgBean msgBean = new Gson().fromJson(str, MsgBean.class);
				handler.post(new Runnable() {
					@Override
					public void run() {
						callBack.onSuccess(msgBean);
					}
				});
				if (msgBean.status.equals("01") && msgBean.data != null) {
					Log.i(TAG, "data: " + msgBean.data);
					String jsonStr = new Gson().toJson(msgBean.data);
					JsonArray jsonArray = new JsonParser().parse(jsonStr).getAsJsonArray();
					for (JsonElement data : jsonArray) {
						if (data != null) {
							String jsonData = new Gson().toJson(data);
							dataToUrls(jsonData);
						}
					}
				}
			}
		});
	}

	private static void dataToUrls(String jsonStr) {
		JsonObject jsonObject = new JsonParser().parse(jsonStr).getAsJsonObject();
		JsonArray jsonArray = jsonObject.getAsJsonArray("files");
		if (jsonArray.size() > 0) {
			List<FaceInfoBean> faceInfoBeanList = new ArrayList<>();
			for (JsonElement faceInfo : jsonArray) {
				FaceInfoBean faceInfoBean = new Gson().fromJson(faceInfo, new TypeToken<FaceInfoBean>() {
				}.getType());
				faceInfoBeanList.add(faceInfoBean);
			}
			ArrayList<String> imageUrls = new ArrayList<>();
			for (FaceInfoBean face : faceInfoBeanList) {
				imageUrls.add(face.picpath);
			}
			saveFiles(imageUrls, true);
			ArrayList<String> featureUrls = new ArrayList<>();
			for (FaceInfoBean face : faceInfoBeanList) {
				featureUrls.add(face.facedata);
			}
			saveFiles(featureUrls, false);
		}
	}

	private static void saveFiles(final ArrayList<String> urls, final boolean saveImg) {
		ArrayList<Observable<Boolean>> observables = new ArrayList<>();
		final AtomicInteger count = new AtomicInteger();
		for (final String url : urls) {
			observables.add(downloadFile(url).subscribeOn(Schedulers.io()).map(new Function<ResponseBody, Boolean>() {
				@Override
				public Boolean apply(ResponseBody responseBody) throws Exception {
					saveIo(responseBody.byteStream(), saveImg, saveImg ? getFileName(url) + Constant.IMG_SUFFIX
							: getFileName(url) + Constant.FEATURE_SUFFIX);
					return true;
				}
			}));
		}
		// observable的merge 将所有的observable合成一个Observable，所有的observable同时发射数据
		Observable.merge(observables).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
			@Override
			public void accept(Boolean b) throws Exception {
				Log.e(TAG, "download is accept---b: " + b);
				if (b) {
					count.addAndGet(1);
					Log.e(TAG, "download is succcess");
				}
			}
		}, new Consumer<Throwable>() {
			@Override
			public void accept(Throwable throwable) throws Exception {
				Log.e(TAG, "download error");
			}
		}, new Action() {
			@Override
			public void run() throws Exception {
				Log.e(TAG, "download complete");
				// 下载成功的数量 和 图片集合的数量一致，说明全部下载成功了
				if (urls.size() == count.get()) {
					Log.i(TAG, "保存成功");
				} else {
					if (count.get() == 0) {
						Log.i(TAG, "保存失败");
					} else {
						Log.i(TAG, "因网络问题 保存成功: " + count + "--保存失败" + (urls.size() - count.get()));
					}
				}
			}
		}, new Consumer<Disposable>() {

			@Override
			public void accept(Disposable disposable) throws Exception {
				// TODO Auto-generated method stub
				Log.e(TAG, "download disposable");
			}
		});
	}

	private static void saveIo(InputStream inputStream, boolean saveImg, String fileName) {
		String localSavePath = Constant.ROOT_PATH + (saveImg ? Constant.SAVE_IMG_DIR : Constant.SAVE_FEATURE_DIR)
				+ File.separator + fileName;
		File localSaveFile = new File(localSavePath);
		if (!localSaveFile.exists()) {
			localSaveFile.getParentFile().mkdirs();
			try {
				localSaveFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileOutputStream fos = null;
		BufferedInputStream bis = null;
		try {
			fos = new FileOutputStream(localSaveFile);
			bis = new BufferedInputStream(inputStream);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = bis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bis != null) {
					bis.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static Observable<ResponseBody> downloadFile(final String url) {
		return Observable.create(new ObservableOnSubscribe<ResponseBody>() {

			@Override
			public void subscribe(final ObservableEmitter<ResponseBody> emitter) throws Exception {
				// TODO Auto-generated method stub
				get(url, new CallBack() {

					@Override
					public void onSuccess(Object o) {
						// TODO Auto-generated method stub
						Log.i(TAG, "onSuccess--下载成功");
						emitter.onNext(((Response) o).body());
						emitter.onComplete();
					}

					@Override
					public void onFailed(Exception e) {
						// TODO Auto-generated method stub
						Log.i(TAG, "onFailure--e: " + e);
						emitter.onError(e);
					}
				});
			}
		});

	}

	private static String getFileName(String url) {
		if (url.contains("/")) {
			String[] parts = url.split("/");
			return parts.length > 1 ? parts[parts.length - 2] : parts[parts.length - 1];
		} else {
			return "123456";
		}

	}
}
