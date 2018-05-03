package cn.org.hentai.client.app;

import cn.org.hentai.client.client.Client;
import cn.org.hentai.tentacle.compress.RLEncoding;
import cn.org.hentai.tentacle.hid.KeyMapping;
import cn.org.hentai.tentacle.system.LocalComputer;
import cn.org.hentai.tentacle.util.Configs;

/**
 * Created by matrixy on 2018/4/9.
 */
public class Tentacle
{
    public Tentacle()
    {
        initCore();
    }

    // 核心模块初始化，提高运行中的性能
    private void initCore()
    {
        // 加载配置文件
        Configs.init("/client.properties");

        KeyMapping.init();

        // 静态成员初始化
        RLEncoding.init();

        LocalComputer.init();

        new Client().start();
    }

    public static void main(String[] args) throws Exception
    {
        new Tentacle();
    }
}
