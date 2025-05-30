package com.tdtsqlscan.ddl;

import com.tdtsqlscan.core.SQLQuery;
import com.tdtsqlscan.core.SQLQuery.Type;

import java.util.List;

/**
 * Representa una sentencia CREATE [UNIQUE] INDEX.
 */
public class CreateIndexQuery extends SQLQuery {
    private final boolean unique;
    private final String indexName;
    private final String tableName;
    private final List<String> columns;

    public CreateIndexQuery(String sql,
                            boolean unique,
                            String indexName,
                            String tableName,
                            List<String> columns) {
        super(sql);
        this.unique = unique;
        this.indexName = indexName;
        this.tableName = tableName;
        this.columns = columns;
    }

    @Override
    public Type getType() {
        return Type.CREATE_INDEX;
    }

    public boolean isUnique() {
        return unique;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumns() {
        return columns;
    }
}
