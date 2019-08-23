package com.yuwei.face.socket.Message;

import com.yuwei.face.socket.utils.Utility;

import org.apache.mina.core.buffer.IoBuffer;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class TemSetConfigReq0x000F extends BaseCommand {

	//0x01
	private static final byte VBR = 0;
	//0x02
	private static final short RATE = 64;
	//0x03
	private static final byte RESOLUTION = 0;
	//0x04
	private static final byte I_INTERVAL = 10;
	//0x05
	private static final byte INPUT_FRATE = 0;
	//0x06
	private static final byte TARGET_FRATE = 0;
	//0x07
	private static final byte[] SHUI_YIN = new byte[7];
	
	//0x10
	private static final byte CONNECT_TYPE = 1;
	private static final String IP = "172.16.1.188";
	private static final short PORT = 9988;
	private static final short CONTECT_TIME = 0xFF;
	private static final short HB_INTERVAL = 15;
	private static final short MAX_TCP_PACK = 0;
	
	//0x11
	private static final byte MAX_PROTOCOL_FRAME = 10;
	
	//0x12
	private static final byte NET_STATUE = 20;
	private static final short NET_STRENGTH = 1;
	
	//0x13
	private static final int MEDIA_KIT_NO = 0;
	private static final short IFRAMES = 0;
	private static final short PFRAMES = 0;
	private static final int TIME_INTERVAL = 0;
	
	//0x14
	private static final short SETTING = 0;
	
	//0x20
	private static final short GPS_UPLOAD_INTERVAL = 0;
	
	//0x30
	private static final byte ADD_0xEF = 0;
	
	//0x40
	private static final int MEDIAID_H = 0;
	private static final int MEDIAID_L = 0;
	private static final short MEDIA_START_OFFSET = 0;
	
	//0x70
	private static final short AVE_TRANS_RATE = 20;
	private static final short REALTIME_TRANS_RATE = 20;
	
	//0x71
	private static final byte[] TEM_STATUS = new byte[8];
	
	//0x72
	private static final int DAY_FLOW = 0;
	private static final int MONTH_FLOW = 0;
	
	//0x74
	private static final byte CH_TYPE = 0;
	private static int TIME_FLOW = 0;
	private static short TIME_LENGTH = 0;
	
	//0x75
	private static final int MEDIA_WORK_ID = 0;
	private static final short MEDIA_CODE = 0;
	
	
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4014404760194495614L;
	//附加参数总数
	private byte addtion_count;
	//附加参数列表
	private List<AddtionData> addtion_datas;

	public TemSetConfigReq0x000F()
	{
		addtion_datas = new ArrayList<AddtionData>();
		AddtionData a1 = new AddtionData();
		a1.setID((byte)0x01);
		a1.setLength((byte)1);
		a1.setDatas(new byte[]{VBR});
		addtion_datas.add(a1);
		
		AddtionData a2 = new AddtionData();
		a2.setID((byte)0x02);
		a2.setLength((byte)2);
		a2.setDatas(Utility.short2Byte(RATE));
		addtion_datas.add(a2);
		
		AddtionData a3 = new AddtionData();
		a3.setID((byte)0x03);
		a3.setLength((byte)1);
		a3.setDatas(new byte[]{RESOLUTION});
		addtion_datas.add(a3);
		
		AddtionData a4 = new AddtionData();
		a4.setID((byte)0x04);
		a4.setLength((byte)1);
		a4.setDatas(new byte[]{I_INTERVAL});
		addtion_datas.add(a4);
		
		AddtionData a5 = new AddtionData();
		a5.setID((byte)0x05);
		a5.setLength((byte)1);
		a5.setDatas(new byte[]{INPUT_FRATE});
		addtion_datas.add(a5);
		
		AddtionData a6 = new AddtionData();
		a6.setID((byte)0x06);
		a6.setLength((byte)1);
		a6.setDatas(new byte[]{TARGET_FRATE});
		addtion_datas.add(a6);
		
		AddtionData a7 = new AddtionData();
		a7.setID((byte)0x07);
		a7.setLength((byte)7);
		a7.setDatas(SHUI_YIN);
		addtion_datas.add(a7);
		
		AddtionData a10 = new AddtionData();
		byte[] data_0x10 = getData0x10();
		a10.setID((byte)0x10);
		a10.setLength((byte)data_0x10.length);
		a10.setDatas(data_0x10);
		addtion_datas.add(a10);
		
		
		AddtionData a11 = new AddtionData();
		a11.setID((byte)0x11);
		a11.setLength((byte)1);
		a11.setDatas(new byte[]{MAX_PROTOCOL_FRAME});
		addtion_datas.add(a11);
		
		AddtionData a12 = new AddtionData();
		byte[] data_0x12 = getData0x12();
		a12.setID((byte)0x12);
		a12.setLength((byte)data_0x12.length);
		a12.setDatas(data_0x12);
		addtion_datas.add(a12);
		
		AddtionData a13 = new AddtionData();
		byte[] data_0x13 = getData0x13();
		a13.setID((byte)0x13);
		a13.setLength((byte)data_0x13.length);
		a13.setDatas(data_0x13);
		addtion_datas.add(a13);
		
		AddtionData a14 = new AddtionData();
		a14.setID((byte)0x14);
		a14.setLength((byte)2);
		a14.setDatas(Utility.short2Byte(SETTING));
		addtion_datas.add(a14);
		
		AddtionData a20 = new AddtionData();
		a20.setID((byte)0x20);
		a20.setLength((byte)2);
		a20.setDatas(Utility.short2Byte(GPS_UPLOAD_INTERVAL));
		addtion_datas.add(a20);
		
		AddtionData a30 = new AddtionData();
		a30.setID((byte)0x30);
		a30.setLength((byte)1);
		a30.setDatas(new byte[]{ADD_0xEF});
		addtion_datas.add(a30);
		
		AddtionData a40 = new AddtionData();
		byte[] data_0x40 = getData0x40();
		a40.setID((byte)0x40);
		a40.setLength((byte)data_0x40.length);
		a40.setDatas(data_0x40);
		addtion_datas.add(a40);
		
		AddtionData a70 = new AddtionData();
		byte[] data_0x70 = getData0x70();
		a70.setID((byte)0x70);
		a70.setLength((byte)data_0x70.length);
		a70.setDatas(data_0x70);
		addtion_datas.add(a70);
		
		AddtionData a71 = new AddtionData();
		a71.setID((byte)0x71);
		a71.setLength((byte)TEM_STATUS.length);
		a71.setDatas(TEM_STATUS);
		addtion_datas.add(a71);
		
		AddtionData a72 = new AddtionData();
		byte[] data_0x72 = getData0x72();
		a72.setID((byte)0x72);
		a72.setLength((byte)data_0x72.length);
		a72.setDatas(data_0x72);
		addtion_datas.add(a72);
		
		AddtionData a74 = new AddtionData();
		byte[] data_0x74 = getData0x74();
		a74.setID((byte)0x74);
		a74.setLength((byte)data_0x74.length);
		a74.setDatas(data_0x74);
		addtion_datas.add(a74);
		
		AddtionData a75 = new AddtionData();
		byte[] data_0x75 = getData0x75();
		a75.setID((byte)0x75);
		a75.setLength((byte)data_0x75.length);
		a75.setDatas(data_0x75);
		addtion_datas.add(a75);
		
		addtion_count = (byte) addtion_datas.size();
	}
	
	private byte[] getData0x10()
	{
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        
        ioBuffer.put(CONNECT_TYPE);
        ioBuffer.putInt(ipToInt(IP));
        ioBuffer.putShort(PORT);
        ioBuffer.putShort(CONTECT_TIME);
        ioBuffer.putShort(HB_INTERVAL);
        ioBuffer.putShort(MAX_TCP_PACK);
        
        ioBuffer.flip();
        byte bs[] = new byte[ioBuffer.remaining()];
        ioBuffer.get(bs);
        ioBuffer.free();
        return bs;
	}
	
	private byte[] getData0x12()
	{
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        
        ioBuffer.put(NET_STATUE);
        ioBuffer.putShort(NET_STRENGTH);
        
        ioBuffer.flip();
        byte bs[] = new byte[ioBuffer.remaining()];
        ioBuffer.get(bs);
        ioBuffer.free();
        return bs;
	}
	
	private byte[] getData0x13()
	{
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        
        ioBuffer.putInt(MEDIA_KIT_NO);
        ioBuffer.putShort(IFRAMES);
        ioBuffer.putShort(PFRAMES);
        ioBuffer.putInt(TIME_INTERVAL);
        
        ioBuffer.flip();
        byte bs[] = new byte[ioBuffer.remaining()];
        ioBuffer.get(bs);
        ioBuffer.free();
        return bs;
	}
	private byte[] getData0x40()
	{
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        
        ioBuffer.putInt(MEDIAID_H);
        ioBuffer.putInt(MEDIAID_L);
        ioBuffer.putShort(MEDIA_START_OFFSET);
        
        ioBuffer.flip();
        byte bs[] = new byte[ioBuffer.remaining()];
        ioBuffer.get(bs);
        ioBuffer.free();
        return bs;
	}
	
	private byte[] getData0x70()
	{
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        
        ioBuffer.putShort(AVE_TRANS_RATE);
        ioBuffer.putShort(REALTIME_TRANS_RATE);
        
        ioBuffer.flip();
        byte bs[] = new byte[ioBuffer.remaining()];
        ioBuffer.get(bs);
        ioBuffer.free();
        return bs;
	}
	
	private byte[] getData0x72()
	{
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        
        ioBuffer.putInt(DAY_FLOW);
        ioBuffer.putInt(MONTH_FLOW);
        
        ioBuffer.flip();
        byte bs[] = new byte[ioBuffer.remaining()];
        ioBuffer.get(bs);
        ioBuffer.free();
        return bs;
	}
	
	private byte[] getData0x74()
	{
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        
        ioBuffer.put(CH_TYPE);
        ioBuffer.putInt(TIME_FLOW);
        ioBuffer.putShort(TIME_LENGTH);
        
        ioBuffer.flip();
        byte bs[] = new byte[ioBuffer.remaining()];
        ioBuffer.get(bs);
        ioBuffer.free();
        return bs;
	}
	
	private byte[] getData0x75()
	{
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        
        ioBuffer.putInt(MEDIA_WORK_ID);
        ioBuffer.putShort(MEDIA_CODE);
        
        ioBuffer.flip();
        byte bs[] = new byte[ioBuffer.remaining()];
        ioBuffer.get(bs);
        ioBuffer.free();
        return bs;
	}
	

	@Override
	public byte[] doDataEncode() {
		// TODO Auto-generated method stub
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        
        ioBuffer.put(addtion_count);
        for(AddtionData data:addtion_datas)
        {
        	ioBuffer.put(data.toData());
        }
        
        ioBuffer.flip();
        byte bs[] = new byte[ioBuffer.remaining()];
        ioBuffer.get(bs);
        ioBuffer.free();
        return bs;
	}
	
	 /**
     * 把IP地址转化为int
     * @param ipAddr
     * @return int
     */
    public static int ipToInt(String ipAddr) {
        try {
            return Utility.byte2Int(ipToBytesByInet(ipAddr));
        } catch (Exception e) {
            throw new IllegalArgumentException(ipAddr + " is invalid IP");
        }
    }
    
    /**
     * 把IP地址转化为字节数组
     * @param ipAddr
     * @return byte[]
     */
    public static byte[] ipToBytesByInet(String ipAddr) {
        try {
            return InetAddress.getByName(ipAddr).getAddress();
        } catch (Exception e) {
            throw new IllegalArgumentException(ipAddr + " is invalid IP");
        }
    }

	public byte getAddtion_count() {
		return addtion_count;
	}

	public void setAddtion_count(byte addtion_count) {
		this.addtion_count = addtion_count;
	}

}
