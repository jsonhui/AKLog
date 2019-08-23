package com.yuwei.face.socket.Message;

import com.yuwei.face.socket.utils.Util;

import org.apache.mina.core.buffer.IoBuffer;


/**
 * 检查DVR状态回应
 * 
 * @author Administrator
 * 
 */
public class DVRToCheckState0xE8 {

	private byte mStatus;
	private byte capacity;
	public byte getCapacity() {
		return capacity;
	}

	public void setCapacity(byte capacity) {
		this.capacity = capacity;
	}

	private int videoCapacity;
	public DVRToCheckState0xE8(byte[] _data) {
		analyzeData(_data);
	}

	private void analyzeData(byte[] _data) {
		IoBuffer ioBuffer = IoBuffer.allocate(_data.length);
		ioBuffer.setAutoExpand(true);
		ioBuffer.put(_data);
		ioBuffer.flip();
		mStatus = ioBuffer.get(15);
		if(_data.length == 21){
			capacity = ioBuffer.get(16);
			byte[] tmp = new byte[4];
			tmp[0] = ioBuffer.get(20);
			tmp[1] = ioBuffer.get(19);
			tmp[2] = ioBuffer.get(18);
			tmp[3] = ioBuffer.get(17);
			videoCapacity = Util.byteToInt2(tmp);
		}
		ioBuffer.free();
		ioBuffer = null;
	}

	public byte getResult() {
		return mStatus;
	}

	public void setResult(byte result) {
		this.mStatus = result;
	}
	
	public int getVideoCapacity() {
		return videoCapacity;
	}

	public void setVideoCapacity(int videoCapacity) {
		this.videoCapacity = videoCapacity;
	}
}
