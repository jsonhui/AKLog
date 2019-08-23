package com.yuwei.face.socket.utils;

public class SocketConstants {

	public static final int VIDEO_FRAME_NUMBER = 4;
//	public static VideoDataProc[] mListVideoCh = new VideoDataProc[Constants.VIDEO_FRAME_NUMBER];

	public static final byte HEADER_FLAG = (byte) 0xAA;
//
//	@SuppressLint("UseSparseArrays")
//	public static final byte SINOPEC_CMD_LABEL = 0x3C;
////	public static final int SINOPEC_RECORDER = 0x60;                 //录像
//	public static final int SINOPEC_REAL_TIME_VEDIO = 0x61;          //实时视频
//	public static final int SINOPEC_HISTORY_RECORDER = 0x62;         //历史记录
//	public static final int SINOPEC_HISTORY_VEDIO = 0x63;            //播放历史视频
//	public static final int SINOPEC_VIDEO_STATE = 0x64;              //视频播放状态：播放、暂停、停止
//	public static final int SINOPEC_VIDEO_SPEED = 0x65;              //播放速度
////	public static final int SINOPEC_ROUND_DISPLAY = 0x66;            //轮循显示
//	public static final int SINOPEC_VIDEO_DISPLAY_SIZE = 0x67;       //视频显示大小
//	public static final int SINOPEC_CHECK_DVR_STATE = 0x68;          //屏查询DVR状态
//	public static final int T6A_STOP_REAL_TIME_VEDIO = 0x6A;		 //停止视频
//	public static final int SINOPEC_I_PLAY = 0x71;					 //帧播放

	public static final byte SINOPEC_CMD_LABEL = 0x3C;
	public static final int MSG_ANSWER_PASS_MSG = 0xB4;               //提问答案上传
	public static final int MSG_EVENT_PASS_MSG = 0xB1;                //事件回应

	public static final byte SINOPEC_RECORDER = (byte)0x60;                 //录像
	public static final byte REAL_TIME_VEDIO_CMD = (byte)0x61;          //实时视频
	public static final byte QUERY_HISTORY_RECORDER = (byte)0x62;         //历史记录
	public static final byte PLAY_HISTORY_VEDIO = (byte)0x63;            //播放历史视频
	public static final byte VIDEO_STATE = (byte)0x64;              //视频播放状态：播放、暂停、停止
	public static final byte SINOPEC_VIDEO_SPEED = (byte)0x65;              //播放速度
	public static final byte POLLING_DISPLAY_VIDEO = (byte)0x66;            //轮循显示
	public static final byte SINOPEC_VIDEO_DISPLAY_SIZE = (byte)0x67;       //视频显示大小
	public static final byte SINOPEC_CHECK_DVR_STATE = (byte)0x68;          //屏查询DVR状态
	public static final byte SINOPEC_I_PLAY = (byte)0x6D;					 //帧播放
	public static final byte STOP_REAL_TIME_VEDIO = (byte)0x6A;		 //停止视频
	public static final byte GET_VEDIO_FRAME_CMD = (byte)0x6B;          //实时视频
	public static final byte VIDOE_DELETE_CMD = (byte)0X6C;				 //录像SDCard容量，超容，视频删除命令
	public static final byte SINOPEC_LOAD_VIDEO = (byte)0x6E;                //视频导出命令
	
	public static final byte CALLBACK_REAL_TIME_VIDEO = (byte)0xE1;		//实时视频回调
	public static final byte CALLBACK_QUERY_HISTORY = (byte)0xE2;		//查询历史视频回调
	public static final byte CALLBACK_PLAY_HISTORY = (byte)0xE3;		//控制历史视频播放回调
	public static final byte CALLBACK_CONTROL_HISTORY = (byte)0xE4;		//控制历史视频播放回调
	public static final byte CALLBACK_HISTORY_SPEED = (byte)0xE5;		//控制历史视频播放速度回调
	public static final byte CALLBACK_POLLING_PLAY = (byte)0xE6;		//轮循显示回调
	public static final byte CALLBACK_HISTORY_FINISH = (byte)0xE9;		//历史视频播放完毕
	public static final byte CALLBACK_REAL_STOP = (byte)0xEA;		//实时视频停止播放回调
	
	public static final int TYPE_HISTORY_START = 0;
	public static final int TYPE_HISTORY_PAUSE = 1;
	public static final int TYPE_HISTORY_STOP = 2;
}
