package com.yuwei.face.socket.Message;


public class CmdCameraPower0x0902 extends BaseCommand{
	private static final long serialVersionUID = -8612109235341284877L;
	private byte isOn = 0;

	public CmdCameraPower0x0902(boolean on){
		super();
		isOn = (byte) (on ? 1 : 0);
	}

	@Override
	public byte[] doDataEncode() {
		byte bs[] = {isOn};
		return bs;
	}
}
