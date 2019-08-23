package com.yuwei.face.socket.Message;

import com.yuwei.face.callback.GetRspChannelStatusInterface;

import org.apache.mina.core.buffer.IoBuffer;

public class DVRToVideoRsp0xE1 implements GetRspChannelStatusInterface {

	private byte Channel_1;
	private byte Channel_2;
	private byte Channel_3;
	private byte Channel_4;
	private byte Channel_5;
	private byte Channel_6;
	private byte Channel_7;
	private byte Channel_8;

	public DVRToVideoRsp0xE1(byte[] _data) {
		analyzeData(_data);
	}

	private void analyzeData(byte[] _data) {
		IoBuffer ioBuffer = IoBuffer.allocate(_data.length);
		ioBuffer.setAutoExpand(true);
		ioBuffer.put(_data);
		ioBuffer.flip();

		Channel_1 = ioBuffer.get(15);
		Channel_2 = ioBuffer.get(16);
		Channel_3 = ioBuffer.get(17);
		Channel_4 = ioBuffer.get(18);
		Channel_5 = ioBuffer.get(19);
		Channel_6 = ioBuffer.get(20);
		Channel_7 = ioBuffer.get(21);
		Channel_8 = ioBuffer.get(22);
		
		ioBuffer.free();
		ioBuffer = null;
	}

	@Override
	public byte getChannel_1() {
		// TODO Auto-generated method stub
		return Channel_1;
	}

	@Override
	public byte getChannel_2() {
		// TODO Auto-generated method stub
		return Channel_2;
	}

	@Override
	public byte getChannel_3() {
		// TODO Auto-generated method stub
		return Channel_3;
	}

	@Override
	public byte getChannel_4() {
		// TODO Auto-generated method stub
		return Channel_4;
	}

	@Override
	public byte getChannel_5() {
		// TODO Auto-generated method stub
		return Channel_5;
	}

	@Override
	public byte getChannel_6() {
		// TODO Auto-generated method stub
		return Channel_6;
	}

	@Override
	public byte getChannel_7() {
		// TODO Auto-generated method stub
		return Channel_7;
	}

	@Override
	public byte getChannel_8() {
		// TODO Auto-generated method stub
		return Channel_8;
	}

}
