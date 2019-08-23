package com.yuwei.face.socket;

import com.yuwei.face.socket.Message.GossCmdConst;
import com.yuwei.face.socket.Message.GossMessage;
import com.yuwei.face.socket.Message.GossPrefix;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * mina自定义解码器 用于粘包、断包处理
 * 
 */
public class GossProtocolDecoder extends CumulativeProtocolDecoder {

	static final String TAG = "GossProtocolDecoder";
	private byte packPrefix[] = new byte[14];

	@Override
	protected boolean doDecode(IoSession ioSession, IoBuffer ioBuffer,
                               ProtocolDecoderOutput out) throws Exception {

		if (ioBuffer.remaining() > 0) {
			if (ioBuffer.remaining() < 14) {
				// 如果包的长度小于消息头长度，明显出现断包的情况，做断包处理
				//Log.e(TAG, "---->>包体明显小于消息头长度!,做断包处理");
				return false;
			}
			ioBuffer.mark();// 标记当前位置，以便reset

			// 头标记
			ioBuffer.get();
			// 头校验
			// 检验位的值等于消息ID字段到分包头/尾标记字段按位异或
			ioBuffer.get();
			// 消息ID
			short msgId = ioBuffer.getShort();
			int intMsgId = msgId & 0xFFFF;

			//if (Constants.DEBUG)Log.d(TAG, "---->>doDecode va msg intMsgId:" + intMsgId);

			// 设备ID
			ioBuffer.getInt();

			// 消息流水号
			// 从0开始，重发的流水号保持不变
			ioBuffer.getShort();
			// 消息体长度
			int bodyLen = ioBuffer.getUnsignedShort();
			//属性
			ioBuffer.get();
			//分包头/尾标记
			byte packageTag = ioBuffer.get();
			//if (Constants.DEBUG)Log.d(TAG, "---->>package packageTag: " + packageTag);

			ioBuffer.reset();// 下标重置到mark()位置

			int remaining = ioBuffer.remaining() - 14;

			if (bodyLen > remaining)// 如果消息内容不够，则重置，相当于不读取size
			{
				//Log.d(TAG, "---->>消息内容不够，重置, bodyLen: " + bodyLen  + " remaining: " + remaining);
				return false;// 父类接收新数据，以拼凑成完整数据
			} else {
				// 取到消息头
				ioBuffer.get(packPrefix);
				//Log.d(tag,"====>>msg packPrefix:" + Utility.getHexBytes(packPrefix));
				GossPrefix gossPrefix = new GossPrefix(packPrefix);
				if (gossPrefix.getHead_flag() != GossCmdConst.HEADER_FLAG) {
					// 如果协议头不对，丢掉整个包
					//Log.w(TAG, "---->>协议头不对，丢掉整个包 : " + (int) (bodyLen & 0xFFFF));
					return true;
				}

				if (!gossPrefix.checkSuccess()) {
					//Log.w(TAG, "---->>校验失败，丢掉所有数据the data check fail!!!!! ");
					// 校验失败，丢掉所有数据
					return true;
				}

				byte bs[] = new byte[bodyLen];
				ioBuffer.get(bs);
				
				int logSize = bodyLen > 50 ? 50 : bodyLen;
				byte logbt[] = new byte[logSize];
				System.arraycopy(bs, 0, logbt, 0, logSize);
				if (intMsgId == 34){
					//Log.d(TAG,"---->>msg data:" + Utility.getHexBytes(logbt) + "...");
				}else{
					//Log.d(TAG,"====>>msgId:" + intMsgId + ", msg data:" + Utility.getHexBytes(logbt));
				}
				try {
					GossMessage gossMessage = new GossMessage(gossPrefix, bs);
					if (gossMessage.getBaseCommand() != null) {
						out.write(gossMessage);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (ioBuffer.remaining() > 0)// 如果读取内容后还粘了包，就让父类再重读 一次，进行下一次解析
				{
					//Log.w(TAG, "---->>读取内容后还粘了包,重读进行下一次解析");
					return true;
				}
			}
		}

		return false;// 处理成功，让父类进行接收下个包
	}

	public static void main(String args[]) {

	}
}
