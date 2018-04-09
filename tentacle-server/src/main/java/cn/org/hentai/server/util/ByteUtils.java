package cn.org.hentai.server.util;

import java.io.FileOutputStream;

/**
 * Created by matrixy on 2017/8/22.
 */
public final class ByteUtils
{
    public static byte[] parse(String hexString)
    {
        String[] hexes = hexString.split(" ");
        byte[] data = new byte[hexes.length];
        for (int i = 0; i < hexes.length; i++) data[i] = (byte)(Integer.parseInt(hexes[i], 16) & 0xff);
        return data;
    }

    public static String toString(byte[] data)
    {
        return toString(data, data.length);
    }

    public static String toString(byte[] buff, int length)
    {
        StringBuffer sb = new StringBuffer(length * 2);
        for (int i = 0; i < length; i++)
        {
            if ((buff[i] & 0xff) < 0x10) sb.append('0');
            sb.append(Integer.toHexString(buff[i] & 0xff).toUpperCase());
            sb.append(' ');
        }
        return sb.toString();
    }

    public static boolean getBit(int val, int pos)
    {
        return getBit(new byte[] {
                (byte)((val >> 0) & 0xff),
                (byte)((val >> 8) & 0xff),
                (byte)((val >> 16) & 0xff),
                (byte)((val >> 24) & 0xff)
        }, pos);
    }

    public static int reverse(int val)
    {
        byte[] bytes = toBytes(val);
        byte[] ret = new byte[4];
        for (int i = 0; i < 4; i++) ret[i] = bytes[3 - i];
        return toInt(ret);
    }

    public static int toInt(byte[] bytes)
    {
        int val = 0;
        for (int i = 0; i < 4; i++) val |= (bytes[i] & 0xff) << ((3 - i) * 8);
        return val;
    }

    public static byte[] toBytes(int val)
    {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++)
        {
            bytes[i] = (byte)(val >> ((3 - i) * 8) & 0xff);
        }
        return bytes;
    }

    public static int getInt(byte[] data, int offset, int length)
    {
        int val = 0;
        for (int i = 0; i < length; i++) val |= (data[offset + i] & 0xff) << ((length - i - 1) * 8);
        return val;
    }

    public static long getLong(byte[] data, int offset, int length)
    {
        long val = 0;
        for (int i = 0; i < length; i++) val |= ((long)data[offset + i] & 0xff) << ((length - i - 1) * 8);
        return val;
    }

    public static boolean getBit(byte[] data, int pos)
    {
        return ((data[pos / 8] >> (pos % 8)) & 0x01) == 0x01;
    }

    public static void main(String[] args) throws Exception
    {
        System.out.println(Integer.toHexString(ByteUtils.reverse((0x41fa3be1))));
    }

    public static void mains(String[] args) throws Exception
    {
        String[] hex = "0B 01 01 00 01 86 01 02 00 00 02 03 00 00 00 03 08 6C 3A FA 41 2D C7 F0 42 05 02 00 00 BA AC".split(" ");
        byte[] data = new byte[hex.length];
        for (int i = 0; i < hex.length; i++) data[i] = (byte)(Integer.parseInt(hex[i], 16) & 0xff);
        System.out.println("Bytes: " + data.length);
        System.out.println(ByteUtils.toString(data));
        System.out.println(ByteUtils.getLong(data, 0, 5));

        long deviceId = ByteUtils.getLong(data, 0, 5);
        int voltage = ByteUtils.getInt(data, 8, 2);
        int currentIndex = data[12];
        int currentAh = ByteUtils.getInt(data, 13, 2);
        int latitude = ByteUtils.getInt(data, 17, 4);
        int longitude = ByteUtils.getInt(data, 21, 4);
        int state = ByteUtils.getInt(data, 27, 2);
        int crc = ByteUtils.getInt(data, 29, 2);

        System.out.println("Voltage: " + voltage);
        System.out.println("CurrentIndex: " + currentIndex);
        System.out.println("Current Ah: " + currentAh);
        System.out.println("Longitude: " + longitude);
        System.out.println("Latitude: " + latitude);
        System.out.println("State: " + state);
        System.out.println("CRC: " + Integer.toHexString(crc));

        new FileOutputStream("d:\\fuck3.dat").write(data);
    }

    public static byte[] concat(byte[] bytes, byte[] data)
    {
        byte[] buff = new byte[bytes.length + data.length];
        System.arraycopy(bytes, 0, buff, 0, bytes.length);
        System.arraycopy(data, 0, buff, bytes.length, data.length);
        return buff;
    }
}
