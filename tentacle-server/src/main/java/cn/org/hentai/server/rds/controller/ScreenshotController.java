package cn.org.hentai.server.rds.controller;

import cn.org.hentai.server.rds.BaseMessageController;
import cn.org.hentai.server.rds.TentacleDesktopSession;
import cn.org.hentai.server.rds.TentacleDesktopSessionHandler;
import cn.org.hentai.tentacle.protocol.Message;
import cn.org.hentai.tentacle.protocol.Packet;

/**
 * Created by matrixy on 2019/1/4.
 * 转发屏幕快照到websocket会话
 */
public class ScreenshotController extends BaseMessageController
{
    @Override
    public boolean authenticateRequired()
    {
        return true;
    }

    @Override
    public Message service(TentacleDesktopSession session, Message msg) throws Exception
    {
        Packet packet = msg.getBody();
        int width = packet.nextShort();
        int height = packet.nextShort();
        long captureTime = packet.nextLong();
        int sequence = packet.nextInt();
        session.getWebsocketContext().sendScreenshot(packet.getBytes());
        return null;
    }
}
