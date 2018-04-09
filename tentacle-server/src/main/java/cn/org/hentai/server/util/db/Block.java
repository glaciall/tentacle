package cn.org.hentai.server.util.db;

import java.math.BigDecimal;

/**
 * Created by matrixy on 2017-03-06.
 */
public class Block
{
    public String sql;
    public Object data;

    public Block(String sql, Object data)
    {
        this.sql = sql;
        this.data = data;
    }

    public Block(String sql)
    {
        this.sql = sql;
        this.data = null;
    }

    public String format()
    {
        return format(true);
    }

    public String format(boolean merged)
    {
        // TODO: 需要处理*号
        // 暂时不处理那种的
        // if (null == this.data) return this.sql;
        String wildcard = "?";
        if (this.sql.indexOf('*') > -1) wildcard = "*";

        Concatenation.Concate concateType = Concatenation.Concate.direct;
        if (this.data instanceof Concatenation)
        {
            concateType = ((Concatenation)this.data).concateType;
            this.data = ((Concatenation)this.data).data;
        }

        // date
        if (this.data instanceof java.util.Date)
        {
            String val = DbUtil.formatDate((java.util.Date)this.data);
            return merged ? this.sql.replace(wildcard, "'" + val + "'") : this.sql;
        }

        // number
        if (this.data instanceof Integer)
        {
            int val = (Integer)this.data;
            if (concateType == Concatenation.Concate.gtz) return val > 0 ? (merged ? this.sql.replace(wildcard, String.valueOf(this.data)) : this.sql) : null;
            else return merged ? this.sql.replace(wildcard, String.valueOf(this.data)) : this.sql;
        }
        if (this.data instanceof Long)
        {
            long val = (Long)this.data;
            if (concateType == Concatenation.Concate.gtz) return val > 0 ? (merged ? this.sql.replace(wildcard, String.valueOf(this.data)) : this.sql) : null;
            else return merged ? this.sql.replace(wildcard, String.valueOf(this.data)) : this.sql;
        }
        if (this.data instanceof Float)
        {
            float val = (Float)this.data;
            if (concateType == Concatenation.Concate.gtz) return val > 0 ? (merged ? this.sql.replace(wildcard, String.valueOf(this.data)) : this.sql) : null;
            else return merged ? this.sql.replace(wildcard, String.valueOf(this.data)) : this.sql;
        }
        if (this.data instanceof Double)
        {
            double val = (Double)this.data;
            if (concateType == Concatenation.Concate.gtz) return val > 0 ? (merged ? this.sql.replace(wildcard, String.valueOf(this.data)) : this.sql) : null;
            else return merged ? this.sql.replace(wildcard, String.valueOf(this.data)) : this.sql;
        }
        if (this.data instanceof Byte)
        {
            byte val = (Byte)this.data;
            if (concateType == Concatenation.Concate.gtz) return val > 0 ? (merged ? this.sql.replace(wildcard, String.valueOf(this.data)) : this.sql) : null;
            else return merged ? this.sql.replace(wildcard, String.valueOf(this.data)) : this.sql;
        }
        // bool
        if (this.data instanceof Boolean)
        {
            boolean val = (Boolean)this.data;
            return merged ? this.sql.replace(wildcard, String.valueOf(val ? 1 : 0)) : this.sql;
        }
        if (this.data instanceof BigDecimal)
        {
            BigDecimal val = (BigDecimal)this.data;
            if (concateType == Concatenation.Concate.gtz && val.compareTo(BigDecimal.ZERO) > 0) return merged ? this.sql.replace(wildcard, String.valueOf(val)) : this.sql;
            return merged ? this.sql.replace(wildcard, String.valueOf(val)) : this.sql;
        }
        // array

        // string，应该放在最后且必然执行，因为null与String傻傻分不清，null instanceof everything
        {
            String val = DbUtil.charQuote((String)this.data);
            if (concateType == Concatenation.Concate.direct)
            {
                // TODO: 没有判断!=的情况
                // TODO: 也没有判断>=、<=等情况
                if (null == val) return this.sql.replaceAll("(?is)(=\\s*\\?)|(like\\s*\\?)", " is null");
                return merged ? this.sql.replace(wildcard, "'" + (val).trim() + "'") : this.sql;
            }
            if (concateType == Concatenation.Concate.notnull)
            {
                if (null == val || val.trim().length() == 0) return null;
                return merged ? this.sql.replace(wildcard, "'" + (val).trim() + "'") : this.sql;
            }
            if (concateType == Concatenation.Concate.like)
            {
                if (null == val || val.trim().length() == 0) return null;
                return merged ? this.sql.replace(wildcard, "'%" + (val).trim() + "%'").replace("=", " like ") : this.sql;
            }
        }
        return null;
    }
}
