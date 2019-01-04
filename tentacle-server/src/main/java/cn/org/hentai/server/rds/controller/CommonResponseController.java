package cn.org.hentai.server.rds.controller;

import cn.org.hentai.server.rds.BaseMessageController;
import cn.org.hentai.server.rds.TentacleDesktopSessionHandler;
import cn.org.hentai.tentacle.protocol.Message;
import cn.org.hentai.tentacle.util.ByteUtils;

/**
 * Created by matrixy on 2019/1/4.
 */
public class CommonResponseController extends BaseMessageController
{
    @Override
    public boolean authenticateRequired()
    {
        return false;
    }

    @Override
    public Message service(TentacleDesktopSessionHandler session, Message msg) throws Exception
    {
        return null;
    }
}
