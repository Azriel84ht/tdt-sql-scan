package com.tdtsqlscan.core;

import java.util.Collections;
import java.util.List;

public class CreateTableQuery extends SQLQuery {
    private final String tableName;
    private final List<ColumnDefinition> columns;
    private final boolean isVolatile;
    private final List<SQLTableRef> sourceTables;
    private final SelectQuery selectQuery;

    public CreateTableQuery(String sql,
                            String tableName,
                            List<ColumnDefinition> columns,
                            boolean isVolatile,
                            List<SQLTableRef> sourceTables,
                            SelectQuery selectQuery) {
        super(sql);
        this.tableName = tableName;
        this.columns = columns;
        this.isVolatile = isVolatile;
        this.sourceTables = sourceTables != null ? sourceTables : Collections.emptyList();
        this.selectQuery = selectQuery;
    }

    @Override
    public Type getType() {
        return Type.CREATE_TABLE;
    }

    public String getTableName() {
        return tableName;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public boolean isVolatile() {
        return isVolatile;
    }

    public List<SQLTableRef> getSourceTables() {
        return sourceTables;
    }

    public SelectQuery getSelectQuery() {
        return selectQuery;
    }
}
