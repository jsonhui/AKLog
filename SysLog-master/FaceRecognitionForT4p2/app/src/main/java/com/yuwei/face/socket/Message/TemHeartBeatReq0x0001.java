package com.yuwei.face.socket.Message;


/**
 * 终端发送心跳消息
 *
 */
public class TemHeartBeatReq0x0001 extends BaseCommand {

	private GossPrefix gossPrefix;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8612109235341284877L;
	

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

}
