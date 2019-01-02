package cn.org.hentai.server.rds;

import cn.org.hentai.server.util.Configs;
import cn.org.hentai.tentacle.util.Log;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by matrixy on 2018/4/15.
 */
public class RemoteDesktopServer extends Thread
{
    private static RemoteDesktopServer instance = null;
    private static final ConcurrentLinkedQueue<RemoteDesktopSession> sessions = new ConcurrentLinkedQueue<RemoteDesktopSession>();
    private long sequence = 1;

    public RemoteDesktopServer()
    {
        if (instance != null) throw new RuntimeException("RemoteDesktopServer was started once before");
        instance = this;
    }

    private void listen() throws Exception
    {
        ServerSocket server = new ServerSocket(Configs.getInt("rds.server.port", 1986), 100, InetAddress.getByName("0.0.0.0"));
        while (!Thread.interrupted())
        {
            Socket conn = server.accept();
            RemoteDesktopSession session = new RemoteDesktopSession(conn);
            session.setId(sequence++);
            sessions.add(session);
            session.start();
            Log.info("Client: " + conn.getInetAddress() + " connected...");
        }
    }

    public static RemoteDesktopSession getSession(long sessionId)
    {
        for (RemoteDesktopSession session : sessions)
        {
            if (session.getId() == sessionId)
            {
                return session;
            }
        }
        return null;
    }

    public static RemoteDesktopSession[] activeSessions()
    {
        return (RemoteDesktopSession[]) sessions.toArray();
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
