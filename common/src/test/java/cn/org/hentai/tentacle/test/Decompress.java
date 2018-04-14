package cn.org.hentai.tentacle.test;

import cn.org.hentai.tentacle.compress.RLEncoding;
import cn.org.hentai.tentacle.graphic.Screenshot;
import cn.org.hentai.tentacle.util.ByteUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;

/**
 * Created by matrixy on 2018/4/14.
 */
public class Decompress
{
    public static void main(String[] args) throws Exception
    {
        Screenshot screenshot = new Screenshot(ImageIO.read(Decompress.class.getResourceAsStream("/10x10.png")));
        byte[] compressedData = new RLEncoding().compress(screenshot.bitmap);

        System.out.println(ByteUtils.toString(compressedData));

        int[] bitmap = new int[screenshot.width * screenshot.height];
        for (int k = 0, i = ((compressedData[0] & 0xff) * 3) + 1; i < compressedData.length; )
        {
            int rl = compressedData[i] & 0xff;
            int red, green, blue;
            if ((rl & 0x80) > 0)
            {
                int index = (compressedData[i + 1] & 0xff) * 3 + 1;
                red = compressedData[index] & 0xff;
                green = compressedData[index + 1] & 0xff;
                blue = compressedData[index + 2] & 0xff;
                i += 2;
            }
            else
            {
                red = compressedData[i + 1] & 0xff;
                green = compressedData[i + 2] & 0xff;
                blue = compressedData[i + 3] & 0xff;
                i += 4;
            }
            for (int s = 0, l = rl & 0x7f; s < l; s++)
                bitmap[k++] = (red << 16) | (green << 8) | blue;
        }
        BufferedImage img = new BufferedImage(screenshot.width, screenshot.height, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, screenshot.width, screenshot.height, bitmap, 0, screenshot.width);
        ImageIO.write(img, "PNG", new FileOutputStream("E:\\test.png"));
    }
}
