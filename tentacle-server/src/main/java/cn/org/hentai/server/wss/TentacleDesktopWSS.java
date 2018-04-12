package cn.org.hentai.server.wss;

import cn.org.hentai.server.util.ByteUtils;
import cn.org.hentai.tentacle.compress.RLEncoding;
import cn.org.hentai.tentacle.graphic.Screenshot;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.nio.ByteBuffer;

/**
 * Created by matrixy on 2018/4/12.
 */
@Component
@ServerEndpoint("/tentacle/desktop/wss")
public class TentacleDesktopWSS
{
    Session session;

    @OnOpen
    public void onOpen(Session session)
    {
        System.out.println("websocket opened: " + session);
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message, Session session)
    {
        System.out.println("receive: " + message);
        try
        {
            // this.session.getBasicRemote().sendText(message.toUpperCase());
            Screenshot screenshot = new Screenshot(ImageIO.read(TentacleDesktopWSS.class.getResourceAsStream("/movie/IMG00000.bmp")));
            this.session.getBasicRemote().sendBinary(ByteBuffer.wrap(new RLEncoding().compress(screenshot.bitmap)));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
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
