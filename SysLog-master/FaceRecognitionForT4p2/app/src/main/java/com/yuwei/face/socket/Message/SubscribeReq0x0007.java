package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * 启动实时媒体请求
 *
 */
public class SubscribeReq0x0007 extends BaseCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5433717698957575146L;
	
	//通道号/帧类型
	private byte ch_frame;
	//媒体启动类型
	private byte start_type;
	//限制时长 	秒
	private short times;
	//限制流量	KB
	private int tranffs;
	//附加消息体掩码
	private byte addtion_no;
	
	


	@Override
	public byte[] doDataEncode() {
		// TODO Auto-generated method stub
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        
        ioBuffer.put(ch_frame);
        ioBuffer.put(start_type);
        ioBuffer.putShort(times);
        ioBuffer.putInt(tranffs);
        ioBuffer.put(addtion_no);
		
        ioBuffer.flip();
        byte bs[] = new byte[ioBuffer.remaining()];
        ioBuffer.get(bs);
        ioBuffer.free();
		return bs;
	}

	public byte getCh_frame() {
		return ch_frame;
	}

	public void setCh_frame(byte ch_frame) {
		this.ch_frame = ch_frame;
	}

	public byte getStart_type() {
		return start_type;
	}

	public void setStart_type(byte start_type) {
		this.start_type = start_type;
	}

	public short getTimes() {
		return times;
	}

	public void setTimes(short times) {
		this.times = times;
	}

	public int getTranffs() {
		return tranffs;
	}

	public void setTranffs(int tranffs) {
		this.tranffs = tranffs;
	}

	public byte getAddtion_no() {
		return addtion_no;
	}

	public void setAddtion_no(byte addtion_no) {
		this.addtion_no = addtion_no;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
