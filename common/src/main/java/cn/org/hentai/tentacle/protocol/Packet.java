package cn.org.hentai.tentacle.protocol;

import cn.org.hentai.tentacle.util.ByteUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by matrixy on 2018/4/14.
 */
public class Packet
{
    int size = 0;
    int offset = 0;
    int maxSize = 0;
    public byte[] data;

    private Packet()
    {
        // do nothing here..
    }

    /**
     * 创建协议数据包
     * @param command 指令，参见cn.org.hentai.tentacle.protocol.Command类
     * @param length 数据包的长度
     * @return
     */
    public static Packet create(byte command, int length)
    {
        Packet p = new Packet();
        p.data = new byte[length + 6 + 1 + 4];
        p.data[0] = 'H';
        p.data[1] = 'E';
        p.data[2] = 'N';
        p.data[3] = 'T';
        p.data[4] = 'A';
        p.data[5] = 'I';
        p.data[6] = command;
        System.arraycopy(ByteUtils.toBytes(length), 0, p.data, 7, 4);
        p.size = 11;
        p.maxSize = length;
        return p;
    }

    public static Packet create(int length)
    {
        Packet p = new Packet();
        p.data = new byte[length];
        p.size = 0;
        p.maxSize = length;
        return p;
    }

    public static Packet create(byte[] data)
    {
        Packet p = new Packet();
        p.data = data;
        p.size = data.length;
        p.maxSize = data.length;
        return p;
    }

    /**
     * 从流中读取并建立一个数据包
     * @param inputStream
     * @return
     */
    public static Packet read(InputStream inputStream) throws Exception
    {
        if (inputStream.available() < 11) return null;
        byte[] head = new byte[11];
        int len = inputStream.read(head);
        int dataLength = ByteUtils.getInt(head, 7,4) & 0x7fffff;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(dataLength + 10);
        byte[] buff = new byte[512];
        for (int i = 0; i < dataLength; i += len)
        {
            len = inputStream.read(buff, 0, Math.min(512, dataLength - i));
            if (len == -1) break;
            baos.write(buff, 0, len);
        }
        Packet p = new Packet();
        p.data = new byte[dataLength + 6 + 1 + 4];
        p.size = 0;
        p.maxSize = p.size;
        p.addBytes(head);
        p.addBytes(baos.toByteArray());
        return p;
    }

    public int size()
    {
        return this.size;
    }

    public Packet addByte(byte b)
    {
        this.data[size++] = b;
        return this;
    }

    public Packet addShort(short s)
    {
        this.data[size++] = (byte)((s >> 8) & 0xff);
        this.data[size++] = (byte)(s & 0xff);
        return this;
    }

    public Packet addInt(int i)
    {
        this.data[size++] = (byte)((i >> 24) & 0xff);
        this.data[size++] = (byte)((i >> 16) & 0xff);
        this.data[size++] = (byte)((i >> 8) & 0xff);
        this.data[size++] = (byte)(i & 0xff);
        return this;
    }

    public Packet addLong(long l)
    {
        this.data[size++] = (byte)((l >> 56) & 0xff);
        this.data[size++] = (byte)((l >> 48) & 0xff);
        this.data[size++] = (byte)((l >> 40) & 0xff);
        this.data[size++] = (byte)((l >> 32) & 0xff);
        this.data[size++] = (byte)((l >> 24) & 0xff);
        this.data[size++] = (byte)((l >> 16) & 0xff);
        this.data[size++] = (byte)((l >> 8) & 0xff);
        this.data[size++] = (byte)(l & 0xff);
        return this;
    }

    public Packet addBytes(byte[] b)
    {
        System.arraycopy(b, 0, this.data, size, b.length);
        size += b.length;
        return this;
    }

    public Packet addBytes(byte[] b, int offset, int length)
    {
        System.arraycopy(b, offset, this.data, size, length);
        size += length;
        return this;
    }

    public Packet reset()
    {
        this.offset = 0;
        this.size = 0;
        return this;
    }

    public Packet rewind()
    {
        this.offset = 0;
        return this;
    }

    public byte nextByte()
    {
        return this.data[offset++];
    }

    public short nextShort()
    {
        return (short)(((this.data[offset++] & 0xff) << 8) | (this.data[offset++] & 0xff));
    }

    public int nextInt()
    {
        return (this.data[offset++] & 0xff) << 24 | (this.data[offset++] & 0xff) << 16 | (this.data[offset++] & 0xff) << 8 | (this.data[offset++] & 0xff);
    }

    public long nextLong()
    {
        return ((long)this.data[offset++] & 0xff) << 56
                | ((long)this.data[offset++] & 0xff) << 48
                | ((long)this.data[offset++] & 0xff) << 40
                | ((long)this.data[offset++] & 0xff) << 32
                | ((long)this.data[offset++] & 0xff) << 24
                | ((long)this.data[offset++] & 0xff) << 16
                | ((long)this.data[offset++] & 0xff) << 8
                | ((long)this.data[offset++] & 0xff);
    }

    public byte[] nextBytes(int length)
    {
        byte[] buf = new byte[length];
        System.arraycopy(this.data, offset, buf, 0, length);
        offset += length;
        return buf;
    }

    public Packet skip(int offset)
    {
        this.offset += offset;
        return this;
    }

    public Packet seek(int index)
    {
        this.offset = index;
        return this;
    }

    public byte[] getBytes()
    {
        if (size == maxSize) return this.data;
        else
        {
            byte[] buff = new byte[size];
            System.arraycopy(this.data, 0, buff, 0, size);
            return buff;
        }
    }

    public static void main(String[] args) throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(ByteUtils.parse("01 02 03 04 05 06 07 00 00 00 11 12 13 14 15 16 17 18 19 20 21 22 23 24 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40"));
        Packet p = Packet.read(bais);
        System.out.println(ByteUtils.toString(p.nextBytes(5)));
        System.out.println(ByteUtils.toString(p.nextBytes(5)));
    }

    public static void main_packet_rw(String[] args) throws Exception
    {
        Packet p = Packet.create(Command.HID_COMMAND, 16);
        p.addByte((byte)0x11).addShort((short)0x2233).addInt(0x44556677).addLong(0x8899aabbccddeeffL).addByte((byte)0xfa);
        for (int i = 0; i < 3; i++)
            System.out.print(Long.toHexString(p.nextLong()) + " ");
        System.out.println();
        System.out.println(ByteUtils.toString(p.getBytes()));
    }

    /**
     * 复制len个字节，到dest的offset位置处
     * @param dest
     * @param offset
     * @param len
     */
    public void copyBytes(byte[] dest, int offset, int len)
    {
        System.arraycopy(this.data, this.offset, dest, offset, len);
        this.offset += len;
    }
}
