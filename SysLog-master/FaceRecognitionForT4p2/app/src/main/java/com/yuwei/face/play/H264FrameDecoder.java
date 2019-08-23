package com.yuwei.face.play;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.mina.core.buffer.IoBuffer;

import com.yuwei.face.service.ChannelFrameData;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;


/**
 * H264 视频帧解析器
 * 仅适用于V3摄像头视频流
 * 可以将视频帧转换成Bitmap，调用getFrame()获取解析后的结果
 * @author wallage
 *
 */
public class H264FrameDecoder {

	public enum PlayState {
		STOPED, PLAYING, RELEASED
	}
	
	public static final String TAG = "H264FrameDecoder";

	private final static String MIME_TYPE = "video/avc"; // H.264 Advanced Video
	private final static int VIDEO_WIDTH = 800;
	private final static int VIDEO_HEIGHT = 480;
	private final static int TIME_INTERNAL = 10; //30

	public static final int MSG_INIT = 1;
	public static final int MSG_DATA = 2;

	List<FrameData> mCurFrameList;
	private final int MAX_FRAME_DATA = 100;
	private Object frameGetLocker = new Object();
//	private SparseArray<Bitmap> mFrameBitmap = new SparseArray<>();
	private boolean needImg = false;
	boolean picGenerating = false;
	private Bitmap mFrameBitmap;
	
	DecoderThread decoderThread;
	HandlerThread handler;
	PlayerHandler mainHandler;
	private int mStartTime = 0;

	public H264FrameDecoder() {
		handler = new HandlerThread("H264FrameDecoder");
		handler.start();
		
		mainHandler = new PlayerHandler(handler.getLooper());
		mCurFrameList = new ArrayList<FrameData>();
	}

	public void release() {
		mainHandler.removeMessages(MSG_INIT);
		mainHandler.removeMessages(MSG_DATA);
		mCurFrameList.clear();
		if (decoderThread != null){
			decoderThread.release();
		}
	}
	
