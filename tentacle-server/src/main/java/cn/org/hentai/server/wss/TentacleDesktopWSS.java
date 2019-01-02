package cn.org.hentai.server.wss;

import cn.org.hentai.server.app.GetHttpSessionConfigurator;
import cn.org.hentai.server.rds.RemoteDesktopServer;
import cn.org.hentai.server.rds.RemoteDesktopSession;
import cn.org.hentai.server.util.Configs;
import cn.org.hentai.tentacle.hid.HIDCommand;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.system.File;
import cn.org.hentai.tentacle.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by matrixy on 2018/4/12.
 */
@Component
@ServerEndpoint(value = "/tentacle/desktop/wss", configurator = GetHttpSessionConfigurator.class)
public class TentacleDesktopWSS
{
    Session session;
    RemoteDesktopSession remoteDesktopSession = null;
    HttpSession httpSession = null;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config)
    {
        System.out.println("websocket opened: " + session);
        this.session = session;
        this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
    }

    @OnMessage
    public void onMessage(String message, Session session)
    {
        JsonObject json = new JsonParser().parse(message).getAsJsonObject();
        String type = json.get("type").getAsString();
        if ("command".equals(type))
        {
            String cmd = json.get("command").getAsString();
            if ("login".equals(cmd))
            {
                if (!Configs.get("rds.access.password").equals(json.get("password").getAsString()))
                {

                    // try { this.session.close(); } catch(Exception e) { }
                    this.sendResponse("login", "密码错误");
                    return;
                }
                this.httpSession.setAttribute("isLogin", true);
                this.sendResponse("login", "success");
            }
            else if ("sessions".equals(cmd))
            {
                JsonObject resp = new JsonObject();
                resp.addProperty("action", "sessions");
                JsonArray rds = new JsonArray();
                for (RemoteDesktopSession se : RemoteDesktopServer.activeSessions())
                {
                    JsonObject seJson = new JsonObject();
                    seJson.addProperty("id", se.getId());
                    seJson.addProperty("name", se.getName());
                    seJson.addProperty("controlling", se.isControlling());
                    rds.add(seJson);
                }
                resp.add("sessions", rds);
                this.sendText(resp.toString());
            }
            else if ("request-control".equals(cmd))
            {
                Long sessionId = json.get("sessionId").getAsLong();
                requestControl(sessionId);
            }
            else if ("get-clipboard".equals(cmd))
            {
                remoteDesktopSession.addCommand(Packet.create(Command.GET_CLIPBOARD, 3).addBytes("GET".getBytes()));
            }
            else if ("set-clipboard".equals(cmd))
            {
                if (!json.has("text"))
                {
                    this.sendResponse("set-clipboard", "发送的文本内容不能为空");
                    return;
                }
                String text = json.get("text").getAsString();
                try
                {
                    byte[] data = text.getBytes("UTF-8");
                    remoteDesktopSession.addCommand(Packet.create(Command.SET_CLIPBOARD, 4 + data.length).addInt(data.length).addBytes(data));
                }
                catch(UnsupportedEncodingException e) { }
            }
            else if ("ls".equals(cmd))
            {
                String path = !json.has("path") ? "" : json.get("path").getAsString();
                byte[] data = null;
                try
                {
                    data = path.getBytes("UTF-8");
                }
                catch(UnsupportedEncodingException e) { }
                remoteDesktopSession.addCommand(Packet.create(Command.LIST_FILES, 4 + data.length).addInt(data.length).addBytes(data));
            }
        }
        if ("hid".equals(type))
        {
            if (remoteDesktopSession == null) return;
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
                remoteDesktopSession.addCommand(p);
            }
        }
    }

    private void requestControl(long sessionId)
    {
        try
        {
            remoteDesktopSession = RemoteDesktopServer.getSession(sessionId);
            remoteDesktopSession.bind(this);
            this.sendResponse("request-control", "success");
        }
        catch(Exception ex)
        {
            this.sendResponse("request-control", ex.getMessage());
        }
    }

    // 下发控制响应
    public void sendControlResponse(int compressMethod, int bandWidth, int colorBits, int screenWidth, int screenHeight)
    {
        sendText("{ \"action\" : \"setup\", \"compressMethod\" : " + compressMethod + ", \"bandWidth\" : " + bandWidth + ", \"colorBits\" : " + colorBits + ", \"screenWidth\" : " + screenWidth + ", \"screenHeight\" : " + screenHeight + " }");
    }

    // 下发文件列表
    public void sendFiles(List<File> files)
    {
        JsonArray fileList = new JsonArray();
        for (int i = 0; i < files.size(); i++)
        {
            File f = files.get(i);
            JsonObject file = new JsonObject();
            file.addProperty("isDirectory", f.isDirectory());
            file.addProperty("length", f.getLength());
            file.addProperty("mtime", f.getLastModifiedTime());
            file.addProperty("name", f.getName());
            fileList.add(file);
        }

        JsonObject result = new JsonObject();
        result.addProperty("action", "ls");
        result.add("files", fileList);

        this.sendText(result.toString());
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
            Log.error(e);
            // throw new RuntimeException(e);
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
        release();
    }

    @OnError
    public void onError(Session session, Throwable ex)
    {
        release();
        ex.printStackTrace();
    }

    private void release()
    {
        try { this.httpSession.invalidate(); } catch(Exception e) { }
        if (null == remoteDesktopSession) return;
        remoteDesktopSession.closeControl();
    }
}
