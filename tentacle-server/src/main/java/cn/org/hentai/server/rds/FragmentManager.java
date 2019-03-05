package cn.org.hentai.server.rds;

import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.Log;

import java.util.LinkedList;

/**
 * Created by matrixy on 2019/3/2.
 * 分包碎片管理，收到新包时要通知受控端，在什么样的时机里，要通知受控端重发分包？
 */
public class FragmentManager
{
    private static FragmentManager instance = null;
    private FragmentManager()
    {

    }
    public static synchronized FragmentManager getInstance()
    {
        if (null == instance) instance = new FragmentManager();
        return instance;
    }

    // 最外层：链表？
    // 第一层：定义类：总包数，已经收到的包数量，有一个分包的数组，用来作分包的桶
    // 第二层：随便什么样的实体类，关键是每加一个分包，都要确定整体包是否完整
    int lastFrameSequence = -1;
    LinkedList<Image> cachedImages = new LinkedList<Image>();
    public void arrange(long sessionId, int sequence, int packetIndex, int packetCount, Packet packet)
    {
        // 如果已经拼够了，那就下发到浏览器端吧
        if (sequence <= lastFrameSequence) return;
        Image image = null;
        for (Image item : cachedImages)
        {
            if (item.sessionId != sessionId || item.sequence != sequence) continue;
            image = item;
            break;
        }
        if (image == null)
        {
            image = new Image(sessionId, sequence, packetCount);
            cachedImages.add(image);
        }
        image.addFragment(packetIndex, packet);
        if (image.isBroken()) return;

        cachedImages.remove(image);
        lastFrameSequence = sequence;
        TentacleDesktopSession session = SessionManager.getSession(sessionId);
        if (session != null) session.sendScreenshot(sequence, image.merge());

        // Log.debug("拼够一个包了，发到浏览器去了: " + sequence);
    }

    static class Image
    {
        private int sequence;
        private long sessionId;
        private int packetCount;
        private int packetReceived;
        private int totalBytes;
        private long createTime;

        public Packet[] fragments;

        public Image(long sessionId, int sequence, int packetCount)
        {
            this.sessionId = sessionId;
            this.sequence = sequence;
            this.packetCount = packetCount;

            this.fragments = new Packet[packetCount];
            this.createTime = System.currentTimeMillis();
        }

        // 添加分包
        public void addFragment(int packetIndex, Packet packet)
        {
            if (this.fragments[packetIndex] != null) return;
            this.fragments[packetIndex] = packet;
            this.packetReceived += 1;
            this.totalBytes += packet.size() - 8 - 4 - 2 - 2 - 32;
        }

        // 是否还是支离破碎的还没有接收完全？
        public boolean isBroken()
        {
            return packetCount != packetReceived;
        }

        // 是否缓存超时
        public boolean isExpired()
        {
            return System.currentTimeMillis() - createTime > 5000;
        }

        // 消息包整合
        public byte[] merge()
        {
            Packet p = Packet.create(totalBytes);
            for (int i = 0; i < packetCount; i++)
            {
                fragments[i].seek(8 + 4 + 2 + 2 + 32);
                p.addBytes(fragments[i].nextBytes());
                fragments[i] = null;
            }
            return p.getBytes();
        }
    }
}
