package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

public class DVRRspVideoState0xE9 {

	private byte result; // 0:正常结束  1:异常结束

	public DVRRspVideoState0xE9(byte[] _data) {
		analyzeData(_data);
	}

	private void analyzeData(byte[] _data) {
		IoBuffer ioBuffer = IoBuffer.allocate(_data.length);
		ioBuffer.setAutoExpand(true);
		ioBuffer.put(_data);
		ioBuffer.flip();
		result = ioBuffer.get(15);
		
		ioBuffer.free();
		ioBuffer = null;
	}
	public byte getResult() {
		return result;
	}

	public void setResult(byte result) {
		this.result = result;
	}

}
