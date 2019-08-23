package com.yuwei.camera.occlusion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import com.yuwei.face.R;
import com.yuwei.sdk.certificate.util.MyOwnTimer;

import java.util.TimerTask;

public class AlarmFlowDialog {
	private WindowManager mWindMangerMessage;
	private LayoutParams vmParamsMessage;
	private View mDialogView;
	private MyOwnTimer timer;
	private Bitmap mBitmap = null;
	private static final int SHOW_TIME = 3000;
	private static final int MSG_CLOSE_FLOATVIEW = 101;
	private static AlarmFlowDialog mAlarmFlowDialog = null;

	public static AlarmFlowDialog getDialog() {
		if (mAlarmFlowDialog == null) {
			mAlarmFlowDialog = new AlarmFlowDialog();
		}
		return mAlarmFlowDialog;
	}

	public void createFloatMessage(Context context, Bitmap image) {
		if (mWindMangerMessage == null) {
			mWindMangerMessage = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			vmParamsMessage = new LayoutParams();
			// 获取浮动窗口视图所在布局
		}
		mBitmap = image;

		// 设置window type
		vmParamsMessage.type = LayoutParams.TYPE_PHONE;
		// 设置图片格式，效果为背景透明
		vmParamsMessage.format = PixelFormat.RGBA_8888;
		// 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
		vmParamsMessage.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
		// 调整悬浮窗显示的停靠位置为左侧置顶
		vmParamsMessage.gravity = Gravity.CENTER;
		// 以屏幕左上角为原点，设置x、y初始值，相对于gravity
		vmParamsMessage.x = 0;
		vmParamsMessage.y = 0;

		// 设置悬浮窗口长宽数据
//		vmParamsMessage.width = 1280;
//		vmParamsMessage.height = 760;
		LayoutInflater inflater = LayoutInflater.from(context);
		// 获取浮动窗口视图所在布局
		mDialogView = inflater.inflate(R.layout.dialog_alarm, null);
		ImageView mAlarmType = mDialogView.findViewById(R.id.dialog_alarm_image);
		mAlarmType.setImageBitmap(mBitmap);
		if (mDialogView.getParent() != null) {
			mWindMangerMessage.removeView(mDialogView);
		}
		mWindMangerMessage.addView(mDialogView, vmParamsMessage);
		mDialogView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
				View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		startTimer(SHOW_TIME);
	}

	private void startTimer(int _time) {
		stopTimer();
		timer = new MyOwnTimer();
		timer.scheduleOneShort(new TimerTask() {

			@Override
			public void run() {
				handler.sendEmptyMessage(MSG_CLOSE_FLOATVIEW);
			}
		}, _time);
	}

	private void stopTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	public void hideFloatView() {
		if (mDialogView != null && mDialogView.getParent() != null) {
			mWindMangerMessage.removeView(mDialogView);
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_CLOSE_FLOATVIEW:
				hideFloatView();
				if (mBitmap != null) {
					mBitmap.recycle();
				}
				break;
			}
		};
	};

}
