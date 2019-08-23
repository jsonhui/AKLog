package com.yuwei.camera.occlusion;

import android.graphics.Bitmap;

public class OcclusionResultModel {
	public Bitmap bitmap;
	public int count;
	public int channel;

	public OcclusionResultModel(Bitmap bitmap,int channel) {
		super();
		this.bitmap = bitmap;
		this.channel = channel;
	}

}
