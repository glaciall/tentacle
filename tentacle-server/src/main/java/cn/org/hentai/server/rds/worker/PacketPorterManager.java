package cn.org.hentai.server.rds.worker;

import cn.org.hentai.server.rds.FragmentManager;
import cn.org.hentai.server.rds.SessionManager;
import cn.org.hentai.server.rds.TentacleDesktopSession;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.Log;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by matrixy on 2019/3/2.
 * 负责对会话所收到的零散的消息包进行拼装组合
 */
public final class PacketPorterManager
{
    PacketPorter[] porters;

    private PacketPorterManager()
    {

    }

    public void init()
    {
        if (porters != null) return;
        porters = new PacketPorter[Runtime.getRuntime().availableProcessors()];
        int i = 0;
        for (PacketPorter porter : porters)
        {
            porter.setName("packet-porter-" + (i++));
            porter.start();
        }
    }

    static PacketPorterManager instance = null;
    public static synchronized PacketPorterManager getInstance()
    {
        if (instance == null)
        {
            instance = new PacketPorterManager();
        }
        return instance;
    }

    public void receiveAndReform(Packet packet)
    {
        porters[(int)Math.abs(packet.nextLong() % porters.length)].dispatch(packet);
    }

    static class PacketPorter extends Thread
    {
        Object lock = new Object();
        LinkedList<Packet> packets = new LinkedList<Packet>();

        public void dispatch(Packet packet)
        {
            synchronized (lock)
            {
                packets.add(packet);
                lock.notifyAll();
            }
        }

        private void checkAndReform(Packet packet)
        {
            packet.rewind();
            // 检查新消息包的合法性
            long sessionId = packet.nextLong();
            int sequence = packet.nextInt();
            int packetIndex = packet.nextShort() & 0xffff;
            int packetCount = packet.nextShort() & 0xffff;
            String secret = new String(packet.nextBytes(32));

            TentacleDesktopSession session = SessionManager.getSession(sessionId);
            if (null == session) return;
            if (session.checkSecret(sequence, packetIndex, secret) == false) return;

            // 将其加入到某某树里去
            FragmentManager.getInstance().arrange(sessionId, sequence, packetIndex, packetCount, packet);

            // 向受控端发送确认消息
            session.replyForPacket(sequence, packetIndex);
        }

        public void run()
        {
            while (!this.isInterrupted())
            {
                try
                {
                    Packet p = null;
                    synchronized (lock)
                    {
                        lock.wait();
                        if (packets.size() > 0) p = packets.removeFirst();
                    }
                    if (p != null) checkAndReform(p);
                }
                catch(Exception e)
                {
                    Log.error(e);
                }
            }
        }
    }
}
