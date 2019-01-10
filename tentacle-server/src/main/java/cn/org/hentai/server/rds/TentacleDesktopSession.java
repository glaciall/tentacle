package cn.org.hentai.server.rds;

import cn.org.hentai.server.controller.FileDownloadController;
import cn.org.hentai.server.rds.coder.TentacleMessageDecoder;
import cn.org.hentai.server.util.ByteHolder;
import cn.org.hentai.server.wss.TentacleDesktopWSS;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Message;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.ByteUtils;
import cn.org.hentai.tentacle.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * Created by matrixy on 2019/1/8.
 */
public class TentacleDesktopSession extends Thread
{
    private Socket connection = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;

    private Client clientInfo = null;

    public TentacleDesktopSession(Socket connection)
    {
        this.connection = connection;
    }

    public void run()
    {
        try
        {
            inputStream = connection.getInputStream();
            outputStream = connection.getOutputStream();

            ByteHolder buffer = new ByteHolder(1024 * 1024 * 10);
            byte[] block = new byte[512];

            long lastActiveTime = System.currentTimeMillis();
            while (!this.isClosed())
            {
                int readableBytes = inputStream.available();
                if (readableBytes > 0)
                {
                    lastActiveTime = System.currentTimeMillis();
                    for (int i = 0, l = (int)Math.ceil(readableBytes / 512f); i < l; i++)
                    {
                        int len = inputStream.read(block, 0, i == l - 1 ? 512 : readableBytes % 512);
                        if (len > 0) buffer.write(block, 0, len);
                    }

                    while (true)
                    {
                        Message msg = TentacleMessageDecoder.read(buffer);
                        if (null == msg) break;

                        handle(msg);
                    }
                    continue;
                }

                long idleTime = System.currentTimeMillis() - lastActiveTime;
                if (idleTime > 5000)
                {
                    Log.debug(String.format("Client Timeout: %s", this.getRemoteAddress().toString()));
                    break;
                }
                Thread.sleep(10);
            }
        }
        catch(Exception e)
        {
            Log.error(e);
        }
        finally
        {
            this.close();
            SessionManager.removeSession(this);
        }
    }

    private boolean isClosed()
    {
        return this.isInterrupted() || connection.isClosed() || connection.isConnected() == false;
    }

