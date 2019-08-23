package com.yuwei.face.socket.Message;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaInfo implements Parcelable,Cloneable {
	
	private long media;//MediaID
	private long startTime;//开始时间
	private long endTime;//介绍时间
	private int videoChannel;//视频通道
	private int voiceChannel;//音频通道
	private byte type;//媒体类型
	
	public long getMedia() {
		return media;
	}
	public void setMedia(long media) {
		this.media = media;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public int getVideoChannel() {
		return videoChannel;
	}
	public void setVideoChannel(int videoChannel) {
		this.videoChannel = videoChannel;
	}
	public int getVoiceChannel() {
		return voiceChannel;
	}
	public void setVoiceChannel(int voiceChannel) {
		this.voiceChannel = voiceChannel;
	}
	public byte getType() {
		return type;
	}
	public void setType(byte type) {
		this.type = type;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(media);
		dest.writeLong(startTime);
		dest.writeLong(endTime);
		dest.writeInt(videoChannel);
		dest.writeInt(voiceChannel);
		dest.writeByte(type);
	}
	
	public static final Creator<MediaInfo> CREATOR = new Creator<MediaInfo>() {

		@Override
		public MediaInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			MediaInfo mediaInfo = new MediaInfo();
			mediaInfo.setMedia(source.readLong());
			mediaInfo.setStartTime(source.readLong());
			mediaInfo.setEndTime(source.readLong());
			mediaInfo.setVideoChannel(source.readInt());
			mediaInfo.setVoiceChannel(source.readInt());
			mediaInfo.setType(source.readByte());
			
			return mediaInfo;
		}

		@Override
		public MediaInfo[] newArray(int size) {
			return new MediaInfo[size];
		}
	};

	@Override
	public String toString() {
		return "ID_" + media + "_通道" + videoChannel;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
}