	public Bitmap getFrame(){
        final int TIMEOUT_MS = 2000;
		//Log.d(TAG,"2====>>call getFrame begin");
//		mFrameBitmap.clear();
		needImg = true;
		mFrameBitmap = null;
		synchronized(frameGetLocker){
//			mFrameBitmap.put(chann, null);
			try {
				frameGetLocker.wait(TIMEOUT_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//Log.d(TAG,"5====>>getFrame end, return bmp");
		return mFrameBitmap;
	}
	
	public void initDecoder(){
		mainHandler.obtainMessage(MSG_INIT).sendToTarget();
	}
	

	public void handleInitDecoder() {
		if (decoderThread == null){
			decoderThread = new DecoderThread();
			decoderThread.start();
		}
	}
	
	

	/**
	 * Find H264 frame head
	 * 
	 * @param buffer
	 * @param len
	 * @return the offset of frame head, return 0 if can not find one
	 */
	static int findHead(byte[] buffer,int offset, int len) {
		int i;
		for (i = offset; i < len; i++) {
			if (checkHead(buffer, i))break;
		}
		if (i == len)
			return 0;
		if (i == offset)
			return 0;
		return i;
	}

	/**
	 * Check if is H264 frame head
	 * 
	 * @param buffer
	 * @param offset
	 * @return whether the src buffer is frame head
	 */
	static boolean checkHead(byte[] buffer, int offset) {
		// 00 00 00 01
		if (buffer[offset] == 0 && buffer[offset + 1] == 0
				&& buffer[offset + 2] == 0 && buffer[offset + 3] == 1)
			return true;
		// 00 00 01
		if (buffer[offset] == 0 && buffer[offset + 1] == 0
				&& buffer[offset + 2] == 1)
			return true;
		return false;
	}
	
	private void recordData(byte[] inData){
		int length = inData.length;
//		Log.d(TAG, "data list size:" + mCurFrameList.size());
		if (length > 4 ){
			synchronized(mCurFrameList){
				byte[] md;
				IoBuffer ioBuffers = IoBuffer.allocate(inData.length);
		        ioBuffers.setAutoExpand(true);
		        ioBuffers.put(inData);
		        ioBuffers.flip();

		        // mlong int 媒体内容长度
				int mlong = ioBuffers.getInt();
				
				// mtime int 时间戳 ms
				int mtime = ioBuffers.getInt();
				if (mStartTime == 0)mStartTime = mtime;
				mtime = mtime - mStartTime;
				
				// md byte[] 媒体数据
				md = new byte[mlong];
				ioBuffers.get(md);
				ioBuffers.free();
				
				FrameData frameData = new FrameData(md);
				//byte[] sd = new byte [40];
				//System.arraycopy(md, 0, sd, 0, 40);
				//Log.d(TAG, "---->>recordData, head:" + Utility.getHexBytes(sd));
				if((md[4] & 0x1f) == 5 || (md[4] & 0x1f) == 7){ //5 为I帧， 7为sps帧后面跟有I帧
					mCurFrameList.clear();
					frameData.type="IFrame";
				}else{
					frameData.type="BPFrame";
				}
				frameData.timeStamp = mtime;
				mCurFrameList.add(frameData);
			}
		}
	}

	public void addData(byte[] inData) {
		//Log.i(TAG, "---->>addData, length:" + inData.length + ", data list size:" + mCurFrameList.size());
		Message msg = mainHandler.obtainMessage(MSG_DATA);
		Bundle bdl = new Bundle();
		bdl.putByteArray("data", inData);
		msg.setData(bdl);
		msg.sendToTarget();
	}
	
//	public void addData(ChannelFrameData data) {
//		//Log.i(TAG, "---->>addData, length:" + inData.length + ", data list size:" + mCurFrameList.size());
//		Message msg = mainHandler.obtainMessage(MSG_DATA);
//		Bundle bdl = new Bundle();
////		bdl.putByteArray("data", inData);
//		bdl.putSerializable("data", data);
//		msg.setData(bdl);
//		msg.sendToTarget();
//	}
	
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
//				ChannelFrameData data =(ChannelFrameData) bdl.getSerializable("data");
				try{
					recordData(data);
				}catch(Exception e){
					e.printStackTrace();
				}
				break;
			default:
			}
		}
	}
	
	public class DecoderThread extends Thread{
		
		int mCount = 0;
		int h264Read = 0;
		int frameOffset = 0;
		private MediaCodec mCodec;
		private CodecOutputSurface mOutSurface;
		public PlayState state = PlayState.STOPED;
 
		byte[] spsData;//00 00 00 01 67对应序列参数集SPS
		byte[] ppsData;//00 00 00 01 68对应图像参数集PPS
		
		@Override
		public void run() {
			initData();
			while(!isInterrupted()){
				try{
					releaseRecord();
					sleep(10);
				}catch(InterruptedException e){
					break;
				}
			}
			super.run();
		}
		
		private void releaseRecord(){
			synchronized (mCurFrameList) {
				for (int i = 0; i < mCurFrameList.size(); i++) {
					FrameData fd = mCurFrameList.get(i);
					if (fd == null) break;
					if (fd.isDecoded) continue;
					if(mCodec == null) break;
					handleData(fd);
					fd.isDecoded = true;
				}
				if (mCurFrameList.size() > MAX_FRAME_DATA){
					//Log.d(TAG,"---->>max record size, clear date");
					mCurFrameList.clear();
				}
			}
		}
		
		@SuppressLint("NewApi")
		public void initData() {
			if (state == PlayState.PLAYING) {
				//mCodec.reset();
				reConfigure();
				return;
			}
			try {
				mCodec = MediaCodec.createDecoderByType(MIME_TYPE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,VIDEO_WIDTH, VIDEO_HEIGHT);
			mOutSurface = new CodecOutputSurface(VIDEO_WIDTH,VIDEO_HEIGHT);
			mCodec.configure(mediaFormat, mOutSurface.getSurface(), null, 0);
			mCodec.start();
			state = PlayState.PLAYING;
		}

		public void reConfigure() {
			if (mCodec != null) {
				MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,VIDEO_WIDTH, VIDEO_HEIGHT);
				//mOutSurface = new CodecOutputSurface(VIDEO_WIDTH,VIDEO_HEIGHT);
				mCodec.configure(mediaFormat, mOutSurface.getSurface(), null, 0);
				mCodec.start();
				state = PlayState.PLAYING;
			}
		}
		
