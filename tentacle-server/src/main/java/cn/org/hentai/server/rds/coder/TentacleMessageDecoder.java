package cn.org.hentai.server.rds.coder;

import cn.org.hentai.server.util.ByteHolder;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Message;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.ByteUtils;

import java.util.Arrays;

/**
 * Created by matrixy on 2019/1/8.
 */
public final class TentacleMessageDecoder
{
    private static final byte[] PROTOCOL_HEADER = "HENTAI".getBytes();
    private static final byte[] PROTOCOL_TAIL = new byte[] { (byte)0xFA, (byte)0xFA, (byte)0xFA };

    public static Message read(ByteHolder buffer)
    {
        if (buffer.size() < 12) return null;

        byte[] header = new byte[6];
        buffer.getBytes(header, 0, header.length);
        if (Arrays.equals(PROTOCOL_HEADER, header) == false)
        {
            throw new RuntimeException("wrong protocol header: " + ByteUtils.toString(header));
        }

        byte command = buffer.get(6);
        int length = buffer.getInt(7);

        if (buffer.size() < length + 6 + 1 + 4 + 3) return null;

        byte[] body = new byte[length];
        buffer.slice(6 + 1 + 4);
        if (length > 0)
        {
            buffer.sliceInto(body, body.length);
        }
        byte[] tail = new byte[3];
        buffer.sliceInto(tail, tail.length);
        if (Arrays.equals(PROTOCOL_TAIL, tail) == false)
        {
            throw new RuntimeException("wrong protocol tail: " + ByteUtils.toString(header));
        }

        return new Message().withCommand(command).withBody(Packet.create(body));
    }
}
