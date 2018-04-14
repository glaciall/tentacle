package cn.org.hentai.tentacle.protocol;

/**
 * Created by matrixy on 2018/4/15.
 */
public final class Command
{
    public static final byte COMMON_RESPONSE = 0x00;
    public static final byte HEARTBEAT = 0x01;
    public static final byte CONTROL_REQUEST = 0x02;
    public static final byte CONTROL_RESPONSE = 0x03;
    public static final byte CLOSE_REQUEST = 0x04;
    public static final byte CLOSE_RESPONSE = 0x05;
    public static final byte HID_COMMAND = 0x06;
    public static final byte SCREENSHOT = 0x07;

    public static final byte TYPE_KEYBOARD = 0x01;
    public static final byte TYPE_MOUSE = 0x02;
}
