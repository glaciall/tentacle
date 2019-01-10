package cn.org.hentai.server.rds.controller;

import cn.org.hentai.server.rds.BaseMessageController;
import cn.org.hentai.server.rds.TentacleDesktopSession;
import cn.org.hentai.server.rds.TentacleDesktopSessionHandler;
import cn.org.hentai.tentacle.protocol.Message;
import cn.org.hentai.tentacle.system.File;
import cn.org.hentai.tentacle.util.ByteUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matrixy on 2019/1/4.
 * 受控端对于列出文件列表应答的处理
 */
public class ListFileResponseController extends BaseMessageController
{
    @Override
    public boolean authenticateRequired()
    {
        return true;
    }

    @Override
    public Message service(TentacleDesktopSession session, Message msg) throws Exception
    {
        byte[] data = msg.getBodyBytes();
        List<File> files = new ArrayList<File>();
        for (int i = 0; i < data.length; )
        {
            boolean isDirectory = data[i] == 1;
            long length = ByteUtils.getLong(data, i += 1, 8);
            long mtime = ByteUtils.getLong(data, i += 8, 8);
            int strlen = ByteUtils.getInt(data, i += 8, 4);
            String name = new String(data, i += 4, strlen, "UTF-8");
            i += strlen;
            files.add(new File(isDirectory, length, mtime, name));
        }
        System.out.println("Files: " + String.valueOf(files));
        session.getWebsocketContext().sendFiles(files);

        return null;
    }
}
