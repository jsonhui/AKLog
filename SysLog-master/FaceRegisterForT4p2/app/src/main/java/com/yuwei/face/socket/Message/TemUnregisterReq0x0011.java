package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * 终端注销消息
 *
 */
public class TemUnregisterReq0x0011 extends BaseCommand {

	private GossPrefix gossPrefix;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4969416990620610708L;
	//设备Id
	private int device_id;
	//应用ID
	private int app_id;
	//标示长度
	private byte mark_len;
	//标示码		长度：mark_len < 256
	private byte[] mark_num;
	//注销标记	默认0x00
	//0:连接时间到注销(在要求终端连接一定时间的,在连接时间到了后注销)
	//1:终端自己重启
	//2:平台下发命令要求断开连接（如强制重连接）
	//3:平台发命令要求终端重启
	//4:主通道断开
	private short unregist_mark;
	//附加项数	默认0
	private byte additon_num;
	
	public TemUnregisterReq0x0011(byte[] bs)
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
        
        device_id = ioBuffer.getInt();
        app_id = ioBuffer.getInt();
        mark_len = ioBuffer.get();
        mark_num = new byte[mark_len];
        ioBuffer.get(mark_num);
        unregist_mark = ioBuffer.getShort();
        additon_num = ioBuffer.get();
        
        ioBuffer.free();
	}
	

	@Override
	public byte[] doDataEncode() {
		// TODO Auto-generated method stub
		return null;
	}

	public GossPrefix getGossPrefix() {
		return gossPrefix;
	}

	public void setGossPrefix(GossPrefix gossPrefix) {
		this.gossPrefix = gossPrefix;
	}

	public int getDevice_id() {
		return device_id;
	}

	public void setDevice_id(int device_id) {
		this.device_id = device_id;
	}

	public int getApp_id() {
		return app_id;
	}

	public void setApp_id(int app_id) {
		this.app_id = app_id;
	}

	public byte getMark_len() {
		return mark_len;
	}

	public void setMark_len(byte mark_len) {
		this.mark_len = mark_len;
	}

	public byte[] getMark_num() {
		return mark_num;
	}

	public void setMark_num(byte[] mark_num) {
		this.mark_num = mark_num;
	}

	public short getUnregist_mark() {
		return unregist_mark;
	}

	public void setUnregist_mark(short unregist_mark) {
		this.unregist_mark = unregist_mark;
	}

	public byte getAdditon_num() {
		return additon_num;
	}

	public void setAdditon_num(byte additon_num) {
		this.additon_num = additon_num;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
