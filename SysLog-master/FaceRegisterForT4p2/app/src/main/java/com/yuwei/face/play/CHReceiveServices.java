package com.yuwei.face.play;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.yuwei.face.callback.CameraStatusCallBack;
import com.yuwei.face.callback.ReceiveYuvFrameCallBack;
import com.yuwei.face.callback.VideoConnStatusCallBack;
import com.yuwei.face.callback.VideoPlayCallBack;
import com.yuwei.face.play.CHReceiveServices;
import com.yuwei.face.socket.GossClient;
import com.yuwei.face.socket.Message.GossPrefix;
import com.yuwei.face.socket.Message.MediaData;
import com.yuwei.face.socket.Message.MediaInfo;
import com.yuwei.face.socket.Message.Rtpdatas;

import java.util.ArrayList;
import java.util.List;

public class CHReceiveServices implements VideoConnStatusCallBack,CameraStatusCallBack,VideoPlayCallBack {

	public static final int VIDEO_CHANNEL_NUM_THREE = 1;

	public static final String TAG = "CHReceiveServices";
	public static final int MSG_CONNECT_INIT = 100;
	public static final int MSG_CONNECT_TIMEOUT = 200;
    private boolean isgorun = false; //继续运行
    private Thread checkThread = null;
    private List<MediaData> ChQueue = null;
    public long totalRecv = 0;
	public long startTime;
	MediaData curMediaData;
	private static CHReceiveServices mChReceiveServices;
	private H264Player mH264Player;
	private H264FrameDecoder framePersonPlayer; //used for play
	private GossClient mGossClient;
	private Surface mSurface;
	private int cameraStaus;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CONNECT_INIT:
				new Thread() {
					@Override
					public void run() {
						if (mGossClient == null){
	                    	 mGossClient = new GossClient();
	                    	 mGossClient.setmVideoConnStatusCallBack(CHReceiveServices.this);
	                    	 mGossClient.startConnectAndLogin();
	                     } else{
	                    	 mGossClient.stopRealTimeVideo();
	                         mGossClient.close();
	                         mGossClient = new GossClient();
	                    	 mGossClient.setmVideoConnStatusCallBack(CHReceiveServices.this);
	                    	 mGossClient.startConnectAndLogin();
	                     }
						sendEmptyMessageDelayed(MSG_CONNECT_TIMEOUT, 5000);
						super.run();
					}
				}.start();
				break;
			case MSG_CONNECT_TIMEOUT:
				if (mGossClient != null){
					mGossClient.stopRealTimeVideo();
					mGossClient.close();
				}
				removeMessages(MSG_CONNECT_TIMEOUT);
                sendEmptyMessageDelayed(MSG_CONNECT_INIT, 3000);
				break;
			}
		}
	};

	public static CHReceiveServices getInstance(){
		if(mChReceiveServices == null){
			synchronized (CHReceiveServices.class){
				if(mChReceiveServices == null){
					mChReceiveServices = new CHReceiveServices();
				}
			}
		}
		return mChReceiveServices;
	}

	public H264FrameDecoder getFramePersonPlayer() {
		return framePersonPlayer;
	}

	private CHReceiveServices(){
		ChQueue = new ArrayList<MediaData>();

		mH264Player = new H264Player();
		framePersonPlayer = new H264FrameDecoder();
		new Thread() {
			@Override
			public void run() {
				sendEmptyMessageDelayed(MSG_CONNECT_TIMEOUT, 5000);
				if (mGossClient == null){
					mGossClient = new GossClient();
					mGossClient.setmVideoConnStatusCallBack(CHReceiveServices.this);
					mGossClient.setmCameraStatusCallBack(CHReceiveServices.this);
					mGossClient.startConnectAndLogin();
				} else{
					mGossClient.stopRealTimeVideo();
                    mGossClient.close();
                    mGossClient = new GossClient();
               	 	mGossClient.setmVideoConnStatusCallBack(CHReceiveServices.this);
               	 	mGossClient.startConnectAndLogin();
				}
				super.run();
			}
		}.start();
	}

	/**
	 * 初始化人脸识别摄像头
	 */
	public void initFaceRecoginzeCamera(Surface surface){
		mSurface = surface;
	}

	public static CHReceiveServices getChReceiveServices(){
		return mChReceiveServices;
	}

    public void AddRtpData(MediaData _data)
    {
    	if(checkThread != null){
    		ChQueue.add(_data);
    	}
    }

	public boolean isStart()
	{
		return isgorun;
	}
	
	public void onStart() {
		isgorun = true;
		try
		{
			checkThread = new Thread(checkGossMessageQueue);
			checkThread.setPriority(Thread.MAX_PRIORITY);
			checkThread.start();
		}
		catch(Exception e)
		{
		}
	}

	public void onStop(){
		new Thread() {
			@Override
			public void run() {
				if (mGossClient != null){
                    mGossClient.stopRealTimeVideo();
                }
				super.run();
			}
		}.start();
    }
	
	public void onDestroy(){
		new Thread() {
			@Override
			public void run() {
				if (mGossClient != null){
                    mGossClient.close();
                }
				super.run();
			}
		}.start();
		DoExit();
	}
	
	public void DoExit()
	{
		if (mH264Player != null) {
			mH264Player.release();
			mH264Player = null;
		}
		if (framePersonPlayer != null) {
			framePersonPlayer.release();
			framePersonPlayer = null;
		}
		isgorun = false;
		ClearData();
		checkThread = null;
		mChReceiveServices = null;
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
	}	
	
	/**
	 * add md to curMediaData
	 * @param md
	 */
	private void addData(MediaData md){
		if(curMediaData== null)
			return;
		byte d1[] = curMediaData.getRtpdata();
		byte d2[] = md.getRtpdata(); //需减去前15字节的rtsp head
		byte ndate[] = new byte[d1.length + d2.length - 15];
		System.arraycopy(d1, 0, ndate, 0, d1.length);
		System.arraycopy(d2, 15, ndate, d1.length, d2.length - 15);
		curMediaData.setRtpdata(ndate);
	}
	
	/**
	 * 累积完整包
	 * @param md
	 * @return true：累积完成，false：未完成
	 */
	private boolean addFrame(MediaData md){
		if (md == null) return false;
		if(md.getMediaType() == 2){
			curMediaData = md;
			return true;//如果是音频数据则不需要分包直接处理
		}
		switch(md.getGossPrefix().getFenbao_flag()){
			case GossPrefix.PACKAGE_HEAD:
				curMediaData = md;
				return false;
			case GossPrefix.PACKAGE_MID:
				addData(md);
				return false;
			case GossPrefix.PACKAGE_END:
				addData(md);
				return true;
			case GossPrefix.PACKAGE_SINGLE:
				curMediaData = md;
				return true;
			default:
				//do nothing
		}
		return false;
	}
	
	Runnable checkGossMessageQueue = new Runnable() {
		@SuppressLint("NewApi")
		@Override
		public void run() {
		    Rtpdatas rtpdatas = new Rtpdatas();
			while(isgorun) {
				try {
					int s = ChQueue.size();
					if(s > 0) 
					{
						if(ChQueue.get(0) == null)
						{
							ChQueue.remove(0);
							continue;
						}
						MediaData md = ChQueue.get(0);
						if (addFrame(md)){
							rtpdatas.fromBinaryData(curMediaData.getRtpdata());
							if (rtpdatas.getB47() == 1) {//视频
								if(md.getChNum() == VIDEO_CHANNEL_NUM_THREE)//只处理人脸识别摄像头的视频流数据
								{
									if(mH264Player!=null)
										mH264Player.addData(rtpdatas.getMdata());
									if(framePersonPlayer!=null)
										framePersonPlayer.addData(rtpdatas.getMdata());
								}

							}else if(rtpdatas.getB47() == 2){//音频
								//Log.d(TAG,"---->>audio format:" + rtpdatas.getMformat());
							}
						}
						ChQueue.remove(0);
					}else{
						Thread.sleep(5);
					}
				} catch (Exception e) {
					e.printStackTrace();
					DoExit();
					break;
				}
			}
		}
	};
	
	public void AddMediaPackage(MediaData _data)
	{
		if(!isStart())
		{
			onStart();
		}
		AddRtpData(_data);
	}
	
	public  void ClearData()
	{
    	if(checkThread != null)
    		ChQueue.clear();
	}	
	
	//返回值KB/S
	public double GetChDownSpeed()
	{
		long cur = System.currentTimeMillis();
		return (totalRecv/1024)/((cur - startTime)/1000);
	}
	
	public void AddReceiveNumers(int _rl)
	{
		totalRecv++;
	}
	
	public void ResetReceiveNumers()
	{
		totalRecv = 0;
		startTime = System.currentTimeMillis();
	}

	@Override
	public void onSessionConnect() {
		Log.i(TAG, "onSessionConnect--cameraStaus:"+cameraStaus);
		removeMessages(MSG_CONNECT_TIMEOUT);
		if(mH264Player != null) {
			new Thread() {
				@Override
				public void run() {
					if (mGossClient != null){
//						mGossClient.playRealTimeVideo(VIDEO_CHANNEL_NUM_THREE);
						mGossClient.showAllChannelVideo();
					}

					super.run();
				}
			}.start();
            mH264Player.initDecoder(mSurface);
            if(framePersonPlayer == null)return;
			framePersonPlayer.initDecoder();
		}
	}


	@Override
	public void onSessionDisConnect() {
		Log.i(TAG, "onSessionDisConnect--isgorun:"+isgorun);
		if(isgorun)
			sendEmptyMessageDelayed(MSG_CONNECT_INIT, 3000);
	}

	@Override
	public void onVideoServerStatusChanged(boolean connected) {

	}

	@Override
	public void onV3PowerCallback(int result) {
		cameraStaus = result;
		Log.i(TAG, "onV3PowerCallback--cameraStaus:"+cameraStaus);
	}

	@Override
	public void onPlayRealVideoCallBack(byte result) {

	}

	@Override
	public void onPollingPlayRealVideoCallBack(boolean result) {

	}

	@Override
	public void onClosePollingPlayRealVideoCallBack(boolean result) {

	}

	@Override
	public void onReceiveHistoryVideoRecords(List<MediaInfo> videoList) {

	}

	@Override
	public void onStartPlayHistoryVideo(boolean result) {

	}

	@Override
	public void onPausePlayHistoryVideo(boolean result) {

	}

	@Override
	public void onStopPlayHistoryVideo(boolean result) {

	}

	@Override
	public void onPlayCompleteCallBack(boolean result) {

	}

	@Override
	public void onHistoryVideoSpeed(byte mode, byte speed, boolean result) {

	}

	@Override
	public void onStopRealVideoCallBack(boolean result) {

	}

	/**请求播放实时视频
	 * @param channel
	 */
	public void playRealTimeVideo(int channel) {
		mGossClient.playRealTimeVideo(channel);
	}
	
	private void sendEmptyMessageDelayed(int what, long time) {
		if (mHandler != null) {
			mHandler.sendEmptyMessageDelayed(what, time);
		}
	}
	
	private void removeMessages(int what) {
		if (mHandler != null) {
			mHandler.removeMessages(what);
		}
	}
}