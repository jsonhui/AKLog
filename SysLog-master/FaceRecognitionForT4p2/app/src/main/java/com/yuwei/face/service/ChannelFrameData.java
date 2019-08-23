package com.yuwei.face.service;

import java.io.Serializable;

public class ChannelFrameData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int channel;
	public byte[] data;
	
	public ChannelFrameData(int channel, byte[] data) {
		super();
		this.channel = channel;
		this.data = data;
	}
	
	

}
