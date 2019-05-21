package cn.org.hentai.client.desktop;

import cn.org.hentai.tentacle.compress.CompressUtil;
import cn.org.hentai.tentacle.graphic.Screenshot;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.Log;

/**
 * Created by matrixy on 2019/5/18.
 */
public class CompressWorker extends Thread
{
    String compressMethod = "rle";          // 压缩方式
    Screenshot lastScreen = null;           // 上一屏的截屏，用于比较图像差
    int sequence = 10000;

    public CompressWorker()
    {
        this.setName("compress-worker");
    }

    private boolean compress() throws Exception
    {
        Screenshot screenshot = ScreenImages.getInstance().getScreenshot();
        if (screenshot == null) return false;
        if (screenshot.isExpired()) return true;

        // 分辨率是否发生了变化？
        if (lastScreen != null && (lastScreen.width != screenshot.width || lastScreen.height != screenshot.height)) lastScreen = null;

        long stime = System.currentTimeMillis();

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

        if (lastScreen != null && changedColors == 0) return true;

        // 2. 压缩
        start = Math.max(start, 0);
        start = 0;
        end = bitmap.length;
        byte[] compressedData = CompressUtil.process(this.compressMethod, bitmap, start, end);

        // 3. 加入已压缩待发送列表中去
        int seq = sequence++;
        Packet packet = Packet.create(Command.SCREENSHOT, compressedData.length + 16);
        packet.addShort((short)screenshot.width)
                .addShort((short)screenshot.height)
                .addLong(screenshot.captureTime)
                .addInt(seq);
        packet.addBytes(compressedData);
        ScreenImages.getInstance().addCompressedScreenshot(packet);

        lastScreen = screenshot;
        return true;
    }

    public void run()
    {
        while (!this.isInterrupted())
        {
            try
            {
                boolean goOn = compress();
                if (goOn == false) break;
            }
            catch (InterruptedException e)
            {
                break;
            }
            catch (Exception e)
            {
                lastScreen = null;
            }
        }
        Log.debug(this.getName() + " terminated...");
    }
}
