package cn.org.hentai.server.app;

import cn.org.hentai.server.rds.TentacleDesktopSession;
import cn.org.hentai.tentacle.util.Configs;
import cn.org.hentai.tentacle.util.Log;

import java.net.*;

/**
 * Created by matrixy on 2019/1/3.
 */
public final class RemoteDesktopApp
{
    public static void init() throws Exception
    {
        ServerSocket server = new ServerSocket(Configs.getInt("rds.server.port", 1986), 100, InetAddress.getByName("0.0.0.0"));
        while (true)
        {
            Socket conn = server.accept();
            Log.debug(String.format("Tentacle Desktop Connected from: %s", conn.getInetAddress().toString()));
            TentacleDesktopSession session = new TentacleDesktopSession(conn);
            session.start();
        }
    }
}
