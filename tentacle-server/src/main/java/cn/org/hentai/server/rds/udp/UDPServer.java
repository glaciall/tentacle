package cn.org.hentai.server.rds.udp;

import cn.org.hentai.server.rds.SessionManager;
import cn.org.hentai.server.rds.TentacleDesktopSession;
import cn.org.hentai.server.rds.worker.PacketPorterManager;
import cn.org.hentai.server.util.Configs;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by matrixy on 2019/3/2.
 */
public class UDPServer extends Thread
{
    public UDPServer()
    {
        this.setName("udp-server");
    }

    public void run()
    {
        try
        {
            int port = Configs.getInt("rds.server.udp-port", 1987);

            DatagramSocket server = new DatagramSocket(port);
            server.setReceiveBufferSize(4096 * 100);

            DatagramPacket p = new DatagramPacket(new byte[32000], 32000);
            while (!this.isInterrupted())
            {
                server.receive(p);
                byte[] data = new byte[p.getLength()];
                System.arraycopy(p.getData(), 0, data, 0, p.getLength());
                try { dispatch(data); } catch(Exception e) { Log.error(e); }
            }
        }
        catch(Exception e)
        {
            Log.error(e);
        }
    }

    private void dispatch(byte[] data) throws Exception
    {
        Packet packet = Packet.create(data);
        PacketPorterManager.getInstance().receiveAndReform(packet);
    }

    public static void init()
    {
        new UDPServer().start();
    }
}
