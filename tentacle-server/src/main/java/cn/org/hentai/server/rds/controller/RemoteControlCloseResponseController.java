package cn.org.hentai.server.rds.controller;

import cn.org.hentai.server.rds.BaseMessageController;
import cn.org.hentai.server.rds.TentacleDesktopSession;
import cn.org.hentai.server.rds.TentacleDesktopSessionHandler;
import cn.org.hentai.tentacle.protocol.Message;

/**
 * Created by matrixy on 2019/1/5.
 * 停止远程控制请求的应答的处理
 */
public class RemoteControlCloseResponseController extends BaseMessageController
{
    @Override
    public boolean authenticateRequired()
    {
        return true;
    }

    @Override
    public Message service(TentacleDesktopSession session, Message msg) throws Exception
    {
        return null;
    }
}
