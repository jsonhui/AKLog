package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

public class CbackCameraPower0x0802 extends BaseCommand{
	private static final long serialVersionUID = -8612109235341284877L;
	public static final int TYPE_OFF = 0;
	public static final int TYPE_ON = 1;
	
	private int type;
	private boolean success;
	
	public CbackCameraPower0x0802(byte[] data){
		analyzeData(data);
	}

	private void analyzeData(byte[] _data) {
		IoBuffer ioBuffer = IoBuffer.allocate(_data.length);
		ioBuffer.setAutoExpand(true);
		ioBuffer.put(_data);
		ioBuffer.flip();
		byte result = ioBuffer.get(0);
		type = (int)result;
		result = ioBuffer.get(1);
		success = (result == 1);
		ioBuffer.free();
		ioBuffer = null;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	@Override
	public byte[] doDataEncode() {
		// TODO Auto-generated method stub
		return null;
	}
}
