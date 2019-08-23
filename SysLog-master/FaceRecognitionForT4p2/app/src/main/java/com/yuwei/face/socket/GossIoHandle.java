package com.yuwei.face.socket;

import android.util.Log;

import com.yuwei.face.socket.Message.GossMessage;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;


public class GossIoHandle extends IoHandlerAdapter {

	static final String TAG = "GossIoHandle";
	public static final int CONNECT_TIMEOUT = 1000;
	private IoSession session;
	private GossListener gossListener;
	
	private final int bindPort = 9988;
	private final String ipService = "127.0.0.1";
	
	private NioSocketConnector mConnector;
	private ConnectFuture mConnectFuture;
	private MyIoFutureListener mMyIoFutureListene = new MyIoFutureListener();
	
	private void server_init()
	{	
		Log.i(TAG, "-----------------server_init--------------");
		if(mConnector == null)
			mConnector = new NioSocketConnector();
		// 创建接受数据的过滤器
		DefaultIoFilterChainBuilder chain = mConnector.getFilterChain();
		chain.addLast("codec", new ProtocolCodecFilter(new GossCodecFactory()));
		mConnector.setHandler(this);
		// set connect timeout
		mConnector.setConnectTimeoutMillis(30 * 1000);
		//创建连接
		mConnectFuture = mConnector.connect(new InetSocketAddress(ipService,bindPort));
//============================使用阻塞的方式来获取Connect Session=====================
//		等待连接完成
//		cf.awaitUninterruptibly();
//		try{
//		Log.i(TAG, "--------------connect service-----------");
//		if(cf != null && cf.isConnected())
//			session = cf.getSession();
//	}catch(RuntimeIoException ex){
//		Log.i(TAG, "--------------RuntimeIoException-----------------");
//		ex.printStackTrace();
//	}
		mConnectFuture.addListener(mMyIoFutureListene);
	}
	
	private class MyIoFutureListener implements IoFutureListener<IoFuture> {

		@Override
		public void operationComplete(IoFuture ioFuture) {
			Log.i(TAG, "--------------operationComplete-----------");
			session = ioFuture.getSession();
			Log.i(TAG, "session:::::::::::" + session);
		}
		
	}
	public GossIoHandle(GossListener gossListener) {
		System.setProperty("java.net.preferIPv6Addresses", "false");
		server_init();
		this.gossListener = gossListener;
	}
	
	public boolean isConnected() {
		return (session != null && session.isConnected());
	}

	public void disconnect() {
		
		if(session != null && session.isConnected()){
			session.close();
			session = null;
		}
		
		if(mConnectFuture != null && mConnectFuture.isConnected()){
			mConnectFuture.removeListener(mMyIoFutureListene);
			mConnectFuture.cancel();
			mConnectFuture = null;
		}
		
		if(mConnector != null){
			mConnector.dispose();
			mConnector = null;
		}
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		Log.i(TAG, "--------------sessionOpened-----------------");
		gossListener.sessionOpened();
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		Log.i(TAG, "--------------sessionClosed-----------------");
		gossListener.sessionClosed(-1);
	}

	public int sendMessage(GossMessage baseMessage) {
		if (session == null) {
			gossListener.onException(new Throwable("not connected"));
			return -1;
		} else {
			try {
				session.write(baseMessage);
			} catch (RuntimeException e) {
				Log.e("send data fail", e.getMessage());
				return -1;
			}
		}
		return 0;
	}
	
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		if (message == null) {
			Log.e(TAG,"message is null");
			return;
		}

		gossListener.onMessage((GossMessage) message, -1);
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		Log.i(TAG, "--------------exceptionCaught-------------");
		cause.printStackTrace();
		gossListener.onException(cause);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
	}
	
	@Override
	public void sessionCreated(IoSession session){
		Log.i(TAG, "-------------sessionCreated----------------");
	}
}
