package com.tdtsqlscan.ddl;

import com.tdtsqlscan.core.SQLQuery;
import com.tdtsqlscan.core.SQLQuery.Type;
import java.util.List;

/**
 * Representa una sentencia CREATE TABLE.
 */
public class CreateTableQuery extends SQLQuery {
    private final String tableName;
    private final List<ColumnDefinition> columns;

    public CreateTableQuery(String sql,
                            String tableName,
                            List<ColumnDefinition> columns) {
        super(sql);
        this.tableName = tableName;
        this.columns = columns;
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
}
