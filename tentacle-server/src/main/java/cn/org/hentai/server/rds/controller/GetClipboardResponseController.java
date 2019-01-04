package cn.org.hentai.server.rds.controller;

import cn.org.hentai.server.rds.BaseMessageController;
import cn.org.hentai.server.rds.TentacleDesktopSessionHandler;
import cn.org.hentai.tentacle.protocol.Message;
import cn.org.hentai.tentacle.protocol.Packet;

/**
 * Created by matrixy on 2019/1/4.
 * 受控端获取剪贴板内容的回应
 */
public class GetClipboardResponseController extends BaseMessageController
{
    @Override
    public boolean authenticateRequired()
    {
        return true;
    }

    @Override
    public Message service(TentacleDesktopSessionHandler session, Message msg) throws Exception
    {
        Packet packet = msg.getBody();
        int len = packet.nextInt();
        String text = new String(packet.nextBytes(len), "UTF-8");
        session.getWebsocketContext().sendClipboardData(text);

        return null;
    }
}
