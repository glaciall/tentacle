package cn.org.hentai.client.worker;

import cn.org.hentai.tentacle.system.LocalComputer;
import cn.org.hentai.tentacle.util.Log;

/**
 * Created by matrixy on 2018/4/9.
 */
public class CaptureWorker extends BaseWorker
{
    public CaptureWorker()
    {
        this.setName("capture-worker");
    }

    public void run()
    {
        while (!this.isTerminated())
        {
            try
            {
                ScreenImages.addScreenshot(LocalComputer.captureScreen());
                sleep(50);
            }
            catch(Exception e)
            {
                Log.error(e);
            }
        }
    }
}
