package cn.org.hentai.tentacle.test;

import cn.org.hentai.tentacle.compress.RLEncoding;
import cn.org.hentai.tentacle.graphic.Screenshot;
import cn.org.hentai.tentacle.util.ByteUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InterruptedIOException;
import java.util.HashMap;

/**
 * Created by matrixy on 2018/4/10.
 */
public class CompressTest
{
    // 行程编码+颜色表图像数据压缩
    public static void main(String[] args) throws Exception
    {
        long stime = System.currentTimeMillis();
        Screenshot screenshot = new Screenshot(ImageIO.read(CompressTest.class.getResourceAsStream("/screenshot.png")));
        stime = System.currentTimeMillis() - stime;
        System.out.println("decode: " + stime);
        RLEncoding.init();
        new RLEncoding().compress(screenshot.bitmap);
        new RLEncoding().compress(screenshot.bitmap);
        long time = System.currentTimeMillis();
        byte[] compressedData = new RLEncoding().compress(screenshot.bitmap);
        time = System.currentTimeMillis() - time;
        System.out.println("Before: " + (screenshot.bitmap.length * 4));
        System.out.println("After: " + compressedData.length);
        System.out.println("Spend: " + time);

        BufferedImage img = new BufferedImage(screenshot.width, screenshot.height, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, screenshot.width, screenshot.height, screenshot.bitmap, 0, screenshot.width);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096 * 1024);
        stime = System.currentTimeMillis();
        ImageIO.write(img, "JPG", baos);
        stime = System.currentTimeMillis() - stime;
        System.out.println("system encoder: " + stime);
    }

    // 查找图像中出现次数最多的颜色
    public static void main_find_main_colors(String[] args) throws Exception
    {
        Screenshot screenshot = new Screenshot(ImageIO.read(CompressTest.class.getResourceAsStream("/pretty.jpg")));
        RLEncoding.init();
        long time = System.currentTimeMillis();
        RLEncoding.findMainColors(screenshot.bitmap);
        time = System.currentTimeMillis() - time;
        System.out.println("Spend: " + time);
    }

    // 行程编码压缩测试
    public static void main_rlecompress_test(String[] args) throws Exception
    {
        Screenshot screenshot = new Screenshot(ImageIO.read(CompressTest.class.getResourceAsStream("/test1.bmp")));

        // 主颜色表
        HashMap<Integer, Integer> colortable = new HashMap<Integer, Integer>();
        colortable.put(0xf6ebbc, 0x01);
        colortable.put(0xffffff, 0x02);
        colortable.put(0xf0f0f0, 0x03);
        colortable.put(0xd4d4d4, 0x04);
        colortable.put(0x66b6ff, 0x05);
        colortable.put(0xe8e8e8, 0x06);

        HashMap<Integer, Integer> colorIndex = new HashMap<Integer, Integer>();
        colorIndex.put(0x01, 0xf6ebbc);
        colorIndex.put(0x02, 0xffffff);
        colorIndex.put(0x03, 0xf0f0f0);
        colorIndex.put(0x04, 0xd4d4d4);
        colorIndex.put(0x05, 0x66b6ff);
        colorIndex.put(0x06, 0xe8e8e8);

        // 行程编码
        // 当前颜色，跟上一个颜色是不是同样的，是：继续前进，累加行程，否，这是个新的起点
        ByteArrayOutputStream baos = new ByteArrayOutputStream(40960);
        int rl = 1;
        int lastColor = screenshot.bitmap[0] & 0xffffff;
        for (int i = 1; i < screenshot.bitmap.length; i++)
        {
            int color = screenshot.bitmap[i] & 0xffffff;
            if (color == lastColor && rl < 127)
            {
                rl += 1;
            }
            else
            {
                if (!colortable.containsKey(lastColor))
                {
                    baos.write(rl);
                    baos.write((byte) ((lastColor >> 16) & 0xff));
                    baos.write((byte) ((lastColor >> 8) & 0xff));
                    baos.write((byte) (lastColor & 0xff));
                }
                else
                {
                    baos.write(rl | 0x80);
                    baos.write(colortable.get(lastColor));
                }

                rl = 1;
                lastColor = color;
            }
        }
        System.out.println("before compress: " + (screenshot.bitmap.length * 4));
        System.out.println("compressed size: " + baos.size());

        // 重建图像
        byte[] rgb = baos.toByteArray();
        int[] bitmap = new int[screenshot.width * screenshot.height];

        System.out.println(ByteUtils.toString(rgb));

        for (int i = 0, s = 0; i < rgb.length; )
        {
            int rLength = rgb[i] & 0xff;
            if ((rLength & 0x80) > 0)
            {
                int color = colorIndex.get(rgb[i + 1] & 0xff);
                for (int k = 0, l = rLength & 0x7f; k < l; k++)
                {
                    bitmap[s++] = color;
                }
                i += 2;
                continue;
            }
            for (int k = 0, l = rLength & 0x7f; k < l; k++)
            {
                int red = rgb[i + 1] & 0xff;
                int green = rgb[i + 2] & 0xff;
                int blue = rgb[i + 3] & 0xff;
                int color = (red << 16) | (green << 8) | (blue);
                bitmap[s++] = color;
            }
            i += 4;
        }

        BufferedImage img = new BufferedImage(screenshot.width, screenshot.height, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, screenshot.width, screenshot.height, bitmap, 0, screenshot.width);
        ImageIO.write(img, "PNG", new FileOutputStream("E:\\test.png"));
    }
}
