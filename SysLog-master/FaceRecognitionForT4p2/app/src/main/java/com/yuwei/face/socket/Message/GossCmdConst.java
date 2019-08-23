package com.yuwei.face.socket.Message;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

public class GossCmdConst {

	public static final byte HEADER_FLAG = 0x26;

	@SuppressLint("UseSparseArrays")
	public static Map<Integer, String> Num2StrCmdMap = new HashMap<Integer, String>();

	public static Map<String, Integer> Str2NumCmdMap = new HashMap<String, Integer>();

	public static final String CMD_STR_HEARTBEAT_REQ = "heartbeat_req";
	
	public static final String CMD_STR_HEARTBEAT_RSP = "heartbeat_rsp";
	
	public static final int CMD_TEM_HERTBEAT_REQ = 0x0001;//终端发送心跳消息
	
	public static final int CMD_TEM_UNREGISTER_REQ = 0x0011;//终端注销消息

	public static final int CMD_NUM_HEARTBEAT_REQ = 0x0000;
	
	public static final int CMD_COMMON_RSP = 0x8000;//通用应答
	
	public static final int CMD_COMMON_MEDIA_RSP = 0x8001;//媒体通用应答
	
	public static final int CMD_VIDEO_SEND_CMD = 0x0900;			//0900代表老的发送给Ma通讯的控制指令
	public static final int CMD_VIDEO_RECEIVE_DATA = 0x0800;		//0800代表老的接收Ma通讯回调的数据

	public static final int CMD_SET_VIDEO_SERVER_PARAM = 0x0901;			//0901代表设置视频服务器参数
	public static final int CMD_VIDEO_SERVER_CALLBACK = 0x0801;			//0801代表设置视频服务器参数后的回调，返回服务器是否连接上

	public static final int CMD_SET_CAMERA_POWER = 0x0902;			//0902代表设置摄像头电源
	public static final int CMD_SET_CAMERA_POWER_CALLBACK = 0x0802;			//0802代表设置摄像头电源后回调，显示是否设置成功

	static {
		generateMap(CMD_STR_HEARTBEAT_REQ, CMD_NUM_HEARTBEAT_REQ);
		generateMap(CMD_STR_HEARTBEAT_RSP, CMD_COMMON_RSP);
	}
	
	public static final String CMD_STR_LOGIN_REQ = "login_req";

	public static final String CMD_STR_LOGIN_RSP = "login_rsp";
	
	public static final int CMD_NUM_LOGIN_REQ = 0x0002;

	public static final int CMD_NUM_LOGIN_RSP = 0x8002;
	
	static {
		generateMap(CMD_STR_LOGIN_REQ, CMD_NUM_LOGIN_REQ);
		generateMap(CMD_STR_LOGIN_RSP, CMD_NUM_LOGIN_RSP);
	}
	
	public static final String CMD_STR_LOGOUT_REQ = "logout_req";

	public static final String CMD_STR_LOGOUT_RSP = "logout_rsp";
	public static final int CMD_TEM_REGISTER_REQ = 0x0003;//终端注册消息
	public static final int CMD_TEM_REGISTER_RSP = 0x8003;//终端注册应答消息
	
	static {
		generateMap(CMD_STR_LOGOUT_REQ, CMD_TEM_REGISTER_REQ);
		generateMap(CMD_STR_LOGOUT_RSP, CMD_TEM_REGISTER_RSP);
	}
	
	public static final String CMD_STR_SUBSCRIBE_REQ = "subscribe_req";

	public static final String CMD_STR_SUBSCRIBE_RSP = "subscribe_rsp";
	
	public static final int CMD_NUM_SUBSCRIBE_REQ = 0x0007;

	public static final int CMD_NUM_SUBSCRIBE_RSP = 0x8007;
	
	public static final String CMD_STR_UNSUBSCRIBE_REQ = "unsubscribe_req";

	public static final String CMD_STR_UNSUBSCRIBE_RSP = "unsubscribe_rsp";
	
	public static final int CMD_NUM_UNSUBSCRIBE_REQ = 0x0008;

	public static final int CMD_NUM_UNSUBSCRIBE_RSP = 0x8008;

	static {
		generateMap(CMD_STR_SUBSCRIBE_REQ, CMD_NUM_SUBSCRIBE_REQ);
		generateMap(CMD_STR_SUBSCRIBE_RSP, CMD_NUM_SUBSCRIBE_RSP);
		
		generateMap(CMD_STR_UNSUBSCRIBE_REQ, CMD_NUM_UNSUBSCRIBE_REQ);
		generateMap(CMD_STR_UNSUBSCRIBE_RSP, CMD_NUM_UNSUBSCRIBE_RSP);
	}
	
