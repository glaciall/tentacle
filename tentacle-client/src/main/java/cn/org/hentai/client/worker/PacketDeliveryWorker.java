package cn.org.hentai.client.worker;

import cn.org.hentai.client.client.Client;
import cn.org.hentai.tentacle.encrypt.MD5;
import cn.org.hentai.tentacle.graphic.Screenshot;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.ByteUtils;
import cn.org.hentai.tentacle.util.Configs;
import cn.org.hentai.tentacle.util.Log;
import cn.org.hentai.tentacle.util.Nonce;

import java.net.*;
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

    static int currentFrameSequence = 0;
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
        synchronized (PacketDeliveryWorker.class)
        {
            if (instance.currentFrameSequence != sequence)
            {
                return;
            }
            instance.fragmentAckFlags[packetIndex] = true;
        }
    }

    // TODO: 通知整包确认接收

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
        String key = Configs.get("client.key");
        final int FRAGMENT_SIZE = 30720;

        while (!this.isTerminated())
        {
            try
            {
                Packet packet = null;
                synchronized (lock)
                {
                    while (packets.size() == 0) lock.wait();
                    packet = packets.removeFirst();
                }

                packet.seek(12 + 11);
                int sequence = packet.nextInt();
                synchronized (PacketDeliveryWorker.class)
                {
                    Arrays.fill(instance.fragmentAckFlags, false);
                    instance.currentFrameSequence = sequence;
                }
                // 分包发送
                // 循环遍历，是否已经全部发送完毕了？
                // 加个重发次数计数
                int packetCount = (int)Math.ceil(packet.size() / (float)FRAGMENT_SIZE);
                DatagramSocket socket = new DatagramSocket();
                socket.setSendBufferSize(FRAGMENT_SIZE * 100);
                socket.connect(serverAddr, serverPort);
                packet.rewind();
                for (int i = 0; i < packetCount; i++)
                {
                    int len = FRAGMENT_SIZE;
                    if (i == packetCount - 1) len = packet.size() - i * FRAGMENT_SIZE;
                    // TODO: 丢掉了92字节，明天再跟，可能是48的n倍关系
                    Packet fragment = Packet.create(8 + 4 + 2 + 2 + 32 + len);
                    fragment.addLong(Client.getCurrentSessionId());
                    fragment.addInt(sequence);
                    fragment.addShort((short)i);
                    fragment.addShort((short)packetCount);
                    fragment.addBytes(MD5.encode(Client.getCurrentSessionSecret() + ":::" + sequence + ":::" + i + ":::" + key).getBytes());
                    fragment.addBytes(packet.nextBytes(len));
                    DatagramPacket p = new DatagramPacket(fragment.getBytes(), fragment.size());
                    socket.send(p);
                }

                // 检查各分包是否已经到达，同时需要注意是否有超时
                sleep(20);
                boolean[] flags = new boolean[packetCount];
                for (int k = 0; k < 1000; k++)
                {
                    long stime = System.currentTimeMillis();
                    int fragmentReceived = 0;
                    int packetResendCount = 0;
                    synchronized (PacketDeliveryWorker.class)
                    {
                        System.arraycopy(instance.fragmentAckFlags, 0, flags, 0, packetCount);
                    }
                    for (int i = 0; i < packetCount; i++)
                    {
                        if (flags[i])
                        {
                            fragmentReceived += 1;
                            continue;
                        }

                        // 补发分包
                        packet.seek(i * FRAGMENT_SIZE);
                        int len = FRAGMENT_SIZE;
                        if (i == packetCount - 1) len = packet.size() - i * FRAGMENT_SIZE;

                        Packet fragment = Packet.create(8 + 4 + 2 + 2 + 32 + len);
                        fragment.addLong(Client.getCurrentSessionId());
                        fragment.addInt(sequence);
                        fragment.addShort((short)i);
                        fragment.addShort((short)packetCount);
                        fragment.addBytes(MD5.encode(Client.getCurrentSessionSecret() + ":::" + sequence + ":::" + i + ":::" + key).getBytes());
                        fragment.addBytes(packet.nextBytes(len));
                        DatagramPacket p = new DatagramPacket(fragment.getBytes(), fragment.size(), serverAddr, serverPort);
                        socket.send(p);

                        packetResendCount += 1;
                    }
                    // Log.debug(String.format("resend sequence[%d] for %3d times and %3d packets resend, total: %4d", sequence, k, packetResendCount, packetCount));
                    if (fragmentReceived == packetCount) break;
                    // 最低停留10毫秒
                    long time = System.currentTimeMillis() - stime;
                    if (time >= 5) continue;
                    else sleep(Math.min(5, Math.max(0, 5 - time)));
                }
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
