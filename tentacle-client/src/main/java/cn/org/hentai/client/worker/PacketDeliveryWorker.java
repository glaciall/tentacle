package cn.org.hentai.client.worker;

import cn.org.hentai.tentacle.graphic.Screenshot;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.Configs;
import cn.org.hentai.tentacle.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by matrixy on 2019/3/3.
 */
public class PacketDeliveryWorker extends BaseWorker
{
    Object lock = new Object();
    CompressWorker compressWorker = null;

    LinkedList<Packet> packets;
    boolean[] fragmentAckFlags = new boolean[10240];

    static PacketDeliveryWorker instance = null;

    public PacketDeliveryWorker(CompressWorker compressWorker)
    {
        this.compressWorker = compressWorker;
        this.packets = new LinkedList<Packet>();
        this.setName("packet-delivery-worker");
        PacketDeliveryWorker.instance = this;
    }

    public void send(Packet packet)
    {
        synchronized (lock)
        {
            packets.add(packet);
            lock.notifyAll();
        }
    }

    public static void fragmentReceived(int sequence, int packetIndex)
    {
        instance.fragmentAckFlags[packetIndex] = true;
    }

    public void run()
    {
        InetAddress serverAddr = null;
        int serverPort = Configs.getInt("server.udp-port", 1987);
        try
        {
            serverAddr = InetAddress.getByName(Configs.get("server.addr"));
        }
        catch(Exception e)
        {
            Log.error(e);
        }

        while (!this.isTerminated())
        {
            try
            {
                Packet packet = null;
                synchronized (lock)
                {
                    lock.wait();
                    if (packet.size() > 0) packet = packets.removeFirst();
                }
                if (null == packet) continue;

                packet.seek(12);
                int sequence = packet.nextInt();
                Arrays.fill(fragmentAckFlags, false);
                // 分包发送
                // 循环遍历，是否已经全部发送完毕了？
                // 加个重发次数计数
                int packetCount = (int)Math.ceil(packet.size() / 1024f);
                DatagramSocket socket = new DatagramSocket();
                socket.setSendBufferSize(4096 * 100);
                for (int i = 0; i < packetCount; i++)
                {
                    int len = 1024;
                    if (i + len > packet.size()) len = packet.size() - i;
                    byte[] fragment = packet.nextBytes(len);
                    DatagramPacket p = new DatagramPacket(fragment, fragment.length, serverAddr, serverPort);
                    socket.send(p);
                    // if (i % 100 == 99) sleep(5);
                }

                // 检查各分包是否已经到达，同时需要注意是否有超时
                boolean expired = false;
                for (int k = 0; k < 1000; k++)
                {
                    Log.debug(String.format("resend sequence[%d] for %d times", sequence, k));
                    long stime = System.currentTimeMillis();
                    int fragmentReceived = 0;
                    for (int i = 0; i < packetCount; i++)
                    {
                        if (fragmentAckFlags[i])
                        {
                            fragmentReceived += 1;
                            continue;
                        }

                        // 补发分包
                        packet.seek(i * 1024);
                        int len = 1024;
                        if (i + len > packet.size()) len = packet.size() - i;
                        byte[] fragment = packet.nextBytes(len);
                        DatagramPacket p = new DatagramPacket(fragment, fragment.length, serverAddr, serverPort);
                        socket.send(p);
                    }
                    if (fragmentReceived == packetCount) break;
                    // 最低停留5毫秒
                    long time = System.currentTimeMillis() - stime;
                    if (time > 5) continue;
                    else sleep(5);
                }

                System.exit(0);
                return;
            }
            catch(Exception e)
            {
                Log.error(e);
            }
        }
    }

    @Override
    public void terminate()
    {
        super.terminate();
        synchronized (lock)
        {
            lock.notifyAll();
        }
    }
}
