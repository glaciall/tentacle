package cn.org.hentai.server.rds.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by matrixy on 2019/3/2.
 */
public class Test
{
    public static void main(String[] args) throws Exception
    {
        DatagramSocket socket = new DatagramSocket(1221);
        // 65507
        DatagramPacket p = new DatagramPacket(new byte[0], 1023, null, 1020);
    }
}
