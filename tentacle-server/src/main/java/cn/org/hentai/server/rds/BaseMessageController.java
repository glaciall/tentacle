package cn.org.hentai.server.rds;

import cn.org.hentai.tentacle.protocol.Message;

/**
 * Created by matrixy on 2019/1/3.
 * 消息指令处理器
 */
public abstract class BaseMessageController
{
    private boolean disconnect = false;

    /**
     * 是否在当前消息对话结束后是否断开与受控端的连接
     * 此方法交于BaseMessageController的实现者调用
     */
    protected final void replyAndDisconnect()
    {
        disconnect = true;
    }

    /**
     * 指示TentacleDesktopHandler是否应当在当前消息对话结束后断开与受控端的连接
     * 此方法由TentacleDesktopHandler.handle()调用并处理
     * @return 返回值由replyAndDisconnect()方法指定
     */
    public final boolean shouldDisconnectAfterConverse()
    {
        return disconnect;
    }

    /**
     * 指示当前指令处理器是否需要认证
     * 认证与否取决于getClient() != null
     * 开发者可在任意Controller里通过session.setClient()来设置客户端信息
     * @return
     */
    public abstract boolean authenticateRequired();

    /**
     * 处理消息，返回响应消息
     * @param session
     * @param msg
     * @return 如果返回null，则不回应客户端
     * @throws Exception
     */
    public abstract Message service(TentacleDesktopSession session, Message msg) throws Exception;
}
