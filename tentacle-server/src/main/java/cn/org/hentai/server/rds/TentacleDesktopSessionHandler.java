package cn.org.hentai.server.rds;

import cn.org.hentai.server.controller.FileDownloadController;
import cn.org.hentai.server.rds.controller.*;
import cn.org.hentai.server.wss.TentacleDesktopWSS;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Message;
import cn.org.hentai.tentacle.protocol.Packet;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matrixy on 2019/1/4.
 * 远程桌面会话
 */
public final class TentacleDesktopSessionHandler
{
    private static final Map<Byte, Class<? extends BaseMessageController>> controllers = new HashMap();

    // 注册消息指令处理器
    // 每增加一个新的指令，必须在此添加相应的消息指令控制器
    static
    {
        registerController(Command.AUTHENTICATE, AuthenticateController.class);
        registerController(Command.HEARTBEAT, HeartbeatController.class);
        registerController(Command.SCREENSHOT, ScreenshotController.class);
        registerController(Command.LIST_FILES_RESPONSE, ListFileResponseController.class);
        registerController(Command.DOWNLOAD_FILE_RESPONSE, DownloadFileResponseController.class);
        registerController(Command.GET_CLIPBOARD_RESPONSE, GetClipboardResponseController.class);
        registerController(Command.SET_CLIPBOARD_RESPONSE, SetClipboardResponseController.class);
        registerController(Command.CONTROL_RESPONSE, RemoteControlResponseController.class);
        registerController(Command.CLOSE_RESPONSE, RemoteControlCloseResponseController.class);
        registerController(Command.COMMON_RESPONSE, CommonResponseController.class);
        registerController(Command.UPLOAD_FILE_RESPONSE, UploadFileResponseController.class);
    }

    private static final void registerController(byte command, Class<? extends BaseMessageController> controllerClass)
    {
        controllers.put(command, controllerClass);
    }

    public static final BaseMessageController getController(byte command)
    {
        Class cls = controllers.get(command);
        try
        {
            if (cls != null) return (BaseMessageController) cls.newInstance();
            else return null;
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
