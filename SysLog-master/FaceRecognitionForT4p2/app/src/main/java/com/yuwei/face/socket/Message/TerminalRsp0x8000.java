package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * 通用应答消息
 *
 */
public class TerminalRsp0x8000 extends BaseCommand {

	private GossPrefix gossPrefix;
	
	public GossPrefix getGossPrefix() {
		return gossPrefix;
	}

	public void setGossPrefix(GossPrefix gossPrefix) {
		this.gossPrefix = gossPrefix;
	}
	
	public TerminalRsp0x8000()
	{
		
	}
	
	public TerminalRsp0x8000(byte[] bs)
	{
		super(bs);
	}

	
	//发送方消息ID
	private short msg_id;
	//应答流水号
	private short msg_serial_no;
	//结果
	//0:成功	 1:失败
	private byte result;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5145993640176355941L;


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
        ioBuffer.free();
	}
	
	@Override
	public byte[] doDataEncode() {
		// TODO Auto-generated method stub
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        
        ioBuffer.putShort(msg_id);
        ioBuffer.putShort(msg_serial_no);
        ioBuffer.put(result);
		
        ioBuffer.flip();
        byte bs[] = new byte[ioBuffer.remaining()];
        ioBuffer.get(bs);
        ioBuffer.free();
		
		
		return bs;
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

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String ss = "TerminalRsp0x8000 [msg_id=" + msg_id + ", msg_serial_no="
				+ msg_serial_no +", result="+result+"]";
		return ss;
	}

}
