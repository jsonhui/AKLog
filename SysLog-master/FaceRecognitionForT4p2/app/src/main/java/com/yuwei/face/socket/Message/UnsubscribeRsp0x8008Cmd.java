package com.yuwei.face.socket.Message;


public class UnsubscribeRsp0x8008Cmd extends BaseCommand {

	 /**
     * 
     */
    private static final long serialVersionUID = -4111284396370731890L;

    private int result;

    
    public UnsubscribeRsp0x8008Cmd(int result) {
		super();
		this.result = result;
		cmd = GossCmdConst.CMD_STR_UNSUBSCRIBE_RSP;
	}

    public UnsubscribeRsp0x8008Cmd()
    {
        super();
        cmd = GossCmdConst.CMD_STR_UNSUBSCRIBE_RSP;
    }

    public int getResult()
    {
        return result;
    }

    public void setResult(int result)
    {
        this.result = result;
    }

	@Override
	public byte[] doDataEncode() {
		// TODO Auto-generated method stub
		return null;
	}
}
