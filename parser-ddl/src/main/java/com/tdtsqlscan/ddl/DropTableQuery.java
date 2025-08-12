package com.tdtsqlscan.ddl;

import com.tdtsqlscan.core.SQLQuery;

public class DropTableQuery extends SQLQuery {

    private final String tableName;

    public DropTableQuery(String sql, String tableName) {
        super(sql);
        this.tableName = tableName;
    }

    @Override
    public Type getType() {
        return Type.DROP_TABLE;
    }

    public String getTableName() {
        return tableName;
    }
}
