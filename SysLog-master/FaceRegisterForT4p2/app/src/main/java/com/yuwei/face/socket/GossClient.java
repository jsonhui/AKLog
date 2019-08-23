package com.yuwei.face.socket;

import android.annotation.SuppressLint;
import android.util.Log;

import com.yuwei.face.callback.CameraStatusCallBack;
import com.yuwei.face.callback.GetRspChannelStatusInterface;
import com.yuwei.face.callback.VideoConnStatusCallBack;
import com.yuwei.face.callback.VideoPlayCallBack;
import com.yuwei.face.play.CHReceiveServices;
import com.yuwei.face.socket.Message.CbackCameraPower0x0802;
import com.yuwei.face.socket.Message.CbackCameraServer0x0801;
import com.yuwei.face.socket.Message.CmdCameraPower0x0902;
import com.yuwei.face.socket.Message.CmdCameraServerParam0x0901;
import com.yuwei.face.socket.Message.DVRRspVideoState0xE9;
import com.yuwei.face.socket.Message.DVRToHistoryRecorder0xE2;
import com.yuwei.face.socket.Message.DVRToHistoryVideoRsp0xE3;
import com.yuwei.face.socket.Message.DVRToRoundDisplay0xE6;
import com.yuwei.face.socket.Message.DVRToVideoRsp0xE1;
import com.yuwei.face.socket.Message.DVRToVideoSpeedRsp0xE5;
import com.yuwei.face.socket.Message.DVRToVideoStateRsp0xE4;
import com.yuwei.face.socket.Message.GossCmdConst;
import com.yuwei.face.socket.Message.GossMessage;
import com.yuwei.face.socket.Message.GossPrefix;
import com.yuwei.face.socket.Message.MediaData;
import com.yuwei.face.socket.Message.MediaInfo;
import com.yuwei.face.socket.Message.MediaRpt0x1000Cmd;
import com.yuwei.face.socket.Message.Rtpdatas;
import com.yuwei.face.socket.Message.TemHeartBeatReq0x0001;
import com.yuwei.face.socket.Message.TemRegisterReq0x0003;
import com.yuwei.face.socket.Message.TemRegisterRsp0x8003;
import com.yuwei.face.socket.Message.TemUnregisterReq0x0011;
import com.yuwei.face.socket.Message.TerminalRsp0x8000;
import com.yuwei.face.socket.Message.TerminalRsp0x8001;
import com.yuwei.face.socket.Message.VideoMaCmd;
import com.yuwei.face.socket.utils.SocketConstants;
import com.yuwei.face.socket.utils.Util;
import com.yuwei.face.socket.utils.Utility;

import java.util.Timer;


public class GossClient implements GossListener {
	private String TAG = "GossClient";
	private static final int STATUS_DIS_CONNECT = 0;
	private static final int STATUS_CONNECT = 1;
	private static int gStatus = STATUS_DIS_CONNECT;//当前socket连接状态
	private GossIoHandle gossIoHandle;
	public static int gDeviceId = 0;

	public static boolean isPlayingHis = false;
	private int mCurrentCh;
    private VideoConnStatusCallBack mVideoConnStatusCallBack;
    private CameraStatusCallBack mCameraStatusCallBack;

    private VideoPlayCallBack mVideoPlayCallBack;

