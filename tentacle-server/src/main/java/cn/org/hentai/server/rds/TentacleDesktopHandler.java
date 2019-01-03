package cn.org.hentai.server.rds;

import cn.org.hentai.server.rds.controller.AuthenticateController;
import cn.org.hentai.server.rds.controller.BaseMessageController;
import cn.org.hentai.server.rds.controller.ControlRequestController;
import cn.org.hentai.server.rds.controller.HeartbeatController;
import cn.org.hentai.tentacle.protocol.Command;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import cn.org.hentai.tentacle.protocol.Message;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matrixy on 2019/1/3.
 */
public class TentacleDesktopHandler extends SimpleChannelInboundHandler<Message>
{
    private static final AttributeKey<Client> SESSION_KEY = AttributeKey.valueOf("session-key");
    private ChannelHandlerContext context;
    private static final Map<Byte, Class<? extends BaseMessageController>> controllers = new HashMap();

    @Override
    protected final void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception
    {
        this.context = ctx;
        handle(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        // super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        super.channelInactive(ctx);
        onDisconnect();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        super.channelActive(ctx);
        onConnected();
    }

    // 当连接失效时触发
    protected void onDisconnect()
    {
        SessionManager.removeSession(this);
    }

    // 当连接建立时触发
    protected void onConnected()
    {
        // do nothing here...
    }

    public SocketAddress getRemoteAddress()
    {
        return this.context.channel().remoteAddress();
    }

    public final Client getClient()
    {
        Attribute<Client> attr = context.channel().attr(SESSION_KEY);
        if (attr == null) return null;
        else return attr.get();
    }

    public final void setClient(Client info)
    {
        context.channel().attr(SESSION_KEY).set(info);
    }

    public final void send(Message msg)
    {
        context.writeAndFlush(msg);
    }

    protected void handle(Message msg)
    {
        BaseMessageController controller = getController(msg.getCommand());
        if (null == controller)
        {
            throw new RuntimeException(String.format("unknown command: %x", msg.getCommand()));
        }

        try
        {
            Message resp = controller.service(this, msg);
            if (resp != null) this.send(resp);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    // 注册消息指令处理器
    // 每增加一个新的指令，必须在此添加相应的消息指令控制器
    static
    {
        controllers.put(Command.AUTHENTICATE, AuthenticateController.class);
        controllers.put(Command.CONTROL_REQUEST, ControlRequestController.class);
        controllers.put(Command.HEARTBEAT, HeartbeatController.class);
    }

    private static BaseMessageController getController(byte command)
    {
        Class cls = controllers.get(command);
        try
        {
            if (cls != null) return (BaseMessageController) cls.newInstance();
            else return null;
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}