package cn.org.hentai.server.wss;

import cn.org.hentai.server.rds.RDServer;
import cn.org.hentai.server.rds.RDSession;
import cn.org.hentai.server.util.Configs;
import cn.org.hentai.tentacle.hid.HIDCommand;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Packet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
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
        if ("command".equals(type))
        {
            String cmd = json.get("command").getAsString();
            if ("request-control".equals(cmd))
            {
                if (!Configs.get("rds.access.password").equals(json.get("password").getAsString()))
                {

                    // try { this.session.close(); } catch(Exception e) { }
                    this.sendResponse("login", "密码错误");
                    return;
                }
                this.sendResponse("login", "success");
                requestControl();
            }
            else if ("get-clipboard".equals(cmd))
            {
                rdSession.addCommand(Packet.create(Command.GET_CLIPBOARD, 3).addBytes("GET".getBytes()));
            }
            else if ("set-clipboard".equals(cmd))
            {
                if (!json.has("text"))
                {
                    this.sendResponse("set-clipboard", "发送的文本内容不能为空");
                    return;
                }
                String text = json.get("text").getAsString();
                byte[] data = text.getBytes();
                rdSession.addCommand(Packet.create(Command.SET_CLIPBOARD, 4 + data.length).addInt(data.length).addBytes(data));
            }
        }
        if ("hid".equals(type))
        {
            if (rdSession == null) return;
            JsonArray actions = json.get("commands").getAsJsonArray();
            for (int i = 0; i < actions.size(); i++)
            {
                JsonObject cmd = actions.get(i).getAsJsonObject();
                Packet p = Packet.create(Command.HID_COMMAND, 11);
                byte hidType = 0x00, eventType = 0x00, key = 0x00;
                short x = 0, y = 0;
                int timestamp = cmd.get("timestamp").getAsInt();
                if ("mouse-down".equals(cmd.get("type").getAsString()))
                {
                    hidType = HIDCommand.TYPE_MOUSE;
                    eventType = 0x01;
                    x = cmd.get("x").getAsShort();
                    y = cmd.get("y").getAsShort();
                }
                else if ("mouse-up".equals(cmd.get("type").getAsString()))
                {
                    hidType = HIDCommand.TYPE_MOUSE;
                    eventType = 0x02;
                    x = cmd.get("x").getAsShort();
                    y = cmd.get("y").getAsShort();
                }
                else if ("mouse-move".equals(cmd.get("type").getAsString()))
                {
                    hidType = HIDCommand.TYPE_MOUSE;
                    eventType = 0x03;
                    x = cmd.get("x").getAsShort();
                    y = cmd.get("y").getAsShort();
                }
                else if ("mouse-wheel".equals(cmd.get("type").getAsString()))
                {
                    hidType = HIDCommand.TYPE_MOUSE;
                    eventType = 0x04;
                    x = cmd.get("x").getAsShort();
                    y = cmd.get("y").getAsShort();
                }
                else if ("key-press".equals(cmd.get("type").getAsString()))
                {
                    hidType = HIDCommand.TYPE_KEYBOARD;
                    eventType = 0x01;
                }
                else if ("key-release".equals(cmd.get("type").getAsString()))
                {
                    hidType = HIDCommand.TYPE_KEYBOARD;
                    eventType = 0x02;
                }
                if (cmd.has("key")) key = cmd.get("key").getAsByte();
                p.addByte(hidType).addByte(eventType).addByte(key).addShort(x).addShort(y).addInt(timestamp);
                rdSession.addCommand(p);
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
                this.sendResponse("request-control", "主机端未连接");
                return;
            }
            rdSession.bind(this);
            this.sendResponse("request-control", "success");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    // 下发控制响应
    public void sendControlResponse(int compressMethod, int bandWidth, int colorBits, int screenWidth, int screenHeight)
    {
        sendText("{ \"action\" : \"setup\", \"compressMethod\" : " + compressMethod + ", \"bandWidth\" : " + bandWidth + ", \"colorBits\" : " + colorBits + ", \"screenWidth\" : " + screenWidth + ", \"screenHeight\" : " + screenHeight + " }");
    }

    // 下发屏幕截图
    public void sendScreenshot(byte[] screenshot)
    {
        try
        {
            if (!this.session.isOpen()) throw new Exception("websocket was closed");
            // this.session.getBasicRemote().sendBinary(ByteBuffer.wrap(screenshot));
            // sendResponse("", "");
            sendBinary(screenshot);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    // 下发剪切板内容
    public void sendClipboardData(String text)
    {
        this.sendResponse("get-clipboard", text);
    }

    public void sendResponse(String action, String result)
    {
        JsonObject resp = new JsonObject();
        resp.addProperty("action", action);
        resp.addProperty("result", result);
        sendText(resp.toString());
    }

    private void sendText(String text)
    {
        try
        {
            this.session.getBasicRemote().sendText(text);
        }
        catch(IOException e)
        {
            try { this.session.close(); } catch(Exception ex) { }
        }
    }

    private void sendBinary(byte[] data)
    {
        try
        {
            this.session.getBasicRemote().sendBinary(ByteBuffer.wrap(data));
        }
        catch(IOException e)
        {
            try { this.session.close(); } catch(Exception ex) { }
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
