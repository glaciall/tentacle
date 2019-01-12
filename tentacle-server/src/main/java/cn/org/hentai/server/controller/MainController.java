package cn.org.hentai.server.controller;

import cn.org.hentai.server.model.Result;
import cn.org.hentai.server.rds.SessionManager;
import cn.org.hentai.server.rds.TentacleDesktopSession;
import cn.org.hentai.tentacle.protocol.Command;
import cn.org.hentai.tentacle.protocol.Message;
import cn.org.hentai.tentacle.protocol.Packet;
import cn.org.hentai.tentacle.util.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by matrixy on 2018/4/12.
 */
@RequestMapping("/")
@Controller
public class MainController
{
    static final AtomicLong httpSessionId = new AtomicLong(0);

    @Value("${spring.http.multipart.maxFileSize}")
    String maxUploadSize;

    @RequestMapping("/")
    public String index(HttpSession session, Model model)
    {
        char unit = maxUploadSize.charAt(maxUploadSize.length() - 2);
        long maxSize = Long.parseLong(maxUploadSize.substring(0, maxUploadSize.length() - 2));
        maxSize = maxSize * (unit == 'M' ? 1024 * 1024 : 1024);

        model.addAttribute("httpSessionId", httpSessionId.addAndGet(1));
        model.addAttribute("maxUploadSize", String.valueOf(maxSize));
        session.setMaxInactiveInterval(60);
        return "index";
    }

    @RequestMapping("/keepalive")
    @ResponseBody
    public Result keepalive()
    {
        // 什么都不做，只是为了保持会话
        return new Result();
    }

    @RequestMapping("/file/upload")
    @ResponseBody
    public Result fileUpload(@RequestParam String fileId,
                             @RequestParam String filePath,
                             @RequestParam Long sessionId,
                             @RequestParam("file") MultipartFile file)
    {
        Result result = new Result();
        try
        {
            if (StringUtils.isEmpty(filePath)) throw new RuntimeException("当前路径不允许上传文件");

            InputStream freader = file.getInputStream();
            int len = -1;
            byte[] block = new byte[4096];
            TentacleDesktopSession session = SessionManager.getSession(sessionId);
            if (null == session) throw new RuntimeException("无效的远程主机会话ID");

            Message msg = new Message();
            msg.setCommand(Command.UPLOAD_FILE);
            Packet packet = Packet.create(4096 + 8);

            // 发送头部
            byte[] nameBytes = file.getOriginalFilename().getBytes("UTF-8");
            byte[] pathBytes = filePath.getBytes("UTF-8");
            // 第0个包就用来传递文件信息吧
            packet.addInt(0);
            packet.addLong(file.getSize());
            packet.addInt(pathBytes.length);
            packet.addBytes(pathBytes);
            packet.addInt(nameBytes.length);
            packet.addBytes(nameBytes);
            msg.setBody(packet);
            session.send(msg);

            int i = 1;
            while ((len = (freader.read(block))) > 0)
            {
                packet.reset();
                packet.addInt(i++);
                packet.addInt(len);
                packet.addBytes(block, 0, len);
                msg.setBody(packet);

                session.send(msg);
            }
        }
        catch(Exception ex)
        {
            result.setError(ex);
        }
        return result;
    }
}
