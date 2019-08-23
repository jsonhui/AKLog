package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

public class DVRToVideoRecorder0xE0 {
	
	private byte cmd;
	private byte isSucceess;
	
	public DVRToVideoRecorder0xE0(byte[] _data) {
		analyzeData(_data);
	}

	private void analyzeData(byte[] _data) {
		IoBuffer ioBuffer = IoBuffer.allocate(_data.length);
		ioBuffer.setAutoExpand(true);
		ioBuffer.put(_data);
		ioBuffer.flip();
		
		cmd = ioBuffer.get(15);
		isSucceess = ioBuffer.get(16);
		
		ioBuffer.free();
		ioBuffer = null;
	}

	public byte getCmd() {
		return cmd;
	}

	public void setCmd(byte cmd) {
		this.cmd = cmd;
	}

	public byte getIsSucceess() {
		return isSucceess;
	}

	public void setIsSucceess(byte isSucceess) {
		this.isSucceess = isSucceess;
	}
}
