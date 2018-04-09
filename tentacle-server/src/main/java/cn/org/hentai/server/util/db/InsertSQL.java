package cn.org.hentai.server.util.db;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by matrixy on 2017-03-06.
 */
public class InsertSQL extends DBSQL
{
    String tableName;
    String primaryKey;
    private ArrayList<Field> fields;
    private Object[] values;
    public InsertSQL()
    {
        super();
        this.fields = new ArrayList<Field>();
    }

    public InsertSQL setTableName(String tableName)
    {
        this.tableName = tableName;
        return this;
    }

    public InsertSQL setPrimaryKey(String pk)
    {
        this.primaryKey = pk;
        return this;
    }

    public InsertSQL valueWith(Object bean) throws RuntimeException
    {
        try
        {
            java.lang.reflect.Field[] clsFields = bean.getClass().getDeclaredFields();
            for (int i = 0; i < clsFields.length; i++)
            {
                java.lang.reflect.Field field = clsFields[i];
                if (Modifier.isStatic(field.getModifiers())) continue;
                Annotation anno = field.getAnnotation(Transient.class);
                if (anno != null) continue;

                String methodName = DbUtil.formatFieldName("get_" + field.getName());
                if (field.getType().isAssignableFrom(Boolean.class)) methodName = DbUtil.formatFieldName(field.getName().replaceAll("^(is)?", "get_"));

                DBField type = (DBField)field.getAnnotation(DBField.class);
                String fieldName = null;
                if (type != null) fieldName = type.name();
                else fieldName = DbUtil.toDBName(field.getName());

                Method method = bean.getClass().getDeclaredMethod(methodName);

                this.fields.add(new Field(fieldName, method.invoke(bean)));
            }
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
        return this;
    }

    public InsertSQL valueWith(String fieldName, Object value)
    {
        if (!fieldName.matches("^\\w+$")) throw new RuntimeException("invalid field name: " + fieldName);
        this.fields.add(new Field(fieldName, value));
        return this;
    }

    public String toSQL(boolean merged)
    {
        StringBuffer sql = new StringBuffer(1024);
        // sql.append("insert into xxx (a,b,c) values (1,2,3)");
        sql.append("insert into " + tableName + " (");
        int fieldCount = 0;
        for (int i = 0, l = fields.size(); i < l; i++)
        {
            Field field = fields.get(i);
            if (field.name.equals(primaryKey)) continue;
            sql.append(field.name);
            if (i < l - 1) sql.append(',');
            fieldCount += 1;
        }
        if (!merged) this.values = new Object[fieldCount];
        sql.append(") values (");
        for (int i = 0, s = 0, l = fields.size(); i < l; i++)
        {
            Field field = fields.get(i);
            if (field.name.equals(this.primaryKey)) continue;
            if (merged) sql.append(DbUtil.valueLiteral(field.value));
            else sql.append('?');
            if (i < l - 1) sql.append(',');
            if (!merged) this.values[s++] = field.value;
        }
        sql.append(")");
        return sql.toString();
    }

    @Override
    public Integer execute()
    {
        return this.getJdbcTemplate().update(toSQL(false), this.values);
    }

    public int save()
    {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int autoIncId = 0;

        jdbcTemplate.update(new PreparedStatementCreator()
        {
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException
            {
                PreparedStatement ps = con.prepareStatement(toSQL(true), PreparedStatement.RETURN_GENERATED_KEYS);
                return ps;
            }
        }, keyHolder);

        autoIncId = keyHolder.getKey().intValue();
        return autoIncId;
    }

    public String toString()
    {
        return toSQL();
    }
}
