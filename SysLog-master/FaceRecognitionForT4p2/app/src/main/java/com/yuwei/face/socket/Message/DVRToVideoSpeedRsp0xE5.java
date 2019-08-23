package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

public class DVRToVideoSpeedRsp0xE5 {
	private byte aheadOrBack;//快进或快退
	private byte kind;      //命令类型：0：0.25速度   ；1：0.5速度   ； 2：1速度；3：2速度
	private byte result;    //0 成功  1失败
 
	public DVRToVideoSpeedRsp0xE5(byte[] _data){
		analyzeData(_data);
	}
	
	private void analyzeData(byte[] _data){
		IoBuffer ioBuffer = IoBuffer.allocate(_data.length);
		ioBuffer.setAutoExpand(true);
        ioBuffer.put(_data);
        ioBuffer.flip();
        aheadOrBack = ioBuffer.get(15);
        kind = ioBuffer.get(16);
        result = ioBuffer.get(17);
		
		ioBuffer.free();
		ioBuffer = null;
	}
	
	public byte getKind() {
		return kind;
	}

	public void setKind(byte kind) {
		this.kind = kind;
	}

	public byte getResult() {
		return result;
	}

	public void setResult(byte result) {
		this.result = result;
	}
	
	public byte getAheadOrBack() {
		return aheadOrBack;
	}

	public void setAheadOrBack(byte aheadOrBack) {
		this.aheadOrBack = aheadOrBack;
	}
}
