package cn.org.hentai.server.util.db;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by matrixy on 2017-03-06.
 */
public class QuerySQL extends DBSQL
{
    // TODO: in (*) 功能的实现

    private ArrayList<String> fields;
    private String tableName;
    private String orderBy;
    private String primaryKey;
    private boolean isAsc = true;
    private Clause clause;
    private ArrayList<Join> joins;

    protected QuerySQL()
    {
        super();
        this.joins = new ArrayList<Join>();
    }

    protected QuerySQL setFields(String... fields)
    {
        if (fields == null) return this;
        this.fields = new ArrayList<String>();
        for (int i = 0, l = fields.length; i < l; i++)
        {
            // if (!fields[i].matches("^(\\w+\\.)?[\\w\\*]+$")) throw new RuntimeException("invalid field name: " + fields[i]);
            this.fields.add(fields[i]);
        }
        return this;
    }

    public QuerySQL setPrimaryKey(String key)
    {
        this.primaryKey = key;
        return this;
    }

    public QuerySQL from(String tableName)
    {
        this.tableName = tableName;
        return this;
    }

    public QuerySQL leftJoin(String tableName, String on)
    {
        this.joins.add(new Join(tableName, on));
        return this;
    }

    public QuerySQL byId(Object id)
    {
        return where(new Clause(primaryKey + " = ?", id));
    }

    public QuerySQL where(Clause clause)
    {
        this.clause = clause;
        return this;
    }

    public QuerySQL orderBy(String field)
    {
        this.orderBy = field;
        return this;
    }

    public QuerySQL orderBy(String field, String order)
    {
        this.orderBy = field;
        this.isAsc = "asc".equalsIgnoreCase(order);
        return this;
    }

    public QuerySQL desc()
    {
        this.isAsc = false;
        return this;
    }

    public QuerySQL asc()
    {
        this.isAsc = true;
        return this;
    }

    private String toWhereClause(boolean merged)
    {
        if (clause == null) return null;
        return clause.toWhereClause();
    }

    private String toWhereClause()
    {
        return toWhereClause(true);
    }

    @Override
    public <E> E query(String sql, Class<?> cls)
    {
        List<E> list = (List<E>)this.getJdbcTemplate().query(sql, new PreparedStatementSetter()
        {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException
            {
                // do nothing here...
            }
        }, new BeanPropertyRowMapper(cls));
        if (list.size() == 0) return null;
        return list.get(0);
    }

    public <E> E query(Class cls)
    {
        return query(toSQL(true), cls);
    }

    public <E> List<E> queryForList(Class cls)
    {
        return (List<E>)this.getJdbcTemplate().query(toSQL(true), (PreparedStatementSetter)null, new BeanPropertyRowMapper(cls));
    }

    public Long queryForCount()
    {
        return this.getJdbcTemplate().queryForObject(toCountSQL(), Long.class);
    }

    public <E> List<E> queryForPaginate(Class cls, int pageIndex, int pageSize)
    {
        // TODO: 参数化查询改造的关键
        /*
        JdbcTemplate jdbcTemplate = this.getJdbcTemplate();
        jdbcTemplate.query(toPageSQL(pageIndex, pageSize), new PreparedStatementSetter(){

            @Override
            public void setValues(PreparedStatement ps) throws SQLException
            {
                ps.setByte();
            }
        }, new BeanPropertyRowMapper(cls));
        */
        return (List<E>)this.getJdbcTemplate().query(toPageSQL(pageIndex, pageSize), (PreparedStatementSetter)null, new BeanPropertyRowMapper(cls));
    }

    public <E> E queryForValue(Class cls)
    {
        E value = null;
        try
        {
            value = (E) jdbcTemplate.queryForObject(toSQL(true), cls);
        }
        catch(EmptyResultDataAccessException e)
        {
            // ..
        }
        return value;
    }

    public <E> List<E> queryForLimit(Class cls, int offset, int count)
    {
        return (List<E>)this.getJdbcTemplate().query(toLimitSQL(offset, count), (PreparedStatementSetter)null, new BeanPropertyRowMapper(cls));
    }

    public <E> List<E> queryForLimit(Class cls, int count)
    {
        return queryForLimit(cls, 0, count);
    }

    // queryForList()
    // queryForCount()
    // queryForPaginate()
    // queryForLimit()

