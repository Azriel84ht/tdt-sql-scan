package com.tdtsqlscan.select;

import com.tdtsqlscan.core.SQLQuery;
import com.tdtsqlscan.core.SQLTableRef;
import com.tdtsqlscan.core.SQLJoin;
import com.tdtsqlscan.core.SQLCondition;
import com.tdtsqlscan.core.SQLOrderItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Representación AST de una consulta SELECT.
 */
public class SelectQuery extends SQLQuery {

    private final List<String> columns = new ArrayList<>();
    private final List<SQLTableRef> tables = new ArrayList<>();
    private final List<SQLJoin> joins = new ArrayList<>();
    private final List<SQLCondition> whereConditions = new ArrayList<>();
    private final List<String> groupBy = new ArrayList<>();
    private final List<SQLCondition> havingConditions = new ArrayList<>();
    private final List<SQLOrderItem> orderBy = new ArrayList<>();
    private Integer limit;
    private Integer offset;

    public SelectQuery(String sql) {
        super(sql);
    }

    @Override
    public Type getType() {
        return Type.SELECT;
    }

    public void addColumn(String col) { columns.add(col.trim()); }
    public List<String> getColumns() { return columns; }

    public void addTable(SQLTableRef tbl) { tables.add(tbl); }
    public List<SQLTableRef> getTables() { return tables; }

    public void addJoin(SQLJoin j) { joins.add(j); }
    public List<SQLJoin> getJoins() { return joins; }

    public void addWhereCondition(SQLCondition c) { whereConditions.add(c); }
    public List<SQLCondition> getWhereConditions() { return whereConditions; }

    public void addGroupBy(String expr) { groupBy.add(expr.trim()); }
    public List<String> getGroupBy() { return groupBy; }

    public void addHavingCondition(SQLCondition c) { havingConditions.add(c); }
    public List<SQLCondition> getHavingConditions() { return havingConditions; }

    public void addOrderBy(SQLOrderItem item) { orderBy.add(item); }
    public List<SQLOrderItem> getOrderBy() { return orderBy; }

    public void setLimit(int lim) { this.limit = lim; }
    public Integer getLimit() { return limit; }

    public void setOffset(int off) { this.offset = off; }
    public Integer getOffset() { return offset; }
}
