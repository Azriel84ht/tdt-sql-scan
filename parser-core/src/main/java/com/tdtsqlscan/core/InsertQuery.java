package com.tdtsqlscan.core;

import java.util.List;

public class InsertQuery extends SQLQuery {

    private final String tableName;
    private final List<String> columns;
    private final List<List<String>> values;
    private final List<SQLTableRef> sourceTables;
    private final SelectQuery selectQuery;

    public InsertQuery(String sql, String tableName, List<String> columns, List<List<String>> values, List<SQLTableRef> sourceTables, SelectQuery selectQuery) {
        super(sql);
        this.tableName = tableName;
        this.columns = columns;
        this.values = values;
        this.sourceTables = sourceTables;
        this.selectQuery = selectQuery;
    }

    @Override
    public Type getType() {
        return Type.INSERT;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<List<String>> getValues() {
        return values;
    }

    public List<SQLTableRef> getSourceTables() {
        return sourceTables;
    }

    public SelectQuery getSelectQuery() {
        return selectQuery;
    }

    public boolean isSelect() {
        return selectQuery != null;
    }
}
