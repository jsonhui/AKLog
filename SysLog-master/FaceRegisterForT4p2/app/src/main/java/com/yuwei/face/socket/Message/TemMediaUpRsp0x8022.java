package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

public class TemMediaUpRsp0x8022 extends BaseCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4926677179877198873L;

	//发送方消息ID
	private short msgid;
	//应答流水号
	private short msg_serial_num;
	// byte 通道号/帧类型
	//BIT0~BIT3:通道号
	//1、2、3、4号通道
	//Bit4~BIT7:帧类型
	//1:视频	2:音频 3:INfo(附加信息)4:图片 5:GPS信息 6:音视频
	private byte mchid;
	// int 事件项编码
	/*
	 * 1:平台下发指令实时媒体 
	 * 2:定时动作 
	 * 3:报警触发 
	 * 4: 平台下发指令历史媒体
	 */
	private byte encoding;
	//确认收到的连续最大媒体包序号
	private int mnum;
	//重传包总数
	private byte rnum;
	//重传包序号列表
	private int rlist;
	

	@Override
	public byte[] doDataEncode() {
		// TODO Auto-generated method stub
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        
        ioBuffer.putShort(msgid);
        ioBuffer.putShort(msg_serial_num);
        ioBuffer.put(mchid);
        ioBuffer.put(encoding);
        ioBuffer.putInt(mnum);
        ioBuffer.put(rnum);
        ioBuffer.putInt(rlist);
        
        ioBuffer.flip();
        byte bs[] = new byte[ioBuffer.remaining()];
        ioBuffer.get(bs);
        ioBuffer.free();
		
		return bs;
	}


	public short getMsgid() {
		return msgid;
	}

	public void setMsgid(short msgid) {
		this.msgid = msgid;
	}

	public short getMsg_serial_num() {
		return msg_serial_num;
	}

	public void setMsg_serial_num(short msg_serial_num) {
		this.msg_serial_num = msg_serial_num;
	}

	public byte getMchid() {
		return mchid;
	}

	public void setMchid(byte mchid) {
		this.mchid = mchid;
	}

	public byte getEncoding() {
		return encoding;
	}

	public void setEncoding(byte encoding) {
		this.encoding = encoding;
	}

	public int getMnum() {
		return mnum;
	}

	public void setMnum(int mnum) {
		this.mnum = mnum;
	}

	public byte getRnum() {
		return rnum;
	}

	public void setRnum(byte rnum) {
		this.rnum = rnum;
	}

	public int getRlist() {
		return rlist;
	}

	public void setRlist(int rlist) {
		this.rlist = rlist;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
