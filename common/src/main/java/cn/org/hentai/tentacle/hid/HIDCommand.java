package cn.org.hentai.tentacle.hid;

/**
 * Created by matrixy on 2018/4/16.
 */
public class HIDCommand
{
    public static final int TYPE_MOUSE = 1;
    public static final int TYPE_KEYBOARD = 2;

    public int type;
    public int timestamp;

    public HIDCommand(int type, int timestamp)
    {
        this.type = type;
        this.timestamp = timestamp;
    }
}
