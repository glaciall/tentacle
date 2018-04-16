package cn.org.hentai.server.rds;

import cn.org.hentai.server.util.Configs;
import cn.org.hentai.server.util.Log;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by matrixy on 2018/4/15.
 */
public class RDServer extends Thread
{
    private static RDServer instance = null;
    private RDSession currentSession = null;

    public RDServer()
    {
        if (instance != null) throw new RuntimeException("RDServer was started once before");
        instance = this;
    }

    private void listen() throws Exception
    {
        ServerSocket server = new ServerSocket(Configs.getInt("rds.server.port", 1986), 100, InetAddress.getByName("0.0.0.0"));
        while (!Thread.interrupted())
        {
            Socket conn = server.accept();
            (currentSession = new RDSession(conn)).start();
            Log.info("Client: " + conn.getInetAddress() + " connected...");
        }
    }

    public static RDSession getCurrentSession()
    {
        return instance.currentSession;
    }

    public void run()
    {
        while (!Thread.interrupted())
        {
            try
            {
                listen();
                Thread.sleep(2000);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
