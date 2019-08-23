package com.yuwei.face.play;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.yuwei.face.callback.ReceiveYuvFrameCallBack;
import com.yuwei.face.socket.utils.Utility;

import org.apache.mina.core.buffer.IoBuffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * H264视频播放器
 * 仅适用于V3摄像头视频流
 * @author wallage
 *
 */
public class H264Player{

	public enum PlayState {
		STOPED, PLAYING, RELEASED
	};

	private FileOutputStream fout;
	private FileOutputStream afout;

	public static final int MSG_INIT = 1;
	public static final int MSG_DATA = 2;

	public static final String TAG = "H264Player";

	private final static String MIME_TYPE = "video/avc"; // H.264 Advanced Video
	public final static int VIDEO_WIDTH = 300;
	public final static int VIDEO_HEIGHT = 300;
	private static final boolean VERBOSE = false;
	private static final int COLOR_FormatI420 = 1;
	private static final int COLOR_FormatNV21 = 2;
	private MediaCodec mCodec;
	private Surface mSurface;
	public PlayState state;

	private AudioThread audioThread;
    private HandlerThread myHandlerThread ;
	PlayerHandler mHandler;
	private int mStartTime = 0;
	List<AudioData> audioDataList = new ArrayList<AudioData>();
	private final String t6TFPath = "/mnt/sdcard2/";

	public H264Player() {
		myHandlerThread = new HandlerThread("H264Player") ;
        myHandlerThread.start();
        
        mHandler = new PlayerHandler(myHandlerThread.getLooper());
        state = PlayState.STOPED;
        
//        audioThread = new AudioThread();
	}

