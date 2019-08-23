package com.yuwei.face.socket.utils;

public class Utility {
	/** 
     * 将short转成byte[2] 
     * @param a 
     * @return 
     */  
    public static byte[] short2Byte(short a){  
        byte[] b = new byte[2];  
          
        b[0] = (byte) (a >> 8);  
        b[1] = (byte) (a);  
          
        return b;  
    }  
      
    /** 
     * 将short转成byte[2] 
     * @param a 
     * @param b 
     * @param offset b中的偏移量 
     */  
    public static void short2Byte(short a, byte[] b, int offset){  
        b[offset] = (byte) (a >> 8);  
        b[offset+1] = (byte) (a);  
    }  
      
    /** 
     * 将byte[2]转换成short 
     * @param b 
     * @return 
     */  
    public static short byte2Short(byte[] b){  
        return (short) (((b[0] & 0xff) << 8) | (b[1] & 0xff));  
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
     * long转byte[8] 
     *  
     * @param a 
     * @param b 
     * @param offset 
     *            b的偏移量 
     */  
    public static void long2Byte(long a, byte[] b, int offset) {          
        b[offset + 0] = (byte) (a >> 56);  
        b[offset + 1] = (byte) (a >> 48);  
        b[offset + 2] = (byte) (a >> 40);  
        b[offset + 3] = (byte) (a >> 32);  
  
        b[offset + 4] = (byte) (a >> 24);  
        b[offset + 5] = (byte) (a >> 16);  
        b[offset + 6] = (byte) (a >> 8);  
        b[offset + 7] = (byte) (a);  
    }  
  
    /** 
     * byte[8]转long 
     *  
     * @param b 
     * @param offset 
     *            b的偏移量 
     * @return 
     */  
    public static long byte2Long(byte[] b, int offset) {  
         return ((((long) b[offset + 0] & 0xff) << 56)  
         | (((long) b[offset + 1] & 0xff) << 48)  
         | (((long) b[offset + 2] & 0xff) << 40)  
         | (((long) b[offset + 3] & 0xff) << 32)  
           
         | (((long) b[offset + 4] & 0xff) << 24)  
         | (((long) b[offset + 5] & 0xff) << 16)  
         | (((long) b[offset + 6] & 0xff) << 8)  
         | (((long) b[offset + 7] & 0xff) << 0));  
    }  
  
    /** 
     * byte[8]转long 
     *  
     * @param b 
     * @return 
     */  
    public static long byte2Long(byte[] b) {  
         return  
         ((b[0]&0xff)<<56)|  
         ((b[1]&0xff)<<48)|  
         ((b[2]&0xff)<<40)|  
         ((b[3]&0xff)<<32)|  
          
         ((b[4]&0xff)<<24)|  
         ((b[5]&0xff)<<16)|  
         ((b[6]&0xff)<<8)|  
         (b[7]&0xff);  
    }  
  
    /** 
     * long转byte[8] 
     *  
     * @param a 
     * @return 
     */  
    public static byte[] long2Byte(long a) {  
        byte[] b = new byte[4 * 2];  
  
        b[0] = (byte) (a >> 56);  
        b[1] = (byte) (a >> 48);  
        b[2] = (byte) (a >> 40);  
        b[3] = (byte) (a >> 32);  
          
        b[4] = (byte) (a >> 24);  
        b[5] = (byte) (a >> 16);  
        b[6] = (byte) (a >> 8);  
        b[7] = (byte) (a >> 0);  
  
        return b;  
    }  
  
    /** 
     * byte数组转int 
     *  
     * @param b 
     * @return 
     */  
    public static int byte2Int(byte[] b) {  
        return ((b[0] & 0xff) << 24) | ((b[1] & 0xff) << 16)  
                | ((b[2] & 0xff) << 8) | (b[3] & 0xff);  
    }  
  
    /** 
     * byte数组转int 
     *  
     * @param b 
     * @param offset 
     * @return 
     */  
    public static int byte2Int(byte[] b, int offset) {  
        return ((b[offset++] & 0xff) << 24) | ((b[offset++] & 0xff) << 16)  
                | ((b[offset++] & 0xff) << 8) | (b[offset++] & 0xff);  
    }  
  
    /** 
     * int转byte数组 
     *  
     * @param a 
     * @return 
     */  
    public static byte[] int2Byte(int a) {  
        byte[] b = new byte[4];  
        b[0] = (byte) (a >> 24);  
        b[1] = (byte) (a >> 16);  
        b[2] = (byte) (a >> 8);  
        b[3] = (byte) (a);  
  
        return b;  
    }  
  
    /** 
     * int转byte数组 
     *  
     * @param a 
     * @param b 
     * @param offset 
     * @return 
     */  
    public static void int2Byte(int a, byte[] b, int offset) {        
        b[offset++] = (byte) (a >> 24);  
        b[offset++] = (byte) (a >> 16);  
        b[offset++] = (byte) (a >> 8);  
        b[offset++] = (byte) (a);  
    } 
    
    public static void printf_bytes(byte[] _data,String tag)
	{
		String tmp = getHexBytes(_data);
	}
    
    public static String getHexBytes(byte[] _data){
    	if (_data == null) return null;
    	String tmp = "";
		for(int i = 0; i < _data.length; i++)
		{
			tmp += toHex(_data[i]);
			tmp += " ";
		}
		return tmp;
    }
    
    /**
     * 将字节转换成 16 进制字符串
     *
     * @param b byte
     * @return String
     */
    public static String toHex(byte b) {
        Integer I = new Integer((((int) b) << 24) >>> 24);
        int i = I.intValue();

        if (i < (byte) 16) {
            return "0" + Integer.toString(i, 16);
        } else {
            return Integer.toString(i, 16);
        }
    }
    
    /**将byte转换为一个长度为8的byte数组，数组每个值代表bit 
     * @param b
     * @return
     */
    public static byte[] byte2Array(byte b) {  
        byte[] array = new byte[8];  
        for (int i = 0; i < 8; i++) {  
            array[i] = (byte)(b & 1);  
            b = (byte) (b >> 1);  
        }  
        return array;  
    }
    
    /**Bit转Byte 
     * @param byteStr
     * @return
     */
    public static byte bitToByte(String byteStr) {
	    int re, len;  
	    if (null == byteStr) {  
	        return 0;  
	    }  
	    len = byteStr.length();  
	    if (len != 4 && len != 8) {  
	        return 0;  
	    }  
	    if (len == 8) {// 8 bit处理  
	        if (byteStr.charAt(0) == '0') {// 正数  
	            re = Integer.parseInt(byteStr, 2);
	        } else {// 负数  
	            re = Integer.parseInt(byteStr, 2) - 256;
	        }  
	    } else {//4 bit处理  
	        re = Integer.parseInt(byteStr, 2);
	    }  
	    return (byte) re;  
	}
}