	public GossClient(){
		Log.i(TAG,"====>>GossClient created");
	}
	/*
	 * 主通道开启
	 */
	public void startConnectAndLogin() {
		if (gossIoHandle == null) {
			gossIoHandle = new GossIoHandle(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.yuwei.ywipcamera.video.socket.socket.client.GossListener#onMessage(com.yuwei.ywipcamera.video.socket.socket.message.GossMessage)
	 *
	 * 应答类型判断
	 *
	 */
	@Override
	synchronized public int onMessage(GossMessage gossMessage, int _winch) {
//		gStatus = STATUS_CONNECT;//收到消息，说明连接正常
		if(gStatus == STATUS_DIS_CONNECT)
			return _winch;
		dealCmd(gossMessage);
		return 0;
	}

	/*
	 * 应答处理
	 *
	 */
//-------------------------------------- 以下为20150910添加  ----------------------------------------------------------------------
	//终端注册应答
	private void doTemRegistRspCmd(TemRegisterReq0x0003 cmd)
	{
		TemRegisterRsp0x8003 temRegisterRsp0x8003  = GossTool.makeTemRegisterRsp0x8003Cmd(cmd);
		GossPrefix gossPrefix = GossTool.makeGossPrefix(cmd.getGossPrefix(),(short)0x8003);
		GossMessage gossMessage = new GossMessage(gossPrefix, temRegisterRsp0x8003);
		sendMessage(gossMessage);
	}
	//终端心跳应答
	private void doTemHeartBeatRspCmd(TemHeartBeatReq0x0001 cmd)
	{
		TerminalRsp0x8000 temHeartBeatRsp0x8000 = GossTool.makeTerminalRsp0x8000Cmd(cmd);
		GossPrefix gossPrefix = GossTool.makeGossPrefix(cmd.getGossPrefix(),(short)0x8000);
		GossMessage gossMessage = new GossMessage(gossPrefix, temHeartBeatRsp0x8000);
		sendMessage(gossMessage);
	}
	//终端注销应答
	private void doTemUnregisterRspCmd(TemUnregisterReq0x0011 cmd)
	{
		TerminalRsp0x8000 temHeartBeatRsp0x8000 = GossTool.makeTerminalRsp0x8000Cmd(cmd);
		GossPrefix gossPrefix = GossTool.makeGossPrefix(cmd.getGossPrefix(),(short)0x8000);
		GossMessage gossMessage = new GossMessage(gossPrefix, temHeartBeatRsp0x8000);
		sendMessage(gossMessage);
	}

	//设置视频服务器参数
	public void  setCameraServerParam(String ip, short port, int termId, int protoType, int connType, String carNum){
		Log.i(TAG,"====>>setCameraServerParam ip:" + ip + ", port:" + port +
				", termId" + termId + ",protoType:" + protoType + ", connType:" + connType + ", carNum:" + carNum);
		CmdCameraServerParam0x0901 cmd = new CmdCameraServerParam0x0901(ip,port,termId,protoType,connType,carNum);
		GossPrefix gossPrefix = GossTool.makeGossPrefix((short) GossCmdConst.CMD_SET_VIDEO_SERVER_PARAM);
		GossMessage gossMessage = new GossMessage(gossPrefix, cmd);
		sendMessage(gossMessage);
	}

	// 设置Camera电源
	public void setCameraPower(boolean on) {
		//Log.d(TAG, "====>>setCameraPower on:" + on);
		CmdCameraPower0x0902 cmd = new CmdCameraPower0x0902(on);
		GossPrefix gossPrefix = GossTool.makeGossPrefix((short) GossCmdConst.CMD_SET_CAMERA_POWER);
		GossMessage gossMessage = new GossMessage(gossPrefix, cmd);
		sendMessage(gossMessage);
	}

	private Timer subscribeReq0x0007_Timer;
	private void Stop_subscribeReq0x0007_Timer()
	{
		if(subscribeReq0x0007_Timer != null)
		{
			subscribeReq0x0007_Timer.cancel();
			subscribeReq0x0007_Timer = null;
		}
	}

	//启动实时媒体请求后应答处理
	private void doTerminalRsp0x8001Cmd(TerminalRsp0x8001 cmd)
	{
		Stop_subscribeReq0x0007_Timer();//停止实时媒体请求定时器
		if(cmd.getResult() == 0)
		{
			//请求成功
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	MediaRpt0x1000Cmd mediaDataCmd;
	Rtpdatas mRtpdatas = new Rtpdatas();

	int prevFrameNum = 0;
	// 媒体数据包上报
	@SuppressLint("NewApi")
	synchronized private int doMediaRptCmd(MediaRpt0x1000Cmd cmd) {

		this.mediaDataCmd = cmd;
		byte[] rtpdata = cmd.getRtpData();
		mRtpdatas.fromBinaryData(rtpdata);
		//int ch = mRtpdatas.getB03() - 1;
		int ch = mRtpdatas.getB03();

		MediaData mediaData = new MediaData(cmd.getGossPrefix(), rtpdata);
		mediaData.setMediaType(mRtpdatas.getB47());
		mediaData.setChNum(ch);
//		byte packageOrder = mediaData.getGossPrefix().getFenbao_flag();
//		Log.i(TAG, "the package order is :::::" + packageOrder);
		switch(mRtpdatas.getB47())
		{
		case 1://视频
			//T4连接V3的通道号为3、4,为使用适应程序，修改一下
//			Log.d(TAG,"---->> receive video frame, add data, channel:" + ch );
			try {
				if(CHReceiveServices.getInstance() != null && gStatus == STATUS_CONNECT)
					CHReceiveServices.getInstance().AddMediaPackage(mediaData);
			} catch(Exception e) {e.printStackTrace();}
			break;

		case 2: //音频
			//if (Constants.DEBUG)Log.d(TAG,"====---->> receive audio frame, channel:" + ch + " format:" + mRtpdatas.getMformat());
			try {
				if(CHReceiveServices.getInstance() != null && gStatus == STATUS_CONNECT)
					CHReceiveServices.getInstance().AddMediaPackage(mediaData);
			} catch(Exception e) {e.printStackTrace();}
			break;
		case 3:
		case 4:
		case 5:
		default:
			Log.i("doMediaRptCmd", "数据不支持,关闭SOCKET chanel " + ch + "--" + ch + "---" + mRtpdatas.getB47() + " headflag=" + mRtpdatas.getHeadFlag());
			//数据不支持,关闭SOCKET
			return -1;
		}

		return 0;
	}

	/*
	 * 发送消息
	 *
	 * @function
	 *
	 * @ sendMessage			主通道 	  	发送
	 * @ sendMessageMedia_0		媒体通道0 	发送
	 * @ sendMessageMedia_1		媒体通道1 	发送
	 * @ sendMessageMedia_2		媒体通道2 	发送
	 * @ sendMessageMedia_3		媒体通道3 	发送
	 *
	 */
	public int sendMessage(GossMessage message) {
		if(gossIoHandle != null)
		{
			return gossIoHandle.sendMessage(message);
		}
		else
			return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.yuwei.ywipcamera.video.socket.socket.client.GossListener#onException(java.lang.Throwable)
	 *
	 */
	@Override
	public void onException(Throwable throwable) {
		Log.d(TAG, "exception socket disconnected");
//		Log.i(TAG, throwable.getMessage());
		if (!gossIoHandle.isConnected()) {
			gStatus = STATUS_DIS_CONNECT;
		}
        if(mVideoConnStatusCallBack != null)
            mVideoConnStatusCallBack.onSessionDisConnect();
	}

	/*
	 * (non-Javadoc)
	 * @see com.yuwei.ywipcamera.video.socket.socket.client.GossListener#sessionOpened()
	 *
	 * TCP连接session建立时执行
	 *
	 */
	@Override
	public void sessionOpened() {
		Log.i(TAG, "====>>socket connected");
		gStatus = STATUS_CONNECT;
        if(mVideoConnStatusCallBack != null)
            mVideoConnStatusCallBack.onSessionConnect();
	}

	/*
	 * (non-Javadoc)
	 * @see com.yuwei.ywipcamera.video.socket.socket.client.GossListener#sessionClosed()
	 *
	 * TCP连接session关闭时执行
	 *
	 */
	@Override
	public void sessionClosed(int _ch) {
		Log.i(TAG, "====>>socket disconnected");
		if(_ch == -1)
		{
			gStatus = STATUS_DIS_CONNECT;
			if(mVideoConnStatusCallBack != null)
                mVideoConnStatusCallBack.onSessionDisConnect();
		}
	}

	/*
	 * 关闭TCP连接, 包含注销请求
	 */
	public void close() {
		Log.i(TAG,"====>>gossclient close");
		if (gossIoHandle != null) {
            gStatus = STATUS_DIS_CONNECT;
            gossIoHandle.disconnect();
            Log.i(TAG, "close center socket!!!!");
        }
	}

	public int getStatus() {
		return gStatus;
	}

	public boolean isConnected(){

		return gStatus == STATUS_CONNECT;
	}

	public void dealCmd(GossMessage gossMessage) {
		int msgid = gossMessage.getGossPrefix().getMessage_id() & 0xFFFF;//只取低16位
		gDeviceId = gossMessage.getGossPrefix().getDevice_id();
//		Log.i(TAG,"---->>deal message id=" + msgid);
		switch (msgid) {
		// 媒体数据包上报
		case GossCmdConst.CMD_NUM_MEDIA_RPT:
//			Log.i(TAG, "接收到音视频数据" + String.format("%04x", msgid));
			MediaRpt0x1000Cmd cmd_0x1000 = (MediaRpt0x1000Cmd) gossMessage.getBaseCommand();
			cmd_0x1000.setGossPrefix(gossMessage.getGossPrefix());
			doMediaRptCmd(cmd_0x1000);
			break;

		//终端注册应答
		case GossCmdConst.CMD_TEM_REGISTER_REQ:
//			Log.i(TAG, "接收到注册应答" + String.format("%04x", msgid));
			TemRegisterReq0x0003 cmd_0x0003 = (TemRegisterReq0x0003)gossMessage.getBaseCommand();
			cmd_0x0003.setGossPrefix(gossMessage.getGossPrefix());
			doTemRegistRspCmd(cmd_0x0003);
			break;
		//终端心跳应答
		case GossCmdConst.CMD_TEM_HERTBEAT_REQ:
//			Log.d(TAG, "接收到心跳应答" + String.format("%04x", msgid));
			TemHeartBeatReq0x0001 cmd_0x0001 = (TemHeartBeatReq0x0001) gossMessage.getBaseCommand();
			cmd_0x0001.setGossPrefix(gossMessage.getGossPrefix());
			doTemHeartBeatRspCmd(cmd_0x0001);
			break;
		//终端注销应答
		case GossCmdConst.CMD_TEM_UNREGISTER_REQ:
			Log.i(TAG, "接收到注销应答" + String.format("%04x", msgid));
			TemUnregisterReq0x0011 cmd_0x0011 =  (TemUnregisterReq0x0011) gossMessage.getBaseCommand();
			cmd_0x0011.setGossPrefix(gossMessage.getGossPrefix());
			doTemUnregisterRspCmd(cmd_0x0011);
			break;
		case GossCmdConst.CMD_COMMON_RSP://通用应答
			Log.i(TAG, "接收到注销应答" + String.format("%04x", msgid));
			TerminalRsp0x8000  cmd_0x8000 =  (TerminalRsp0x8000) gossMessage.getBaseCommand();
			cmd_0x8000.setGossPrefix(gossMessage.getGossPrefix());
			break;
		case GossCmdConst.CMD_COMMON_MEDIA_RSP://媒体通用应答
			Log.i(TAG, "接收到媒体通用应答" + String.format("%04x", msgid));
			TerminalRsp0x8001 cmd_0x8001 = (TerminalRsp0x8001) gossMessage.getBaseCommand();
			cmd_0x8001.setGossPrefix(gossMessage.getGossPrefix());
			doTerminalRsp0x8001Cmd(cmd_0x8001);
			break;
		case GossCmdConst.CMD_VIDEO_RECEIVE_DATA://接收video回调数据，老MA的数据转换来的
			//Log.d(TAG,"---->>receive from va video callback data");
			if (mVideoPlayCallBack != null){
				VideoMaCmd cmd = (VideoMaCmd) gossMessage.getBaseCommand();
				decodeMaCallback(cmd);
			}
			break;
		case GossCmdConst.CMD_VIDEO_SERVER_CALLBACK:
			if (mCameraStatusCallBack != null){
				CbackCameraServer0x0801 cmd = (CbackCameraServer0x0801) gossMessage.getBaseCommand();
                mCameraStatusCallBack.onVideoServerStatusChanged(cmd.isConnected());
			}
			break;
		case GossCmdConst.CMD_SET_CAMERA_POWER_CALLBACK:
			Log.i(TAG,"---->>receive from va camera power callback");
			if (mCameraStatusCallBack != null){
				CbackCameraPower0x0802 cmd = (CbackCameraPower0x0802) gossMessage.getBaseCommand();
				//返回1：上电成功，2：下电成功， -1：上电失败，-2：下电失败
				if (cmd.getType() == CbackCameraPower0x0802.TYPE_ON){
                    mCameraStatusCallBack.onV3PowerCallback(cmd.isSuccess() ? 1 : -1);
				}else{
                    mCameraStatusCallBack.onV3PowerCallback(cmd.isSuccess() ? 2 : -2);
				}
			}
			break;
		default:

			break;
		}
	}

	private void decodeMaCallback(VideoMaCmd cmd){
		byte maCmd = cmd.getMaCmd();
		Utility.printf_bytes(cmd.getOriginBytes(), "GossProtocolEncoder");
		switch(maCmd){
		case SocketConstants.CALLBACK_REAL_TIME_VIDEO:
			isPlayingHis = false;
		    DVRToVideoRsp0xE1 dvrToVideoRsp0xE1 = new DVRToVideoRsp0xE1(cmd.getOriginBytes());
	   		int status = get_Rsp_Channel_Status(mCurrentCh, dvrToVideoRsp0xE1);
            try {
                //Log.d(TAG,"7====>>receive from va and send PlayRealVideo reasult to binder client, status:" + status);
                if(mVideoPlayCallBack != null)
                    mVideoPlayCallBack.onPlayRealVideoCallBack((byte)status);
            } catch (Exception e) {
                e.printStackTrace();
            }
			break;
		case SocketConstants.CALLBACK_POLLING_PLAY:
			DVRToRoundDisplay0xE6 dvrToRoundDisplay0xE6 = new DVRToRoundDisplay0xE6(cmd.getOriginBytes());
			byte pollingResult = dvrToRoundDisplay0xE6.getIsSucceess();
			if (dvrToRoundDisplay0xE6.getCmd() == 0) {
				// 开启轮循显示
                try {
                    if(mVideoPlayCallBack != null)
                        mVideoPlayCallBack.onPollingPlayRealVideoCallBack(pollingResult == 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
			} else if (dvrToRoundDisplay0xE6.getCmd() == 1) {
				// 关闭轮循显示
                try {
                    if(mVideoPlayCallBack != null)
                        mVideoPlayCallBack.onClosePollingPlayRealVideoCallBack(pollingResult == 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
			}
			break;
		case SocketConstants.CALLBACK_QUERY_HISTORY:
			//Log.d(TAG,"====>>> receive query history callback");
			if (mVideoPlayCallBack != null){
				DVRToHistoryRecorder0xE2 dvrToHistoryRecorder0xE2 = new DVRToHistoryRecorder0xE2(mVideoPlayCallBack);
			   	dvrToHistoryRecorder0xE2.doReceive(cmd.getOriginBytes());
			}
			break;
		case SocketConstants.CALLBACK_PLAY_HISTORY:
			DVRToHistoryVideoRsp0xE3 dvr0xE3 = new DVRToHistoryVideoRsp0xE3(cmd.getOriginBytes());
			if (mVideoPlayCallBack != null){
				try {
                    mVideoPlayCallBack.onStartPlayHistoryVideo(dvr0xE3.getResult() == 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		case SocketConstants.CALLBACK_CONTROL_HISTORY:
			Log.i("GossProtocolEncoder", "----------receive video ctrl msd-------------");
			DVRToVideoStateRsp0xE4 dvrToVideoStateRsp0xE4 = new DVRToVideoStateRsp0xE4(cmd.getOriginBytes());
			byte ctlResult = dvrToVideoStateRsp0xE4.getResult();
			byte cmdType = dvrToVideoStateRsp0xE4.getCmd();
			switch (cmdType) {
			case SocketConstants.TYPE_HISTORY_START:
				// 开始
				if (mVideoPlayCallBack != null){
					try {
                        mVideoPlayCallBack.onStartPlayHistoryVideo(ctlResult == 0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case SocketConstants.TYPE_HISTORY_PAUSE:
				// 暂停
				if (mVideoPlayCallBack != null){
					try {
                        mVideoPlayCallBack.onPausePlayHistoryVideo(ctlResult == 0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case SocketConstants.TYPE_HISTORY_STOP:
				// 停止
				if (mVideoPlayCallBack != null){
					try {
                        mVideoPlayCallBack.onStopPlayHistoryVideo(ctlResult == 0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			default:
			}
			break;
		case SocketConstants.CALLBACK_HISTORY_FINISH:
			DVRRspVideoState0xE9 dvrRspVideoState0xE9 = new DVRRspVideoState0xE9(cmd.getOriginBytes());
			byte playResult = dvrRspVideoState0xE9.getResult();
			if (mVideoPlayCallBack != null){
				try {
                    mVideoPlayCallBack.onPlayCompleteCallBack(playResult == 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		case SocketConstants.CALLBACK_HISTORY_SPEED:
			DVRToVideoSpeedRsp0xE5 speedRsp0xE5 = new DVRToVideoSpeedRsp0xE5(cmd.getOriginBytes());
			if (mVideoPlayCallBack != null){
				try {
                    mVideoPlayCallBack.onHistoryVideoSpeed(speedRsp0xE5.getAheadOrBack(),
							speedRsp0xE5.getKind(), speedRsp0xE5.getResult()== 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		case SocketConstants.CALLBACK_REAL_STOP:
			//Log.d(TAG,"====>>stop real play");
			byte data[] = cmd.getmData();
			if (data.length >= 2 && mVideoPlayCallBack != null){
				try {
                    mVideoPlayCallBack.onStopRealVideoCallBack(data[1] == 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		default:
		}
	}

	/**获取DVR回应的通道的状态
	 * @param channelNum
	 * @param channelStatusInterface
	 * @return
	 */
	private int get_Rsp_Channel_Status(int channelNum,GetRspChannelStatusInterface channelStatusInterface){
		int status = -1;
		switch (channelNum) {
			case 1:
				status = channelStatusInterface.getChannel_1();
				break;
			case 2:
				status = channelStatusInterface.getChannel_2();
				break;
			case 3:
				status = channelStatusInterface.getChannel_3();
				break;
			case 4:
				status = channelStatusInterface.getChannel_4();
				break;
			case 5:
				status = channelStatusInterface.getChannel_5();
				break;
			case 6:
				status = channelStatusInterface.getChannel_6();
				break;
			case 7:
				status = channelStatusInterface.getChannel_7();
				break;
			case 8:
				status = channelStatusInterface.getChannel_8();
				break;
			default:
				status = channelStatusInterface.getChannel_3();
		}
		return status;
	}

	/**
	 * 发送老Ma命令至Va
	 * @param cmdname
	 * @param data
	 */
	private void sendDvrCmd(byte cmdname, byte[] data){
		if (gStatus != STATUS_CONNECT){
			Log.d("His","====>>socket not connected" + gStatus);
			return;
		}
		GossPrefix gossPrefix = GossTool.makeGossPrefix((short)GossCmdConst.CMD_VIDEO_SEND_CMD);
		VideoMaCmd cmd = new VideoMaCmd(cmdname, data);
		GossMessage gossMessage = new GossMessage(gossPrefix, cmd);
		sendMessage(gossMessage);
	}

	/**
	 * 播放指定通道实时视频
	 * @param channel
	 */
	public void playRealTimeVideo(int channel) {
		mCurrentCh = channel;
		byte [] chanel = GossTool.getChannelNum(channel);
		//Log.d(TAG,"6====>>send to va playRealTimeVideo channel:" + channel);
		sendDvrCmd(SocketConstants.REAL_TIME_VEDIO_CMD, chanel);
	}

	/**
	 * 显示全部通道实时视频
	 */
	public void showAllChannelVideo(){
		byte[] data = {(byte) 0xff,(byte) 0xff};
		mCurrentCh = 0;
		sendDvrCmd(SocketConstants.REAL_TIME_VEDIO_CMD,data);
	}

	/**
	 * 停止播放实时视频
	 */
	public void stopRealTimeVideo() {
		byte[] chanel = {(byte) 0xff};
		sendDvrCmd(SocketConstants.STOP_REAL_TIME_VEDIO, chanel);
	}

	/**
	 * 轮循播放实时视频
	 */
	public void pollingPlayRealVideo() {
		byte[] data ={0x00};
		sendDvrCmd(SocketConstants.POLLING_DISPLAY_VIDEO, data);
	}

	/**
	 * 关闭轮循播放
	 */
	public void closePollingPlayRealVideo() {
		byte[] data = {0x01};
		sendDvrCmd(SocketConstants.POLLING_DISPLAY_VIDEO, data);
	}

	/**
	 * 查询历史视频
	 * @param startTime
	 * @param endTime
	 */
	public void queryHistoryVideo(String startTime, String endTime, int channel, String eventStr) {
		if(startTime == null || endTime == null)
			throw new NullPointerException("开始时间或结束时间不能为空");

		byte[] data = new byte[24];
		byte [] startTimeArray = Util.timeToByte(startTime);
		System.arraycopy(startTimeArray, 0, data, 0, 4);

		byte [] endTimeArray = Util.timeToByte(endTime);
		System.arraycopy(endTimeArray, 0, data, 4, 4);

		byte[] media = new byte[4];
		System.arraycopy(media,0,data,8, media.length);

		if (channel <= 0){
			data[12] = (byte) 0xff;               //视频通道
			data[13] = (byte)0xff;
		}else{
			byte [] chans = GossTool.getChannelNum(channel);
			data[12] = chans[0];               //视频通道
			data[13] = chans[1];
		}

		data[14] = 0x00;                  //音频通道
		data[15] = 0x00;
		//byte[] mask = {(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff};        //事件掩码
		byte[] mask = getEventData(eventStr);        //事件掩码
		System.arraycopy(mask,0,data,16, mask.length);
		byte[] businessmask = new byte[4];      //业务掩码
		System.arraycopy(businessmask,0,data,20, businessmask.length);

		Log.d("His","====>>>send to va query history record");
		sendDvrCmd(SocketConstants.QUERY_HISTORY_RECORDER, data);
	}

	private byte[] getEventData(String eventStr) {
        byte[] data = new byte[4];
		if(eventStr == null){
			data[0] = 0x00;
            data[1] = 0x00;
            data[2] = 0x00;
            data[3] = 0x00;
		}else if (eventStr.equals("开门事件")) {
            data[0] = 0x01;
            data[1] = 0x00;
            data[2] = 0x00;
            data[3] = 0x00;
        } else if (eventStr.equals("关门事件")) {
            data[0] = 0x02;
            data[1] = 0x00;
            data[2] = 0x00;
            data[3] = 0x00;
        } else if (eventStr.equals("超速事件")) {
            data[0] = 0x04;
            data[1] = 0x00;
            data[2] = 0x00;
            data[3] = 0x00;
        } else if (eventStr.equals("重车")) {
            data[0] = 0x08;
            data[1] = 0x00;
            data[2] = 0x00;
            data[3] = 0x00;
        } else if (eventStr.equals("空车")) {
            data[0] = 0x10;
            data[1] = 0x00;
            data[2] = 0x00;
            data[3] = 0x00;
        } else if (eventStr.equals("紧急警报")) {
            data[0] = 0x20;
            data[1] = 0x00;
            data[2] = 0x00;
            data[3] = 0x00;
        } else {
            data[0] = (byte) 0xff;
            data[1] = (byte) 0xff;
            data[1] = (byte) 0xff;
            data[1] = (byte) 0xff;
        }
        return data;
    }

	/**
	 * 播放指定的历史视频
	 * @param mediaInfo
	 */
	public void playHistoryVideo(MediaInfo mediaInfo) {
		byte[] data = new byte[17];
		System.arraycopy(Util.int2Byte(mediaInfo.getMedia()), 0, data, 0, 4);
		System.arraycopy(Util.convertData(mediaInfo.getStartTime()),0,data,4, 4);
		System.arraycopy(Util.convertData(mediaInfo.getEndTime()),0, data,8, 4);

		byte [] chanel = GossTool.getChannelNum(mediaInfo.getVideoChannel());
		//byte[] ch = {(byte) 0xff,(byte) 0xff};
		System.arraycopy(chanel,0,data,12, 2); //视频通道
		byte [] audioChannel = GossTool.getChannelNum(mediaInfo.getVoiceChannel());
		System.arraycopy(chanel,0,data,14, 2); //音频通道
		//System.arraycopy(Util.shortToByte((short)mediaInfo.getVoiceChannel()),0,data,14,2);
		data[16] = mediaInfo.getType();
//		Log.d(TAG,"====>>>playHistoryVideo send data:" + Utility.getHexBytes(data));

		sendDvrCmd(SocketConstants.PLAY_HISTORY_VEDIO,data);
	}

	/**
	 * 开始播放历史视频
	 */
	public void startPlayHistoryVideo() {
		//Log.d(TAG,"====>>>Play HistoryVideo");
		byte[]data = {0};
		sendDvrCmd(SocketConstants.VIDEO_STATE,data);
	}

	/**
	 * 暂停历史视频播放
	 */
	public void pausePlayHistoryVideo() {
		//Log.d(TAG,"====>>>pause HistoryVideo");
		byte[]data = {1};
		sendDvrCmd(SocketConstants.VIDEO_STATE,data);
	}

	/**
	 * 停止历史视频播放
	 */
	public void stopPlayHistoryVideo() {
		//Log.d(TAG,"====>>>stop HistoryVideo");
		byte[]data = {2};
		sendDvrCmd(SocketConstants.VIDEO_STATE,data);
	}

	/**
	 * 设置历史视频播放速度
	 * @param mode 0：前进，1：后退
	 * @param speed	0：0.25倍速播放，1：0.5倍速播放，2：1倍速播放，3：2倍速播放，4: 4倍速播放
	 *
	 */
	public void setHistoryPlaySpeed(byte mode, byte speed){
		byte[]data = {mode,speed};
		sendDvrCmd(SocketConstants.SINOPEC_VIDEO_SPEED,data);
	}

    public VideoConnStatusCallBack getmVideoConnStatusCallBack() {
        return mVideoConnStatusCallBack;
    }

    public void setmVideoConnStatusCallBack(VideoConnStatusCallBack mVideoConnStatusCallBack) {
        this.mVideoConnStatusCallBack = mVideoConnStatusCallBack;
    }
    public CameraStatusCallBack getmCameraStatusCallBack() {
        return mCameraStatusCallBack;
    }

    public void setmCameraStatusCallBack(CameraStatusCallBack mCameraStatusCallBack) {
        this.mCameraStatusCallBack = mCameraStatusCallBack;
    }
    public VideoPlayCallBack getmVideoPlayCallBack() {
        return mVideoPlayCallBack;
    }

    public void setmVideoPlayCallBack(VideoPlayCallBack mVideoPlayCallBack) {
        this.mVideoPlayCallBack = mVideoPlayCallBack;
    }
}
