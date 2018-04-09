package cn.org.hentai.client.worker;

/**
 * Created by matrixy on 2018/4/9.
 */
public class CaptureWorker extends Thread
{
    private void captureAndStore() throws Exception
    {

    }

    public void run()
    {
        while (true)
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
