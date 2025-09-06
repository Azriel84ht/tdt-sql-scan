package com.tdtsqlscan.core;

import java.util.List;

public class CreateIndexQuery extends SQLQuery {
    private final boolean isUnique;
    private final String indexName;
    private final String tableName;
    private final List<String> columns;

    public CreateIndexQuery(String sql, boolean isUnique, String indexName, String tableName, List<String> columns) {
        super(sql);
        this.isUnique = isUnique;
        this.indexName = indexName;
        this.tableName = tableName;
        this.columns = columns;
    }

    @Override
    public Type getType() {
        return Type.CREATE_INDEX;
    }

    public boolean isUnique() {
        return isUnique;
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
