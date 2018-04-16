package cn.org.hentai.client.worker;

import cn.org.hentai.tentacle.compress.CompressUtil;
import cn.org.hentai.tentacle.graphic.Screenshot;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.ByteUtils;

/**
 * Created by matrixy on 2018/4/10.
 */
public class CompressWorker implements Runnable
{
    String compressMethod = "rle";
    Screenshot lastScreen = null;

    public CompressWorker()
    {
        // do nothing here
    }

    public CompressWorker(String method)
    {
        this.compressMethod = method;
    }

    private void compress() throws Exception
    {
        Screenshot screenshot = ScreenImages.getScreenshot();
        while (true)
        {
            screenshot = ScreenImages.getScreenshot();
            if (!ScreenImages.hasScreenshots()) break;
        }
        if (screenshot.isExpired()) return;

        // 分辨率是否发生了变化？
        if (lastScreen != null && (lastScreen.width != screenshot.width || lastScreen.height != screenshot.height)) lastScreen = null;

        // 1. 求差
        int[] bitmap = new int[screenshot.bitmap.length];
        int changedColors = 0;
        for (int i = 0; lastScreen != null && i < lastScreen.bitmap.length; i++)
        {
            if (screenshot.bitmap[i] == lastScreen.bitmap[i])
            {
                bitmap[i] = 0;
            }
            else
            {
                changedColors += 1;
                bitmap[i] = screenshot.bitmap[i];
            }
        }
        if (changedColors == 0) return;

        // 2. 压缩
        byte[] compressedData = CompressUtil.process(this.compressMethod, screenshot.bitmap);

        // 3. 入队列
        Packet packet = Packet.create(Command.SCREENSHOT, compressedData.length + 12);
        packet.addShort((short)screenshot.width)
                .addShort((short)screenshot.height)
                .addLong(screenshot.captureTime);
        packet.addBytes(compressedData);
        ScreenImages.addCompressedScreen(packet);

        lastScreen = screenshot;
    }

    public void run()
    {
        while (!Thread.interrupted())
        {
            try
            {
                compress();
                Thread.sleep(5);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
