package com.yuwei.face.socket.Message;

import com.yuwei.face.socket.utils.Constants;

import org.apache.mina.core.buffer.IoBuffer;

import java.io.UnsupportedEncodingException;


public class CmdCameraServerParam0x0901 extends BaseCommand{
	private static final long serialVersionUID = -8612109235341284877L;
	private int intIp = 0;
	private short port = 0;
	private int termId = 0;
	private int protoType = Constants.APP_PRO_TYPE_RTMP;
	private int connType = Constants.CONN_TYPE_TCP;
	private String carNum;

	public CmdCameraServerParam0x0901(String ip, short port, int termId, int protoType, int connType, String carNum){
		super();
		if (ip != null){
			String[] ips = ip.split("\\.");
			if(ips.length == 4){
				for(int i = 0; i < 4; i++){
					int num = Integer.parseInt(ips[i]);
					intIp |= num <<  i * 8;
				}
			}
		}
		this.port = port;
		this.termId = termId;
		this.protoType = protoType;
		this.connType = connType;
		this.carNum = carNum;
	}

	@Override
	public byte[] doDataEncode() {
		IoBuffer ioBuffer = IoBuffer.allocate(1000);
		ioBuffer.setAutoExpand(true);

		ioBuffer.putInt(intIp);
		ioBuffer.putShort(port);
		ioBuffer.putInt(termId);
		ioBuffer.putInt(protoType);
		ioBuffer.putInt(connType);
		try {
			byte carByte[] = carNum.getBytes("utf-8");
			if (carByte.length > 16){
				byte tmp[] = new byte[16];
				System.arraycopy(carByte, 0, tmp, 0, 16);
				ioBuffer.put(tmp);
			}else{
				ioBuffer.put(carByte);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		ioBuffer.flip();
		byte bs[] = new byte[ioBuffer.remaining()];
		ioBuffer.get(bs);
		ioBuffer.free();
		return bs;
	}
}
