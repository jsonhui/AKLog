package com.yuwei.face.socket.Message;

import org.apache.mina.core.buffer.IoBuffer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Rtpdatas implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = -6721351656177904054L;
    
    private GossPrefix gossPrefix;
    
    
    public void fromBinaryData(byte[] data)
    {
        IoBuffer ioBuffer = IoBuffer.allocate(data.length);
        ioBuffer.setAutoExpand(true);
        ioBuffer.put(data);
        ioBuffer.flip();
        
        int remaining = data.length;//ioBuffer.remaining();
		//int startPosition = ioBuffer.position();
        if(gossPrefix != null)
        {
        	// int 设备ID
    		termid = gossPrefix.getDevice_id();
    		// byte 分包头/尾标记
    		/*
    		 * 0x01表示标记头
    		 * 0x10表示标记尾
    		 * 0x11 表示单包
    		 * 0x00 表示中间包
    		 */
    		hfflag = gossPrefix.getFenbao_flag();
        }
       
        //传输ID
        ioBuffer.getInt();
        // int 节拍内序列号
     	beatnum = ioBuffer.getShort();
     	// int 历史查询序列号
 		historynum = ioBuffer.get();
 		// byte 通道号/帧类型
		mchid = ioBuffer.get();
		/*
		 * int 通道号 1,2,3,4
		 */
		b03 = mchid & 0x0f;
		/*
		 * int 帧类型 1:视频,2:音频,3:Info(附加信息),4:图片,5:GPS信息
		 */
		b47 = (mchid & 0xf0) >> 4;
		// int 媒体包序号 从0开始
		mnum = ioBuffer.getInt();
		// int 媒体格式 1:H264 2:MPEG4 3:WMV 4:AMR 5:ADPCM  6:AAC  7:JPEG  8:G..711  9:G..726
		mformat = ioBuffer.get();
		// int 事件项编码
		/*
		 * 1:平台下发指令实时媒体 
		 * 2:定时动作 
		 * 3:报警触发 
		 * 4: 平台下发指令历史媒体
		 */
		encoding = ioBuffer.get();
		// int 分辨率/频率
		rate = ioBuffer.get();
		// byte[] 内容
		byte _mdata[] = new byte[remaining - 15];
		ioBuffer.get(_mdata);
		setNewMdata(_mdata);
		_mdata = null;
        ioBuffer.free();
    }
    
    private byte headflag;
    private byte[] reserver_1;
    private int termid;
    private int serial;
    private byte[] reserver_2;
    private byte hfflag;
    private int beatid;
    private int beatnum;
    private int historynum;
    private byte mchid;
    private int b03;
    private int b47;
    private int mnum;
    private int mformat;
    private int encoding;
    private int rate;
    private byte[] mdata;
    private List<byte[]> mdatas = new ArrayList<byte[]>();
    
    public byte getHeadFlag() {
    	return this.headflag;
    }
    
    public void setHeadFlag(byte headflag) {
    	this.headflag = headflag;
    }

    public byte[] getReserver_1() {
    	return this.reserver_1;
    }
    
    public void setReserver_1(byte[] reserver_1) {
    	this.reserver_1 = reserver_1;
    }

    public int getTermid() {
    	return this.termid;
    }
    
    public void setTermid(int termid) {
    	this.termid = termid;
    }
    
    public int getSerial() {
    	return this.serial;
    }
    
    public void setSerial(int serial) {
    	this.serial = serial;
    }
    
    public byte[] getReserver_2() {
    	return this.reserver_2;
    }
    
    public void setReserver_2(byte[] reserver_2) {
    	this.reserver_2 = reserver_2;
    }
    
    public GossPrefix getGossPrefix() {
		return gossPrefix;
	}

	public void setGossPrefix(GossPrefix gossPrefix) {
		this.gossPrefix = gossPrefix;
	}

	public byte getHfflag() {
    	return this.hfflag;
    }
    
    public void setHfflag(byte hfflag) {
    	this.hfflag = hfflag;
    }

    public int getBeatid() {
    	return this.beatid;
    }
    
    public void setBeatid(int beatid) {
    	this.beatid = beatid;
    }
    
    public int getBeatnum() {
    	return this.beatnum;
    }
    
    public void setBeatnum(int beatnum) {
    	this.beatnum = beatnum;
    }
    
    public int getHistoryNum() {
    	return this.historynum;
    }
    
    public void setHistoryNum(int historynum) {
    	this.historynum = historynum;
    }
    
    public byte getMchid() {
    	return this.mchid;
    }
    
    public void setMchid(byte mchid) {
    	this.mchid = mchid;
    }
    
    public int getB03() {
    	return this.b03;
    }
    
    public void setB03(int b03) {
    	this.b03 = b03;
    }
    
    public int getB47() {
    	return this.b47;
    }
    
    public void setB47(int b47) {
    	this.b47 = b47;
    }
    
    public int getMnum() {
    	return this.mnum;
    }
    
    public void setMnum(int mnum) {
    	this.mnum = mnum;
    }
    
    public int getMformat() {
    	return this.mformat;
    }
    
    public void setMformat(int mformat) {
    	this.mformat = mformat;
    }
    
    public int getEncoding() {
    	return this.encoding;
    }
    
    public void setEncoding(int encoding) {
    	this.encoding = encoding;
    }
    
    public int getRate() {
    	return this.rate;
    }
    
    public void setRate(int rate) {
    	this.rate = rate;
    }
    
    public byte[] getMdata() {
    	return this.mdata;
    }
    
    public void setMdata(byte[] _mdata) {
    	this.mdata = _mdata;
    }
    
    public void setNewMdata(byte[] _mdata) {
    	this.mdata = _mdata;
    }
    
    private int mlong;
    private int mtime;
    private byte[] md;
    private int data_len = 0;//计算媒体数据总长度
    
    public byte[] mdataFromBinary(byte[] mdata) {
    	IoBuffer ioBuffers = IoBuffer.allocate(mdata.length);
        ioBuffers.setAutoExpand(true);
        ioBuffers.put(mdata);
        ioBuffers.flip();

        while(ioBuffers.hasRemaining())
        {
        	// mlong int 媒体内容长度
    		mlong = ioBuffers.getInt();
    		setMlong(mlong);
    		
    		// mtime int 时间戳 ms
    		mtime = ioBuffers.getInt();
    		setMtime(mtime);
    		
    		// md byte[] 媒体数据
    		md = new byte[mlong];
    		ioBuffers.get(md);
    		mdatas.add(md);
    		data_len += md.length;
        }
		
		ioBuffers.free();
		return md;
    }
    
    public int getMlong() {
    	return this.mlong;
    }
    
    public void setMlong(int mlong) {
    	this.mlong = mlong;
    }
    
    public int getMtime() {
    	return this.mtime;
    }
    
    public void setMtime(int mtime) {
    	this.mtime = mtime;
    }
    
    public byte[] getMd() {
    	return this.md;
    }
    
    public void setMd(byte[] md) {
    	this.md = md;
    }
    
}
