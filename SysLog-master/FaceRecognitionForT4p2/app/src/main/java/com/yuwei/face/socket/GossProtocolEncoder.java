package com.yuwei.face.socket;

import android.util.Log;

import com.yuwei.face.socket.Message.GossMessage;
import com.yuwei.face.socket.utils.Utility;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class GossProtocolEncoder extends ProtocolEncoderAdapter
{
	public static final String TAG = "GossProtocolEncoder";

    @Override
    public void encode(IoSession ioSession, Object object, ProtocolEncoderOutput out)
            throws Exception
    {
    	GossMessage message = (GossMessage) object;

        IoBuffer ioBuffer = IoBuffer.allocate(1000);
        ioBuffer.setAutoExpand(true);
        byte bs[] = GossTool.convertGossMessageToByte(message);
        Log.d(TAG,"====>> socket send bytes:" + Utility.getHexBytes(bs));
        ioBuffer.put(bs);
        ioBuffer.flip();
        out.write(ioBuffer);
        ioBuffer.free();
    }

}
