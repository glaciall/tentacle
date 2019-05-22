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
    public static final byte LIST_FILES = 0x12;                     // 获取文件列表
    public static final byte LIST_FILES_RESPONSE = 0x13;            // 获取文件列表的应答
    public static final byte DOWNLOAD_FILE = 0x14;                  // 下载文件
    public static final byte DOWNLOAD_FILE_RESPONSE = 0x15;         // 下载文件的应答
    public static final byte UPLOAD_FILE = 0x16;                    // 上传文件
    public static final byte UPLOAD_FILE_RESPONSE = 0x17;           // 上传文件的应答
    public static final byte AUTHENTICATE = 0x18;                   // 客户端认证
    public static final byte AUTHENTICATE_RESPONSE = 0x19;          // 客户端认证应答

    public static final byte SCREENSHOT_FRAGMENT = 0x20;            // 屏幕截图的分包
    public static final byte SCREENSHOT_FRAGMENT_RESPONSE = 0x21;   // 确认收到屏幕截图分包的应答


    public static final byte TYPE_MOUSE = 0x01;                     // 类型：鼠标
    public static final byte TYPE_KEYBOARD = 0x02;                  // 类型：键盘
}
