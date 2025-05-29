package com.tdtsqlscan.select;

import com.tdtsqlscan.core.SQLQuery;
import com.tdtsqlscan.core.SQLTableRef;
import com.tdtsqlscan.core.SQLJoin;
import com.tdtsqlscan.core.SQLCondition;
import com.tdtsqlscan.core.SQLOrderItem;

import java.util.ArrayList;
import java.util.List;

/**
 * AST específico de una consulta SELECT.
 */
public class SelectQuery extends SQLQuery {

    private final List<String>        columns        = new ArrayList<>();
    private final List<SQLTableRef>   tables         = new ArrayList<>();
    private final List<SQLJoin>       joins          = new ArrayList<>();
    private final List<SQLCondition>  whereConds     = new ArrayList<>();
    private final List<String>        groupBy        = new ArrayList<>();
    private final List<SQLCondition>  havingConds    = new ArrayList<>();
    private final List<SQLOrderItem>  orderBy        = new ArrayList<>();

    private int limit  = -1;
    private int offset = -1;

    public SelectQuery(String sqlText) {
        super(sqlText);
    }

    @Override
    public String getType() {
        return "SELECT";
    }

    public void addColumn(String col) {
        columns.add(col.trim());
    }

    public List<String> getColumns() {
        return columns;
    }

    public void addTable(SQLTableRef table) {
        tables.add(table);
    }

    public List<SQLTableRef> getTables() {
        return tables;
    }

    public void addJoin(SQLJoin join) {
        joins.add(join);
    }

    public List<SQLJoin> getJoins() {
        return joins;
    }

    public void addWhereCondition(SQLCondition cond) {
        whereConds.add(cond);
    }

    public List<SQLCondition> getWhereConditions() {
        return whereConds;
    }

    public void addGroupBy(String expr) {
        groupBy.add(expr.trim());
    }

    public List<String> getGroupBy() {
        return groupBy;
    }

    public void addHavingCondition(SQLCondition cond) {
        havingConds.add(cond);
    }

    public List<SQLCondition> getHavingConditions() {
        return havingConds;
    }

    public void addOrderBy(SQLOrderItem item) {
        orderBy.add(item);
    }

    public List<SQLOrderItem> getOrderBy() {
        return orderBy;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }
}
