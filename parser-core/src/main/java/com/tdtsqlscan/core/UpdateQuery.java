package com.tdtsqlscan.core;

import java.util.List;

public class UpdateQuery extends SQLQuery {
    private final String targetTable;
    private final List<SQLTableRef> sourceTables;

    public UpdateQuery(String sql, String targetTable, List<SQLTableRef> sourceTables) {
        super(sql);
        this.targetTable = targetTable;
        this.sourceTables = sourceTables;
    }

    @Override
    public Type getType() {
        return Type.UPDATE;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public List<SQLTableRef> getSourceTables() {
        return sourceTables;
    }
}
