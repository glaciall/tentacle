package cn.org.hentai.server.util;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by matrixy on 2017/8/14.
 */
public class Configs
{
    static Properties properties = new Properties();
    static {
        try
        {
            properties.load(Configs.class.getResourceAsStream("/application.properties"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static String get(String key)
    {
        Object val = properties.get(key);
        if (null == val) return null;
        else return String.valueOf(val).trim();
    }

    public static int getInt(String key, int defaultVal)
    {
        String val = get(key);
        if (null == val) return defaultVal;
        else return Integer.parseInt(val);
    }
}
