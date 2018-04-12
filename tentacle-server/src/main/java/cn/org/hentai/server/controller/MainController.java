package cn.org.hentai.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by matrixy on 2018/4/12.
 */
@RequestMapping("/")
@Controller
public class MainController
{
    @RequestMapping("/")
    public String index()
    {
        return "index";
    }
}
