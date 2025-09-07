package com.tdtsqlscan.core;

import java.util.ArrayList;
import java.util.List;

public class SelectQuery extends SQLQuery {
    private final List<String> columns = new ArrayList<>();
    private final List<SQLTableRef> tables = new ArrayList<>();
    private final List<SQLJoin> joins = new ArrayList<>();
    private final List<SQLCondition> whereConditions = new ArrayList<>();
    private final List<String> groupBy = new ArrayList<>();

    public SelectQuery(String sql) {
        super(sql);
    }

    @Override
    public Type getType() {
        return Type.SELECT;
    }

    public void addColumn(String column) {
        columns.add(column);
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

    public void addWhereCondition(SQLCondition condition) {
        whereConditions.add(condition);
    }

    public List<SQLCondition> getWhereConditions() {
        return whereConditions;
    }

    public void addGroupBy(String column) {
        groupBy.add(column);
    }

    public List<String> getGroupBy() {
        return groupBy;
    }
}
