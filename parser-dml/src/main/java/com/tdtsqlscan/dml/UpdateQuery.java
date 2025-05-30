package com.tdtsqlscan.dml;

import com.tdtsqlscan.core.SQLAssignment;
import com.tdtsqlscan.core.SQLCondition;
import com.tdtsqlscan.core.SQLQuery;

import java.util.List;

public class UpdateQuery extends SQLQuery {

    private String tableName;
    private List<SQLAssignment> assignments;
    private SQLCondition condition;

    public UpdateQuery(String tableName, List<SQLAssignment> assignments, SQLCondition condition) {
        this.tableName = tableName;
        this.assignments = assignments;
        this.condition = condition;
    }

    public String getTableName() {
        return tableName;
    }

    public List<SQLAssignment> getAssignments() {
        return assignments;
    }

    public SQLCondition getCondition() {
        return condition;
    }

    @Override
    public String getType() {
        return "UPDATE";
    }
}
