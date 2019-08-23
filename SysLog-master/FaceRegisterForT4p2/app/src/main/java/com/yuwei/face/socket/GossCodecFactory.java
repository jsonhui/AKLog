package com.yuwei.face.socket;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;


public class GossCodecFactory implements ProtocolCodecFactory
{
    private GossProtocolEncoder gossEncoder = new GossProtocolEncoder();
    private GossProtocolDecoder gossDecoder = new GossProtocolDecoder();
    
    @Override
    public ProtocolEncoder getEncoder(IoSession paramIoSession) throws Exception
    {
        return gossEncoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession paramIoSession) throws Exception
    {
        return gossDecoder;
    }

}
