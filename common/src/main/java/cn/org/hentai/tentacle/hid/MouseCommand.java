package cn.org.hentai.tentacle.hid;

/**
 * Created by matrixy on 2018/4/16.
 */
public class MouseCommand extends HIDCommand
{
    public static final int TYPE_DOWN = 0x01;
    public static final int TYPE_UP = 0x02;
    public static final int TYPE_MOVE = 0x03;

    public int type;
    public int key;
    public int x;
    public int y;
    public int timestamp;

    public MouseCommand(int type, int key, int x, int y, int timestamp)
    {
        this.type = type & 0x03;
        this.key = key & 0x03;
        this.x = x;
        this.y = y;
        this.timestamp = timestamp;
    }
}
