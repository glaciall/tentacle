package cn.org.hentai.server.rds.coder;

import cn.org.hentai.server.util.ByteUtils;
import cn.org.hentai.tentacle.protocol.Message;
import cn.org.hentai.tentacle.protocol.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledDirectByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by matrixy on 2019/1/3.
 */
public class MessageDecoder extends ByteToMessageDecoder
{
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        /**
         *   p.data[0] = 'H';
             p.data[1] = 'E';
             p.data[2] = 'N';
             p.data[3] = 'T';
             p.data[4] = 'A';
             p.data[5] = 'I';
             p.data[6] = command;
             p.data[7:10] = int length
             .... data body ....
         */
        if (in.readableBytes() < 12) return;

        byte[] header = new byte[6];
        in.getBytes(0, header);
        if ("HENTAI".equals(new String(header)) == false)
            throw new RuntimeException("wrong protocol header: " + ByteUtils.toString(header));

        byte command = in.getByte(6);
        int length = in.getInt(7);
        if (in.readableBytes() < length + 6 + 1 + 4) return;

        byte[] body = new byte[length];
        in.readBytes(6 + 1 + 4);
        in.readBytes(body);

        out.add(new Message().withCommand(command).withBody(Packet.create(body)));
    }
}
