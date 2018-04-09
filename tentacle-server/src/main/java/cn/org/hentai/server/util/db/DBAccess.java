package cn.org.hentai.server.util.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by matrixy on 2017-03-06.
 */
public abstract class DBAccess
{
    @Autowired
    JdbcTemplate jdbcTemplate;

    // *************************************************************************
    // *************************************************************************
    // 类配置
    public abstract String[] configureFields();

    public abstract String configureTableName();

    public String primaryKey()
    {
        return "id";
    }

    public String[] alias(String prefix, String... extras)
    {
        String[] fields = configureFields();
        String[] aliasedFields = new String[fields.length + extras.length];
        for (int i = 0; i < fields.length; i++) aliasedFields[i] = prefix + '.' + fields[i];
        for (int i = 0; i < extras.length; i++)
        {
            String field = extras[i];
            if (field.indexOf(' ') == -1 && field.indexOf('_') > -1) field = DbUtil.formatFieldName(field);
            aliasedFields[i + fields.length] = field;
        }
        return aliasedFields;
    }

    // 修改相关
    public UpdateSQL update()
    {
        return new UpdateSQL().setTableName(configureTableName()).setPrimaryKey(this.primaryKey());
    }

    // 插入相关
    public final InsertSQL insertInto()
    {
        return new InsertSQL().setTableName(configureTableName()).setPrimaryKey(this.primaryKey());
    }

    public final InsertSQL insertInto(String tableName)
    {
        return new InsertSQL().setTableName(tableName).setPrimaryKey(this.primaryKey());
    }

    // 查询相关
    protected final QuerySQL select()
    {
        return new QuerySQL().setFields(configureFields()).from(configureTableName()).setPrimaryKey(primaryKey());
    }

    public final QuerySQL select(String... fields)
    {
        return new QuerySQL().setFields(fields).setPrimaryKey(primaryKey()).from(configureTableName());
    }

    public final Clause clause(String sql, Object value)
    {
        return new Clause().and(sql, value);
    }

    public final Clause clause(String sql)
    {
        return new Clause().and(sql, null);
    }

    // 条件联接器
    public static Concatenation gtz(Object data)
    {
        return new Concatenation(Concatenation.Concate.gtz, data);
    }

    public static Concatenation notnull(Object data)
    {
        return new Concatenation(Concatenation.Concate.notnull, data);
    }

    public static Concatenation like(Object data)
    {
        return new Concatenation(Concatenation.Concate.like, data);
    }

    // SQL执行函数
    private static final PreparedStatementSetter blankSetter = new PreparedStatementSetter()
    {
        @Override
        public void setValues(PreparedStatement ps) throws SQLException
        {
            // do nothing here
        }
    };
    public <E> List<E> query(String sql, Class cls)
    {
        System.err.println("SQL: " + sql);
        return (List<E>)jdbcTemplate.query(sql, blankSetter, new BeanPropertyRowMapper(cls));
    }

    public <E> E queryForValue(String sql, Class type)
    {
        System.err.println("SQL: " + sql);
        E value = null;
        try
        {
            value = (E) jdbcTemplate.queryForObject(sql, type);
        }
        catch(EmptyResultDataAccessException e)
        {
            // ..
        }
        return value;
    }

    public <E> E queryOne(String sql, Class cls)
    {
        System.err.println("SQL: " + sql);
        List<E> list = query(sql, cls);
        if (list == null || list.size() == 0) return null;
        else return list.get(0);
    }

    public Integer execute(String sql, Object...values)
    {
        System.err.println("SQL: " + sql);
        if (values != null && values.length > 0)
            return jdbcTemplate.update(sql, values);
        else
            return jdbcTemplate.update(sql);
    }

    // 日期函数
    public String today()
    {
        return date(0);
    }

    public String tomorrow()
    {
        return date(1);
    }

    public String date(int offset)
    {
        long time = System.currentTimeMillis() + (1000L * 60 * 60 * 24 * offset);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date(time));
    }

    public String date(Date date, int offset)
    {
        if (null == date) return null;
        long time = date.getTime() + (1000L * 60 * 60 * 24 * offset);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date(time));
    }

    public String month(Date date, int offset)
    {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1 + offset;
        Calendar cl = Calendar.getInstance();
        cl.set(year, month, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");

        return sdf.format(cl.getTime());
    }
}
