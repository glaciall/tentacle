package cn.org.hentai.server.wss;

import cn.org.hentai.server.rds.RDServer;
import cn.org.hentai.server.rds.RDSession;
import cn.org.hentai.server.util.ByteUtils;
import cn.org.hentai.server.util.Log;
import cn.org.hentai.tentacle.compress.RLEncoding;
import cn.org.hentai.tentacle.graphic.Screenshot;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Packet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
        JsonObject json = new JsonParser().parse(message).getAsJsonObject();
        String type = json.get("type").getAsString();
        Log.debug("Receive: " + type);
        if ("command".equals(type))
        {
            String cmd = json.get("command").getAsString();
            if ("request-control".equals(cmd))
            {
                requestControl();
            }
        }
        if ("hid".equals(type))
        {
            if (rdSession == null) return;
            JsonArray actions = json.get("commands").getAsJsonArray();
            for (int i = 0; i < actions.size(); i++)
            {
                JsonObject cmd = actions.get(i).getAsJsonObject();
                Packet p = Packet.create(Command.HID_COMMAND, 7);
                byte hidType = 0x00, eventType = 0x00, key = 0x00;
                int timestamp = cmd.get("timestamp").getAsInt();
                if ("mouse-down".equals(cmd.get("type").getAsString()))
                {
                    hidType = Command.TYPE_MOUSE;
                    eventType = 0x01;
                }
                else if ("mouse-up".equals(cmd.get("type").getAsString()))
                {
                    hidType = Command.TYPE_MOUSE;
                    eventType = 0x02;
                }
                else if ("mouse-move".equals(cmd.get("type").getAsString()))
                {
                    hidType = Command.TYPE_MOUSE;
                    eventType = 0x03;
                }
                if (cmd.has("key")) key = cmd.get("key").getAsByte();
                p.addByte(hidType).addByte(eventType).addByte(key).addInt(timestamp);
                rdSession.addHIDCommand(p);
            }
        }
    }

    private void requestControl()
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
        if (null == rdSession) return;
        rdSession.closeControl();
    }

    @OnError
    public void onError(Session session, Throwable ex)
    {
        ex.printStackTrace();
    }
}
