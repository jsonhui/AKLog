package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * 终端注册消息
 *
 */
public class TemRegisterReq0x0003 extends BaseCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3421812326122394911L;
	
	private GossPrefix gossPrefix;
	
	//设备ID
	private int device_id;
	//应用ID
	private int app_id;
	//标示长度
	private byte mark_length;
	//标示码 n个byte，n<256
	private byte[] mark_num;
	//支持到音频数
	//音频按位标示音频通道
	//BIT0~BIT7表示:1~8通道
	private byte suport_audio_num;
	//支持到视频数
	private byte suport_video_num;
	//协议版本
	//高字节表示主版本，低字节表示子版本
	private short protocol_version;
	//软件版本
	//高字节表示主版本，低字节表示子版本
	private short soft_version;
	//软件日期
	//格林威治时间，相对于2000.1.1过去的秒数目
	private int soft_date;
	//硬件型号
	private short hard_type;
	//软件功能标记
	private int soft_func_mark;
	//注册标记	默认0x00
	//0:正常注册
	//1：平台下发命令要求连接
	//2:超时重连（没有收到中心应答，判断离线）
	//3:连接异常重连（收到应答有误或其他错误）
	private short regist_mark;
	//通信模式
	//1:GPRS 2:DEGE 3:3G 4:4G
	private byte comm_mode;
	//SIM卡类型
	//1:GSM 2:WCDMA 3:CDMA2000 4:TDSCDMA 20:WIFI
	private byte sim_type;
	//附加项数	默认0
	private byte addtion_flag;
	//附加项		通用TLV项 0x71 0x73 0x12
	
	public TemRegisterReq0x0003(byte[] bs)
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
        mark_length =  ioBuffer.get();
        mark_num = new byte[mark_length];
        ioBuffer.get(mark_num);
        suport_audio_num = ioBuffer.get();
        suport_video_num = ioBuffer.get();
        protocol_version = ioBuffer.getShort();
        soft_version = ioBuffer.getShort();
        soft_date = ioBuffer.getInt();
        hard_type = ioBuffer.getShort();
        soft_func_mark = ioBuffer.getInt();
        regist_mark = ioBuffer.getShort();
        comm_mode = ioBuffer.get();
        sim_type = ioBuffer.get();
        addtion_flag = ioBuffer.get();
        
        ioBuffer.free();
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

	public byte getMark_length() {
		return mark_length;
	}

	public void setMark_length(byte mark_length) {
		this.mark_length = mark_length;
	}

	public byte[] getMark_num() {
		return mark_num;
	}

	public void setMark_num(byte[] mark_num) {
		this.mark_num = mark_num;
	}

	public byte getSuport_audio_num() {
		return suport_audio_num;
	}

	public void setSuport_audio_num(byte suport_audio_num) {
		this.suport_audio_num = suport_audio_num;
	}

	public byte getSuport_video_num() {
		return suport_video_num;
	}

	public void setSuport_video_num(byte suport_video_num) {
		this.suport_video_num = suport_video_num;
	}

	public short getProtocol_version() {
		return protocol_version;
	}

	public void setProtocol_version(short protocol_version) {
		this.protocol_version = protocol_version;
	}

	public short getSoft_version() {
		return soft_version;
	}

	public void setSoft_version(short soft_version) {
		this.soft_version = soft_version;
	}

	public int getSoft_date() {
		return soft_date;
	}

	public void setSoft_date(int soft_date) {
		this.soft_date = soft_date;
	}

	public short getHard_type() {
		return hard_type;
	}

	public void setHard_type(short hard_type) {
		this.hard_type = hard_type;
	}

	public int getSoft_func_mark() {
		return soft_func_mark;
	}

	public void setSoft_func_mark(int soft_func_mark) {
		this.soft_func_mark = soft_func_mark;
	}

	public short getRegist_mark() {
		return regist_mark;
	}

	public void setRegist_mark(short regist_mark) {
		this.regist_mark = regist_mark;
	}

	public byte getComm_mode() {
		return comm_mode;
	}

	public void setComm_mode(byte comm_mode) {
		this.comm_mode = comm_mode;
	}

	public byte getSim_type() {
		return sim_type;
	}

	public void setSim_type(byte sim_type) {
		this.sim_type = sim_type;
	}

	public byte getAddtion_flag() {
		return addtion_flag;
	}

	public void setAddtion_flag(byte addtion_flag) {
		this.addtion_flag = addtion_flag;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	

	public GossPrefix getGossPrefix() {
		return gossPrefix;
	}

	public void setGossPrefix(GossPrefix gossPrefix) {
		this.gossPrefix = gossPrefix;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String re = "TemRegisterReq0x0003 [device_id="+device_id+", app_id="+app_id+", mark_length="+mark_length
				+", mark_num="+mark_num+", suport_audio_num="+suport_audio_num+", suport_video_num="+suport_video_num
				+", protocol_version="+protocol_version+", soft_version="+soft_version+", soft_date="+soft_date
				+", hard_type="+hard_type+", soft_func_mark="+soft_func_mark+", regist_mark="+regist_mark
				+", comm_mode="+comm_mode+", sim_type="+sim_type+", addtion_flag="+addtion_flag;
		return re;
	}

	@Override
	public byte[] doDataEncode() {
		// TODO Auto-generated method stub
		return null;
	}

}
