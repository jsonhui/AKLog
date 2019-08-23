package com.yuwei.face.socket.Message;

import com.yuwei.face.callback.VideoPlayCallBack;
import com.yuwei.face.socket.utils.Util;

import org.apache.mina.core.buffer.IoBuffer;

import java.util.ArrayList;
import java.util.List;

public class DVRToHistoryRecorder0xE2 {
	public static final String TAG = "DVRToHistoryRecorder0xE2";
	private int num;//?????
	private int seesion;//????
	private byte videoNum;//????????
	public static List<MediaInfo> medialist = new ArrayList<MediaInfo>();
	private VideoPlayCallBack mVideoPlayCallBack;
	public void doReceive(byte[] _data)
	{
		analyzeData(_data);
	}
	
	public DVRToHistoryRecorder0xE2(VideoPlayCallBack videoPlayCallBack) {
		this.mVideoPlayCallBack = videoPlayCallBack;
	}

	private void analyzeData(byte[] _data) {
		IoBuffer ioBuffer = IoBuffer.allocate(_data.length);
		ioBuffer.setAutoExpand(true);
		ioBuffer.put(_data);
		ioBuffer.flip();
		num = (int)(ioBuffer.get(15)&0xff);
		seesion = (int)(ioBuffer.get(16)&0xff);
		videoNum = ioBuffer.get(17);
		if(seesion == 0){
			medialist.clear();
		}
		if(seesion < num){
			for(int i = 0;i<videoNum;i++){
				MediaInfo mediaInfo = new MediaInfo();			
				short videoChannel = 0;//????
				short voiceChannel = 0;//????
				byte type = 0;//????
	
				mediaInfo.setMedia(ioBuffer.getUnsignedInt(18 + i*17));
				
				mediaInfo.setStartTime(Util.convertToInt(ioBuffer.getUnsignedInt(22 + i*17)));
				mediaInfo.setEndTime(Util.convertToInt(ioBuffer.getUnsignedInt(26 + i*17)));
				
				short tmp = Util.convertShort(ioBuffer.getShort(30 + i*17));
				if((tmp & 1) == 1){
					videoChannel = 1;
				}else if((tmp & 2) == 2){
					videoChannel = 2;
				}else if((tmp & 4) == 4){
					videoChannel = 3;
				}else if((tmp & 8) == 8){
					videoChannel = 4;
				}else if((tmp & 16) == 16){
					videoChannel = 5;
				}else if((tmp & 32) == 32){
					videoChannel = 6;
				}else if((tmp & 64) == 64){
					videoChannel = 7;
				}else if((tmp & 128) == 128){
					videoChannel = 8;
				}
				tmp = Util.convertShort(ioBuffer.getShort(32 + i*17));
				if((tmp & 1) == 1){
					voiceChannel = 1;
				}else if((tmp & 2) == 2){
					voiceChannel = 2;
				}else if((tmp & 4) == 4){
					voiceChannel = 3;
				}else if((tmp & 8) == 8){
					voiceChannel = 4;
				}else if((tmp & 16) == 16){
					voiceChannel = 5;
				}else if((tmp & 32) == 32){
					voiceChannel = 6;
				}else if((tmp & 64) == 64){
					voiceChannel = 7;
				}else if((tmp & 128) == 128){
					voiceChannel = 8;
				}
				
				type = ioBuffer.get(34 + i*17);
				
				mediaInfo.setVideoChannel(videoChannel);
				mediaInfo.setVoiceChannel(voiceChannel);
				mediaInfo.setType(type);
				medialist.add(mediaInfo);
			}
		}
		//Log.d(TAG,"====>>>decode history record, now mediaInfoListSize:" + medialist.size());
		if((seesion + 1) == num)
		{
			// 历史视频数据接收完毕
			if (mVideoPlayCallBack != null) {
				try {
					//Log.d(TAG,"====>>>decode history record, send to app, mediaInfoListSize:" + medialist.size());
					mVideoPlayCallBack.onReceiveHistoryVideoRecords(medialist);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		ioBuffer.free();
		ioBuffer = null;
	}

	public List<MediaInfo> getMedialist() {
		return medialist;
	}

	public void setMedialist(List<MediaInfo> medialist) {
		this.medialist = medialist;
	}
}
