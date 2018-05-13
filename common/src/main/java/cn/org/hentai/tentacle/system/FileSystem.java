package cn.org.hentai.tentacle.system;

import java.io.File;

/**
 * Created by matrixy on 2018/5/12.
 */
public final class FileSystem
{
    /**
     * 列出本地文件
     * @param path 文件目录，空字符串将列出根目录
     * @return 文件列表
     */
    public static File[] list(String path)
    {
        return "".equals(path) ? File.listRoots() : new File(path).listFiles();
    }
}
