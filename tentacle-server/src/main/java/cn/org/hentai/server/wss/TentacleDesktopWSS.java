package cn.org.hentai.server.wss;

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
            // this.session.getBasicRemote().sendText(message.toUpperCase());
            // String fname = ("00000" + message).replaceAll("^0+(\\d{5})$", "$1");
            int[] current = new Screenshot(ImageIO.read(TentacleDesktopWSS.class.getResourceAsStream("/movie/" + message + ".png"))).bitmap;
            long time = System.currentTimeMillis();
            int[] diff = new int[current.length];
            if (lastBitmap != null)
            {
                for (int i = 0; i < current.length; i++)
                {
                    // 如果相同，则保留0，否则为新的颜色
                    diff[i] = current[i] == lastBitmap[i] ? 0 : current[i];
                }
            }
            byte[] compressedData = new RLEncoding().compress(lastBitmap == null ? current : diff);
            time = System.currentTimeMillis() - time;
            // System.out.println("original: " + current.length * 4);
            // System.out.println("compressed: " + compressedData.length);
            System.out.println("ratio: " + new BigDecimal((current.length * 4.0f / compressedData.length)).setScale(2, BigDecimal.ROUND_DOWN));
            System.out.println("Spend: " + time);
            System.out.println("*******************************************************");
            this.session.getBasicRemote().sendBinary(ByteBuffer.wrap(compressedData));
            lastBitmap = current;
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
