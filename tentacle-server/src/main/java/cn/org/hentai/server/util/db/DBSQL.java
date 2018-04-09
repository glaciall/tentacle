package cn.org.hentai.server.util.db;

import cn.org.hentai.server.util.SpringManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by matrixy on 2016/12/21.
 */
public class DBSQL
{
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    public DBSQL()
    {
        this.jdbcTemplate = (JdbcTemplate) SpringManager.getApplicationContext().getBean("jdbcTemplate");
    }

    protected void setJdbcTemplate(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    protected JdbcTemplate getJdbcTemplate()
    {
        return this.jdbcTemplate;
    }

    public String toSQL()
    {
        return toSQL(true);
    }

    protected String toSQL(boolean merged)
    {
        return null;
    }

    public String toPreparedSQL()
    {
        return null;
    }

    public Integer execute()
    {
        return null;
    }

    /*
    public Object execute(Class<?> cls)
    {
        return null;
    }
    */

    public <E> E query(String sql, Class<?> cls)
    {
        return null;
    }
}
