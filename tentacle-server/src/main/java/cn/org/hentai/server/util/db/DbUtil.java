package cn.org.hentai.server.util.db;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by matrixy on 2017-03-06.
 */
public final class DbUtil
{
    private DbUtil() { }

    protected static String charQuote(String val)
    {
        if (null == val) return val;
        return val.replaceAll("'", "\\\\'");
    }

    protected static String formatFieldName(String name)
    {
        String newName = "";
        boolean found = false;
        if (name.indexOf('.') > -1) name = name.replaceAll("`?\\w+`?\\.", "");
        for (int i = 0; i < name.length(); i++)
        {
            char chr = name.charAt(i);
            if (chr == '_')
            {
                found = true;
                continue;
            }
            newName += found ? Character.toUpperCase(chr) : chr;
            found = false;
        }
        return newName;
    }

    protected static String toDBName(String name)
    {
        StringBuffer newName = new StringBuffer(32);
        for (int i = 0; i < name.length(); i++)
        {
            char chr = name.charAt(i);
            if (Character.isUpperCase(chr)) newName.append('_');
            newName.append(Character.toLowerCase(chr));
        }
        return newName.toString();
    }

    protected static String formatDate(Date date)
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    protected static String valueLiteral(Object data)
    {
        String val = "null";
        if (data != null)
        {
            // number, bigdecimal, boolean, string, date, timestamp
            if (Number.class.isInstance(data)) val = String.valueOf(data);
            else if (Boolean.class.isInstance(data)) val = String.valueOf(((Boolean)data) ? 1 : 0);
            else if (String.class.isInstance(data)) val = "".equals(String.valueOf(val).trim()) ? "" : "'" + String.valueOf(data) + "'";
            else if (Date.class.isInstance(data)) val = "'" + formatDate((Date)data) + "'";
            else if (BigDecimal.class.isInstance(data)) val = String.valueOf(data);
        }
        return val;
    }
}
