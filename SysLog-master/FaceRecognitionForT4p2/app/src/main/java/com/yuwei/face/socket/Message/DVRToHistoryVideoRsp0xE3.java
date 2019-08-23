package com.yuwei.face.socket.Message;

import com.yuwei.face.socket.utils.Util;

import org.apache.mina.core.buffer.IoBuffer;


public class DVRToHistoryVideoRsp0xE3 {

	private int mediaID;
	private int startTime;
	private int endTime;
	private short videoChannel;
	private short musicChannel;
	private String type;
	private byte result;
	
	public DVRToHistoryVideoRsp0xE3(byte[] _data) {
		analyzeData(_data);
	}

	private void analyzeData(byte[] _data) {
		IoBuffer ioBuffer = IoBuffer.allocate(_data.length);
		ioBuffer.setAutoExpand(true);
		ioBuffer.put(_data);
		ioBuffer.flip();

		setMediaID(ioBuffer.getInt(15));
		setStartTime(Util.convertToInt(ioBuffer.getInt(19)));
		setEndTime(Util.convertToInt(ioBuffer.getInt(23)));
		setVideoChannel(ioBuffer.getShort(27));
		setMusicChannel(ioBuffer.getShort(29));
		
		byte kind = ioBuffer.get(31);
		String[] s = new String[8];
		for (int i = 0; i < 8; i++) {
			int bits = (kind >> i) & 0x01;
			s[i] = bits + "";
		}

		if (s[0].equalsIgnoreCase("0"))
			type = "h264";
		else if (s[1].equalsIgnoreCase("1"))
			type = "jpeg";
		else if (s[4].equalsIgnoreCase("0"))
			type = "AAC";
		else if (s[5].equalsIgnoreCase("1"))
			type = "AMR";
		else if (s[6].equalsIgnoreCase("2"))
			type = "G.726";

		result = ioBuffer.get(32);
		ioBuffer.free();
		ioBuffer = null;
	}

	public String getType() {
		return type;
	}

	public byte getResult() {
		return result;
	}
	public int getMediaID() {
		return mediaID;
	}

	public void setMediaID(int mediaID) {
		this.mediaID = mediaID;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public short getVideoChannel() {
		return videoChannel;
	}

	public void setVideoChannel(short videoChannel) {
		this.videoChannel = videoChannel;
	}

	public short getMusicChannel() {
		return musicChannel;
	}

	public void setMusicChannel(short musicChannel) {
		this.musicChannel = musicChannel;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setResult(byte result) {
		this.result = result;
	}

}
