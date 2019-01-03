package cn.org.hentai.server.rds.controller;

import cn.org.hentai.server.rds.TentacleDesktopHandler;
import cn.org.hentai.tentacle.protocol.Message;

/**
 * Created by matrixy on 2019/1/3.
 */
public class ControlRequestController extends BaseMessageController
{
    @Override
    public boolean authenticateRequired()
    {
        return true;
    }

    @Override
    public Message service(TentacleDesktopHandler handler, Message msg)
    {
        System.out.println("小样的居然请求控制了。。。");
        return null;
    }
}