		@SuppressLint("NewApi")
		public void handleData(FrameData inData) {
			boolean handled = false;
			while (!handled && state == PlayState.PLAYING) {
				try {
					handled = onFrame(inData);
					Thread.sleep(TIME_INTERNAL);
				} catch (Exception e) {
					e.printStackTrace();
					//Log.d(TAG, "---->>decode error");
//					if (mCodec != null) {
//						try {
//							//mCodec.reset();
//							reConfigure();
//						} catch (Exception e1) {
//							e1.printStackTrace();
//						}
//					}
				}
			}
		}
		
		private boolean onFrame(FrameData inData) {
			byte buf[] = inData.data;
			int offset = 0;
			int length = inData.data.length;
			
			final int TIMEOUT_USEC = 10000;
			MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
			if(mCodec == null) return true;
			// Get input buffer index
			ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
			int inputBufferIndex = mCodec.dequeueInputBuffer(TIMEOUT_USEC);

			//Log.d(TAG, "---->>onFrame index:" + inputBufferIndex + ", frame type:" + inData.type + ", timeStamp:" + inData.timeStamp);
			if (inputBufferIndex >= 0) {
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(buf, offset, length);
				mCodec.queueInputBuffer(inputBufferIndex, 0, length, inData.timeStamp, 0);
				//mCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount * TIME_INTERNAL, 0);
				mCount++;
			} else {
				return false;
			}

			// Get output buffer index
	        boolean outputDone = false;
			while (!outputDone) {
				int decoderStatus = mCodec.dequeueOutputBuffer(info, TIMEOUT_USEC);
				if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
					// no output available yet
					outputDone = true;
//					Log.d(TAG, "no output from decoder available");
				} else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					// not important for us, since we're using Surface
					outputDone = true;
//					Log.d(TAG, "decoder output buffers changed");
				} else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					outputDone = true;
//					Log.d(TAG, "decoder output format changed");
				} else if (decoderStatus < 0) {
					outputDone = true;
//					Log.d(TAG,"unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
				} else {
					// decoderStatus >= 0
					 Log.i(TAG, "surface decoder given buffer " + decoderStatus + " (size=" + info.size + ")");
					if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
						 Log.d(TAG, "output EOS");
						outputDone = true;
					}

					boolean doRender = (info.size != 0);

