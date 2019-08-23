package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * 通用TLV附加项格式
 *
 */
public class AddtionData {
	
	private byte ID;
	//长度
	private byte length;
	//数据
	private byte[] datas;
	
	public byte[] toData()
	{
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        
        ioBuffer.put(ID);
        ioBuffer.put(length);
        ioBuffer.put(datas);
        
        ioBuffer.flip();
        byte bs[] = new byte[ioBuffer.remaining()];
        ioBuffer.get(bs);
        ioBuffer.free();
        return bs;
	}
	
	public byte getID() {
		return ID;
	}
	public void setID(byte iD) {
		ID = iD;
	}
	public byte getLength() {
		return length;
	}
	public void setLength(byte length) {
		this.length = length;
	}
	public byte[] getDatas() {
		return datas;
	}
	public void setDatas(byte[] datas) {
		this.datas = datas;
	}
}
