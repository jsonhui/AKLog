package com.yuwei.face.socket;

import com.yuwei.face.socket.Message.BaseCommand;
import com.yuwei.face.socket.Message.GossCmdConst;
import com.yuwei.face.socket.Message.GossMessage;
import com.yuwei.face.socket.Message.GossPrefix;
import com.yuwei.face.socket.Message.MediaRpt0x1000Cmd;
import com.yuwei.face.socket.Message.SubscribeReq0x0007;
import com.yuwei.face.socket.Message.TemHeartBeatReq0x0001;
import com.yuwei.face.socket.Message.TemMediaUpRsp0x8022;
import com.yuwei.face.socket.Message.TemRegisterReq0x0003;
import com.yuwei.face.socket.Message.TemRegisterRsp0x8003;
import com.yuwei.face.socket.Message.TemSetConfigReq0x000F;
import com.yuwei.face.socket.Message.TemUnregisterReq0x0011;
import com.yuwei.face.socket.Message.TerminalRsp0x8000;
import com.yuwei.face.socket.Message.UnsubscribeReq0x0008Cmd;

import org.apache.mina.core.buffer.IoBuffer;

import java.text.ParseException;

public class GossTool {

	public static int seq = 0;

	public static synchronized int genSeq() {
		int seq1 = seq;
		seq++;
		return seq1;
	}

	public static byte[] convertGossMessageToByte(GossMessage gossMessage) {
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
		ioBuffer.setAutoExpand(true);
		GossPrefix gossPrefix = gossMessage.getGossPrefix();
		BaseCommand baseCommand = gossMessage.getBaseCommand();
		byte body[] = baseCommand.toData();
		
//		printf_bytes(body, body.length, "convertGossMessageToByte");
		gossPrefix.setBodyLen(body.length);
		byte header[] = gossPrefix.toBytes();

		byte check = 0;
		for (byte b : header) {
			check ^= b;
		}
		ioBuffer.put(GossCmdConst.HEADER_FLAG);
		ioBuffer.put((byte) check);
		ioBuffer.put(header);
		ioBuffer.put(body);
		ioBuffer.flip();
		byte rData[] = new byte[ioBuffer.remaining()];
		ioBuffer.get(rData);
		ioBuffer.free();
//		Utility.printf_bytes(rData,"convertGossMessageToByte");
		return rData;
	}

	public static void main(String args[]) {
	}
	
	public static GossPrefix makeGossPrefix(short msgId)
	{
		GossPrefix gossPrefix = new GossPrefix();
		gossPrefix.setMessage_id(msgId);
		gossPrefix.setDevice_id(GossClient.gDeviceId);
		gossPrefix.setMessage_serial_num((short)0);
		gossPrefix.setProperty((byte)0);
		gossPrefix.setFenbao_flag((byte)0x11);
		return gossPrefix;
	}
	
	public static GossPrefix makeGossPrefix(GossPrefix _gossPrefix,short msgId)
	{
		GossPrefix gossPrefix = new GossPrefix();
		gossPrefix.setMessage_id(msgId);
		gossPrefix.setDevice_id(_gossPrefix.getDevice_id());
		gossPrefix.setMessage_serial_num(_gossPrefix.getMessage_serial_num());
		gossPrefix.setProperty((byte)0);
		gossPrefix.setFenbao_flag((byte)0x11);
		
		return gossPrefix;
	}
	
	public static GossPrefix makeGossPrefix(int srcAppType, int srcAppId,
			int dstAppType, int dstAppId) {
		GossPrefix gossPrefix = new GossPrefix();
		return gossPrefix;
	}

