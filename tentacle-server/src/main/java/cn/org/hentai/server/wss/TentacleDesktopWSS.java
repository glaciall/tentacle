package cn.org.hentai.server.wss;

import cn.org.hentai.server.rds.RDServer;
import cn.org.hentai.server.rds.RDSession;
import cn.org.hentai.server.util.ByteUtils;
import cn.org.hentai.tentacle.compress.RLEncoding;
import cn.org.hentai.tentacle.graphic.Screenshot;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.math.BigDecimal;
import java.nio.ByteBuffer;

/**
 * Created by matrixy on 2018/4/12.
 */
@Component
@ServerEndpoint("/tentacle/desktop/wss")
public class TentacleDesktopWSS
{
    Session session;
    RDSession rdSession = null;

    // 上一屏的图像
    int[] lastBitmap = null;

    @OnOpen
    public void onOpen(Session session)
    {
        System.out.println("websocket opened: " + session);
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message, Session session)
    {
        try
        {
            rdSession = RDServer.getCurrentSession();
            if (null == rdSession)
            {
                this.session.getBasicRemote().sendText("client is not connected yet");
                return;
            }
            rdSession.bind(this);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void sendScreenshot(byte[] screenshot)
    {
        try
        {
            if (!this.session.isOpen()) throw new Exception("websocket was closed");
            this.session.getBasicRemote().sendBinary(ByteBuffer.wrap(screenshot));
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @OnClose
    public void onClose()
    {
        System.out.println("websocket closed...");
    }

    @OnError
    public void onError(Session session, Throwable ex)
    {
        ex.printStackTrace();
    }
}