					// As soon as we call releaseOutputBuffer, the buffer will be
					// forwarded
					// to SurfaceTexture to convert to a texture. The API doesn't
					// guarantee
					// that the texture will be available before the call returns,
					// so we
					// need to wait for the onFrameAvailable callback to fire.
					mCodec.releaseOutputBuffer(decoderStatus, doRender);
					if (doRender && needImg) {
						picGenerating = true;
						needImg = false;
						synchronized (frameGetLocker) {
							//Log.d(TAG, "3====>>awaiting decode of frame ");
							try{
								mOutSurface.awaitNewImage();
								mOutSurface.drawImage(true);
//								mFrameBitmap.put(channel, mOutSurface.getFrame());
//								saveBitmap(mOutSurface.getFrame(),channel);
								mFrameBitmap = mOutSurface.getFrame();
								//Log.d(TAG, "4====>>notify frame ");
							}catch(RuntimeException e){
								//Log.d(TAG, "====>>time out");
							}finally{
								frameGetLocker.notify();
								picGenerating = false;
							}
						}
					}
				}
			}
			return true;
		}

		public void saveBitmap(Bitmap bm,int chan) {
			File f = new File("/mnt/sdcard/", System.currentTimeMillis()+"_"+chan+".jpg");
			if (f.exists()) {
				f.delete();
			}
			try {
				FileOutputStream out = new FileOutputStream(f);
				bm.compress(Bitmap.CompressFormat.PNG, 90, out);
				out.flush();
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
//		private void initHeadParam(byte[] data,int offset, int len){
//			int start = offset;
//			int end = findHead(data, start + 4, len);
//			int size = end - start;
//			if (len > 250){
//				byte[] bs = new byte[30];
//				System.arraycopy(data, offset, bs, 0, 30);
//			}
//			while (end > 0) {
//				if (data[start + 3] == 103 || data[start + 4] == 103) {
//					spsData = new byte[size];
//					System.arraycopy(data, start, spsData, 0, size);
//				} else if (data[start + 3] == 104 || data[start + 4] == 104) {
//					ppsData = new byte[size];
//					System.arraycopy(data, start, ppsData, 0, size);
//				}
//				start = end;
//				end = findHead(data, start + 4, len);
//				size = end - start;
//				if(spsData != null && ppsData != null){
//					break;
//				}
//			}
//			Log.d(TAG,"---->>spsData:" + Utility.getHexBytes(spsData));
//			Log.d(TAG,"---->>ppsData:" + Utility.getHexBytes(ppsData));
//		}
		
		public void release() {
			state = PlayState.RELEASED;
			try {
				if (mCodec != null)mCodec.release();
				this.interrupt();
				this.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public class FrameData{
		public boolean isDecoded = false;
		public byte[] data;
		public String type = "";
		public int timeStamp;
		public int channel;
		public FrameData(byte[] data) {
			super();
			this.data = data;
		}
		
		public FrameData(byte[] data, String type, int timeStamp) {
			super();
			this.data = data;
			this.type = type;
			this.timeStamp = timeStamp;
		}
	}
	
	/**
     * Holds state associated with a Surface used for MediaCodec decoder output.
     * <p>
     * The constructor for this class will prepare GL, create a SurfaceTexture,
     * and then create a Surface for that SurfaceTexture.  The Surface can be passed to
     * MediaCodec.configure() to receive decoder output.  When a frame arrives, we latch the
     * texture with updateTexImage(), then render the texture with GL to a pbuffer.
     * <p>
     * By default, the Surface will be using a BufferQueue in asynchronous mode, so we
     * can potentially drop frames.
     */
    private static class CodecOutputSurface
            implements SurfaceTexture.OnFrameAvailableListener {
        private STextureRender mTextureRender;
        private SurfaceTexture mSurfaceTexture;
        private Surface mSurface;

        private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
        private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
        int mWidth;
        int mHeight;

        private Object mFrameSyncObject = new Object();     // guards mFrameAvailable
        private boolean mFrameAvailable;

        private ByteBuffer mPixelBuf;                       // used by saveFrame()

        /**
         * Creates a CodecOutputSurface backed by a pbuffer with the specified dimensions.  The
         * new EGL context and surface will be made current.  Creates a Surface that can be passed
         * to MediaCodec.configure().
         */
        public CodecOutputSurface(int width, int height) {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException();
            }
            mWidth = width;
            mHeight = height;

            eglSetup();
            makeCurrent();
            setup();
        }

        /**
         * Creates interconnected instances of TextureRender, SurfaceTexture, and Surface.
         */
        private void setup() {
            mTextureRender = new STextureRender();
            mTextureRender.surfaceCreated();

             Log.d(TAG, "textureID=" + mTextureRender.getTextureId());
            mSurfaceTexture = new SurfaceTexture(mTextureRender.getTextureId());

            // This doesn't work if this object is created on the thread that CTS started for
            // these test cases.
            //
            // The CTS-created thread has a Looper, and the SurfaceTexture constructor will
            // create a Handler that uses it.  The "frame available" message is delivered
            // there, but since we're not a Looper-based thread we'll never see it.  For
            // this to do anything useful, CodecOutputSurface must be created on a thread without
            // a Looper, so that SurfaceTexture uses the main application Looper instead.
            //
            // Java language note: passing "this" out of a constructor is generally unwise,
            // but we should be able to get away with it here.
            mSurfaceTexture.setOnFrameAvailableListener(this);

            mSurface = new Surface(mSurfaceTexture);

            mPixelBuf = ByteBuffer.allocateDirect(mWidth * mHeight * 4);
            mPixelBuf.order(ByteOrder.LITTLE_ENDIAN);
        }

        /**
         * Prepares EGL.  We want a GLES 2.0 context and a surface that supports pbuffer.
         */
        private void eglSetup() {
            mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
                throw new RuntimeException("unable to get EGL14 display");
            }
            int[] version = new int[2];
            if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
                mEGLDisplay = null;
                throw new RuntimeException("unable to initialize EGL14");
            }

            // Configure EGL for pbuffer and OpenGL ES 2.0, 24-bit RGB.
            int[] attribList = {
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                    EGL14.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            if (!EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length,
                    numConfigs, 0)) {
                throw new RuntimeException("unable to find RGB888+recordable ES2 EGL config");
            }

            // Configure context for OpenGL ES 2.0.
            int[] attrib_list = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
            };
            mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
                    attrib_list, 0);
            checkEglError("eglCreateContext");
            if (mEGLContext == null) {
                throw new RuntimeException("null context");
            }

            // Create a pbuffer surface.
            int[] surfaceAttribs = {
                    EGL14.EGL_WIDTH, mWidth,
                    EGL14.EGL_HEIGHT, mHeight,
                    EGL14.EGL_NONE
            };
            mEGLSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, configs[0], surfaceAttribs, 0);
            checkEglError("eglCreatePbufferSurface");
            if (mEGLSurface == null) {
                throw new RuntimeException("surface was null");
            }
        }

        /**
         * Discard all resources held by this class, notably the EGL context.
         */
        public void release() {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
                EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
                EGL14.eglReleaseThread();
                EGL14.eglTerminate(mEGLDisplay);
            }
            mEGLDisplay = EGL14.EGL_NO_DISPLAY;
            mEGLContext = EGL14.EGL_NO_CONTEXT;
            mEGLSurface = EGL14.EGL_NO_SURFACE;

            mSurface.release();

            // this causes a bunch of warnings that appear harmless but might confuse someone:
            //  W BufferQueue: [unnamed-3997-2] cancelBuffer: BufferQueue has been abandoned!
            //mSurfaceTexture.release();

            mTextureRender = null;
            mSurface = null;
            mSurfaceTexture = null;
        }

        /**
         * Makes our EGL context and surface current.
         */
        public void makeCurrent() {
            if (!EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
                throw new RuntimeException("eglMakeCurrent failed");
            }
        }

        /**
         * Returns the Surface.
         */
        public Surface getSurface() {
            return mSurface;
        }

        /**
         * Latches the next buffer into the texture.  Must be called from the thread that created
         * the CodecOutputSurface object.  (More specifically, it must be called on the thread
         * with the EGLContext that contains the GL texture object used by SurfaceTexture.)
         */
        public void awaitNewImage() {
            final int TIMEOUT_MS = 500;

//            synchronized (mFrameSyncObject) {
                while (!mFrameAvailable) {
                    try {
                        // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                        // stalling the test if it doesn't arrive.
                        mFrameSyncObject.wait(TIMEOUT_MS);
                        // Log.d(TAG, "====>>wait finish, mFrameAvailable:" + mFrameAvailable);
                        if (!mFrameAvailable) {
                            // TODO: if "spurious wakeup", continue while loop
                            throw new RuntimeException("frame wait timed out");
                        }
                    } catch (InterruptedException ie) {
                        // shouldn't happen
                        throw new RuntimeException(ie);
                    }
                }
                mFrameAvailable = false;
//            }

            // Latch the data.
            mTextureRender.checkGlError("before updateTexImage");
            mSurfaceTexture.updateTexImage();
        }

        /**
         * Draws the data from SurfaceTexture onto the current EGL surface.
         *
         * @param invert if set, render the image with Y inverted (0,0 in top left)
         */
        public void drawImage(boolean invert) {
            mTextureRender.drawFrame(mSurfaceTexture, invert);
        }

        // SurfaceTexture callback
        @Override
        public void onFrameAvailable(SurfaceTexture st) {
            // Log.d(TAG, "====>>new frame available");
            synchronized (mFrameSyncObject) {
                if (mFrameAvailable) {
                    throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
                }
                mFrameAvailable = true;
                // Log.d(TAG, "====>>new frame notifyAll");
                mFrameSyncObject.notifyAll();
            }
        }
        
        public Bitmap getFrame(){
        	mPixelBuf.rewind();
            GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,mPixelBuf);

            Bitmap bmp = null;
            try {
                bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                mPixelBuf.rewind();
                bmp.copyPixelsFromBuffer(mPixelBuf);
            } catch(Exception e) {
                e.printStackTrace();
            }
            return bmp;
        }

        /**
         * Saves the current frame to disk as a PNG image.
         */
        public void saveFrame(String filename) throws IOException {
            // glReadPixels gives us a ByteBuffer filled with what is essentially big-endian RGBA
            // data (i.e. a byte of red, followed by a byte of green...).  To use the Bitmap
            // constructor that takes an int[] array with pixel data, we need an int[] filled
            // with little-endian ARGB data.
            //
            // If we implement this as a series of buf.get() calls, we can spend 2.5 seconds just
            // copying data around for a 720p frame.  It's better to do a bulk get() and then
            // rearrange the data in memory.  (For comparison, the PNG compress takes about 500ms
            // for a trivial frame.)
            //
            // So... we set the ByteBuffer to little-endian, which should turn the bulk IntBuffer
            // get() into a straight memcpy on most Android devices.  Our ints will hold ABGR data.
            // Swapping B and R gives us ARGB.  We need about 30ms for the bulk get(), and another
            // 270ms for the color swap.
            //
            // We can avoid the costly B/R swap here if we do it in the fragment shader (see
            // http://stackoverflow.com/questions/21634450/ ).
            //
            // Having said all that... it turns out that the Bitmap#copyPixelsFromBuffer()
            // method wants RGBA pixels, not ARGB, so if we create an empty bitmap and then
            // copy pixel data in we can avoid the swap issue entirely, and just copy straight
            // into the Bitmap from the ByteBuffer.
            //
            // Making this even more interesting is the upside-down nature of GL, which means
            // our output will look upside-down relative to what appears on screen if the
            // typical GL conventions are used.  (For ExtractMpegFrameTest, we avoid the issue
            // by inverting the frame when we render it.)
            //
            // Allocating large buffers is expensive, so we really want mPixelBuf to be
            // allocated ahead of time if possible.  We still get some allocations from the
            // Bitmap / PNG creation.

            mPixelBuf.rewind();
            GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                mPixelBuf);

            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(filename));
                Bitmap bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                mPixelBuf.rewind();
                bmp.copyPixelsFromBuffer(mPixelBuf);
                bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
                bmp.recycle();
            } finally {
                if (bos != null) bos.close();
            }
             {
                Log.d(TAG, "Saved " + mWidth + "x" + mHeight + " frame as '" + filename + "'");
            }
        }

        /**
         * Checks for EGL errors.
         */
        private void checkEglError(String msg) {
            int error;
            if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
                throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
            }
        }
    }


    /**
     * Code for rendering a texture onto a surface using OpenGL ES 2.0.
     */
    private static class STextureRender {
        private static final int FLOAT_SIZE_BYTES = 4;
        private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
        private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
        private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
        private final float[] mTriangleVerticesData = {
                // X, Y, Z, U, V
                -1.0f, -1.0f, 0, 0.f, 0.f,
                 1.0f, -1.0f, 0, 1.f, 0.f,
                -1.0f,  1.0f, 0, 0.f, 1.f,
                 1.0f,  1.0f, 0, 1.f, 1.f,
        };

        private FloatBuffer mTriangleVertices;

        private static final String VERTEX_SHADER =
                "uniform mat4 uMVPMatrix;\n" +
                "uniform mat4 uSTMatrix;\n" +
                "attribute vec4 aPosition;\n" +
                "attribute vec4 aTextureCoord;\n" +
                "varying vec2 vTextureCoord;\n" +
                "void main() {\n" +
                "    gl_Position = uMVPMatrix * aPosition;\n" +
                "    vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                "}\n";

        private static final String FRAGMENT_SHADER =
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +      // highp here doesn't seem to matter
                "varying vec2 vTextureCoord;\n" +
                "uniform samplerExternalOES sTexture;\n" +
                "void main() {\n" +
                "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                "}\n";

        private float[] mMVPMatrix = new float[16];
        private float[] mSTMatrix = new float[16];

        private int mProgram;
        private int mTextureID = -12345;
        private int muMVPMatrixHandle;
        private int muSTMatrixHandle;
        private int maPositionHandle;
        private int maTextureHandle;

        public STextureRender() {
            mTriangleVertices = ByteBuffer.allocateDirect(
                    mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mTriangleVertices.put(mTriangleVerticesData).position(0);

            Matrix.setIdentityM(mSTMatrix, 0);
        }

        public int getTextureId() {
            return mTextureID;
        }

        /**
         * Draws the external texture in SurfaceTexture onto the current EGL surface.
         */
        public void drawFrame(SurfaceTexture st, boolean invert) {
            checkGlError("onDrawFrame start");
            st.getTransformMatrix(mSTMatrix);
            if (invert) {
                mSTMatrix[5] = -mSTMatrix[5];
                mSTMatrix[13] = 1.0f - mSTMatrix[13];
            }

            // (optional) clear to green so we can see if we're failing to set pixels
            GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(mProgram);
            checkGlError("glUseProgram");

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);

            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
            GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
            checkGlError("glVertexAttribPointer maPosition");
            GLES20.glEnableVertexAttribArray(maPositionHandle);
            checkGlError("glEnableVertexAttribArray maPositionHandle");

            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
            GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
            checkGlError("glVertexAttribPointer maTextureHandle");
            GLES20.glEnableVertexAttribArray(maTextureHandle);
            checkGlError("glEnableVertexAttribArray maTextureHandle");

            Matrix.setIdentityM(mMVPMatrix, 0);
            GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            checkGlError("glDrawArrays");

            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        }

        /**
         * Initializes GL state.  Call this after the EGL surface has been created and made current.
         */
        public void surfaceCreated() {
            mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
            if (mProgram == 0) {
                throw new RuntimeException("failed creating program");
            }

            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
            checkLocation(maPositionHandle, "aPosition");
            maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
            checkLocation(maTextureHandle, "aTextureCoord");

            muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            checkLocation(muMVPMatrixHandle, "uMVPMatrix");
            muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
            checkLocation(muSTMatrixHandle, "uSTMatrix");

            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);

