package cn.org.hentai.tentacle.system;

import java.util.Date;

/**
 * Created by matrixy on 2018/5/13.
 */
public class File
{
    boolean isDirectory;
    long length;
    long lastModifiedTime;
    String name;

    public File(boolean isDirectory, long length, long lastModifiedTime, String name)
    {
        this.isDirectory = isDirectory;
        this.length = length;
        this.lastModifiedTime = lastModifiedTime;
        this.name = name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "File{" +
                "isDirectory=" + isDirectory +
                ", length=" + length +
                ", lastModifiedTime=" + new Date(lastModifiedTime).toLocaleString() +
                ", name='" + name + '\'' +
                '}';
    }
}