	private void closeFile(){
		try{
//			fout.flush();
//			fout.close();
//			afout.flush();
//			afout.close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void release() {
		//Log.d(TAG, "---->>H264Player release");
		state = PlayState.RELEASED;
		closeFile();
		myHandlerThread.quit();
		try {
			if(mSurface != null){
				mSurface.release();
			}
			if (mCodec != null){
				mCodec.release();
			}
			if(audioThread != null){
				audioThread.release();
				audioThread.interrupt();
				audioThread.join(500);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public void initDecoder(Surface surface) {
		//Log.d(TAG, "---->>initDecoder");
		mSurface = surface;
		mHandler.obtainMessage(MSG_INIT).sendToTarget();
//		mHandler.sendEmptyMessageDelayed(MSG_INIT, 3000);
	}

	@SuppressLint("NewApi")
	public void handleInitDecoder() {
		if (state == PlayState.PLAYING) {
			return;
		}
		try {
			mCodec = MediaCodec.createDecoderByType(MIME_TYPE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, VIDEO_WIDTH, VIDEO_HEIGHT);
		mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
		mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
		mCodec.configure(mediaFormat, mSurface, null, 0);
		mCodec.start();
		state = PlayState.PLAYING;
	}

	public void reConfigure() {
		if (mCodec != null) {
			MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
					VIDEO_WIDTH, VIDEO_HEIGHT);
			mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
			mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
			mCodec.configure(mediaFormat, mSurface, null, 0);
			mCodec.start();
			state = PlayState.PLAYING;
		}
	}

	private static boolean isImageFormatSupported(Image image) {
		int format = image.getFormat();
		switch (format) {
			case ImageFormat.YUV_420_888:
			case ImageFormat.NV21:
			case ImageFormat.YV12:
				return true;
		}
		return false;
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static byte[] getDataFromImage(Image image, int colorFormat) {
		if (colorFormat != COLOR_FormatI420 && colorFormat != COLOR_FormatNV21) {
			throw new IllegalArgumentException("only support COLOR_FormatI420 " + "and COLOR_FormatNV21");
		}
		if (!isImageFormatSupported(image)) {
			throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
		}
		Rect crop = image.getCropRect();
		int format = image.getFormat();
		int width = crop.width();
		int height = crop.height();
		Image.Plane[] planes = image.getPlanes();
		byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
		byte[] rowData = new byte[planes[0].getRowStride()];
		if (VERBOSE) Log.v(TAG, "get data from " + planes.length + " planes");
		int channelOffset = 0;
		int outputStride = 1;
		for (int i = 0; i < planes.length; i++) {
			switch (i) {
				case 0:
					channelOffset = 0;
					outputStride = 1;
					break;
				case 1:
					if (colorFormat == COLOR_FormatI420) {
						channelOffset = width * height;
						outputStride = 1;
					} else if (colorFormat == COLOR_FormatNV21) {
						channelOffset = width * height + 1;
						outputStride = 2;
					}
					break;
				case 2:
					if (colorFormat == COLOR_FormatI420) {
						channelOffset = (int) (width * height * 1.25);
						outputStride = 1;
					} else if (colorFormat == COLOR_FormatNV21) {
						channelOffset = width * height;
						outputStride = 2;
					}
					break;
			}
			ByteBuffer buffer = planes[i].getBuffer();
			int rowStride = planes[i].getRowStride();
			int pixelStride = planes[i].getPixelStride();
			if (VERBOSE) {
				Log.v(TAG, "pixelStride " + pixelStride);
				Log.v(TAG, "rowStride " + rowStride);
				Log.v(TAG, "width " + width);
				Log.v(TAG, "height " + height);
				Log.v(TAG, "buffer size " + buffer.remaining());
			}
			int shift = (i == 0) ? 0 : 1;
			int w = width >> shift;
			int h = height >> shift;
			buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
			for (int row = 0; row < h; row++) {
				int length;
				if (pixelStride == 1 && outputStride == 1) {
					length = w;
					buffer.get(data, channelOffset, length);
					channelOffset += length;
				} else {
					length = (w - 1) * pixelStride + 1;
					buffer.get(rowData, 0, length);
					for (int col = 0; col < w; col++) {
						data[channelOffset] = rowData[col * pixelStride];
						channelOffset += outputStride;
					}
				}
				if (row < h - 1) {
					buffer.position(buffer.position() + rowStride - length);
				}
			}
			if (VERBOSE) Log.v(TAG, "Finished reading data from plane " + i);
		}
		return data;
	}
	
	/**
	 * 
	 * @param buf
	 * @param timestamp 微秒
	 * @return
	 */
	private boolean onFrame(byte[] buf, int timestamp) {
		// Get input buffer index
		if(mCodec == null) return false;
		ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
		ByteBuffer[] outBuffers = mCodec.getOutputBuffers();
		int inputBufferIndex = mCodec.dequeueInputBuffer(30000);

		//Log.d(TAG, "onFrame index:" + inputBufferIndex);
		if (inputBufferIndex >= 0) {
			ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
			inputBuffer.clear();
			inputBuffer.put(buf, 0, buf.length);
			mCodec.queueInputBuffer(inputBufferIndex, 0, buf.length, timestamp, 0);
		} else {
			Log.i(TAG, "-------111--------decode fail----------------");
			return false;
		}

		// Get output buffer index
		MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
		int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 30000);
		while (outputBufferIndex >= 0) {
			mCodec.releaseOutputBuffer(outputBufferIndex, true);
			outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
		}
		//Log.d(TAG, "---->>showOnFrame end");
		return true;
	}

	public void addData(byte[] inData) {
		//Log.d(TAG, "---->>add frame Data , length：" + inData.length);
		if(mHandler == null) return;
		Message msg = mHandler.obtainMessage(MSG_DATA);
		Bundle bdl = new Bundle();
		bdl.putByteArray("data", inData);
		msg.setData(bdl);
		msg.sendToTarget();
	}
	
	public void addAudioData(byte[] inData){
		//Log.d(TAG, "---->>add audio Data , length：" + inData.length + ", audioDataList size:" + audioDataList.size());
		synchronized(audioDataList){
			if (inData == null) return;
			if (!audioThread.isPlayRunning() && audioDataList.size() > 10){
				//audioThread.start();
			}
			byte[] md;
			IoBuffer ioBuffers = IoBuffer.allocate(inData.length);
	        ioBuffers.setAutoExpand(true);
	        ioBuffers.put(inData);
	        ioBuffers.flip();

	        // mlong int 媒体内容长度
			int mlong = ioBuffers.getInt();
			
			// mtime int 时间戳 ms 去掉头，以第2个数据帧的时间戳为起始点
			int mtime = ioBuffers.getInt();
			if (mStartTime == 0){
				mStartTime = 1;
				mtime = 0;
			}else if (mStartTime == 1){
				mStartTime = mtime;
				mtime = 0;
			}else{
				mtime = mtime - mStartTime;
			}
			
			if (inData.length != mlong + 8){
				int dsize = inData.length > 30 ? 30 : inData.length;
				byte mdt[] = new byte[dsize];
				System.arraycopy(inData, 0, mdt, 0, dsize);
				//Log.d(TAG,"---->>error audio data:" + Utility.getHexBytes(mdt));
				ioBuffers.free();
				return;
			}
			
			// md byte[] 媒体数据
			md = new byte[mlong];
			ioBuffers.get(md);
			
			ioBuffers.free();
			
			//audioDataList.add(new AudioData(mtime,md));
		}
	}
	
	@SuppressLint("NewApi")
	public void handleData(byte[] mdata) {
		final byte[] md;
		final IoBuffer ioBuffers = IoBuffer.allocate(mdata.length);
        ioBuffers.setAutoExpand(true);
        ioBuffers.put(mdata);
        ioBuffers.flip();

        // mlong int 媒体内容长度
		final int mlong = ioBuffers.getInt();
		
		// mtime int 时间戳 ms
		final int mtime = ioBuffers.getInt();
//		if (mStartTime == 0)mStartTime = mtime;
//		mtime = mtime - mStartTime;

		int dsize = mdata.length > 30 ? 30 : mdata.length;
		byte mdt[] = new byte[dsize];
		System.arraycopy(mdata, 0, mdt, 0, dsize);
//		Log.i(TAG,"---->>player handle mdata:" + Utility.getHexBytes(mdt));
		if (mdata.length != mlong + 8){
//			Log.i(TAG,"---->>error  mdata:" + Utility.getHexBytes(mdt));
			ioBuffers.free();
			return;
		}
		
		// md byte[] 媒体数据
		md = new byte[mlong];
		ioBuffers.get(md);
		
		ioBuffers.free();
		try{
			onFrame(md, mtime * 1000);
//			onCallImageData(md, mtime * 1000);
		}catch(Exception e){
			e.printStackTrace();
			//Log.d(TAG, "---->>decode error");
			if (mCodec != null) {
				try {
					mHandler.removeMessages(MSG_DATA);
					//mCodec.reset();
					reConfigure();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public class PlayerHandler extends Handler {

		public PlayerHandler() {
			super();
		}

		public PlayerHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_INIT:
				handleInitDecoder();
				break;
			case MSG_DATA:
				Bundle bdl = msg.getData();
				byte[] data = bdl.getByteArray("data");
				handleData(data);
				break;
			default:
			}
		}
	}
	
	public class AudioThread extends Thread {
		final int AUDIO_SR = 8000;
	    final int AUDIO_CONF = AudioFormat.CHANNEL_OUT_STEREO;
	    final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	    final int AUDIO_MODE = AudioTrack.MODE_STREAM;
	    final int AUDIO_STREAM_TYPE = AudioManager.STREAM_MUSIC;
	    
		private AudioTrack audioTrack;
		private MediaCodec audioDecoder;
		private MediaFormat mediaFormat;
		private boolean isPlayRunning = false;

		@SuppressLint("NewApi")
		public AudioThread() {
			super();

	        //int minBuffSize = AudioTrack.getMinBufferSize(AUDIO_SR, AUDIO_CONF, AUDIO_FORMAT);
	        
	        // 创建一个 AudioTrack 实例
	        //audioTrack = new AudioTrack(AUDIO_STREAM_TYPE, AUDIO_SR, AUDIO_CONF, AUDIO_FORMAT, 2048, AUDIO_MODE);
	        //audioTrack.setVolume(0.9f);//设置当前音量大小 
		}
		
		public void release(){
			if (audioTrack != null){
				audioTrack.release();
				audioTrack = null;
			}
			try {
				if (audioDecoder != null){
					audioDecoder.release();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private boolean initAudioDecoder(byte[] csd_0){
	        //Log.d(TAG,"---->>initAudioDecoder csd_0:" + Utility.getHexBytes(csd_0));
	        audioTrack = new AudioTrack(AUDIO_STREAM_TYPE, AUDIO_SR, AUDIO_CONF, AUDIO_FORMAT, 2048, AUDIO_MODE);
	        audioTrack.play();
			try {
	            //需要解码数据的类型
	            String mine = "audio/mp4a-latm";
	            //初始化解码器
	            audioDecoder = MediaCodec.createDecoderByType(mine);
	            
	            //MediaFormat用于描述音视频数据的相关参数
	            mediaFormat = new MediaFormat();
	            //数据类型
	            mediaFormat.setString(MediaFormat.KEY_MIME, mine);
	            //声道个数
	            mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2);
//	            mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 0);
	            //采样率
	            mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, AUDIO_SR);
//	            mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 0);
	            //比特率
	            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128000);
//	            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 24000);
	            //用来标记AAC是否有adts头，1->有
	            mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 1);
	            //用来标记aac的类型
                mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, 0);
	            //mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
	            //ByteBuffer key
	            byte[] bytes = new byte[]{csd_0[7],csd_0[8]};  
                ByteBuffer bb = ByteBuffer.wrap(bytes);
    	        //Log.d(TAG,"---->>initAudioDecoder bytes:" + Utility.getHexBytes(bytes));
                mediaFormat.setByteBuffer("csd-0", bb);
	            //解码器配置
	            audioDecoder.configure(mediaFormat, null, null, 0);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        }
	        if (audioDecoder == null) {
	            return false;
	        }
	        audioDecoder.start();
	        return true;
		}
		
		private void reconfigDecoder(){
			if (audioDecoder != null) {
				audioDecoder.configure(mediaFormat, null, null, 0);
				audioDecoder.start();
			}
		}

		

		public boolean isPlayRunning() {
			return isPlayRunning;
		}

		public void setPlayRunning(boolean isPlayRunning) {
			this.isPlayRunning = isPlayRunning;
		}

		@Override
		public void run() {
			isPlayRunning = true;
			while(!isInterrupted()){
				try {
					handleAudioData();
					sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void handleAudioData(){
			synchronized(audioDataList){
				if (audioDataList.size() > 0){
					for(Iterator<AudioData> iter = audioDataList.iterator(); iter.hasNext();){
						AudioData adata = iter.next();
						byte[] data = adata.getData();

						int sz = data.length > 30 ? 30 : data.length;
						byte[] logd = new byte[sz];
						System.arraycopy(data, 0, logd, 0, sz);
						//Log.d(TAG,"---->>handleAudioData data:" + Utility.getHexBytes(logd));
						decode(adata);
						iter.remove();
					}
				}
			}
		}
		
		/**
	     * aac解码+播放
	     */
	    @SuppressLint("NewApi")
	    public void decode(AudioData audioData) {
	    	try {
				afout.write(audioData.getData());
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	
	    	if (audioDecoder == null){
	    		initAudioDecoder(audioData.getData());
	    		return;
	    	}
	        try {
		        //等待时间，0->不等待，-1->一直等待
		        long kTimeOutUs = 5000;
	            //返回一个包含有效数据的input buffer的index,-1->不存在

				ByteBuffer[] inputBuffers = audioDecoder.getInputBuffers();
	            int inputBufIndex = audioDecoder.dequeueInputBuffer(kTimeOutUs);
		    	//Log.d(TAG,"---->> decode audio data,length:" + audioData.data.length + ", time:" + audioData.time);
	            if (inputBufIndex >= 0) {
	                //获取当前的ByteBuffer
	            	ByteBuffer dstBuf = inputBuffers[inputBufIndex];
	            	if(dstBuf == null){  
                        return ;  
                    }
	                //清空ByteBuffer
	                dstBuf.clear();
	                //填充数据
	                dstBuf.put(audioData.data, 0, audioData.data.length);
	                
	                //将指定index的input buffer提交给解码器
	                audioDecoder.queueInputBuffer(inputBufIndex, 0, audioData.data.length, audioData.time, 0);
	            }
	            //编解码器缓冲区
	            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
	            //返回一个output buffer的index，-1->不存在
	            int outputBufferIndex = audioDecoder.dequeueOutputBuffer(info, kTimeOutUs);

				//Log.d(TAG,"---->>decode AudioData outputBufferIndex:" + outputBufferIndex);
	            while (outputBufferIndex >= 0) {
	            	if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        //Log.i(TAG, "---->>audio encoder: codec config buffer");
                        audioDecoder.releaseOutputBuffer(outputBufferIndex, false);
    	                outputBufferIndex = audioDecoder.dequeueOutputBuffer(info, kTimeOutUs);
                        continue;
                    }
	            	
	            	if (info.size != 0) {
	            		ByteBuffer[] outBufs = audioDecoder.getOutputBuffers();
                        ByteBuffer outBuf = outBufs[outputBufferIndex];

                        outBuf.position(info.offset);
                        outBuf.limit(info.offset + info.size);
                        byte[] data = new byte[info.size];
                        outBuf.get(data);
                        // fosDecoder.write(data);
                        // 播放音乐
    	    	    	
    	                //Log.d(TAG,"---->>write data to audioTrack, size:" + info.size);
                        audioTrack.write(data, 0, info.size);

                    }
	                //释放已经解码的buffer
	                audioDecoder.releaseOutputBuffer(outputBufferIndex, false);
	                //解码未解完的数据
	                outputBufferIndex = audioDecoder.dequeueOutputBuffer(info, kTimeOutUs);
	            }
	        } catch (Exception e) {
                Log.d(TAG,"---->>decode audio data error, reset");
	            e.printStackTrace();
	            try{
	            	//audioDecoder.reset();
	            	reconfigDecoder();
	            }catch(Exception e2){
	            	e2.printStackTrace();
	            }
	        }
	    }
	}
	
	public class AudioData{
		private int time;
		private byte [] data;
		private boolean isPlayed = false;
		public AudioData(int time, byte[] data) {
			super();
			this.time = time * 1000; //微秒
			this.data = data;
		}
		public int getTime() {
			return time;
		}
		public void setTime(int time) {
			this.time = time;
		}
		public byte[] getData() {
			return data;
		}
		public void setData(byte[] data) {
			this.data = data;
		}
		public boolean isPlayed() {
			return isPlayed;
		}
		public void setPlayed(boolean isPlayed) {
			this.isPlayed = isPlayed;
		}
	}
}