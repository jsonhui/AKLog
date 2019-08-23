package com.yuwei.face.socket.Message;


public class MediaRpt0x1000Cmd extends BaseCommand {

	/**
     * 
     */
	private static final long serialVersionUID = -4111284396370731890L;

	// time int 从启动媒体开始到现在的毫秒数
	private int time;

	// seq Int 媒体序列号
	private int seq;
	private byte[] rtpdata;
	private GossPrefix gossPrefix;

	public MediaRpt0x1000Cmd() {
		super();
		cmd = GossCmdConst.CMD_STR_MEDIA_RPT;
	}
	
	@Override
	protected void doDataDecode(byte[] bs) {
		rtpdata = bs;
	}

	public MediaRpt0x1000Cmd(byte[] bs)
	{
		super(bs);
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public byte[] getRtpData() {
		return rtpdata;
	}

	public void setRtpData(byte[] _rtpdata) {
		rtpdata = new byte[_rtpdata.length];
		System.arraycopy(_rtpdata, 0, rtpdata, 0, _rtpdata.length);
	}

	@Override
	public byte[] doDataEncode() {
		return null;
	}

	public GossPrefix getGossPrefix() {
		return gossPrefix;
	}

	public void setGossPrefix(GossPrefix gossPrefix) {
		this.gossPrefix = gossPrefix;
	}
}
