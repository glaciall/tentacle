package cn.org.hentai.tentacle.protocol;

/**
 * Created by matrixy on 2018/4/15.
 */
public final class Command
{
    public static final byte COMMON_RESPONSE = 0x00;                // 常规应答
    public static final byte HEARTBEAT = 0x01;                      // 心跳包
    public static final byte CONTROL_REQUEST = 0x02;                // 请求控制
    public static final byte CONTROL_RESPONSE = 0x03;               // 请求控制包的应答
    public static final byte CLOSE_REQUEST = 0x04;                  // 关闭控制
    public static final byte CLOSE_RESPONSE = 0x05;                 // 关闭控制包的应答（未启用）
    public static final byte HID_COMMAND = 0x06;                    // 人机接口指令
    public static final byte SCREENSHOT = 0x07;                     // 屏幕截图
    public static final byte SET_CLIPBOARD = 0x08;                  // 设置剪切板内容
    public static final byte SET_CLIPBOARD_RESPONSE = 0x09;         // 设置剪切板内容的应答
    public static final byte GET_CLIPBOARD = 0x10;                  // 获取剪切板内容
    public static final byte GET_CLIPBOARD_RESPONSE = 0x11;         // 获取剪切板内容的应答

    public static final byte TYPE_MOUSE = 0x01;                     // 类型：鼠标
    public static final byte TYPE_KEYBOARD = 0x02;                  // 类型：键盘
}
