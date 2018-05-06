package cn.org.hentai.client.client;

import cn.org.hentai.client.worker.*;
import cn.org.hentai.tentacle.hid.HIDCommand;
import cn.org.hentai.tentacle.hid.KeyboardCommand;
import cn.org.hentai.tentacle.hid.MouseCommand;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.ByteUtils;
import cn.org.hentai.tentacle.util.Configs;
import cn.org.hentai.tentacle.util.Log;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by matrixy on 2018/4/15.
 */
public class Client extends Thread
{
    // 是否正在发送截图
    boolean working = false;

    BaseWorker captureWorker;
    BaseWorker compressWorker;
    HIDCommandExecutor hidCommandExecutor;

    Socket conn;
    InputStream inputStream;
    OutputStream outputStream;

    long lastActiveTime = 0L;

    // 与服务器间的会话处理
    private void converse() throws Exception
    {
        working = false;
        conn = new Socket(Configs.get("server.addr"), Configs.getInt("server.port", 1986));
        conn.setSoTimeout(30000);
        inputStream = conn.getInputStream();
        outputStream = conn.getOutputStream();

        lastActiveTime = System.currentTimeMillis();
        Log.info("Connected to server...");

        // TODO 1. 身份验证
        while (true)
        {
            if (System.currentTimeMillis() - lastActiveTime > 30000) break;
            // 有无下发下来的数据包
            Packet packet = Packet.read(inputStream);
            if (packet != null)
            {
                lastActiveTime = System.currentTimeMillis();
                processCommand(packet);
            }

            // 处理服务器下发的指令
            // 有无需要上报的截图
            if (ScreenImages.hasCompressedScreens())
            {
                lastActiveTime = System.currentTimeMillis();
                sendScreenImages();
                continue;
            }
            // 如果闲置超过20秒，则发送一个心跳包
            if (System.currentTimeMillis() - lastActiveTime > 20000)
            {
                Packet p = Packet.create(Command.HEARTBEAT, 5);
                p.addBytes("HELLO".getBytes());
                outputStream.write(p.getBytes());
                outputStream.flush();
                lastActiveTime = System.currentTimeMillis();
            }
            sleep(5);
        }
        Log.info("Connection closed...");
    }

    // 处理服务器端下发的指令
    private void processCommand(Packet packet) throws Exception
    {
        packet.skip(6);
        int cmd = packet.nextByte();
        int length = packet.nextInt();
        Packet resp = null;
        // 心跳
        if (cmd == Command.HEARTBEAT)
        {
            resp = Packet.create(Command.COMMON_RESPONSE, 4).addByte((byte)'O').addByte((byte)'J').addByte((byte)'B').addByte((byte)'K');
        }
        // 开始远程控制
        else if (cmd == Command.CONTROL_REQUEST)
        {
            if (working) throw new RuntimeException("Already working on capture screenshots...");
            working = true;

            // TODO: 暂不响应服务器端的控制请求的细节要求，比如压缩方式、带宽、颜色位数等
            int compressMethod = packet.nextByte() & 0xff;
            int bandWidth = packet.nextByte() & 0xff;
            int colorBits = packet.nextByte() & 0xff;
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            resp = Packet.create(Command.CONTROL_RESPONSE, 15)
                    .addByte((byte)0x01)                            // 压缩方式
                    .addByte((byte)0x00)                            // 带宽
                    .addByte((byte)0x03)                            // 颜色位数
                    .addShort((short)screenSize.getWidth())         // 屏幕宽度
                    .addShort((short)screenSize.getHeight())        // 屏幕高度
                    .addLong(System.currentTimeMillis());           // 当前系统时间戳
            (captureWorker = new CaptureWorker()).start();
            (compressWorker = new CompressWorker()).start();
            (hidCommandExecutor = new HIDCommandExecutor()).start();
        }
        // 获取剪切板内容
        else if (cmd == Command.GET_CLIPBOARD)
        {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable content = clipboard.getContents(null);
            if (content.isDataFlavorSupported(DataFlavor.stringFlavor))
            {
                String text = (String)clipboard.getData(DataFlavor.stringFlavor);
                // 剪切板没有内容就别回应了
                byte[] bytes = text.getBytes();
                if (text != null && text.length() > 0)
                    resp = Packet.create(Command.GET_CLIPBOARD_RESPONSE, 4 + bytes.length).addInt(bytes.length).addBytes(bytes);
            }
        }
        // 设置剪切板内容
        else if (cmd == Command.SET_CLIPBOARD)
        {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            int len = packet.nextInt();
            String text = new String(packet.nextBytes(len));
            StringSelection selection = new StringSelection(text);
            clipboard.setContents(selection, null);
            resp = Packet.create(Command.SET_CLIPBOARD_RESPONSE, 4).addBytes("OJBK".getBytes());
        }
        // 键鼠事件处理
        else if (cmd == Command.HID_COMMAND)
        {
            int hidType = packet.nextByte() & 0xff;
            int eventType = packet.nextByte() & 0xff;
            int key = packet.nextByte() & 0xff;
            short x = packet.nextShort();
            short y = packet.nextShort();
            int timestamp = packet.nextInt() & 0xffffffff;
            HIDCommand hidCommand = null;
            if (hidType == HIDCommand.TYPE_MOUSE)
            {
                hidCommand = new MouseCommand(eventType, key, x, y, timestamp);
            }
            else
            {
                hidCommand = new KeyboardCommand(key, eventType, timestamp);
            }
            hidCommandExecutor.add(hidCommand);
        }
        // 停止远程控制
        else if (cmd == Command.CLOSE_REQUEST)
        {
            resp = Packet.create(Command.CLOSE_RESPONSE, 4).addBytes("OJBK".getBytes());
            working = false;
            captureWorker.terminate();
            compressWorker.terminate();
            hidCommandExecutor.terminate();
            ScreenImages.clear();
        }

        // 发送响应至服务器端
        if (resp != null)
        {
            outputStream.write(resp.getBytes());
            outputStream.flush();
        }
    }

    // 发送压缩后的屏幕截图
    private void sendScreenImages() throws Exception
    {
        if (!working) return;
        Packet p = ScreenImages.getCompressedScreen();
        p.skip(6 + 1 + 4 + 2 + 2 + 8);
        // Log.debug("Sequence: " + p.nextInt());
        outputStream.write(p.getBytes());
        outputStream.flush();
    }

    // 关闭连接，中断工作线程
    private void release()
    {
        working = false;
        try { inputStream.close(); } catch(Exception e) { }
        try { outputStream.close(); } catch(Exception e) { }
        try { conn.close(); } catch(Exception e) { }
        try
        {
            captureWorker.terminate();
        }
        catch(Exception e) { }
        try
        {
            compressWorker.terminate();
        }
        catch(Exception e) { }
    }

    private void sleep(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch(Exception e) { }
    }

    public void run()
    {
        while (true)
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
            sleep(5000);
        }
    }
}
