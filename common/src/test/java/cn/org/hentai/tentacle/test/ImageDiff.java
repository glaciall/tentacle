package cn.org.hentai.tentacle.test;

import cn.org.hentai.tentacle.compress.RLEncoding;
import cn.org.hentai.tentacle.graphic.Screenshot;
import cn.org.hentai.tentacle.system.LocalComputer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by matrixy on 2018/4/20.
 */
public class ImageDiff
{
    static int SEQ = 1;

    static Screenshot lastScreen = null;

    public static void main(String[] args) throws Exception
    {
        LocalComputer.init();
        lastScreen = LocalComputer.captureScreen();
        for (int i = 0; i < 100; )
        {
            if (diff()) i++;
            Thread.sleep(5);
        }
    }

    public static boolean diff() throws Exception
    {
        Screenshot screenshot = LocalComputer.captureScreen();
        int[] bitmap = new int[screenshot.bitmap.length];
        int diffCount = 0;
        for (int i = 0; i < screenshot.bitmap.length; i++)
        {
            if (screenshot.bitmap[i] != lastScreen.bitmap[i])
            {
                diffCount++;
                bitmap[i] = screenshot.bitmap[i];
            }
        }
        if (diffCount == 0) return false;
        // byte[] compressed = new RLEncoding().compress(bitmap,0, bitmap.length);
        // System.out.println("Compressed: " + compressed.length);
        // bitmap = new RLEncoding().decompress(screenshot1.width, screenshot1.height, compressed);

        BufferedImage img = new BufferedImage(screenshot.width, screenshot.height, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, screenshot.width, screenshot.height, bitmap, 0, screenshot.width);
        ImageIO.write(img, "PNG", new FileOutputStream("E:\\frames\\" + (SEQ++) + ".png"));
        lastScreen = screenshot;
        return true;
    }
}
