package cn.org.hentai.server.rds.controller;

import cn.org.hentai.server.rds.BaseMessageController;
import cn.org.hentai.server.rds.TentacleDesktopSession;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Message;
import cn.org.hentai.tentacle.protocol.Packet;

/**
 * Created by matrixy on 2019/1/3.
 * 受控端连接会话的心跳响应
 */
public class HeartbeatController extends BaseMessageController
{
    static final Message OJBK = new Message().withCommand(Command.HEARTBEAT).withBody(Packet.create(4).addBytes("OJBK".getBytes()));

    @Override
    public boolean authenticateRequired()
    {
        return false;
    }

    @Override
    public Message service(TentacleDesktopSession session, Message msg)
    {
        return OJBK;
    }
}
