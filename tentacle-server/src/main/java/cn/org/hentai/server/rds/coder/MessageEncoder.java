package cn.org.hentai.server.rds.coder;

import cn.org.hentai.tentacle.protocol.Message;
import cn.org.hentai.tentacle.protocol.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by matrixy on 2019/1/3.
 */
public class MessageEncoder extends MessageToByteEncoder<Message>
{
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception
    {
        // 协议头
        out.writeBytes("HENTAI".getBytes());
        // Command
        out.writeByte(msg.getCommand());
        // 消息体长度
        out.writeInt(msg.getBody().size());
        // 消息体
        if (msg.getBody().size() > 0)
            out.writeBytes(msg.getBody().getBytes());
    }
}
