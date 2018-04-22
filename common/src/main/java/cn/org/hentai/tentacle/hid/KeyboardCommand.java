package cn.org.hentai.tentacle.hid;

/**
 * Created by matrixy on 2018/4/16.
 */
public class KeyboardCommand extends HIDCommand
{
    public static final int KEY_PRESS = 0x01;
    public static final int KEY_RELEASE = 0x02;

    public int keycode;
    public int eventType;
    public KeyboardCommand(int keycode, int eventType, int timestamp)
    {
        super(TYPE_KEYBOARD, timestamp);
        this.keycode = keycode;
        this.eventType = eventType;
    }
}
