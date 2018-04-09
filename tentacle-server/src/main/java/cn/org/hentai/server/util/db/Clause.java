package cn.org.hentai.server.util.db;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matrixy on 2017-03-06.
 */
public class Clause
{
    private List<Block> conditions = null;

    public Clause()
    {
        this.conditions = new ArrayList<Block>();
    }

    public Clause(String sql, Object value)
    {
        this();
        and(sql, value);
    }

    // clause("id = ?", 0).and("name like ?", name)
    // 需要考虑一下如何支持多级条件分组
    public Clause and(String sql, Object value)
    {
        conditions.add(new Block(sql, value));
        return this;
    }

    public Clause and(String sql)
    {
        return and(sql, null);
    }

    public ArrayList getValues()
    {
        ArrayList values = new ArrayList((int)(conditions.size() * 1.5f));
        for (int i = 0; i < conditions.size(); i++)
        {
            Block block = conditions.get(i);
            if (block.data instanceof Concatenation)
            {
                values.add(((Concatenation)block.data).data);
            }
            else values.add(block.data);
        }
        return values;
    }

    public String toWhereClause()
    {
        return toWhereClause(true);
    }

    public String toWhere()
    {
        StringBuffer sql = new StringBuffer(1024);
        for (int i = 0, l = conditions.size(); i < l; i++)
        {
            String condition = conditions.get(i).format(false);
            if (null == condition) continue;
            sql.append(condition);
            if (i < l - 1) sql.append(" and ");
        }
        return sql.length() == 0 ? null : sql.toString().replaceAll("\\s+and\\s+$", "");
    }

    public String toWhereClause(boolean merged)
    {
        String where = "";
        for (int i = 0, l = conditions.size(); i < l; i++)
        {
            String condition = conditions.get(i).format();
            if (null == condition) continue;
            where += condition;
            if (i < l - 1) where += " and ";
        }
        return "".equals(where) ? null : where.replaceAll("\\s+and\\s+$", "");
    }
}
