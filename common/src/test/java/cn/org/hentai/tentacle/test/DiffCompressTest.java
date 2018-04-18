package cn.org.hentai.tentacle.test;

import cn.org.hentai.tentacle.compress.RLEncoding;
import cn.org.hentai.tentacle.graphic.Screenshot;
import cn.org.hentai.tentacle.util.ByteUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;

/**
 * Created by matrixy on 2018/4/18.
 */
public class DiffCompressTest
{
    public static void main(String[] args) throws Exception
    {
        Screenshot lastScreenshot = new Screenshot(ImageIO.read(DiffCompressTest.class.getResourceAsStream("/test1.bmp")));
        Screenshot screenshot = new Screenshot(ImageIO.read(DiffCompressTest.class.getResourceAsStream("/test1.png")));
        int[] bitmap = new int[screenshot.bitmap.length];
        int changedColors = 0, start = -1, end = bitmap.length;
        for (int i = 0; i < bitmap.length; i++)
        {
            if (lastScreenshot.bitmap[i] == screenshot.bitmap[i])
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
        RLEncoding.init();
        byte[] compressedData = new RLEncoding().compress(bitmap, start, end);
        System.out.println("Diff and compressed: " + compressedData.length);
        System.out.println("Colors changed: " + changedColors);
        System.out.println("from: " + start + ", to: " + end);
        System.out.println(ByteUtils.toString(compressedData));

        // decompress
        bitmap = new RLEncoding().decompress(screenshot.width, screenshot.height, compressedData);

        BufferedImage img = new BufferedImage(screenshot.width, screenshot.height, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, screenshot.width, screenshot.height, bitmap, 0, screenshot.width);
        ImageIO.write(img, "PNG", new FileOutputStream("E:\\test.png"));
    }
}
