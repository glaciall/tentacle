package cn.org.hentai.server.util.db;

/**
 * Created by matrixy on 2017-03-06.
 */
public class Concatenation
{
    public static enum Concate
    {
        direct, gtz, notnull, like
    };

    public Concate concateType;
    public Object data;

    public Concatenation(Concate concateType, Object data)
    {
        this.concateType = concateType;
        this.data = data;
    }
}