	public static final String CMD_STR_GPSINFO_RPT = "gpsinfo_rpt";

	public static final String CMD_STR_GPSINFO_ACK = "gpsinfo_ack";
	
	public static final int CMD_NUM_GPSINFO_RPT = 0x0020;
	
	public static final int CMD_NUM_GPSINFO_ACK = 0x8020;
	
	static {
		generateMap(CMD_STR_GPSINFO_RPT, CMD_NUM_GPSINFO_RPT);
		generateMap(CMD_STR_GPSINFO_ACK, CMD_NUM_GPSINFO_ACK);
	}
	
	public static final String CMD_STR_MEDIA_RPT = "media_rpt";
	
	public static final String CMD_STR_MEDIA_ACK = "media_ack";
	
	public static final int CMD_NUM_MEDIA_RPT = 0x0022;//媒体数据上报
	
	public static final int CMD_NUM_MEDIA_ACK = 0x9000;
	
	static {
		generateMap(CMD_STR_MEDIA_RPT, CMD_NUM_MEDIA_RPT);
		generateMap(CMD_STR_MEDIA_ACK, CMD_NUM_MEDIA_ACK);
	}
	
	public static final String CMD_STR_RESETMEDIA_REQ = "reset_media_req";
	
	public static final String CMD_STR_RESETMEDIA_RSP = "reset_media_rsp";
	
	public static final int CMD_NUM_RESETMEDIA_REQ = 0x1001;
	
	public static final int CMD_NUM_RESETMEDIA_RSP = 0x9001;
	
	static {
		generateMap(CMD_STR_RESETMEDIA_REQ, CMD_NUM_RESETMEDIA_REQ);
		generateMap(CMD_STR_RESETMEDIA_RSP, CMD_NUM_RESETMEDIA_RSP);
	}
	
	public static final String CMD_STR_GETMDLIST_REQ = "getmdlist_req";
	
	public static final String CMD_STR_GETMDLIST_RSP = "getmdlist_rsp";
	
	public static final int CMD_NUM_GETMDLIST_REQ = 0x1002;
	
	public static final int CMD_NUM_GETMDLIST_RSP = 0x9002;
	
	static {
		generateMap(CMD_STR_GETMDLIST_REQ, CMD_NUM_GETMDLIST_REQ);
		generateMap(CMD_STR_GETMDLIST_RSP, CMD_NUM_GETMDLIST_RSP);
	}
	
	public static final String CMD_STR_SENDMDLIST_REQ = "sendmdlist_req";
	
	public static final String CMD_STR_SENDMDLIST_RSP = "sendmdlist_rsp";
	
	public static final int CMD_NUM_SENDMDLIST_REQ = 0x1003;
	
	public static final int CMD_NUM_SENDMDLIST_RSP = 0x9003;
	
	static {
		generateMap(CMD_STR_SENDMDLIST_REQ, CMD_NUM_SENDMDLIST_REQ);
		generateMap(CMD_STR_SENDMDLIST_RSP, CMD_NUM_SENDMDLIST_RSP);
	}
	
	public static final String CMD_STR_MDUPDATE_RST = "mdupdate_rst";
	
	public static final String CMD_STR_MDUPDATE_RSP = "mdupdate_rsp";
	
	public static final int CMD_NUM_MDUPDATE_RST = 0x1004;
	
	public static final int CMD_NUM_MDUPDATE_RSP = 0x9004;
	
	static {
		generateMap(CMD_STR_MDUPDATE_RST, CMD_NUM_MDUPDATE_RST);
		generateMap(CMD_STR_MDUPDATE_RSP, CMD_NUM_MDUPDATE_RSP);
	}
	
	public static final String CMD_STR_GETMD_REQ = "getmd_req";
	
	public static final String CMD_STR_GETMD_RSP = "getmd_rsp";
	
	public static final int CMD_NUM_GETMD_REQ = 0x1005;
	
	public static final int CMD_NUM_GETMD_RSP = 0x9005;
	
	static {
		generateMap(CMD_STR_GETMD_REQ, CMD_NUM_GETMD_REQ);
		generateMap(CMD_STR_GETMD_RSP, CMD_NUM_GETMD_RSP);
	}
	
	public static final String CMD_STR_QUERYUPMEDIA_REQ = "queryupmedia_req";
	
