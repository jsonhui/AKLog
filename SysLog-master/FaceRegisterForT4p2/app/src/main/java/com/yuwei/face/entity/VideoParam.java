package com.yuwei.face.entity;

/**
 * 存储视频数据实体
 */
public class VideoParam {
     private int previewWidth;//预览宽度
     private int previewHeight;//预览高度
     private byte[] videoData;//视频数据，原始的YUV数据

     public int getPreviewWidth() {
          return previewWidth;
     }

     public void setPreviewWidth(int previewWidth) {
          this.previewWidth = previewWidth;
     }

     public int getPreviewHeight() {
          return previewHeight;
     }

     public void setPreviewHeight(int previewHeight) {
          this.previewHeight = previewHeight;
     }

     public byte[] getVideoData() {
          return videoData;
     }

     public void setVideoData(byte[] videoData) {
          this.videoData = videoData;
     }
}
