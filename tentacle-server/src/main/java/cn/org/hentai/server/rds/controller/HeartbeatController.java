package cn.org.hentai.server.rds.controller;

import cn.org.hentai.server.rds.TentacleDesktopHandler;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Message;
import cn.org.hentai.tentacle.protocol.Packet;

/**
 * Created by matrixy on 2019/1/3.
 */
public class HeartbeatController extends BaseMessageController
{
    @Override
    public boolean authenticateRequired()
    {
        return false;
    }

    @Override
    public Message service(TentacleDesktopHandler handler, Message msg)
    {
        System.out.println("心跳");
        Message resp = new Message().withCommand(Command.HEARTBEAT).withBody(Packet.create(4).addBytes("OJBK".getBytes()));
        return resp;
    }
}