    private final void handle(Message msg)
    {
        BaseMessageController controller = TentacleDesktopSessionHandler.getController(msg.getCommand());
        if (null == controller)
        {
            throw new RuntimeException(String.format("unknown command: %x", msg.getCommand()));
        }

        try
        {
            Message resp = controller.service(this, msg);
            if (resp != null) this.send(resp);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        if (controller.shouldDisconnectAfterConverse())
        {
            this.interrupt();
        }
    }

    // 与websocket会话关联的引用
    private TentacleDesktopWSS websocketContext = null;

    public TentacleDesktopWSS getWebsocketContext()
    {
        return websocketContext;
    }

    /**
     * 与websocket建立关联关系
     * @param websocketSession
     */
    public void bind(TentacleDesktopWSS websocketSession)
    {
        if (this.websocketContext != null) throw new RuntimeException("目标主机已经处于其它会话的控制中");
        this.websocketContext = websocketSession;
        Client info = this.getClient();
        info.setControlling(true);

        // 发送请求控制消息到受控端
        // body的三个字节的含义如下，虽然目前实际上没有用到
        // 0x01 : 压缩方式
        // 0x00 : 带宽
        // 0x03 : 颜色位数
        Message msg = new Message().withCommand(Command.CONTROL_REQUEST).withBody(new byte[] { 0x01, 0x00, 0x03 });
        this.send(msg);
    }

    /**
     * 解除与websocket会话的绑定，并且下发停止受控消息到受控端
     */
    public void unbind()
    {
        try
        {
            Message msg = new Message().withCommand(Command.CLOSE_REQUEST).withBody("CLOSE".getBytes());
            this.send(msg);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        try
        {
            getClient().setControlling(false);
        }
        catch(Exception ex) { }
        this.websocketContext = null;
    }

    // 请求受控机剪贴板内容
    public void getClipboardData()
    {
        this.send(new Message().withCommand(Command.GET_CLIPBOARD).withBody("GET".getBytes()));
    }

    // 设置受控机剪贴板内容
    public void setClipboardData(String text)
    {
        byte[] data = null;
        try
        {
            data = text.getBytes("UTF-8");
        }
        catch(UnsupportedEncodingException ex) { }
        Packet p = Packet.create(4 + data.length).addInt(data.length).addBytes(data);
        this.send(new Message().withCommand(Command.SET_CLIPBOARD).withBody(p));
    }

    // 请求列出受控机文件列表
    public void listFiles(String filePath)
    {
        byte[] data = null;

        try
        {
            data = filePath.getBytes("UTF-8");
        }
        catch(UnsupportedEncodingException e) { }

        Packet p = Packet.create(4 + data.length).addInt(data.length).addBytes(data);
        this.send(new Message().withCommand(Command.LIST_FILES).withBody(p));
    }

    // 请求受控机文件传输（文件下载：受控端到控制端）
    private FileDownloadController fileDownloadController = null;

    public void downloadFile(String path, String name, FileDownloadController controller)
    {
        if (this.fileDownloadController != null) throw new RuntimeException("同一时间只能传送一个文件");
        byte[] bPath = null, bName = null;
        try
        {
            bPath = path.getBytes("UTF-8");
            bName = name.getBytes("UTF-8");
        }
        catch(UnsupportedEncodingException ex) { }
        Packet p = Packet.create(8 + bPath.length + bName.length)
                .addInt(bPath.length)
                .addBytes(bPath)
                .addInt(bName.length)
                .addBytes(bName);
        this.send(new Message().withCommand(Command.DOWNLOAD_FILE).withBody(p));
        this.fileDownloadController = controller;
    }

    // 受控端发送的文件按40960字节进行分包，这里原样进行转交给FileDownloadController即可
    public void sendFileFragment(byte[] block)
    {
        this.fileDownloadController.receivePartial(block);
        // 受控端将在文件发送完成后，发送一个零长度的消息体过来作为结束标志
        // 在这里解除与FileDownloadController的关联关系
        if (block.length == 0) this.fileDownloadController = null;
    }

    // 发送HID设备指令到受控机
    public void sendHIDCommand(byte hidType, byte eventType, byte key, short x, short y, int timestamp)
    {
        Packet p = Packet.create(11).addByte(hidType)
                .addByte(eventType)
                .addByte(key)
                .addShort(x)
                .addShort(y)
                .addInt(timestamp);

        Message msg = new Message()
                .withCommand(Command.HID_COMMAND)
                .withBody(p);
        this.send(msg);
    }

    public SocketAddress getRemoteAddress()
    {
        return this.connection.getRemoteSocketAddress();
    }

    public synchronized void send(Message message)
    {
        try
        {
            byte[] body = message.getBodyBytes();

            outputStream.write("HENTAI".getBytes());
            outputStream.write(message.getCommand());
            outputStream.write(ByteUtils.toBytes(body.length));
            outputStream.write(body);
            outputStream.flush();
        }
        catch(Exception ex)
        {
            if (ex instanceof SocketException || ex instanceof IOException)
            {
                Log.error(ex);
                this.close();
            }
            else throw new RuntimeException(ex);
        }
    }

    public void close()
    {
        try { inputStream.close(); } catch(Exception e) { }
        try { outputStream.close(); } catch(Exception e) { }
        try { connection.close(); } catch(Exception e) { }
    }

    public Client getClient()
    {
        return this.clientInfo;
    }

    public void setClient(Client clientInfo)
    {
        this.clientInfo = clientInfo;
    }
}
