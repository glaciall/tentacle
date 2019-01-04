package cn.org.hentai.server.rds.controller;

import cn.org.hentai.server.rds.BaseMessageController;
import cn.org.hentai.server.rds.TentacleDesktopSessionHandler;
import cn.org.hentai.tentacle.protocol.Message;
import cn.org.hentai.tentacle.protocol.Packet;

/**
 * Created by matrixy on 2019/1/4.
 * 受控端对于文件下载的消息回应
 * 因为受控端按40960字节对文件进行分包传输，所以每一个分包都会调用一次此类的service()方法
 * 而在这里简易将文件分包数据转交给FileDownloadController即可
 */
public class DownloadFileResponseController extends BaseMessageController
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
        int blockLength = packet.nextInt();
        byte[] block = packet.nextBytes(blockLength);

        session.sendFileFragment(block);

        return null;
    }
}
