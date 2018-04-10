package cn.org.hentai.client.worker;

import cn.org.hentai.tentacle.graphic.Screenshot;

import java.util.LinkedList;

/**
 * Created by matrixy on 2018/4/9.
 */
public final class ScreenImages
{
    static LinkedList<Screenshot> screenshotImages = new LinkedList<Screenshot>();
    static LinkedList<Object> screenDifferences = new LinkedList<Object>();

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


}
