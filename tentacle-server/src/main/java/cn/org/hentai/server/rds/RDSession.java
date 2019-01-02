package cn.org.hentai.server.rds;

import cn.org.hentai.server.controller.MainController;
import cn.org.hentai.server.util.ByteUtils;
import cn.org.hentai.server.wss.TentacleDesktopWSS;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.system.File;
import cn.org.hentai.tentacle.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by matrixy on 2018/4/15.
 */
public class RDSession extends Thread
{
    Socket connection = null;
    InputStream inputStream = null;
    OutputStream outputStream = null;
    TentacleDesktopWSS websocketService = null;
    LinkedList<Packet> commands = new LinkedList<Packet>();

    boolean needSendStartCommand = false;
    boolean remoteControlling = false;
    boolean closeControl = false;

    long lastActiveTime = 0;

    MainController controller = null;

    public RDSession(Socket conn)
    {
        this.connection = conn;
    }

    // 与WebSocket会话绑定，并且通知客户端开始转发截图
    public void bind(TentacleDesktopWSS websocketService)
    {
        this.websocketService = websocketService;
        this.needSendStartCommand = true;
    }

    // 结束会话
    public void closeControl()
    {
        this.closeControl = true;
    }

    // 保存键鼠控制指令包，准备下发到客户端
    public void addCommand(Packet hidPacket)
    {
        synchronized (commands)
        {
            commands.add(hidPacket);
        }
    }

    // 请求文件传送
    public synchronized void requestFile(String path, String name, MainController controller)
    {
        if (this.controller != null) throw new RuntimeException("同一时间只能传送一个文件");
        byte[] bPath = null, bName = null;
        try
        {
            bPath = path.getBytes("UTF-8");
            bName = name.getBytes("UTF-8");
        }
        catch(UnsupportedEncodingException ex) { }
        addCommand(Packet.create(Command.DOWNLOAD_FILE, 8 + bPath.length + bName.length).addInt(bPath.length).addBytes(bPath).addInt(bName.length).addBytes(bName));
        this.controller = controller;
    }

    private void converse() throws Exception
    {
        connection.setSoTimeout(30000);
        inputStream = connection.getInputStream();
        outputStream = connection.getOutputStream();

        lastActiveTime = System.currentTimeMillis();

        while (!Thread.interrupted())
        {
            if (System.currentTimeMillis() - lastActiveTime > 30000) break;

            if (remoteControlling && closeControl)
            {
                remoteControlling = false;
                closeControl = false;
                sendCloseControlCommand();
                lastActiveTime = System.currentTimeMillis();
            }

            if (needSendStartCommand)
            {
                outputStream.write(Packet.create(Command.CONTROL_REQUEST, 3).addByte((byte)0x01).addByte((byte)0x00).addByte((byte)0x03).getBytes());
                outputStream.flush();
                Packet resp = null;
                while (true)
                {
                    resp = Packet.read(inputStream);
                    if (null != resp) break;
                    sleep(10);
                }
                needSendStartCommand = false;
                remoteControlling = true;
                resp.seek(11);
                int compressMethod = resp.nextByte() & 0xff;
                int bandWidth = resp.nextByte() & 0xff;
                int colorBits = resp.nextByte() & 0xff;
                int screenWidth = resp.nextShort() & 0xffff;
                int screenHeight = resp.nextShort() & 0xffff;
                this.websocketService.sendControlResponse(compressMethod, bandWidth, colorBits, screenWidth, screenHeight);
            }

            Packet packet = Packet.read(inputStream);
            if (packet != null)
            {
                process(packet);
                lastActiveTime = System.currentTimeMillis();
            }
            // 下发控制指令
            if (commands.size() > 0)
            {
                sendCommands();
                lastActiveTime = System.currentTimeMillis();
            }
            sleep(5);
        }
    }

    // 下发关闭控制指令
    private void sendCloseControlCommand() throws Exception
    {
        Packet p = Packet.create(Command.CLOSE_REQUEST, 5);
        p.addBytes("CLOSE".getBytes());
        outputStream.write(p.getBytes());
        outputStream.flush();
    }

    // 下发键鼠控制指令
    private void sendCommands() throws Exception
    {
        synchronized (commands)
        {
            while (commands.size() > 0)
            {
                Packet hidPacket = commands.removeFirst();
                outputStream.write(hidPacket.getBytes());
                outputStream.flush();
            }
        }
    }

    // 接收上发上来的屏幕截图，通过WebSocket转发到浏览器端
    private void process(Packet packet) throws Exception
    {
        packet.skip(6);
        byte cmd = packet.nextByte();
        int dataLength = packet.nextInt();
        Packet resp = null;
        if (cmd == Command.SCREENSHOT)
        {
            // 宽，高，时间，压缩数据
            int width = packet.nextShort();
            int height = packet.nextShort();
            long captureTime = packet.nextLong();
            int sequence = packet.nextInt();
            packet.rewind();
            packet.skip(11);
            websocketService.sendScreenshot(packet.nextBytes(dataLength));
        }
        else if (Command.HEARTBEAT == cmd)
        {
            resp = Packet.create(Command.COMMON_RESPONSE, 4).addBytes("OJBK".getBytes());
        }
        else if (Command.SET_CLIPBOARD_RESPONSE == cmd)
        {
            websocketService.sendResponse("set-clipboard", "success");
        }
        else if (Command.GET_CLIPBOARD_RESPONSE == cmd)
        {
            int len = packet.nextInt();
            String text = new String(packet.nextBytes(len), "UTF-8");
            websocketService.sendClipboardData(text);
        }
        else if (Command.LIST_FILES_RESPONSE == cmd)
        {
            byte[] data = packet.nextBytes(dataLength);
            List<File> files = new ArrayList<File>();
            for (int i = 0; i < data.length; )
            {
                boolean isDirectory = data[i] == 1;
                long length = ByteUtils.getLong(data, i += 1, 8);
                long mtime = ByteUtils.getLong(data, i += 8, 8);
                int strlen = ByteUtils.getInt(data, i += 8, 4);
                String name = new String(data, i += 4, strlen, "UTF-8");
                i += strlen;
                files.add(new File(isDirectory, length, mtime, name));
            }
            websocketService.sendFiles(files);
        }
        else if (Command.DOWNLOAD_FILE_RESPONSE == cmd)
        {
            int blockLength = packet.nextInt();
            byte[] part = packet.nextBytes(blockLength);
            controller.receivePart(part);
            if (part.length == 0) controller = null;
        }

        if (resp != null)
        {
            outputStream.write(resp.getBytes());
            outputStream.flush();
        }
    }

    private void sleep(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch(Exception e) { }
    }

    private void release()
    {
        try { inputStream.close(); } catch(Exception e) { }
        try { outputStream.close(); } catch(Exception e) { }
        try { connection.close(); } catch(Exception e) { }
    }

    public void run()
    {
        try
        {
            converse();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            release();
        }
    }
}
