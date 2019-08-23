package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * 协议标识0x3C
 * 向VA通过socket发送的视频指令结构体
 * @author wallage
 *
 */
public class VideoMaCmd extends BaseCommand{
	public static final String TAG = "VideoMaCmd";

	private static final long serialVersionUID = 1L;

	byte maCmd;
	byte[] mData;
	byte[] originBytes;
	
	public VideoMaCmd(byte[] bs){
		originBytes = bs;
		doDataDecode(bs);
	}
	
	public VideoMaCmd(byte cmd, byte[] mData) {
		super();
		this.maCmd = cmd;
		this.mData = mData;
	}

	public byte[] getOriginBytes() {
		return originBytes;
	}

	public void setOriginBytes(byte[] originBytes) {
		this.originBytes = originBytes;
	}

	public byte getMaCmd() {
		return maCmd;
	}

	public void setMaCmd(byte maCmd) {
		this.maCmd = maCmd;
	}

	public byte[] getmData() {
		return mData;
	}

	public void setmData(byte[] mData) {
		this.mData = mData;
	}

	@Override
	protected void doDataDecode(byte[] bs) {
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        ioBuffer.put(bs);
        ioBuffer.flip();
        //head 0xAA
        ioBuffer.get();
        //msg lengh
        ioBuffer.get();
        
        ioBuffer.get(); //0x3C
        ioBuffer.get(); //0x5A
        ioBuffer.get(); //0xA5
        
        maCmd = ioBuffer.get();
        
        byte[] tmp = new byte[7];
        ioBuffer.get(tmp); //7 empty byte
        
        int length = ioBuffer.get();
        ioBuffer.get(); //0x00
        
    	mData = new byte[length];
    	ioBuffer.get(mData);
        ioBuffer.free();
	}



	@Override
	public byte[] doDataEncode() {
		byte[] bb = null;
		if(mData == null)
			bb = new byte[16];
		else
			bb = new byte[mData.length + 16];
		int pos = 0;
		bb[pos++] = (byte) 0xAA;
		pos++;
		bb[pos++] = 0x3C;
		bb[pos++] = 0x5A;
		bb[pos++] = (byte) 0xA5;
		bb[pos++] = maCmd;
		bb[pos++] = 0x00;
		pos += 6;
		//内容长度
		if(mData != null){
			bb[pos++] = (byte) mData.length;
			bb[pos++] = 0x00;
		}else{
			bb[pos++] = 0x00;
			bb[pos++] = 0x00;
		}
		
		if(mData != null && mData.length>0){
			for(int i=0;i<mData.length;i++)
			{
				bb[pos++] = mData[i];
			}
		}
		bb[1] = (byte) (pos - 2);
		byte check = 0x00;
		for(int i = 0;i < pos;i++)
		{
			check ^= bb[i];
		}
		bb[pos++] = check;
		return bb;
	}
}
