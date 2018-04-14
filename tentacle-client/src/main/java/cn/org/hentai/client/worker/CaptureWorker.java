package cn.org.hentai.client.worker;

import cn.org.hentai.tentacle.system.LocalComputer;

/**
 * Created by matrixy on 2018/4/9.
 */
public class CaptureWorker extends Thread
{
    private void captureAndStore() throws Exception
    {
        ScreenImages.addScreenshot(LocalComputer.captureScreen());
    }

    public void run()
    {
        while (!Thread.interrupted())
        {
            try
            {
                captureAndStore();
                // TODO: FPS控制
                Thread.sleep(100);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
