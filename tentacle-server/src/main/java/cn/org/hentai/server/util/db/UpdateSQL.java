package cn.org.hentai.server.util.db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by matrixy on 2017-03-06.
 */
public class UpdateSQL extends DBSQL
{
    String tableName = null;
    String primaryKey = null;
    ArrayList<Field> fields = null;
    HashMap<String, String> skipFields = null;
    HashMap<String, String> storeFields = null;
    Clause clause = null;
    Object bean = null;
    ArrayList values;

    protected UpdateSQL()
    {
        super();
        this.fields = new ArrayList<Field>();
        this.skipFields = new HashMap<String, String>();
        this.storeFields = new HashMap<String, String>();
    }

    public UpdateSQL valueWith(Object bean)
    {
        return with(bean);
    }

    public UpdateSQL valueWith(String fieldName, Object value)
    {
        if (!fieldName.matches("^\\w+$")) throw new RuntimeException("invalid field name: " + fieldName);
        this.fields.add(new Field(fieldName, value));
        return this;
    }

    private UpdateSQL with(String name, Object value)
    {
        return valueWith(name, value);
    }

    public UpdateSQL with(Object bean)
    {
        this.bean = bean;
        try
        {
            java.lang.reflect.Field[] clsFields = bean.getClass().getDeclaredFields();
            for (int i = 0; i < clsFields.length; i++)
            {
                java.lang.reflect.Field field = clsFields[i];
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (field.getName().equals(this.primaryKey)) continue;
                Annotation anno = field.getAnnotation(Transient.class);
                if (anno != null) continue;
                String methodName = DbUtil.formatFieldName("get_" + field.getName());
                if (field.getType().isAssignableFrom(Boolean.class)) methodName = DbUtil.formatFieldName(field.getName().replaceAll("^(is)?", "get_"));
                Method method = bean.getClass().getDeclaredMethod(methodName);

                String fieldName = null;
                DBField type = field.getAnnotation(DBField.class);
                if (type != null) fieldName = type.name();
                else fieldName = DbUtil.toDBName(field.getName());

                this.fields.add(new Field(fieldName, method.invoke(bean)));
            }
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
        return this;
    }

    public UpdateSQL skip(String...fields)
    {
        for (int i = 0; i < fields.length; i++) this.skipFields.put(fields[i], null);
        return this;
    }

    public UpdateSQL only(String...fields)
    {
        for (int i = 0; i < fields.length; i++) this.storeFields.put(fields[i], null);
        return this;
    }

    public UpdateSQL byId()
    {
        this.clause = new Clause().and(this.primaryKey + " = ?", getPKValue());
        return this;
    }

    public UpdateSQL by(Clause clause)
    {
        this.clause = clause;
        return this;
    }

    protected UpdateSQL setTableName(String tableName)
    {
        this.tableName = tableName;
        return this;
    }

    protected UpdateSQL setPrimaryKey(String pk)
    {
        this.primaryKey = pk;
        return this;
    }

    private Object getPKValue()
    {
        if (this.bean == null) throw new RuntimeException("You should call valueWith(Object bean) before method by(...)");
        try
        {
            // System.out.println(formatFieldName("get_" + this.primaryKey));
            Method method = this.bean.getClass().getDeclaredMethod(DbUtil.formatFieldName("get_" + this.primaryKey));
            return method.invoke(this.bean);
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public String toSQL(boolean merged)
    {
        StringBuffer sql = new StringBuffer(1024);
        // update xxx set a = 1, b = 2, c = 3 where id = ?
        sql.append("update ");
        sql.append(this.tableName);
        sql.append(" set ");
        int fieldCount = 0;
        if (!merged) this.values = new ArrayList();
        for (int i = 0, l = fields.size(); i < l; i++)
        {
            Field field = fields.get(i);
            if (this.skipFields.containsKey(field.name)) continue;
            if (this.storeFields.size() > 0 && !this.storeFields.containsKey(field.name)) continue;
            sql.append(field.name);
            sql.append(" = ");
            fieldCount += 1;
            if (merged) sql.append(DbUtil.valueLiteral(field.value));
            else
            {
                sql.append('?');
                this.values.add(field.value);
            }
            if (i < l - 1) sql.append(',');
        }
        if (null == clause) throw new RuntimeException("missing where clause");
        String where = merged ? clause.toWhereClause() : clause.toWhere();
        if (where != null)
        {
            sql.append(" where ");
            sql.append(where);
            if (!merged) this.values.addAll(clause.getValues());
        }
        else throw new RuntimeException("missing where clause: " + sql);
        return sql.toString().replaceAll(",\\s+where", " where");
    }

    public Integer execute()
    {
        String sql = toSQL(false);
        // System.err.println("SQL: " + sql);
        if (this.values != null && this.values.size() > 0)
            return this.getJdbcTemplate().update(sql, this.values.toArray());
        else
            return this.getJdbcTemplate().update(sql);
    }

    public String toString()
    {
        return toSQL();
    }
}
