package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * 媒体通用应答消息
 *
 */
public class TerminalRsp0x8001 extends BaseCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1197964753729870546L;

	//发送方消息ID
	private short msg_id;
	//应答流水号
	private short msg_serial_no;
	//结果
	//0:成功	 1:失败
	private byte result;
	//通道号/帧类型
	private byte ch_frame;
	
	public TerminalRsp0x8001()
	{
		
	}
	
	public TerminalRsp0x8001(byte[] bs)
	{
		super(bs);
	}
	
	@Override
	protected void doDataDecode(byte[] bs) {
		// TODO Auto-generated method stub
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        ioBuffer.put(bs);
        ioBuffer.flip();
        
        msg_id = ioBuffer.getShort();
        msg_serial_no = ioBuffer.getShort();
        result = ioBuffer.get();
        ch_frame = ioBuffer.get();
        ioBuffer.free();
	}
	
	

	@Override
	public byte[] doDataEncode() {
		// TODO Auto-generated method stub
		return null;
	}

	public short getMsg_id() {
		return msg_id;
	}

	public void setMsg_id(short msg_id) {
		this.msg_id = msg_id;
	}

	public short getMsg_serial_no() {
		return msg_serial_no;
	}

	public void setMsg_serial_no(short msg_serial_no) {
		this.msg_serial_no = msg_serial_no;
	}

	public byte getResult() {
		return result;
	}

	public void setResult(byte result) {
		this.result = result;
	}

	public byte getCh_frame() {
		return ch_frame;
	}

	public void setCh_frame(byte ch_frame) {
		this.ch_frame = ch_frame;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	private GossPrefix gossPrefix;
	
	public GossPrefix getGossPrefix() {
		return gossPrefix;
	}

	public void setGossPrefix(GossPrefix gossPrefix) {
		this.gossPrefix = gossPrefix;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String ss = "TerminalRsp0x8001 [msg_id=" + msg_id + ", msg_serial_no="
				+ msg_serial_no +", result="+result+", ch_frame="+ch_frame+"]";
		return ss;
	}

}
