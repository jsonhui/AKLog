package com.yuwei.face.socket.utils;

/**
 * 常量定义类
 * @author wallage
 *
 */
public class Constants {
	public static final boolean DEBUG = false;
	
	public static final String FACTORY_MODE = "*2*161#";        //工厂模式
    public static final String FACTORY_MODE_2 = "a";
    public static final String SETTING_PWD = "140509";          //从业资格证进入主菜单的密码
    public static final String SETTING_DEBUG = "*2*162#";       //开启输出调试信息密码
    
    public static final String ANCAI_PASSWORD = "*acjt*1557*";    //安彩默认密码
    public static final String XINSHIJIE_PASSWORD = "yc12782aa";  //新世界默认密码
    public static final String DEFAULT_PASSWORD = "*a135b246";    //其他默认密码

	//设置
	public static int USER_WIFI_STATUS = 0;//用户wifi状态 0:停用  1:启用
    public static int SYSTEM_WIFI_STATUS = 1;//系统wifi状态 0:停用  1:启用
	
	public final static int FUNCTION_NUM_UNKNOWN = -1;
	public final static int DEVICE_TYPE_UNKNOWN = 0;
	
	public static byte[] RegisterCmd = null;
	public static String CONFIG_INIT_BAUDRATE = null;			//初始波特率
	public static String CONFIG_DEFAULT_BAUDRATE = null; 		//默认波特率

	public static byte TimeZone = 0;//时区
	public static byte TimeZoneMin = 0;//时区分
	
	public static int SCREEN_HIGH_PX = 1;
	public static int SCREEN_NORMAL_PX = 2;
	public static int SCREEN_PX_TYPE = SCREEN_NORMAL_PX;
	
    public static final String ACTION_HIDE_NAV =  "com.pnd.hide.systemui";
    public static final String ACTION_SHOW_NAV =  "com.pnd.show.systemui";
    
    public static final String ACTION_HIDE_STAT =  "com.pnd.hide.statusbar";
    public static final String ACTION_SHOW_STAT =  "com.pnd.show.statusbar";

	public static final String T6A_FunctionNum_57009 = "57009";
	public static final String T3A_FunctionNum_57909 = "57907";
	public static final String T4_FunctionNum_57700 = "57700";
    public static final String T4_FunctionNum_57701 = "57701";
	public static final String T4_FunctionNum_57704 = "57704";
	public static final String T4_FunctionNum_57706 = "57706";
	public static final String T4_FunctionNum_57710 = "57710";
    public static String FunctionNum = T4_FunctionNum_57700;
	
	/**********************************设置模块************************************/
  	public static final int SETTING_BRIGHTNESS = 700;//设置界面实时更新亮度
  	public static final int SETTING_CARPARAM = 701;//查询车辆参数
  	public static final int SETTING_CARPARAM_IP= 702;///查询网络ip端口
  	public static final int SETTING_CARPARAM_HYIP = 703;//华银关于本机显示网络ip端口
  	public static final int SETTING_VERSION_INFO = 704;//EPU版本和MPU版本
  	public static final int SETTING_SHOWNAVI = 705;//显示导航设置
  	public static final int SETTING_CHECK_RTC = 706;//故障检测录像状态
  	public static final int SETTING_CENTERSERVICE_SYS = 707;//安全设定中显示参数
  	public static final int SETTING_SHOW = 708;
  	public static final int SETTING_HIDE = 709;
  	public static final int SETTING_VIDEOEXTRACT_RSP = 710;//视频提取
  	public static final int SETTING_CLOSEMONITOR = 711;
  	public static final int SETTING_T6ACHECK = 712;
  	public static final int SETTING_BT_DISCONNECT = 713;
  	public static final int SETTING_BT_CONNECT = 714;
  	public static final int SETTING_WIFI_APSWITCH = 715;
  	public static final int SETTING_WIFIAP_CLOSE = 716;
  	public static final int SEEINT_CREATE_AP_FAIL = 717;
  	public static final int SETTING_SET_AP_SUCCESS = 718;
  	
