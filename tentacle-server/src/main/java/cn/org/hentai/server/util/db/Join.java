package cn.org.hentai.server.util.db;

/**
 * Created by matrixy on 2017-03-06.
 */
public class Join
{
    public String tableName;
    public String on;
    public Join(String tableName, String on)
    {
        this.tableName = tableName;
        this.on = on;
    }
}
