package com.tdtsqlscan.dml;

import com.tdtsqlscan.core.SQLQuery;
import com.tdtsqlscan.core.SQLCondition;

/**
 * Representa una sentencia DELETE.
 */
public class DeleteQuery extends SQLQuery {

    private final String table;
    private final SQLCondition condition;

    public DeleteQuery(String rawSql, String table, SQLCondition condition) {
        super(rawSql);
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
