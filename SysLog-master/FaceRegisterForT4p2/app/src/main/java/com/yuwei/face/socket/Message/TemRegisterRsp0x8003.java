package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

import java.nio.ByteOrder;

/**
 * 终端注册消息应答
 *
 */
public class TemRegisterRsp0x8003 extends BaseCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2734406551302349947L;

	private short msgId;//发送方消息ID
	private short rsp_msg_serial_num;//应答流水号
	//result:0:成功，1:失败，2:重定向
	private byte result;
	//支持最大协议版本
	//高字节表示主版本 ，低字节表示子版本
	private short max_protocol_version_suport;
	//软件功能标记
	private int software_mark;
	//格林威治时间，相对于2000.1.1过去到秒数目
	private int cur_time;
	//心跳时间间隔	0:不做修改
	private byte hear_beat_interval;
	//是否启动TCP通道	0:不启动，1:启动
	private byte need_start_tcp;
	//重定向IP
	private int re_ip;
	//重定向端口
	private short re_port;
	//描述该消息体长度
	private short msg_length = 23;
	
	public TemRegisterRsp0x8003()
	{
		super();
		setMsgId((short)GossCmdConst.CMD_TEM_REGISTER_RSP);
		
		
	}
	
	@Override
	protected void doDataDecode(byte[] bs) {
		// TODO Auto-generated method stub
        
	}
	

	public short getMsgId() {
		return msgId;
	}

	public void setMsgId(short msgId) {
		this.msgId = msgId;
	}

	public short getRsp_msg_serial_num() {
		return rsp_msg_serial_num;
	}

	public void setRsp_msg_serial_num(short rsp_msg_serial_num) {
		this.rsp_msg_serial_num = rsp_msg_serial_num;
	}

	public byte getResult() {
		return result;
	}

	public void setResult(byte result) {
		this.result = result;
	}

	public short getMax_protocol_version_suport() {
		return max_protocol_version_suport;
	}

	public void setMax_protocol_version_suport(short max_protocol_version_suport) {
		this.max_protocol_version_suport = max_protocol_version_suport;
	}

	public int getSoftware_mark() {
		return software_mark;
	}

	public void setSoftware_mark(int software_mark) {
		this.software_mark = software_mark;
	}

	public int getCur_time() {
		return cur_time;
	}

	public void setCur_time(int cur_time) {
		this.cur_time = cur_time;
	}

	public byte getHear_beat_interval() {
		return hear_beat_interval;
	}

	public void setHear_beat_interval(byte hear_beat_interval) {
		this.hear_beat_interval = hear_beat_interval;
	}

	public byte getNeed_start_tcp() {
		return need_start_tcp;
	}

	public void setNeed_start_tcp(byte need_start_tcp) {
		this.need_start_tcp = need_start_tcp;
	}

	public int getRe_ip() {
		return re_ip;
	}

	public void setRe_ip(int re_ip) {
		this.re_ip = re_ip;
	}

	public short getRe_port() {
		return re_port;
	}

	public void setRe_port(short re_port) {
		this.re_port = re_port;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public short getMsg_length() {
		return msg_length;
	}

	public void setMsg_length(short msg_length) {
		this.msg_length = msg_length;
	}

	@Override
	public byte[] doDataEncode() {
		// TODO Auto-generated method stub
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        ioBuffer.order(ByteOrder.BIG_ENDIAN);
		
        ioBuffer.putShort(msgId);
        ioBuffer.putShort(rsp_msg_serial_num);
        ioBuffer.put(result);
        ioBuffer.putShort(max_protocol_version_suport);
        ioBuffer.putInt(software_mark);
        ioBuffer.putInt(cur_time);
        ioBuffer.put(hear_beat_interval);
        ioBuffer.put(need_start_tcp);
        ioBuffer.putInt(re_ip);
        ioBuffer.putShort(re_port);
		
        ioBuffer.flip();
        byte bs[] = new byte[ioBuffer.remaining()];
        ioBuffer.get(bs);
        ioBuffer.free();
		return bs;
	}
	
	public static void printf_bytes(byte[] _data, int _len,String tag)
	{
		{
			String tmp = "";
			for(int i = 0; i < _len; i++)
			{
				tmp += toHex(_data[i]);
				tmp += " ";
			}
		}
	}
    /**
     * 将字节转换成 16 进制字符串
     *
     * @param b byte
     * @return String
     */
    private static String toHex(byte b) {
        Integer I = new Integer((((int) b) << 24) >>> 24);
        int i = I.intValue();

        if (i < (byte) 16) {
            return "0" + Integer.toString(i, 16);
        } else {
            return Integer.toString(i, 16);
        }
    }

}
