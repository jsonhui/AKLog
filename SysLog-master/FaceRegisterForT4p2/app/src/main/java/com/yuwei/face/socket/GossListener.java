
package com.yuwei.face.socket;

import com.yuwei.face.socket.Message.GossMessage;

public interface GossListener
{

	int onMessage(GossMessage cpMessage, int _winch);

    void onException(Throwable throwable);

    void sessionOpened();

    void sessionClosed(int i);
}
