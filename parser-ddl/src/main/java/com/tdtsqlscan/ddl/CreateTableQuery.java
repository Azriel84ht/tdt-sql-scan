package com.tdtsqlscan.ddl;

import com.tdtsqlscan.core.SQLQuery;
import com.tdtsqlscan.select.SelectQuery;
import java.util.List;
import java.util.Collections;

/**
 * Representa una sentencia CREATE TABLE.
 */
public class CreateTableQuery extends SQLQuery {
    private final String tableName;
    private final List<ColumnDefinition> columns;
    private final boolean isVolatile;
    private final List<String> sourceTables;
    private final SelectQuery selectQuery;

    public CreateTableQuery(String sql,
                            String tableName,
                            List<ColumnDefinition> columns,
                            boolean isVolatile) {
        this(sql, tableName, columns, isVolatile, null, null);
    }

    public CreateTableQuery(String sql,
                            String tableName,
                            List<ColumnDefinition> columns,
                            boolean isVolatile,
                            List<String> sourceTables) {
        this(sql, tableName, columns, isVolatile, sourceTables, null);
    }

    public CreateTableQuery(String sql,
                            String tableName,
                            List<ColumnDefinition> columns,
                            boolean isVolatile,
                            List<String> sourceTables,
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

    public List<String> getSourceTables() {
        return sourceTables;
    }

    public SelectQuery getSelectQuery() {
        return selectQuery;
    }
}
