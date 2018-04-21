package cn.org.hentai.tentacle.hid;

/**
 * Created by matrixy on 2018/4/16.
 */
public class MouseCommand extends HIDCommand
{
    public static final int TYPE_DOWN = 0x01;
    public static final int TYPE_UP = 0x02;
    public static final int TYPE_MOVE = 0x03;

    public int eventType;           // 事件类型，按下，放开，移动
    public int key;                 // 1左键，2中键，3右键
    public int x;
    public int y;

    public MouseCommand(int eventType, int key, int x, int y, int timestamp)
    {
        super(TYPE_MOUSE, timestamp);
        this.eventType = eventType & 0x03;
        this.key = key & 0x03;
        this.x = x;
        this.y = y;
    }
}
