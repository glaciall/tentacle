package cn.org.hentai.client.desktop;

import cn.org.hentai.client.client.Client;
import cn.org.hentai.tentacle.encrypt.MD5;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.Configs;
import cn.org.hentai.tentacle.util.Log;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * Created by matrixy on 2019/5/18.
 */
public class PacketDeliveryWorker extends Thread
{
    public PacketDeliveryWorker()
    {
        this.setName("packet-delivery-worker");
        PacketDeliveryWorker.instance = this;
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
        String key = Configs.get("client.key");
        final int FRAGMENT_SIZE = 30720;

        while (!this.isInterrupted())
        {
            try
            {
                Packet packet = ScreenImages.getInstance().getCompressedScreenshot();
                if (null == packet) break;

                packet.seek(12 + 11);
                int sequence = packet.nextInt();
                synchronized (PacketDeliveryWorker.class)
                {
                    Arrays.fill(PacketDeliveryWorker.instance.fragmentAckFlags, false);
                    PacketDeliveryWorker.instance.currentFrameSequence = sequence;
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
                    if (this.isInterrupted()) return;
                    int len = FRAGMENT_SIZE;
                    if (i == packetCount - 1) len = packet.size() - i * FRAGMENT_SIZE;
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
                    if (this.isInterrupted()) return;
                    long stime = System.currentTimeMillis();
                    int fragmentReceived = 0;
                    int packetResendCount = 0;
                    synchronized (PacketDeliveryWorker.class)
                    {
                        System.arraycopy(PacketDeliveryWorker.instance.fragmentAckFlags, 0, flags, 0, packetCount);
                    }
                    for (int i = 0; i < packetCount; i++)
                    {
                        if (this.isInterrupted()) return;
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
            catch(InterruptedException e)
            {
                break;
            }
            catch(Exception e)
            {
                Log.error(e);
            }
        }

        Log.debug(this.getName() + " terminated...");
    }

    boolean[] fragmentAckFlags = new boolean[10240];
    static int currentFrameSequence = 0;
    static PacketDeliveryWorker instance = null;
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
}
