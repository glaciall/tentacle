package cn.org.hentai.server.rds;

import cn.org.hentai.server.util.ByteUtils;
import cn.org.hentai.server.util.Log;
import cn.org.hentai.server.wss.TentacleDesktopWSS;
import cn.org.hentai.tentacle.compress.RLEncoding;
import cn.org.hentai.tentacle.graphic.Screenshot;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Packet;
import jdk.internal.util.xml.impl.Input;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by matrixy on 2018/4/15.
 */
public class RDSession extends Thread
{
    Socket connection = null;
    InputStream inputStream = null;
    OutputStream outputStream = null;
    TentacleDesktopWSS websocketService = null;
    LinkedList<Packet> hidCommands = new LinkedList<Packet>();

    boolean startCapture = false;
    boolean remoteControlling = false;
    boolean closeControl = false;

    long lastActiveTime = 0;

    public RDSession(Socket conn)
    {
        this.connection = conn;
    }

    // 与WebSocket会话绑定，并且通知客户端开始转发截图
    public void bind(TentacleDesktopWSS websocketService)
    {
        this.websocketService = websocketService;
        this.startCapture = true;
    }

    // 结束会话
    public void closeControl()
    {
        this.closeControl = true;
    }

    // 保存键鼠控制指令包，准备下发到客户端
    public void addHIDCommand(Packet hidPacket)
    {
        synchronized (hidCommands)
        {
            hidCommands.add(hidPacket);
        }
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

            if (startCapture)
            {
                outputStream.write(Packet.create(Command.CONTROL_REQUEST, 3).addByte((byte)0x01).addByte((byte)0x00).addByte((byte)0x03).getBytes());
                outputStream.flush();
                System.out.println("control command sent");
                Packet resp = null;
                while (true)
                {
                    resp = Packet.read(inputStream);
                    if (null != resp) break;
                    sleep(10);
                }
                startCapture = false;
                remoteControlling = true;
                System.out.println("client response: " + ByteUtils.toString(resp.getBytes()));
            }

            Packet packet = Packet.read(inputStream);
            if (packet != null)
            {
                process(packet);
                lastActiveTime = System.currentTimeMillis();
            }
            // 下发HID控制指令
            if (hidCommands.size() > 0)
            {
                sendHIDCommands();
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
        while (Packet.read(inputStream) == null) sleep(5);
    }

    // 下发键鼠控制指令
    private void sendHIDCommands() throws Exception
    {
        synchronized (hidCommands)
        {
            while (hidCommands.size() > 0)
            {
                Packet hidPacket = hidCommands.removeFirst();
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
        if (cmd == Command.HEARTBEAT)
        {
            // do nothing here...
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
