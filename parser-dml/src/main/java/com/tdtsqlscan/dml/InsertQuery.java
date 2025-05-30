package com.tdtsqlscan.dml;

import com.tdtsqlscan.core.SQLQuery;
import java.util.List;

/**
 * Representa un INSERT INTO ... (cols) VALUES (...), (...).
 */
public class InsertQuery extends SQLQuery {

    private final String tableName;
    private final List<String> columns;
    private final List<List<String>> values;

    public InsertQuery(String sql, String tableName, List<String> columns, List<List<String>> values) {
        super(sql);
        this.tableName = tableName;
        this.columns = columns;
        this.values = values;
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

}
