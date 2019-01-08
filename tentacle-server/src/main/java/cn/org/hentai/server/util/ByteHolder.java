package cn.org.hentai.server.util;

import cn.org.hentai.tentacle.util.ByteUtils;

import java.util.Arrays;

/**
 * Created by matrixy on 2018-06-15.
 */
public class ByteHolder
{
    int offset = 0;
    int size = 0;
    byte[] buffer = null;

    public ByteHolder(int bufferSize)
    {
        this.buffer = new byte[bufferSize];
    }

    public int size()
    {
        return this.size;
    }

    public void write(byte[] data)
    {
        write(data, 0, data.length);
    }

    public void write(byte[] data, int offset, int length)
    {
        if (this.offset + length >= buffer.length)
            throw new RuntimeException(String.format("exceed the max buffer size, max length: %d, data length: %d", buffer.length, length));

        // 复制一下内容
        System.arraycopy(data, offset, buffer, this.offset, length);

        this.offset += length;
        this.size += length;
    }

    public byte[] array()
    {
        return Arrays.copyOf(this.buffer, this.size);
    }

    public void write(byte b)
    {
        this.buffer[offset++] = b;
        this.size += 1;
    }

    public void sliceInto(byte[] dest, int length)
    {
        System.arraycopy(this.buffer, 0, dest, 0, length);
        // 往前挪length个位
        System.arraycopy(this.buffer, length, this.buffer, 0, this.size - length);
        this.offset -= length;
        this.size -= length;
    }

    /**
     * 从buffer的position位置起，复制length个字节到dest数组中
     * @param dest 目标数组
     * @param position buffer的开始位置
     * @param length 从buffer复制的字节数
     */
    public void getBytes(byte[] dest, int position, int length)
    {
        System.arraycopy(this.buffer, position, dest, 0, length);
    }

    public void slice(int length)
    {
        // 往前挪length个位
        System.arraycopy(this.buffer, length, this.buffer, 0, this.size - length);
        this.offset -= length;
        this.size -= length;
    }

    public byte get(int position)
    {
        return this.buffer[position];
    }

    public int getInt(int position)
    {
        return ByteUtils.getInt(this.buffer, position, 4);
    }

    public void clear()
    {
        this.offset = 0;
        this.size = 0;
    }
}