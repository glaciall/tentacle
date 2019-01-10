package cn.org.hentai.server.rds.controller;

import cn.org.hentai.server.rds.BaseMessageController;
import cn.org.hentai.server.rds.TentacleDesktopSession;
import cn.org.hentai.server.rds.TentacleDesktopSessionHandler;
import cn.org.hentai.tentacle.protocol.Message;
import cn.org.hentai.tentacle.protocol.Packet;

/**
 * Created by matrixy on 2019/1/4.
 * 受控端对于远程控制请求的应答的处理
 */
public class RemoteControlResponseController extends BaseMessageController
{
    @Override
    public boolean authenticateRequired()
    {
        return true;
    }

    @Override
    public Message service(TentacleDesktopSession session, Message msg) throws Exception
    {
        Packet resp = msg.getBody();
        int compressMethod = resp.nextByte() & 0xff;
        int bandWidth = resp.nextByte() & 0xff;
        int colorBits = resp.nextByte() & 0xff;
        int screenWidth = resp.nextShort() & 0xffff;
        int screenHeight = resp.nextShort() & 0xffff;
        session.getWebsocketContext().sendControlResponse(compressMethod, bandWidth, colorBits, screenWidth, screenHeight);

        return null;
    }
}
