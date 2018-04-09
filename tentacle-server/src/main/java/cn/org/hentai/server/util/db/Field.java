package cn.org.hentai.server.util.db;

/**
 * Created by matrixy on 2017-03-06.
 */
public class Field
{
    public String name;
    public Object value;

    public Field(String name, Object value)
    {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Field{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
