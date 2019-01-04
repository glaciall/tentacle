package cn.org.hentai.server.rds;

import cn.org.hentai.server.rds.TentacleDesktopHandler;
import cn.org.hentai.tentacle.protocol.Message;

/**
 * Created by matrixy on 2019/1/3.
 * 消息指令处理器
 */
public abstract class BaseMessageController
{
    private boolean disconnect = false;

    protected final void replyAndDisconnect()
    {
        disconnect = true;
    }

    public boolean shouldDisconnectAfterConverse()
    {
        return disconnect;
    }

    /**
     * 指示当前指令是否需要认证
     * @return
     */
    public abstract boolean authenticateRequired();

    /**
     * 处理消息，返回响应消息
     * @param handler
     * @param msg
     * @return 如果返回null，则不回应客户端
     * @throws Exception
     */
    public abstract Message service(TentacleDesktopHandler handler, Message msg) throws Exception;
}
