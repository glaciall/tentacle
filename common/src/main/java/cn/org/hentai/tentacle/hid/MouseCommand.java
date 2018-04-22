package cn.org.hentai.tentacle.hid;

/**
 * Created by matrixy on 2018/4/16.
 */
public class MouseCommand extends HIDCommand
{
    public static final int TYPE_DOWN = 0x01;
    public static final int TYPE_UP = 0x02;
    public static final int TYPE_MOVE = 0x03;
    public static final int TYPE_WHEEL = 0x04;

    public int eventType;           // 事件类型，按下，放开，移动
    public int key;                 // 1左键，2中键，3右键，或1 向上，2向下
    public int x;
    public int y;

    public MouseCommand(int eventType, int key, int x, int y, int timestamp)
    {
        super(TYPE_MOUSE, timestamp);
        this.eventType = eventType & 0xff;
        this.key = key & 0xff;
        this.x = x;
        this.y = y;
    }
}
