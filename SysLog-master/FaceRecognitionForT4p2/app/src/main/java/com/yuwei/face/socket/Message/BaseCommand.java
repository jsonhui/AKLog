package com.yuwei.face.socket.Message;

import java.io.Serializable;
import java.util.Date;

abstract public class BaseCommand implements Serializable {

	/**
     * 
     */
	private static final long serialVersionUID = 294939242694590618L;

	private final Date createTime = new Date();

	public byte[] toData()
	{
		byte data[] = doDataEncode();
		return data;
	}

	protected String cmd;


	public BaseCommand() {

	}
	
	public BaseCommand(byte[] bs)
	{
		doDataDecode(bs);
	}
	
	/**
	 * 解析消息体	20150910 add
	 * @param bs	消息体数据
	 */
	protected void doDataDecode(byte[] bs){}
	
	/**
	 * 打包发送的数据	20150910 add
	 * @return	二进制数据
	 */
	abstract public byte[] doDataEncode();

	public String getCmd() {
		return cmd;
	}

	public Date getCreateTime() {
		return createTime;
	}

}
