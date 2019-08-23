package com.yuwei.face.socket.Message;

import java.io.Serializable;


public class GossMessage implements Serializable {

	static final String tag = "GossMessage";
	private static final long serialVersionUID = -4442180153588656887L;

	protected GossPrefix gossPrefix;
	private String args;

	protected BaseCommand baseCommand;
	
	public GossMessage(){}
	
	public GossMessage(GossPrefix gossPrefix, BaseCommand baseCommand) {
		this.gossPrefix = gossPrefix;
		this.baseCommand = baseCommand;
	}

	public GossMessage(GossPrefix gossPrefix, byte[] bs) {
		this.gossPrefix = gossPrefix;
		int msgId = gossPrefix.getMessage_id()& 0xFFFF;//只取低16位
		
		switch (msgId) {
		case GossCmdConst.CMD_NUM_MEDIA_RPT://媒体数据上报
			baseCommand = new MediaRpt0x1000Cmd(bs);
			break;
		case GossCmdConst.CMD_TEM_REGISTER_REQ://终端注册
			baseCommand = new TemRegisterReq0x0003(bs);
			break;
		case GossCmdConst.CMD_TEM_HERTBEAT_REQ://终端发送心跳消息
			baseCommand = new TemHeartBeatReq0x0001();
			break;
		case GossCmdConst.CMD_TEM_UNREGISTER_REQ://终端注销
			baseCommand = new TemUnregisterReq0x0011(bs);
			break;
		case GossCmdConst.CMD_COMMON_RSP://通用应答
			baseCommand = new TerminalRsp0x8000(bs);
			break;
		case GossCmdConst.CMD_COMMON_MEDIA_RSP://媒体通用应答
			baseCommand = new TerminalRsp0x8001(bs);
			break;
		case GossCmdConst.CMD_VIDEO_SEND_CMD://发送video控制指令
			baseCommand = new VideoMaCmd(bs);
			break;
		case GossCmdConst.CMD_VIDEO_RECEIVE_DATA://接收video回调数据
			baseCommand = new VideoMaCmd(bs);
			break;
		case GossCmdConst.CMD_VIDEO_SERVER_CALLBACK://接收video回调数据
			baseCommand = new CbackCameraServer0x0801(bs);
			break;
		case GossCmdConst.CMD_SET_CAMERA_POWER_CALLBACK://接收video回调数据
			baseCommand = new CbackCameraPower0x0802(bs);
			break;
		default:
//			LogFactory.getLog(getClass()).warn(
//					"no deal msgId = " + String.format("%x", msgId));
			break;
		}
	}

	public GossPrefix getGossPrefix() {
		return gossPrefix;
	}

	public void setGossPrefix(GossPrefix gossPrefix) {
		this.gossPrefix = gossPrefix;
	}

	public BaseCommand getBaseCommand() {
		return baseCommand;
	}

	public void setBaseCommand(BaseCommand baseCommand) {
		this.baseCommand = baseCommand;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}
	

}
