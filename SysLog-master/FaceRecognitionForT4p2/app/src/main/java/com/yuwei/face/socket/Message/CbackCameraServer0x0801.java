package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

public class CbackCameraServer0x0801 extends BaseCommand{
	private static final long serialVersionUID = -8612109235341284877L;
	private boolean connected;
	
	public CbackCameraServer0x0801(byte[] data){
		analyzeData(data);
	}

	private void analyzeData(byte[] _data) {
		IoBuffer ioBuffer = IoBuffer.allocate(_data.length);
		ioBuffer.setAutoExpand(true);
		ioBuffer.put(_data);
		ioBuffer.flip();
		byte result = ioBuffer.get(0);
		connected = (result == 1);
		ioBuffer.free();
		ioBuffer = null;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	@Override
	public byte[] doDataEncode() {
		// TODO Auto-generated method stub
		return null;
	}
}
