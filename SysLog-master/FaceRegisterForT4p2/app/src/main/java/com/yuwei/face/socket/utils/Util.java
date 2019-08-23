package com.yuwei.face.socket.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Util {

	public static String getYMDHMSBCDTime(byte[] b) {
		// TODO Auto-generated method stub
		StringBuffer buffer = new StringBuffer("20");
		for (int i = 0; i < 6; i++) {
			String link = "";
			if (i >= 0 && i <= 1) {
				link = "-";
			} else if (i == 2) {
				link = " ";
			} else if (i > 2 && i < 5) {
				link = ":";
			} else {
				link = "";
			}
			String stmp = Integer.toHexString(b[i] & 0xFF);
			buffer.append((stmp.length() == 1) ? "0" + stmp + link : stmp
					+ link);
		}
		return buffer.toString();
	}
	
	public static String getYMDBCDTime(byte[] b) {
		// TODO Auto-generated method stub
		StringBuffer buffer = new StringBuffer("20");
        for(int i=0;i<3;i++)
        {
        	String link = "";
       	 		if(i>=0&&i<=1){
       		 link = "-";
       	 		}else{
       		 link = "";
       	 		}
        	  String stmp = Integer.toHexString(b[i] & 0xFF);
        	  buffer.append((stmp.length()==1)? "0"+stmp+link : stmp+link);
        }
		return buffer.toString();
	}
	
	public static String getOthersBCD(byte[] b) {
		// TODO Auto-generated method stub
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			String stmp = Integer.toHexString(b[i] & 0xFF);
			buffer.append((stmp.length() == 1) ? "0" + stmp : stmp);
		}
		return buffer.toString();
	}
	//ASCII转化为String类型
	public static String ASCII2String(byte[] b){
	        StringBuffer s = new StringBuffer();
	        for(int i=0;i<b.length;i++){
	        	if((b[i] != 0) && (b[i] != -1)){
	        		char c = (char) b[i];
	        		s.append(c);
	        	}
	        }
		return  s.toString();
	}
	//YY:MM:DD HH:MM:SS 转化为 YYMMDDHHMMSS
	public static String timeStringFormat2String(String str) {
		   //先定义一个集合来存放分解后的字符
		   List<String> list = new ArrayList<String>();
		   String streee = "";
		   
		   for (int i = 0; i <str.length(); i++) {
		    streee = str.substring(i, i + 1);
		    list.add(streee);
		   }
		   StringBuffer strb = new StringBuffer();
		   for (int j = 0; j < list.size(); j++) {
		    String a = list.get(j).toString();
		    if (!a.equals("-")&&!a.equals(" ")&&!a.equals(":")){
		     strb.append(a);
		   }
		   }
		   return strb.toString();
		  }
	//十六进制字符串转化为byte数组
	public static byte[] hexStringToByte(String hex) {
	    int len = (hex.length() / 2);   
	    byte[] result = new byte[len];   
	    char[] achar = hex.toCharArray();   
	    for (int i = 0; i < len; i++) {   
	     int pos = i * 2;   
	     result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));   
	    }   
	    return result;   
	}  
	private static byte toByte(char c) {   
	    byte b = (byte) "0123456789ABCDEF".indexOf(c);   
	    return b;   
	}
	
	/** 
     * 字节数组的低位是整型的高字节位  
     */  
    public static byte[] int2ByteArray(int target) {  
        byte[] array = new byte[4];  
        for (int i = 0; i < 4; i++) {  
            int offSet = array.length -i -1;  
            array[i] = (byte) (target >> 8 * offSet & 0xFF);  
        }  
        return array;  
    }
    /** 
     * 注释：short到字节数组的转换！ 
     */ 
    public static byte[] shortToByte(short s) { 
    	byte[] targets = new byte[2];  
        for (int i = 0; i < 2; i++) {  
            int offset = (targets.length - 1 - i) * 8;  
            targets[i] = (byte) ((s >>> offset) & 0xff);  
        }  
        return targets; 
    }
    
    
    /**byte ascii码转16进制
	 * @param tmp
	 * @return
	 */
	public static String byteToHexString(byte[] bArray){
		String result = null;
		StringBuffer sb = new StringBuffer(bArray.length);
		try {
			for(int i=0;i<bArray.length;i++){
				String sTemp = Integer.toHexString(0xFF & bArray[i]);
				if (sTemp.length() < 2)
					sb.append(0);
				sb.append(sTemp.toUpperCase(Locale.getDefault()));
			}
			result = sb.toString();
		} catch (Exception e) {
			// TODO: handle exception
			result = null;
		}
		
		return result;
	}
		/** 
	    * @函数功能: 10进制串转为BCD码
	    * @输入参数: 10进制串
	    * @输出结果: BCD码
	    */
	public static byte[] str2Bcd(String asc) {
	    int len = asc.length();
	    int mod = len % 2;

	    if (mod != 0) {
	     asc = "0" + asc;
	     len = asc.length();
	    }

	    byte abt[] = new byte[len];
	    if (len >= 2) {
	     len = len / 2;
	    }

	    byte bbt[] = new byte[len];
	    abt = asc.getBytes();
	    int j, k;

	    for (int p = 0; p < asc.length()/2; p++) {
	     if ( (abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
	    	 j = abt[2 * p] - '0';
	     } else if ( (abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
	    	 j = abt[2 * p] - 'a' + 0x0a;
	     } else {
	    	 j = abt[2 * p] - 'A' + 0x0a;
	     }

	     if ( (abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
    	 	 k = abt[2 * p + 1] - '0';
	     } else if ( (abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
	    	 k = abt[2 * p + 1] - 'a' + 0x0a;
	     }else {
	    	 k = abt[2 * p + 1] - 'A' + 0x0a;
	     }

	     int a = (j << 4) + k;
	     byte b = (byte) a;
	     bbt[p] = b;
	    }
	    return bbt;
	}
	
	/**是否含有汉子
	 * @param str
	 * @return
	 */
	public static boolean hasgbk(String str)
	 {
	     char[] chars=str.toCharArray();
	     boolean isGB2312=false;
	     for(int i=0;i<chars.length;i++){
            byte[] bytes=(""+chars[i]).getBytes();
            if(bytes.length==2){
                int[] ints=new int[2];
                ints[0]=bytes[0]& 0xff;
                ints[1]=bytes[1]& 0xff;
                if(ints[0]>=0x81 && ints[0]<=0xFE && ints[1]>=0x40 && ints[1]<=0xFE){
                    isGB2312=true;
                    break;
                }
            }
            if(Character.isLetter(chars[i])){
           	 	isGB2312=true;
           	 	break;
            }
	     }
	     
	  return isGB2312;
	 } 
	
	/**判断是否含有字母和数字
	 * @param str
	 * @return
	 */
	public static boolean hasAlphaNum(String str){
		boolean flag = false;
		boolean result = str.matches(".*\\p{Alpha}.*");
		boolean result2 = str.matches(".*\\d+.*");
		if(result || result2)
			flag = true;
		return flag;
	}
	
	public static int byteToInt2(byte[] b) {

        int mask=0xff;
        int temp=0;
        int n=0;
        for(int i=0;i<b.length;i++){
           n<<=8;
           temp=b[i]&mask;
           n|=temp;
       }
       return n;
   }
	
	/** 
     * 将byte[2]转换成short 
     * @param b 
     * @param offset 
     * @return  
     */  
    public static short byte2Short(byte[] b, int offset){  
        return (short) (((b[offset] & 0xff) << 8) | (b[offset+1] & 0xff));  
    } 
    
    /** 
	 * 二进制字符串转byte 
	 */  
	public static byte bitToByte(String byteStr) {
	    int re = 0;   
        if (byteStr.charAt(0) == '0') {// 正数  
            re = Integer.parseInt(byteStr, 2);
        } else {// 负数  
            re = Integer.parseInt(byteStr, 2) - 256;
        }
	    return (byte) re;  
	} 
	
	//将时间格式的字符串转化四个字节的数组
	public static byte[] timeToByte(String _time){
		long time = 0;
		long time1 = 0;
		try {
			time1 = (new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2000-01-01 00:00")).getTime();
			time = (new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(_time)).getTime();
			time = (time - time1)/1000 - 8*60*60;
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		byte[] b = new byte[4];
        b[3] = (byte) ((time >> 24 ) & 0xff);
        b[2] = (byte) ((time >> 16) & 0xff);
        b[1] = (byte) ((time >> 8) & 0xff);
        b[0] = (byte) (time & 0xff);
        return b;
	}
	
	//将时间格式的字符串转化四个字节的数组 (大端模式)
	public static byte[] timeToByteBigEndian(String _time){
		long time = 0;
		long time1 = 0;
		try {
			time1 = (new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2000-01-01 00:00")).getTime();
			time = (new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(_time)).getTime();
			time = (time - time1)/1000 - 8*60*60;
		} catch (Exception e) {
		}
		byte[] b = new byte[4];
        b[0] = (byte) ((time >> 24 ) & 0xff);
        b[1] = (byte) ((time >> 16) & 0xff);
        b[2] = (byte) ((time >> 8) & 0xff);
        b[3] = (byte) (time & 0xff);
        return b;
	}
	
	
	/**将接收的小端数据反转处理后在转成Int
	 * @param num
	 * @return
	 */
	public static int convertToInt(long num){
		byte[] b = new byte[4];
        b = convertData(num);
       return byteToInt2(b);
	}
	
	/**数据高低位转化
	 * @param num
	 * @return
	 */
	public static byte[] convertData(long num){
		byte[] b = new byte[4];
		b[3] = (byte) ((num >> 24 ) & 0xff);
        b[2] = (byte) ((num >> 16) & 0xff);
        b[1] = (byte) ((num >> 8) & 0xff);
        b[0] = (byte) (num & 0xff);
        return b;
	}
	/**将时间毫秒转化成格林威治时间
	 * @param timeArray
	 * @return
	 */
	public static String convertToTime(long timeArray){
		long time1 = 0;
		long time = 0;
		try{
			time1 = (new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2000-01-01 00:00")).getTime();
			time = timeArray*1000 + time1;
		}catch(Exception ex){
		}
		
		Date now = new Date(time);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		return dateFormat.format(now);
	}
	//整形转化为字节数组
	public static byte[] int2Byte(long a) {
        byte[] b = new byte[4];
        b[0] = (byte) ((a >> 24 ) & 0xff);
        b[1] = (byte) ((a >> 16) & 0xff);
        b[2] = (byte) ((a >> 8) & 0xff);
        b[3] = (byte) (a & 0xff);
        return b;
    }
	
	//字节数组转化为整数
	public static int bytesToInt(byte[] src, int offset) {  
	    int value;    
	    value = (int) ((src[offset] & 0xFF)   
	            | ((src[offset+1] & 0xFF)<<8)   
	            | ((src[offset+2] & 0xFF)<<16)   
	            | ((src[offset+3] & 0xFF)<<24));  
	    return value;  
	}
	
	/**short小端协议转换
	 * @return
	 */
	public static short convertShort(short num){
		byte[] array = new byte[2];
		array[1] = (byte) ((num >> 8) & 0xff);
		array[0] = (byte) (num & 0xff);
		return byte2Short(array,0);
	}
	
	public static long convertStrToMillis(String timeStr) {
		long time = 0;
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timeStr));
			time = c.getTimeInMillis();
		} catch (Exception ex) {
		}
		return time;
	}
	
	/**格式化时间 ：yyyy-MM-dd HH:mm:ss的格式
	 * @param millTime
	 * @return
	 */
	public static String converFormatTimeString(String timeStr){
		long mill = convertStrToMillis(timeStr);
		Date now = new Date(mill);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(now);
	}
	
	/**将毫秒转换成指定格式的时间
	 * @param mills
	 * @return
	 */
	public static String converMillsToFormatString(long mills){
		Date now = new Date(mills);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(now);
	}
	/**获取较早的一个时间
	 * @param timeList
	 * @return
	 */
	public static long getTheEarlyerTime(List<String> timeList){
		if(timeList.size() == 0)
			return 0;
		long time = convertStrToMillis(timeList.get(0));
		for(int i = 0;i<timeList.size();i++){
			long tmpTime = convertStrToMillis(timeList.get(i));
			if(tmpTime<time)
				time = tmpTime;
		}
		return time;
	}
}