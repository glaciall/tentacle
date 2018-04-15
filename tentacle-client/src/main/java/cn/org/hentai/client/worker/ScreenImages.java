package cn.org.hentai.client.worker;

import cn.org.hentai.tentacle.graphic.Screenshot;
import cn.org.hentai.tentacle.protocol.Packet;

import java.util.LinkedList;

/**
 * Created by matrixy on 2018/4/9.
 */
public final class ScreenImages
{
    static LinkedList<Screenshot> screenshotImages = new LinkedList<Screenshot>();
    static LinkedList<Packet> compressedScreens = new LinkedList<Packet>();

    // 原始截图相关
    public static void addScreenshot(Screenshot screenshot)
    {
        synchronized (screenshotImages)
        {
            screenshotImages.add(screenshot);
        }
    }

    public static Screenshot getScreenshot()
    {
        if (screenshotImages.size() == 0) return null;
        synchronized (screenshotImages)
        {
            return screenshotImages.removeFirst();
        }
    }

    // 压缩后的图像数据相关
    public static boolean hasCompressedScreens()
    {
        return compressedScreens.size() > 0;
    }

    public static void addCompressedScreen(Packet packet)
    {
        synchronized (compressedScreens)
        {
            compressedScreens.add(packet);
        }
    }

    public static Packet getCompressedScreen()
    {
        if (compressedScreens.size() == 0) return null;
        synchronized (compressedScreens)
        {
            return compressedScreens.removeFirst();
        }
    }
}
