package cn.org.hentai.client.client;

import cn.org.hentai.client.worker.CaptureWorker;
import cn.org.hentai.client.worker.CompressWorker;
import cn.org.hentai.tentacle.util.Configs;

import java.net.Socket;

/**
 * Created by matrixy on 2018/4/15.
 */
public class Client extends Thread
{
    CaptureWorker captureWorker;
    CompressWorker compressWorker;

    private void converse() throws Exception
    {
        Socket conn = new Socket(Configs.get("server.addr"), Configs.getInt("server.port", 1986));
        // 1. 身份验证

        // 2. 读取服务器端发来的数据包：心跳包或控制指令包


        // 3. 上报屏幕截图
    }

    public void run()
    {
        while (true)
        {
            try
            {
                converse();
                Thread.sleep(5000);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
