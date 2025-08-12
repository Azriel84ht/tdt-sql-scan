package com.tdtsqlscan.dml;

import com.tdtsqlscan.core.SQLQuery;
import java.util.List;

public class UpdateQuery extends SQLQuery {

    private final String targetTable;
    private final List<String> sourceTables;

    public UpdateQuery(String sql, String targetTable, List<String> sourceTables) {
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

    public List<String> getSourceTables() {
        return sourceTables;
    }
}