	// 取消媒体订阅
	public static UnsubscribeReq0x0008Cmd makeUnsubscribeReq0x0008Cmd(int type,
                                                                      int clientId, String authCode, int termId, int mediaType, int mchId, int clientype) {
		UnsubscribeReq0x0008Cmd unsubscribeReq0x0008Cmd = new UnsubscribeReq0x0008Cmd();
		unsubscribeReq0x0008Cmd.setType(type);
		unsubscribeReq0x0008Cmd.setClientId(clientId);
		unsubscribeReq0x0008Cmd.setAuthCode(authCode);
		unsubscribeReq0x0008Cmd.setTermId(termId);
		unsubscribeReq0x0008Cmd.setMediaType(mediaType);
		unsubscribeReq0x0008Cmd.setMchId(mchId);
		unsubscribeReq0x0008Cmd.setClienType(clientype);
		return unsubscribeReq0x0008Cmd;
	}
	
	
	private static int calculateCurTime()
	{
		String start="2000-01-01 00:00:00";
		java.text.DateFormat df=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		java.util.Calendar c1=java.util.Calendar.getInstance(); 
		try 
		{
			
			c1.setTime(df.parse(start));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		long start_milis = c1.getTimeInMillis();
		
		return (int)(System.currentTimeMillis() - start_milis);
	}
	
//---------------------------------------add in 20180704 liulei------------------------------------------------
	public static void createRegisterMsg0x0003Cmd(){
		
	}
//--------------------------------------- add in 20150910 --------------------------------------------------------------------------	
	public static TemRegisterRsp0x8003 makeTemRegisterRsp0x8003Cmd(TemRegisterReq0x0003 reqCmd)
	{
		TemRegisterRsp0x8003 temRegisterRsp0x8003 = new TemRegisterRsp0x8003();
		temRegisterRsp0x8003.setRsp_msg_serial_num(reqCmd.getGossPrefix().getMessage_serial_num());
		temRegisterRsp0x8003.setResult((byte)0);
		temRegisterRsp0x8003.setMax_protocol_version_suport(reqCmd.getProtocol_version());
		temRegisterRsp0x8003.setSoftware_mark(reqCmd.getSoft_func_mark());
		temRegisterRsp0x8003.setCur_time(calculateCurTime());
		temRegisterRsp0x8003.setHear_beat_interval((byte)0);
		temRegisterRsp0x8003.setNeed_start_tcp((byte)0);
		temRegisterRsp0x8003.setRe_ip((byte)0);
		temRegisterRsp0x8003.setRe_port((byte)0);
		
		return temRegisterRsp0x8003;
	}
	
	public static TerminalRsp0x8000 makeTerminalRsp0x8000Cmd(TemHeartBeatReq0x0001 reqCmd)
	{
		TerminalRsp0x8000 temHearBeatRsp0x8000 = new TerminalRsp0x8000();
		temHearBeatRsp0x8000.setMsg_id(reqCmd.getGossPrefix().getMessage_id());
		temHearBeatRsp0x8000.setMsg_serial_no(reqCmd.getGossPrefix().getMessage_serial_num());
		temHearBeatRsp0x8000.setResult((byte)0);
		
		return temHearBeatRsp0x8000;
	}
	
	public static TerminalRsp0x8000 makeTerminalRsp0x8000Cmd(TemUnregisterReq0x0011 reqCmd)
	{
		TerminalRsp0x8000 temHearBeatRsp0x8000 = new TerminalRsp0x8000();
		temHearBeatRsp0x8000.setMsg_id(reqCmd.getGossPrefix().getMessage_id());
		temHearBeatRsp0x8000.setMsg_serial_no(reqCmd.getGossPrefix().getMessage_serial_num());
		temHearBeatRsp0x8000.setResult((byte)0);
		
		return temHearBeatRsp0x8000;
	}
	
	public static TemSetConfigReq0x000F makeTemSetConfigReq0x000FCmd()
	{
		TemSetConfigReq0x000F temSetConfigReq0x000F = new TemSetConfigReq0x000F();
		return temSetConfigReq0x000F;
	}
	
	public static SubscribeReq0x0007 makeSubscribeReq0x0007Cmd(int mchid)
	{
		byte chid;
		switch(mchid)
		{
			case 0:
				chid = 0x11;
				break;
			case 1:
				chid = 0x12;
				break;
			case 2:
				chid = 0x13;
				break;
			case 3:
				chid = 0x14;
				break;
			default:
				chid = 0x11;
				break;
		}
		
		SubscribeReq0x0007 subscribeReq0x0007 = new SubscribeReq0x0007();
		subscribeReq0x0007.setCh_frame(chid);//1通道  / 1视频
		subscribeReq0x0007.setStart_type((byte)0x00);//实时(不限制时间流量)
		subscribeReq0x0007.setTimes((short)0);
		subscribeReq0x0007.setTranffs(0);
		subscribeReq0x0007.setAddtion_no((byte)0);
		
		return subscribeReq0x0007;
	}
	
	public static TemMediaUpRsp0x8022 makeTemMediaUpRsp0x8022Cmd(MediaRpt0x1000Cmd cmd, int mchid, int total)
	{
		byte chid;
		switch(mchid)
		{
			case 0:
				chid = 0x11;
				break;
			case 1:
				chid = 0x12;
				break;
			case 2:
				chid = 0x13;
				break;
			case 3:
				chid = 0x14;
				break;
			default:
				chid = 0x11;
				break;
		}
		TemMediaUpRsp0x8022 temMediaUpRsp0x8022 = new TemMediaUpRsp0x8022();
		temMediaUpRsp0x8022.setMsgid((byte)0x8022);
		if(cmd != null)
		{
			temMediaUpRsp0x8022.setMsg_serial_num(cmd.getGossPrefix().getMessage_serial_num());
		}else
		{
			temMediaUpRsp0x8022.setMsg_serial_num((short)0);
		}
		temMediaUpRsp0x8022.setMchid(chid);
		temMediaUpRsp0x8022.setEncoding((byte)1);
		temMediaUpRsp0x8022.setMnum(total);
		temMediaUpRsp0x8022.setRnum((byte)0);
		temMediaUpRsp0x8022.setRlist(0);
		return temMediaUpRsp0x8022;
	}
	
	public static byte[] getChannelNum(int channelNum){
		 int channel = 1 << (channelNum - 1);
		byte[] channelArray = new byte[2];
		channelArray[0] = (byte) (channel & 0xFF);
		channelArray[1] = (byte) (channel & 0xFF00);
		return channelArray;
	}
	
//-----------------------------------------------------------------------------------------------------------------------------------	
	
}
