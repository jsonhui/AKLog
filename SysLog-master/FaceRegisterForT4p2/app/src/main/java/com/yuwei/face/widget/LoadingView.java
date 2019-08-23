package com.yuwei.face.widget;

import com.yuwei.face.register.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * 查询进度View
 * 
 * @author Administrator
 * 
 */
public class LoadingView extends LinearLayout {
	private Context context;
	private ImageView loadingImage;
	private TextView loading_text;
	private String tipDefault = "请稍候";
	private Runnable hideRunnable;
	private Handler mHandler;
	
	public LoadingView(Context _context,AttributeSet attrs) {
		super(_context,attrs);
		this.context = _context;
		getView();
		mHandler = new Handler();
	}

	private void getView() {
		View view = null;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.loading_view, this);
		loadingImage = (ImageView) view.findViewById(R.id.loading_progress);
		loading_text = (TextView) view.findViewById(R.id.loading_text);
		loading_text.setText(tipDefault);
	}
	private int getVisibilityState(){
		return this.getVisibility();
	}
	private void hide(){
		this.setVisibility(View.GONE);
	}
	
	public void setTextColor(int color){
		loading_text.setTextColor(color);
	}
	
	public void startAnimation(String tip){
		setVisibility(View.VISIBLE);
		if (tip == null || tip.length() == 0){
			loading_text.setText(tipDefault);
		}else{
			loading_text.setText(tip);
		}
		loadingImage.setBackgroundResource(R.anim.syncwaitstatus);
		AnimationDrawable animation = (AnimationDrawable) loadingImage.getBackground();
		if(animation != null)
			animation.start();
	}
	
	public void startLoadingView(String tip) {
		startAnimation(tip);
		if (hideRunnable != null){
			mHandler.removeCallbacks(hideRunnable);
		}
		hideRunnable = new Runnable() {
			@Override
			public void run() {
				if (getVisibilityState() == View.VISIBLE) {
					((Activity) context).runOnUiThread(new Runnable() {

						@Override
						public void run() {
//							loadingImage.setBackgroundResource(R.drawable.yuwei_sapi_error);
//							loading_text.setText("加载失败");
							startHideTimer();
						}
					});
				}
			}
		};
		mHandler.postDelayed(hideRunnable, 20000);
	}
	
	public void showFailIcon(String notifyStr){
		loadingImage.setBackgroundResource(R.drawable.yuwei_sapi_error);
		loading_text.setText(notifyStr);
	}
	
	public void showSuccess(String notifyStr){
		loadingImage.clearAnimation();
		loadingImage.setVisibility(View.GONE);
		loading_text.setText(notifyStr);
	}
	
	/**显示视频提取进度
	 * @param progress
	 * 
	 */
	public void showProgress(String progress){
		loading_text.setText(progress +"%");
	}
	
	private void startHideTimer() {
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						hide();
					}
				});
			}
		}, 5000);
	}
	
	public void startInvisibleTimer() {
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						LoadingView.this.setVisibility(View.INVISIBLE);
					}
				});				
			}
		}, 5000);
	}
}
