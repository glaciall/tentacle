package cn.org.hentai.tentacle.graphic;

/**
 * Created by matrixy on 2018/4/9.
 * 发生图像变化的区域
 */
public final class HotSpot
{
    // 宽度
    public int width;

    // 高度
    public int height;

    // 区域位图
    public int[] bitmap;

    private HotSpot(int width, int height)
    {
        this.width = width;
        this.height = height;
        this.bitmap = new int[width * height];
    }

    public static HotSpot create(int width, int height)
    {
        return new HotSpot(width, height);
    }
}
