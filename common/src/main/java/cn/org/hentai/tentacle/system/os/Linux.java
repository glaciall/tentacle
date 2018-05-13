package cn.org.hentai.tentacle.system.os;

import cn.org.hentai.tentacle.system.SystemCall;

/**
 * Created by matrixy on 2018/5/8.
 */
public class Linux implements SystemCall
{
    public boolean login(String username, String password) throws Exception
    {
        return false;
    }

    public boolean lock() throws Exception
    {
        return false;
    }
}