	public static final String CMD_STR_QUERYUPMEDIA_RSP = "queryupmedia_rsp";
	
	public static final int CMD_NUM_QUERYUPMEDIA_REQ = 0x1006;
	
	public static final int CMD_NUM_QUERYUPMEDIA_RSP = 0x9006;
	
	static {
		generateMap(CMD_STR_QUERYUPMEDIA_REQ, CMD_NUM_QUERYUPMEDIA_REQ);
		generateMap(CMD_STR_QUERYUPMEDIA_RSP, CMD_NUM_QUERYUPMEDIA_RSP);
	}
	
	public static final String CMD_STR_MEDIAFILESTART_REQ = "mediafilestart_req";
	
	public static final String CMD_STR_MEDIAFILESTART_RSP = "mediafilestart_rsp";
	
	public static final int CMD_NUM_MEDIAFILESTART_REQ = 0x1007;
	
	public static final int CMD_NUM_MEDIAFILESTART_RSP = 0x9007;
	
	static {
		generateMap(CMD_STR_MEDIAFILESTART_REQ, CMD_NUM_MEDIAFILESTART_REQ);
		generateMap(CMD_STR_MEDIAFILESTART_RSP, CMD_NUM_MEDIAFILESTART_RSP);
	}
	
	public static final String CMD_STR_MEDIAFILESTOP_REP = "mediafilestop_rep";
	
	public static final String CMD_STR_MEDIAFILESTOP_RSP = "mediafilestop_rsp";
	
	public static final int CMD_NUM_MEDIAFILESTOP_REP = 0x1008;
	
	public static final int CMD_NUM_MEDIAFILESTOP_RSP = 0x9008;
	
	static {
		generateMap(CMD_STR_MEDIAFILESTOP_REP, CMD_NUM_MEDIAFILESTOP_REP);
		generateMap(CMD_STR_MEDIAFILESTOP_RSP, CMD_NUM_MEDIAFILESTOP_RSP);
	}
	
	public static final String CMD_STR_MEDIAFILEFINISH_REQ = "mediafilefinish_req";
	
	public static final String CMD_STR_MEDIAFILEFINISH_RSP = "mediafilefinish_rsp";
	
	public static final int CMD_NUM_MEDIAFILEFINISH_REQ = 0x1009;
	
	public static final int CMD_NUM_MEDIAFILEFINISH_RSP = 0x9009;
	
	static {
		generateMap(CMD_STR_MEDIAFILEFINISH_REQ, CMD_NUM_MEDIAFILEFINISH_REQ);
		generateMap(CMD_STR_MEDIAFILEFINISH_RSP, CMD_NUM_MEDIAFILEFINISH_RSP);
	}
	
	public static final String CMD_STR_CLIENTUPDATE_RST = "clientupdate_rst";
	
	public static final String CMD_STR_CLIENTUPDATE_RSP = "clientupdate_rsp";
	
	public static final int CMD_NUM_CLIENTUPDATE_RST = 0x100a;
	
	public static final int CMD_NUM_CLIENTUPDATE_RSP = 0x900a;
	
	static {
		generateMap(CMD_STR_CLIENTUPDATE_RST, CMD_NUM_CLIENTUPDATE_RST);
		generateMap(CMD_STR_CLIENTUPDATE_RSP, CMD_NUM_CLIENTUPDATE_RSP);
	}
	
	public static final String CMD_STR_GETSVRLIST_REQ = "getsvrlist_req";
	
	public static final String CMD_STR_GETSVRLIST_RSP = "getsvrlist_rsp";
	
	public static final int CMD_NUM_GETSVRLIST_REQ = 0x100b;
	
	public static final int CMD_NUM_GETSVRLIST_RSP = 0x900b;
	
	static {
		generateMap(CMD_STR_GETSVRLIST_REQ, CMD_NUM_GETSVRLIST_REQ);
		generateMap(CMD_STR_GETSVRLIST_RSP, CMD_NUM_GETSVRLIST_RSP);
	}
	
	public final static int MSG_FORMAT_JSON = 1;

	public final static int MSG_FORMAT_BSON = 0;

	public final static String MARK_CMD = "cmd";

	public final static String MARK_BODY = "body";

	public final static String MARK_HEAD = "head";

	public static final int RESULT_SUCCESS = 0;
	
	public static void generateMap(String str, int num) {
		Str2NumCmdMap.put(str, num);
		Num2StrCmdMap.put(num, str);
	}
}
