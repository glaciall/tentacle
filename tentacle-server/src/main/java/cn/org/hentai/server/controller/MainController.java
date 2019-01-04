package cn.org.hentai.server.controller;

import cn.org.hentai.server.model.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by matrixy on 2018/4/12.
 */
@RequestMapping("/")
@Controller
public class MainController
{
    @RequestMapping("/")
    public String index(HttpSession session)
    {
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
}
