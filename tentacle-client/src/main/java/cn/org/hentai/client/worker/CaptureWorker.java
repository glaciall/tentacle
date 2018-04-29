package cn.org.hentai.client.worker;

import cn.org.hentai.tentacle.system.LocalComputer;
import cn.org.hentai.tentacle.util.Log;

/**
 * Created by matrixy on 2018/4/9.
 */
public class CaptureWorker extends BaseWorker
{
    private void captureAndStore() throws Exception
    {
        ScreenImages.addScreenshot(LocalComputer.captureScreen());
    }

    public void run()
    {
        while (!this.isTerminated())
        {
            try
            {
                captureAndStore();
                // TODO: FPS控制
                sleep(50);
            }
            catch(Exception e)
            {
                Log.error(e);
            }
        }
    }
}
