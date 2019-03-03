package cn.org.hentai.client.worker;

import cn.org.hentai.tentacle.compress.CompressUtil;
import cn.org.hentai.tentacle.graphic.Screenshot;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.ByteUtils;
import cn.org.hentai.tentacle.util.Log;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;

/**
 * Created by matrixy on 2018/4/10.
 */
public class CompressWorker extends BaseWorker
{
    String compressMethod = "rle";          // 压缩方式
    Screenshot lastScreen = null;           // 上一屏的截屏，用于比较图像差
    int sequence = 10000;
    boolean resetScreenshot = false;
    PacketDeliveryWorker deliveryWorker = null;

    public CompressWorker()
    {
        deliveryWorker = new PacketDeliveryWorker(this);
        this.setName("compress-worker");
        deliveryWorker.start();
    }

    public CompressWorker(String method)
    {
        this.compressMethod = method;
    }

    public void resetScreenshot()
    {
        resetScreenshot = true;
    }

    private void compress() throws Exception
    {
        Screenshot screenshot = null;
        while (true)
        {
            if (!ScreenImages.hasScreenshots()) break;
            screenshot = ScreenImages.getScreenshot();
        }
        if (screenshot == null || screenshot.isExpired()) return;

        if (resetScreenshot) lastScreen = null;

        // 分辨率是否发生了变化？
        if (lastScreen != null && (lastScreen.width != screenshot.width || lastScreen.height != screenshot.height)) lastScreen = null;

        // 1. 求差
        int[] bitmap = new int[screenshot.bitmap.length];
        int changedColors = 0, start = -1, end = bitmap.length;
        if (lastScreen != null)
        {
            for (int i = 0; i < bitmap.length; i++)
            {
                if (lastScreen.bitmap[i] == screenshot.bitmap[i])
                {
                    bitmap[i] = 0;
                }
                else
                {
                    if (start == -1) start = i;
                    else end = i;
                    changedColors += 1;
                    bitmap[i] = screenshot.bitmap[i];
                }
            }
        }
        else bitmap = screenshot.bitmap;

        if (lastScreen != null && changedColors == 0) return;
        // Log.debug("Changed colors: " + changedColors);

        // 2. 压缩
        start = Math.max(start, 0);
        start = 0;
        end = bitmap.length;
        byte[] compressedData = CompressUtil.process(this.compressMethod, bitmap, start, end);

        // Log.debug("Compress Ratio: " + (screenshot.bitmap.length * 4.0f / compressedData.length));
        // Log.debug("After: " + (compressedData.length / 1024));

        // 3. 入队列
        Packet packet = Packet.create(Command.SCREENSHOT, compressedData.length + 16);
        packet.addShort((short)screenshot.width)
                .addShort((short)screenshot.height)
                .addLong(screenshot.captureTime)
                .addInt(sequence++);
        packet.addBytes(compressedData);
        Log.debug(String.format("screenshot: %d", sequence));
        // ScreenImages.addCompressedScreen(packet);
        deliveryWorker.send(packet);

        lastScreen = screenshot;
    }

    public void run()
    {
        while (!this.isTerminated())
        {
            try
            {
                compress();
                sleep(100);
            }
            catch(Exception e)
            {
                Log.error(e);
            }
        }
        if (deliveryWorker != null) deliveryWorker.terminate();
    }
}
