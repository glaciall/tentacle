package cn.org.hentai.tentacle.compress;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by matrixy on 2019/3/8.
 */
public class DCTTest
{
    public static void main(String[] args) throws Exception
    {
        DCT dct = new DCT(25);
        BufferedImage image = ImageIO.read(new File("E:\\test\\20091124171232523.bmp"));
        int width = image.getWidth();
        int height = image.getHeight();
        int[] bitmap = image.getRGB(0, 0, width, height, null, 0, width);
        long time = System.currentTimeMillis();
        for (int y = 0; y < height; y += 8)
        {
            for (int x = 0; x < width; x += 8)
            {
                // int[][] block = dct.forwardDCT(null);
            }
        }
        time = System.currentTimeMillis() - time;

        System.out.println(String.format("Before: %8d", bitmap.length));
        // System.out.println(String.format("After : %8d", img.length));
        System.out.println(String.format("Spend : %8d", time));
    }
}
