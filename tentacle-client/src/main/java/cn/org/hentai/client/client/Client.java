package cn.org.hentai.client.client;

import cn.org.hentai.client.worker.CaptureWorker;
import cn.org.hentai.client.worker.CompressWorker;
import cn.org.hentai.client.worker.ScreenImages;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.Configs;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by matrixy on 2018/4/15.
 */
public class Client extends Thread
{
    CaptureWorker captureWorker;
    CompressWorker compressWorker;

    Socket conn;
    InputStream inputStream;
    OutputStream outputStream;

    private void converse() throws Exception
    {
        conn = new Socket(Configs.get("server.addr"), Configs.getInt("server.port", 1986));
        conn.setSoTimeout(30000);
        inputStream = conn.getInputStream();
        outputStream = conn.getOutputStream();

        // TODO 1. 身份验证

        while (true)
        {
            // 有无下发下来的数据包
            Packet packet = Packet.read(inputStream);
            if (packet == null)
            {
                sleep(30);
                continue;
            }

            // 有无需要上报的截图
            if (ScreenImages.hasCompressedScreens())
            {

            }
        }
    }

    private void release()
    {
        try { inputStream.close(); } catch(Exception e) { }
        try { outputStream.close(); } catch(Exception e) { }
        try { conn.close(); } catch(Exception e) { }
        try
        {
            captureWorker.interrupt();
        }
        catch(Exception e) { }
        try
        {
            compressWorker.interrupt();
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
