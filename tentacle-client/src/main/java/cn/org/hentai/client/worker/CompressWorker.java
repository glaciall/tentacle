package cn.org.hentai.client.worker;

import cn.org.hentai.tentacle.graphic.Screenshot;

/**
 * Created by matrixy on 2018/4/10.
 */
public class CompressWorker extends Thread
{
    private void compress() throws Exception
    {
        Screenshot screenshot = ScreenImages.getScreenshot();
        if (screenshot == null) return;
        while (screenshot.isExpired()) screenshot = ScreenImages.getScreenshot();

        // 与上一张屏幕截图进行差异化比较
        // 进行压缩
        // 压缩后的格式，应该是什么样的？
        // COLOR_TABLE x 256
        // RLEncoding...
        //
    }

    public void run()
    {
        while (true)
        {
            try
            {
                compress();
                Thread.sleep(100);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
