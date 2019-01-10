package cn.org.hentai.server.rds.controller;

import cn.org.hentai.server.rds.BaseMessageController;
import cn.org.hentai.server.rds.TentacleDesktopSession;
import cn.org.hentai.server.rds.TentacleDesktopSessionHandler;
import cn.org.hentai.tentacle.protocol.Message;

/**
 * Created by matrixy on 2019/1/4.
 * 设置受控端剪贴板的回应处理
 */
public class SetClipboardResponseController extends BaseMessageController
{
    @Override
    public boolean authenticateRequired()
    {
        return true;
    }

    @Override
    public Message service(TentacleDesktopSession session, Message msg) throws Exception
    {
        session.getWebsocketContext().sendResponse("set-clipboard", "success");

        return null;
    }
}
