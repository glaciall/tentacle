package cn.org.hentai.client.client;

import cn.org.hentai.client.worker.*;
import cn.org.hentai.tentacle.encrypt.MD5;
import cn.org.hentai.tentacle.hid.HIDCommand;
import cn.org.hentai.tentacle.hid.KeyboardCommand;
import cn.org.hentai.tentacle.hid.MouseCommand;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.system.FileSystem;
import cn.org.hentai.tentacle.util.ByteUtils;
import cn.org.hentai.tentacle.util.Configs;
import cn.org.hentai.tentacle.util.Log;
import cn.org.hentai.tentacle.util.Nonce;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.*;
import java.net.Socket;

/**
 * Created by matrixy on 2018/4/15.
 */
public class Client extends Thread
{
    // 是否正在发送截图
    boolean working = false;

    boolean authenticated = false;

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

        // 1. 身份验证
        String clientName = Configs.get("client.name", "unknown");
        byte clientNameBytes[] = clientName.getBytes("UTF-8");
        if (clientName.length() > 20) throw new RuntimeException("受控端名称不能超过20个字符");
        Packet packet = Packet.create(Command.AUTHENTICATE, 64 + clientNameBytes.length + 4);
        packet.addInt(clientNameBytes.length);
        packet.addBytes(clientNameBytes);
        String nonce = Nonce.generate(32);
        packet.addBytes(nonce.getBytes());
        packet.addBytes(MD5.encode(nonce + ":::" + Configs.get("client.key")).getBytes());

        send(packet);

        while (true)
        {
            if (System.currentTimeMillis() - lastActiveTime > 30000) break;
            // 有无下发下来的数据包
            packet = Packet.read(inputStream);
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
        if (cmd != Command.AUTHENTICATE_RESPONSE && authenticated == false) return;
        if (cmd == Command.AUTHENTICATE_RESPONSE)
        {
            if (packet.nextByte() == 0x00) authenticated = true;
            else
            {
                Log.info("会话认证失败");
                System.exit(1);
            }
        }
        // 心跳
        else if (cmd == Command.HEARTBEAT)
        {
            System.out.println("收到服务器反馈的心跳包");
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
                byte[] bytes = text.getBytes("UTF-8");
                if (text != null && text.length() > 0)
                    resp = Packet.create(Command.GET_CLIPBOARD_RESPONSE, 4 + bytes.length).addInt(bytes.length).addBytes(bytes);
            }
        }
        // 设置剪切板内容
        else if (cmd == Command.SET_CLIPBOARD)
        {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            int len = packet.nextInt();
            String text = new String(packet.nextBytes(len), "UTF-8");
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
        // 列出文件列表
        else if (cmd == Command.LIST_FILES)
        {
            int len = packet.nextInt();
            String path = new String(packet.nextBytes(len), "UTF-8");
            File[] files = FileSystem.list(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(40960);
            for (int i = 0; files != null && i < files.length; i++)
            {
                File file = files[i];
                // 是否目录，长度，权限
                String name = file.getName();
                if ("".equals(name)) name = file.getAbsolutePath();
                byte[] fbytes = name.getBytes("UTF-8");
                baos.write(file.isDirectory() ? 1 : 0);
                baos.write(ByteUtils.toBytes(file.length()));
                baos.write(ByteUtils.toBytes(file.lastModified()));
                baos.write(ByteUtils.toBytes(fbytes.length));
                baos.write(fbytes);
            }
            resp = Packet.create(Command.LIST_FILES_RESPONSE, baos.size()).addBytes(baos.toByteArray());
        }
        // 传送文件到服务器端
        else if (cmd == Command.DOWNLOAD_FILE)
        {
            int pLength = packet.nextInt();
            String path = new String(packet.nextBytes(pLength), "UTF-8");
            int nLength = packet.nextInt();
            String name = new String(packet.nextBytes(nLength), "UTF-8");
            new FileTransferWorker(this, new File(new File(path), name)).start();
        }

        // 发送响应至服务器端
        if (resp != null)
        {
            send(resp);
        }
    }

    public synchronized void send(Packet packet) throws IOException
    {
        outputStream.write(packet.getBytes());
        outputStream.flush();
    }

    // 发送压缩后的屏幕截图
    private void sendScreenImages() throws Exception
    {
        if (!working) return;
        Packet p = ScreenImages.getCompressedScreen();
        p.skip(6 + 1 + 4 + 2 + 2 + 8);
        send(p);
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
