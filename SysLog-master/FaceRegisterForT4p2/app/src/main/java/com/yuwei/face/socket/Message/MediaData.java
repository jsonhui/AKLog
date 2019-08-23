package com.yuwei.face.socket.Message;

/**
 * 媒体数据内容
 *
 */
public class MediaData {
	
	//媒体数据消息头
	private GossPrefix gossPrefix;
	//媒体数据
	private  byte[] rtpdata;
	//媒体类型
	private int mediaType;//1:表示视频；2：表示音频
	private int chNum;// 通道号

	public MediaData(GossPrefix gossPrefix, byte[] rtpdata)
	{
		this.gossPrefix = gossPrefix;
		this.rtpdata = rtpdata;
	}

	public GossPrefix getGossPrefix() {
		return gossPrefix;
	}

	public byte[] getRtpdata() {
		return rtpdata;
	}

	public void setRtpdata(byte[] rtpdata) {
		this.rtpdata = rtpdata;
	}
	
	public int getMediaType() {
		return mediaType;
	}

	public void setMediaType(int mediaType) {
		this.mediaType = mediaType;
	}
	public int getChNum() {
		return chNum;
	}

	public void setChNum(int chNum) {
		this.chNum = chNum;
	}
}