  	public static final String PACKAGE_GAODE_NAVI = "com.autonavi.xmgd.navigator";//高德导航
	public static final String PACKAGE_KAILIDE_NAVI = "cld.navi.mobile.mainframe";//凯立德
	public static final String PACKAGE_BAIDU_NAVI = "com.baidu.navi.hd";//百度导航
	public static final String PACKAGE_GAODE_MAP = "com.autonavi.amapauto";//高德地图车机版
	public static final int QUERY_INPUT_SIGNAL_CMD = 0x43;
	public static final int QUERY_INPUT_SIGNAL_CALL_BACK_CMD = 0x42;
	public static final int CHANGE_OUT_PUT_SIGNAL_CMD = 0x48;
	public static final int TRANSFER_CAN_DATA_CMD = 0x52;
	public static final int CONFIG_CAN_RATE_CMD = 0x53;
	public static final int CLOSE_CAN_RATE_CMD = 0x54;
	public static final int SERAIL_DATA_CMD = 0x58;
	public static final int CONFIG_RATE_RESPONSE_CMD = 0x59;
	public static final int CLOSE_SP_RATE_CONFIG_CMD = 0x60;
	public static final int QUERY_EPU_VERSION_CMD = 0x62;
	public static final int REPORT_CAN_SPEED_CMD = 0x70;
	public static final int REPORT_CAN_TURN_SPEED_CMD = 0x71;
	public static final int QUERY_MAIN_POWER_CMD = 0x68;
  	public static final int PLUSE_SPEED_CMD = 0x6A;//脉冲速度
  	public static final int LOW_VOLTAGE_ALARM_CMD = 0x6E;
  	public static final int BATTERY_LOW_VOLTAGE_ALARM_CMD = 0X82;
  	public static final int EPU_LOW_VOLTAGE_ALARM_CMD = 0x83;
  	
  	//ma data cmd type
	public static final int MA_DATA_LIMITED_SPEED = 0XC2;
	public static final int MA_DATA_OVER_SPEED = 0x63;
	public static final int MA_DATA_DRIVER_INFO = 0x8F;
	public static final int MA_DATA_TIME_AND_LOCTION = 0x4F;
	
	//map data cmd 
	public static final int MAP_DATA_TIME_AND_LOCTION = 0x4F;
	public static final int MAP_DATA_TERMINAL_0X6A = 0x6A;
	public static final int MAP_DATA_TERMINAL_0X6B = 0x6B;
	public static final int MAP_DATA_TERMINAL_TO_PND = 0x5C;
	public static final int MAP_DATA_GPU_PROTO = 0x55;
	public static final int MAP_DATA_NAVI_DATA = 0x17;  //taxi model
	public static final int MAP_DATA_INTEREST_DATA = 0x18;	//taxi model
	public static final int MAP_DATA_LINE_QUERY = 0x13;
	public static final int MAP_DATA_INTEREST_QUERY = 0x14;
	public static final int MAP_DATA_CLEAR_LINE = 0x15;
	public static final int MAP_DATA_CLEAR_INTEREST = 0x16;
	
	public static final int MAP_MSG_TOP_LIGHT = 0x13;
	
	// setting data cmd type
	public static final int SETTINGS_DATA_LIMITED_SPEED = 0XC2;
	public static final int SETTINGS_TERMINAL_SET_RSP = 0xC4;
	public static final int SETTINGS_DATA_GPU_PROTO = 0x55;
	public static final int SETTINGS_DATA_TIME_AND_LOCTION = 0x4F;
	
	//是否断电
    public static boolean IsPowerOff = false;
    public static void SetPowerOn()
	{
		Constants.IsPowerOff = false;
	}
	
    public static void SetPowerOff()
	{
		Constants.IsPowerOff = true;
	}
    
    //==================硬件版本编号=================
    public static int SBase_HARDVERSION_NULL = 0x00;
	public static int SBase_HARDVERSION_V10 = 0x10;
	public static int SBase_HARDVERSION_V11 = 0x11;
	public static int SBase_HARDVERSION_V12 = 0x12;
	public static int SBase_HARDVERSION_V13 = 0x13;
	public static int SBase_HARDVERSION_V14 = 0x15;
	public static int SBase_HARDVERSION_V20 = 0x20;
	public static int SBase_HARDVERSION_V30 = 0x30;
	public static int SBase_HARDVERSION_V40 = 0x40;
	public static int SBase_HARDVERSION_V41 = 0x41;
	public static int SBase_HARDVERSION_V42 = 0x42;
	public static int SBase_HARDVERSION_V43 = 0x43;
	public static int SBase_HARDVERSION_ERR = 0xFF;
	
	//Ic card
	public final static int IC_BLOCK_LENGTH = 32;
	public final static int IC_CAPACITY_LENGTH = 256;
		
	public final static int APP_PRO_TYPE_RTMP = 0;
	public final static int APP_PRO_TYPE_GBV808SD = 1;		// 山东标准
	public final static int APP_PRO_TYPE_GBV808SC = 2;		// 四川标准
	public final static int APP_PRO_TYPE_LOCALTRANS = 3;	// 本地传输
	public final static int APP_PRO_TYPE_GBV808SZ = 4;		// 深圳标准
	public final static int APP_PRO_TYPE_GBV808GJ = 5;		// 公交标准 
	public final static int APP_PRO_TYPE_YWTBIPC = 6;		// ipc
	public final static int APP_PRO_TYPE_GBV808BB = 7;		// 部标标准
	public final static int APP_PRO_TYPE_MAX = 8;
	
	public final static int CONN_TYPE_UDP = 0;
	public final static int CONN_TYPE_TCP = 1;
	public final static int CONN_TYPE_FTP = 2;
	
}