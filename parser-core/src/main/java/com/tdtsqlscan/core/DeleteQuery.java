package com.tdtsqlscan.core;

public class DeleteQuery extends SQLQuery {
    private final String table;
    private final SQLCondition condition;

    public DeleteQuery(String sql, String table, SQLCondition condition) {
        super(sql);
        this.table = table;
        this.condition = condition;
    }

    @Override
    public Type getType() {
        return Type.DELETE;
    }

    public String getTable() {
        return table;
    }

    public SQLCondition getCondition() {
        return condition;
    }
}
