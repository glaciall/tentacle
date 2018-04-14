package cn.org.hentai.client.worker;

import cn.org.hentai.tentacle.compress.CompressUtil;
import cn.org.hentai.tentacle.graphic.Screenshot;

/**
 * Created by matrixy on 2018/4/10.
 */
public class CompressWorker extends Thread
{
    Screenshot lastScreen = null;

    private void compress() throws Exception
    {
        Screenshot screenshot = ScreenImages.getScreenshot();
        if (screenshot == null) return;
        while (screenshot.isExpired()) screenshot = ScreenImages.getScreenshot();

        // 分辨率是否发生了变化？
        if (lastScreen.width != screenshot.width || lastScreen.height != screenshot.height) lastScreen = null;
        // 1. 求差
        for (int i = 0; lastScreen != null && i < lastScreen.bitmap.length; i++)
            screenshot.bitmap[i] = screenshot.bitmap[i] == lastScreen.bitmap[i] ? 0 : screenshot.bitmap[i];

        // 2. 压缩
        byte[] compressedData = CompressUtil.process("rle", screenshot.bitmap);

        // 3. 入队列

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
