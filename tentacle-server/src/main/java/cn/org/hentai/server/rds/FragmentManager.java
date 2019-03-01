package cn.org.hentai.server.rds;

import cn.org.hentai.tentacle.protocol.Packet;

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
    LinkedList<Image> cachedImages = new LinkedList<Image>();
    public void add(Packet packet)
    {
        // 如果已经拼够了，那就下发到浏览器端吧
    }

    static class Image
    {
        private int sequence;
        private long sessionId;
        private int packetCount;
        private int packetReceived;

        public Packet[] fragments;

        public Image(long sessionId, int sequence, int packetCount)
        {
            this.sessionId = sessionId;
            this.sequence = sequence;
            this.packetCount = packetCount;

            this.fragments = new Packet[packetCount];
        }

        // 添加分包
        public void addFragment(int packetIndex, Packet packet)
        {
            if (this.fragments[packetIndex] != null) return;
            this.fragments[packetIndex] = packet;
            this.packetReceived += 1;
        }

        // 是否还是支离破碎的还没有接收完全？
        public boolean isBroken()
        {
            return packetCount != packetReceived;
        }

        // TODO: 消息包整合
        public byte[] merge()
        {
            return null;
        }
    }
}
