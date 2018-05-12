package cn.org.hentai.tentacle.system;

import java.io.File;

/**
 * Created by matrixy on 2018/5/12.
 */
public final class FileSystem
{
    public static File[] list(String path)
    {
        return "".equals(path) ? File.listRoots() : new File(path).listFiles();
    }
}
