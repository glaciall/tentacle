package cn.org.hentai.client.desktop;

import cn.org.hentai.tentacle.graphic.Screenshot;
import cn.org.hentai.tentacle.protocol.Packet;

import java.util.LinkedList;

/**
 * Created by matrixy on 2019/5/18.
 */
public final class ScreenImages
{
    LinkedList<Screenshot> screenshots;
    LinkedList<Packet> compressedScreenshots;
    boolean screenshotMonitorAwaken;
    boolean compressedScreenshotMonitorAwaken;
    Object screenshotsLock = new Object();
    Object compressedScreenshotsLock = new Object();

    private ScreenImages()
    {
        screenshots = new LinkedList<Screenshot>();
        compressedScreenshots = new LinkedList<Packet>();
    }

    // 放入一个未压缩的原始截屏画面
    public void addScreenshot(Screenshot screenshot)
    {
        synchronized (screenshotsLock)
        {
            screenshots.addLast(screenshot);
            screenshotsLock.notifyAll();
        }
    }

    // 获取一个未压缩的原始截屏画面
    public Screenshot getScreenshot()
    {
        Screenshot screenshot = null;
        screenshotMonitorAwaken = false;
        synchronized (screenshotsLock)
        {
            while (screenshotMonitorAwaken == false && screenshots.size() == 0) try { screenshotsLock.wait(); } catch(Exception ex) { }
            if (screenshots.size() > 0) screenshot = screenshots.removeLast();
            screenshots.clear();
        }
        return screenshot;
    }

    // 放入一个压缩后的截屏数据
    public void addCompressedScreenshot(Packet packet)
    {
        synchronized (compressedScreenshotsLock)
        {
            compressedScreenshots.addLast(packet);
            compressedScreenshotsLock.notifyAll();
        }
    }

    // 获取一个已压缩的截屏数据
    public Packet getCompressedScreenshot()
    {
        Packet packet = null;
        compressedScreenshotMonitorAwaken = false;
        synchronized (compressedScreenshotsLock)
        {
            while (compressedScreenshotMonitorAwaken == false && compressedScreenshots.size() == 0) try { compressedScreenshotsLock.wait(); } catch(Exception ex) { }
            if (compressedScreenshots.size() > 0) packet = compressedScreenshots.removeLast();
        }
        return packet;
    }

    // 唤醒全部监听线程
    public void awakeAll()
    {
        synchronized (screenshotsLock)
        {
            screenshotMonitorAwaken = true;
            screenshots.clear();
            screenshotsLock.notifyAll();
        }
        synchronized (compressedScreenshotsLock)
        {
            compressedScreenshotMonitorAwaken = true;
            compressedScreenshots.clear();
            compressedScreenshotsLock.notifyAll();
        }
    }

    static ScreenImages instance;
    public static synchronized ScreenImages getInstance()
    {
        if (null == instance) instance = new ScreenImages();
        return instance;
    }
}
