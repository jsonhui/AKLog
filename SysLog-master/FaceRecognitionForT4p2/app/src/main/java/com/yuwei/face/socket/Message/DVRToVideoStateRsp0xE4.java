package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

public class DVRToVideoStateRsp0xE4 {

	private byte cmd; // 0 开始 1暂停 2停止
	private byte result; // 0成功 1失败

	public DVRToVideoStateRsp0xE4(byte[] _data) {
		analyzeData(_data);
	}

	private void analyzeData(byte[] _data) {
		IoBuffer ioBuffer = IoBuffer.allocate(_data.length);
		ioBuffer.setAutoExpand(true);
		ioBuffer.put(_data);
		ioBuffer.flip();

		cmd = ioBuffer.get(15);
		result = ioBuffer.get(16);
		
		ioBuffer.free();
		ioBuffer = null;
	}

	public byte getCmd() {
		return cmd;
	}

	public void setCmd(byte cmd) {
		this.cmd = cmd;
	}

	public byte getResult() {
		return result;
	}

	public void setResult(byte result) {
		this.result = result;
	}

}
