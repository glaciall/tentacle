package cn.org.hentai.client.desktop;

import cn.org.hentai.tentacle.graphic.Screenshot;
import cn.org.hentai.tentacle.system.LocalComputer;
import cn.org.hentai.tentacle.util.Log;

/**
 * Created by matrixy on 2019/5/18.
 */
public class CaptureWorker extends Thread
{
    public CaptureWorker()
    {
        this.setName("capture-worker");
    }

    public void run()
    {
        while (!this.isInterrupted())
        {
            Screenshot screenshot = LocalComputer.captureScreen();
            ScreenImages.getInstance().addScreenshot(screenshot);
            try
            {
                Thread.sleep(50);
            }
            catch(InterruptedException e)
            {
                break;
            }
        }
        Log.debug(this.getName() + " terminated...");
    }
}
