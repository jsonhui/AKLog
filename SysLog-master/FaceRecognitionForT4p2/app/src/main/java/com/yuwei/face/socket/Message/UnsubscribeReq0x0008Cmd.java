package com.yuwei.face.socket.Message;


public class UnsubscribeReq0x0008Cmd extends BaseCommand {

	/**
     * 
     */
	private static final long serialVersionUID = 5888999416653285569L;

	// type int 退订阅类型 0:退订阅所有 1:退订阅GPS 2:退订阅媒体(音视频)
	int type;

	// clientid int 客户端ID
	int clientid;

	// authcode string 签权码
	String authcode;

	// termid int 设备ID
	int termid;

	// mediatype int 0=视频, 1=音频
	int mediatype;

	// mchid int 通道ID
	int mchid;
	
	// clientype int 0=主通道,非0=媒体通道
	int clientype;

	public UnsubscribeReq0x0008Cmd() {
		super();
		cmd = GossCmdConst.CMD_STR_UNSUBSCRIBE_REQ;
	}
	
	public String getAuthCode() {
		return authcode;
	}

	public void setAuthCode(String authcode) {
		this.authcode = authcode;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getClientId() {
		return clientid;
	}

	public void setClientId(int clientid) {
		this.clientid = clientid;
	}

	public int getTermId() {
		return termid;
	}

	public void setTermId(int termid) {
		this.termid = termid;
	}

	public int getMediaType() {
		return mediatype;
	}

	public void setMediaType(int mediatype) {
		this.mediatype = mediatype;
	}

	public int getMchId() {
		return mchid;
	}

	public void setMchId(int mchid) {
		this.mchid = mchid;
	}

	public int getClienType() {
		return clientype;
	}

	public void setClienType(int clientype) {
		this.clientype = clientype;
	}

	@Override
	public byte[] doDataEncode() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
