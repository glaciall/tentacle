package cn.org.hentai.server.rds;

import cn.org.hentai.server.wss.TentacleDesktopWSS;
import cn.org.hentai.tentacle.graphic.Screenshot;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Packet;
import jdk.internal.util.xml.impl.Input;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by matrixy on 2018/4/15.
 */
public class RDSession extends Thread
{
    Socket connection = null;
    InputStream inputStream = null;
    OutputStream outputStream = null;
    TentacleDesktopWSS websocketService = null;

    boolean startCapture = false;

    long lastHeartbeatTime = 0;

    public RDSession(Socket conn)
    {
        this.connection = conn;
    }

    public void bind(TentacleDesktopWSS websocketService)
    {
        this.websocketService = websocketService;
        this.startCapture = true;
    }

    private void converse() throws Exception
    {
        connection.setSoTimeout(30000);
        inputStream = connection.getInputStream();
        outputStream = connection.getOutputStream();

        while (!Thread.interrupted())
        {
            sendHeartbeat();

            Packet packet = Packet.read(inputStream);
            if (packet != null)
            {
                process(packet);
            }
            sleep(10);
        }
    }

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
            byte[] data = packet.nextBytes(dataLength - 12);

            websocketService.sendScreenshot(data);
        }

        if (resp != null)
        {
            outputStream.write(resp.getBytes());
            outputStream.flush();
        }
    }

    private void sendHeartbeat() throws Exception
    {
        if (System.currentTimeMillis() - lastHeartbeatTime < 3000) return;
        Packet p = Packet.create(Command.HEARTBEAT, 5);
        p.addBytes("HELLO".getBytes());
        outputStream.write(p.getBytes());
        outputStream.flush();
        while (Packet.read(inputStream) == null) sleep(5);
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
