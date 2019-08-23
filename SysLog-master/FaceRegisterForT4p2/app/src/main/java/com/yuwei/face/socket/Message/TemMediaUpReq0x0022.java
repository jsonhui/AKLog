package com.yuwei.face.socket.Message;


/**
 * 终端媒体上报请求
 *
 */
public class TemMediaUpReq0x0022 extends BaseCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1456377716972115761L;
	//传输ID 每次传输过程的唯一ID
	private int trans_id;
	//节拍内序列号
	//从0开始的序列号
	private short mfg_num;
	//历史查询序列号
	private byte history_qry_num;
	//通道号/帧类型
	//BIT0~BIT3:通道号
	//1、2、3、4号通道
	//Bit4~BIT7:帧类型
	//1:视频	2:音频 3:INfo(附加信息)4:图片 5:GPS信息 6:音视频
	private byte ch_frame_type;
	//媒体包序号		从0开始
	private int media_no;
	//媒体格式
	//1:H264 2:MPEG4 3:WMV 4:AMR 5:ADPCM 6:AAC 7:JPEG 8:G.711 9:G.726
	private byte media_type;
	//事件项编码
	//1:平台下发实时媒体指令
	//2:定时动作
	//3:报警触发
	//4:平台下发指令查询并上传历史媒体
	//5:平台下发指令启动历史媒体
	//6:对讲
	//7:平台下发指令下载历史媒体
	private byte event_num;
	//分辨率/采样率
	//视频:
	//BIT0~BIT2: 0-QCIF 1-CIF 2-DCIF 3-DI
	//BIT3~BIT7: 帧率
	//音频:
	//1) AAC音频编码码率 (kbit/s)
	//0:16 1:22 2:24 3:32 4:48 5:64 6:96 7:128
	//2) AMR编解码速率模式 (kbit/s)
	//0:4.75 1:5.15 2:5.90 3:6.70 4:7.40 5:7.95 6:10.20 7:12.20
	//3) G.726编解码协议速率 (kbit/s)
	//0:16 1:24 2:32 3:40 5:16 for ASF 6:24 for ASF 7:32 for ASF
	//8:40 for ASF
	private byte reslu_samp_rate;
	//------媒体数据内容-------
	
	private byte[] rtpdata;
	
	

	@Override
	public byte[] doDataEncode() {
		// TODO Auto-generated method stub
		
		return null;
	}


	public int getTrans_id() {
		return trans_id;
	}

	public void setTrans_id(int trans_id) {
		this.trans_id = trans_id;
	}

	public short getMfg_num() {
		return mfg_num;
	}

	public void setMfg_num(short mfg_num) {
		this.mfg_num = mfg_num;
	}

	public byte getHistory_qry_num() {
		return history_qry_num;
	}

	public void setHistory_qry_num(byte history_qry_num) {
		this.history_qry_num = history_qry_num;
	}

	public byte getCh_frame_type() {
		return ch_frame_type;
	}

	public void setCh_frame_type(byte ch_frame_type) {
		this.ch_frame_type = ch_frame_type;
	}

	public int getMedia_no() {
		return media_no;
	}

	public void setMedia_no(int media_no) {
		this.media_no = media_no;
	}

	public byte getMedia_type() {
		return media_type;
	}

	public void setMedia_type(byte media_type) {
		this.media_type = media_type;
	}

	public byte getEvent_num() {
		return event_num;
	}

	public void setEvent_num(byte event_num) {
		this.event_num = event_num;
	}

	public byte getReslu_samp_rate() {
		return reslu_samp_rate;
	}

	public void setReslu_samp_rate(byte reslu_samp_rate) {
		this.reslu_samp_rate = reslu_samp_rate;
	}

	public byte[] getRtpdata() {
		return rtpdata;
	}

	public void setRtpdata(byte[] rtpdata) {
		this.rtpdata = rtpdata;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
