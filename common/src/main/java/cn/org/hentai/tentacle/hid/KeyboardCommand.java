package cn.org.hentai.tentacle.hid;

/**
 * Created by matrixy on 2018/4/16.
 */
public class KeyboardCommand extends HIDCommand
{
    public int keycode;
    public KeyboardCommand(int keycode, int timestamp)
    {
        super(TYPE_KEYBOARD, timestamp);
        this.keycode = keycode;
    }
}
