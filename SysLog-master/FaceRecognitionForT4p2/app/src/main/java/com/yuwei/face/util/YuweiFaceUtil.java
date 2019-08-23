package com.yuwei.face.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.yuwei.face.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

public class YuweiFaceUtil {
	public static final String ROOT_PATH = "/data/data";
	private static String TAG = "YuweiFaceUtil";

	public static Bitmap getimage(String srcPath, boolean needRoundCorner) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// 设置司机图片显示的宽跟高
		float hh = 270f;
		float ww = 240f;
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 2;// be=1表示不缩放
		if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置缩放比例
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		if (needRoundCorner)
			bitmap = getRoundedCornerBitmap(bitmap, 20);
		return bitmap;
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int round) {
		try {
			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
			final float roundPx = round;
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(Color.BLACK);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

			final Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

			canvas.drawBitmap(bitmap, src, rect, paint);
			return output;
		} catch (Exception e) {
			return bitmap;
		}
	}

	public static Bitmap getBitmapByYuvData(byte[] yuvData, int width, int height) {
		int frameSize = width * height;
		int[] rgba = new int[frameSize];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int y = (0xff & ((int) yuvData[i * width + j]));
				int u = (0xff & ((int) yuvData[frameSize + (i >> 1) * width + (j & ~1) + 0]));
				int v = (0xff & ((int) yuvData[frameSize + (i >> 1) * width + (j & ~1) + 1]));
				y = y < 16 ? 16 : y;
				int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
				int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
				int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));
				r = r < 0 ? 0 : (r > 255 ? 255 : r);
				g = g < 0 ? 0 : (g > 255 ? 255 : g);
				b = b < 0 ? 0 : (b > 255 ? 255 : b);
				rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
			}
		}
		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bmp.setPixels(rgba, 0, width, 0, 0, width, height);
		return bmp;
	}

	public static final String YUWEI_NOTIFY_TYPE = "yuwei_notify_type";
	public static final String YUWEI_TTS_STR = "yuwei_tts_str";
	public static final int SIMPLE_SOUND = 1;
	private static NotificationManager nm = null;

	public static void showTopMessage(Context context, String _msg) {
		if (context == null)
			return;
		if (nm != null)
			nm.cancel(1);
		else
			nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Bundle bundle = new Bundle();
		bundle.putInt(YUWEI_NOTIFY_TYPE, SIMPLE_SOUND);
		bundle.putString(YUWEI_TTS_STR, _msg);
		Notification notification = new Notification.Builder(context).setExtras(bundle).setContentText(_msg)
				.setSmallIcon(R.drawable.evaluate_1).build();
		nm.notify(1, notification);
	}

	// 判断人脸识别引擎已激活
	public static boolean isEngineActivated(Context context) {
		File activatedFile = new File(ROOT_PATH + File.separator + getPackageName(context) + "/files/.asf_install.dat");
		Log.i(TAG, "path==>" + activatedFile.getAbsolutePath());
		if (activatedFile.exists() && activatedFile.isFile() && activatedFile.length() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * [获取应用程序包名]
	 * 
	 * @param context
	 * @return 当前应用的包名
	 */
	public static synchronized String getPackageName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.packageName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将图片转换成Base64编码的字符串
	 *
	 * @param path
	 *            图片本地路径
	 * @return base64编码的字符串
	 */
	public static String imageToBase64(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		InputStream is = null;
		byte[] data;
		String result = null;
		try {
			is = new FileInputStream(path);
			// 创建一个字符流大小的数组。
			data = new byte[is.available()];
			// 写入数组
			is.read(data);
			// 用默认的编码格式进行编码
			result = Base64.encodeToString(data, Base64.NO_WRAP);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Log.i(TAG, "result==> " + result);
		return result;
	}

	public static String getCurrTime() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		// 获取当前时间
		Date date = new Date(System.currentTimeMillis());
		return simpleDateFormat.format(date);
	}
}
