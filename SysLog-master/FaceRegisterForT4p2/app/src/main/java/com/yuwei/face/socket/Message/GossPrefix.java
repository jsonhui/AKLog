package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

import java.io.Serializable;
import java.nio.ByteOrder;

public class GossPrefix implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -1863613937808309603L;
    private byte my_check;//计算的校验和
    
    public static final byte PACKAGE_HEAD = 0x01;	//0x01表示标记头
    public static final byte PACKAGE_END = 0x10;	//0x10表示标记尾
    public static final byte PACKAGE_SINGLE = 0x11;	//0x11表示单包
    public static final byte PACKAGE_MID = 0x00;	//0x00表示中间包
    
    /**
     * 检查消息头校验
     * @return	
     */
    public boolean checkSuccess()
    {
         return head_check == my_check;
    }
    
    private IoBuffer ioBuffer = IoBuffer.allocate(1000);
    private byte[] check_data = new byte[12];
    public GossPrefix(byte[] data)
    {
        ioBuffer.setAutoExpand(true);
        ioBuffer.put(data);
        ioBuffer.flip();
        //头标记
        head_flag = ioBuffer.get();
        //头校验
        //检验位的值等于消息ID字段到分包头/尾标记字段按位异或
        head_check = ioBuffer.get();
        ioBuffer.mark();
        //取校验和后12位
	    ioBuffer.get(check_data);
	    for(int i=0;i<check_data.length;i++)
	    {
	    	my_check ^= check_data[i];//异或计算校验和
	    }
	    ioBuffer.reset();
	    
        //消息ID
        message_id = ioBuffer.getShort();

        //设备ID
        device_id= ioBuffer.getInt();
        
        //消息流水号
        //从0开始，重发的流水号保持不变
        message_serial_num = ioBuffer.getShort();
        //消息体长度
        message_length = (short) ioBuffer.getUnsignedShort();
        //message_length = ioBuffer.getShort();
        //属性	 默认为0
        //BIT0:1:表示消息体后存在一个异或校验位
        //BIT1:1:表示需要应答
        //BIT2:1:表示重发
        property = ioBuffer.get();
        //分包头/尾标记
        //0x01表示标记头
        //0x10表示标记尾
        //0x11表示单包
        //0x00表示中间包
        fenbao_flag = ioBuffer.get();
//        ioBuffer.free();
    }

    public GossPrefix()
    {
    	setHead_flag(GossCmdConst.HEADER_FLAG);
    }

    public byte[] toBytes()
    {
        IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        ioBuffer.order(ByteOrder.BIG_ENDIAN);
        
        ioBuffer.putShort(message_id);
        ioBuffer.putInt(device_id);
        ioBuffer.putShort(message_serial_num);
        ioBuffer.putShort(message_length);
        ioBuffer.put(property);
        ioBuffer.put(fenbao_flag);
        ioBuffer.flip();
        byte bs[] = new byte[ioBuffer.remaining()];
        ioBuffer.get(bs);
        ioBuffer.free();
        return bs;
    }
    
    protected byte head_flag = 0x26;//头标记
    
    //头校验
    //检验位的值等于消息ID字段到分包头/尾标记字段按位异或
    protected byte head_check;
    
    protected short message_id;//消息ID
    
    protected int device_id;//设备ID
    
    //消息流水号
    //从0开始，重发的流水号保持不变
    protected short message_serial_num;
    
    //消息体长度
    protected short message_length;
    
    //属性	 默认为0
    //BIT0:1:表示消息体后存在一个异或校验位
    //BIT1:1:表示需要应答
    //BIT2:1:表示重发
    protected byte property;
    
    //分包头/尾标记
    //0x01表示标记头
    //0x10表示标记尾
    //0x11表示单包
    //0x00表示中间包
    protected byte fenbao_flag;
    public byte getHead_flag() {
		return head_flag;
	}

	public void setHead_flag(byte head_flag) {
		this.head_flag = head_flag;
	}

	public byte getHead_check() {
		return head_check;
	}

	public void setHead_check(byte head_check) {
		this.head_check = head_check;
	}

	public short getMessage_id() {
		return message_id;
	}

	public void setMessage_id(short message_id) {
		this.message_id = message_id;
	}

	public int getDevice_id() {
		return device_id;
	}

	public void setDevice_id(int device_id) {
		this.device_id = device_id;
	}

	public short getMessage_serial_num() {
		return message_serial_num;
	}

	public void setMessage_serial_num(short message_serial_num) {
		this.message_serial_num = message_serial_num;
	}

	public short getMessage_length() {
		return message_length;
	}

	public void setMessage_length(short message_length) {
		this.message_length = message_length;
	}

	public byte getProperty() {
		return property;
	}

	public void setProperty(byte property) {
		this.property = property;
	}

	public byte getFenbao_flag() {
		return fenbao_flag;
	}

	public void setFenbao_flag(byte fenbao_flag) {
		this.fenbao_flag = fenbao_flag;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
    public String toString()
    {
        return "GossPrefix [head_flag=" + head_flag + ", message_id=" + message_id + ", device_id="
                + String.format("%04x", device_id) + ", message_serial_num=" + message_serial_num + ", message_length=" + message_length
                + ", property=" + property + ", fenbao_flag=" + fenbao_flag  + "]";
    }
	
    public void setBodyLen(int bodyLen)
    {
        this.message_length = (short) bodyLen;
    }
}