//            mTextureID = textures[0];
			mTextureID = textures[0];
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
            checkGlError("glBindTexture mTextureID");

            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
            checkGlError("glTexParameter");
        }

        /**
         * Replaces the fragment shader.  Pass in null to reset to default.
         */
        public void changeFragmentShader(String fragmentShader) {
            if (fragmentShader == null) {
                fragmentShader = FRAGMENT_SHADER;
            }
            GLES20.glDeleteProgram(mProgram);
            mProgram = createProgram(VERTEX_SHADER, fragmentShader);
            if (mProgram == 0) {
                throw new RuntimeException("failed creating program");
            }
        }

        private int loadShader(int shaderType, String source) {
            int shader = GLES20.glCreateShader(shaderType);
            checkGlError("glCreateShader type=" + shaderType);
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
            return shader;
        }

        private int createProgram(String vertexSource, String fragmentSource) {
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
            if (vertexShader == 0) {
                return 0;
            }
            int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
            if (pixelShader == 0) {
                return 0;
            }

            int program = GLES20.glCreateProgram();
            if (program == 0) {
                Log.e(TAG, "Could not create program");
            }
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
            return program;
        }

        public void checkGlError(String op) {
            int error;
            while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                Log.e(TAG, op + ": glError " + error);
                throw new RuntimeException(op + ": glError " + error);
            }
        }

        public static void checkLocation(int location, String label) {
            if (location < 0) {
                throw new RuntimeException("Unable to locate '" + label + "' in program");
            }
        }
    }
}