    public String toSQL(boolean merged)
    {
        String whereClause = toWhereClause(merged);
        String fieldSet = "";
        for (int i = 0; this.fields != null && i < this.fields.size(); i++)
        {
            String fieldName = this.fields.get(i);
            fieldSet += fieldName;
            if (fieldName.indexOf(' ') == -1 && fieldName.indexOf('_') > -1) fieldSet += " as " + DbUtil.formatFieldName(fieldName);
            if (i < this.fields.size() - 1) fieldSet += ',';
        }
        StringBuffer sqlJoin = new StringBuffer();
        for (int i = 0; i < joins.size(); i++)
        {
            Join join = joins.get(i);
            sqlJoin.append(" left join " + join.tableName + " on " + join.on);
        }
        String sql = "select " + ("".equals(fieldSet) ? "*" : fieldSet) + " from " + tableName + " " + sqlJoin + " " + (null == whereClause ? "" : " where " + whereClause) + (orderBy == null ? "" : " order by " + orderBy + " " + (isAsc ? "asc" : "desc"));
        // System.err.println("SQL: " + sql);
        return sql;
    }

    public String toCountSQL()
    {
        String whereClause = toWhereClause();
        String fieldSet = "";
        for (int i = 0; this.fields != null && i < this.fields.size(); i++)
        {
            String fieldName = this.fields.get(i);
            fieldSet += fieldName;
            if (fieldName.indexOf(' ') == -1 && fieldName.indexOf('_') > -1) fieldSet += " as " + DbUtil.formatFieldName(fieldName);
            if (i < this.fields.size() - 1) fieldSet += ',';
        }
        StringBuffer sqlJoin = new StringBuffer();
        for (int i = 0; i < joins.size(); i++)
        {
            Join join = joins.get(i);
            sqlJoin.append(" left join " + join.tableName + " on " + join.on);
        }
        String sql = "select count(*) as recordcount from " + tableName + " " + sqlJoin + " " + (null == whereClause ? "" : " where " + whereClause);
        // System.err.println("SQL: " + sql);
        return sql;
    }

    public String toPageSQL(int pageIndex, int pageSize)
    {
        pageIndex = Math.max(1, pageIndex);
        String whereClause = toWhereClause();
        String fieldSet = "";
        for (int i = 0; this.fields != null && i < this.fields.size(); i++)
        {
            String fieldName = this.fields.get(i);
            fieldSet += fieldName;
            if (fieldName.indexOf(' ') == -1 && fieldName.indexOf('_') > -1) fieldSet += " as " + DbUtil.formatFieldName(fieldName);
            if (i < this.fields.size() - 1) fieldSet += ',';
        }
        StringBuffer sqlJoin = new StringBuffer();
        for (int i = 0; i < joins.size(); i++)
        {
            Join join = joins.get(i);
            sqlJoin.append(" left join " + join.tableName + " on " + join.on);
        }
        String sql = "select " + ("".equals(fieldSet) ? "*" : fieldSet) + " from " + tableName + " " + sqlJoin + " " + (null == whereClause ? "" : " where " + whereClause) + (orderBy == null ? "" : " order by " + orderBy + " " + (isAsc ? "asc" : "desc"));
        sql = sql + " limit " + ((pageIndex - 1) * pageSize) + ", " + pageSize;
        // System.err.println("SQL: " + sql);
        return sql;
    }

    public String toLimitSQL(int count)
    {
        return toLimitSQL(0, count);
    }

    public String toLimitSQL(int offset, int count)
    {
        String whereClause = toWhereClause();
        String fieldSet = "";
        for (int i = 0; this.fields != null && i < this.fields.size(); i++)
        {
            String fieldName = this.fields.get(i);
            fieldSet += fieldName;
            if (fieldName.indexOf(' ') == -1 && fieldName.indexOf('_') > -1) fieldSet += " as " + DbUtil.formatFieldName(fieldName);
            if (i < this.fields.size() - 1) fieldSet += ',';
        }
        StringBuffer sqlJoin = new StringBuffer();
        for (int i = 0; i < joins.size(); i++)
        {
            Join join = joins.get(i);
            sqlJoin.append(" left join " + join.tableName + " on " + join.on);
        }
        String sql = "select " + ("".equals(fieldSet) ? "*" : fieldSet) + " from " + tableName + " " + sqlJoin + " " + (null == whereClause ? "" : " where " + whereClause) + (orderBy == null ? "" : " order by " + orderBy + " " + (isAsc ? "asc" : "desc"));
        sql = sql + " limit " + offset + ", " + count;
        // System.err.println("SQL: " + sql);
        return sql;
    }

    public String toString()
    {
        return toSQL();
    }
}
