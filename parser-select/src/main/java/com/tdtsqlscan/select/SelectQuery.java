package com.tdtsqlscan.select;

import com.tdtsqlscan.core.SQLQuery;
import com.tdtsqlscan.core.SQLTableRef;
import com.tdtsqlscan.core.SQLJoin;
import com.tdtsqlscan.core.SQLCondition;
import java.util.ArrayList;
import java.util.List;

/**
 * AST específico de una consulta SELECT.
 */
public class SelectQuery extends SQLQuery {

    private final List<String>        columns    = new ArrayList<>();
    private final List<SQLTableRef>   tables     = new ArrayList<>();
    private final List<SQLJoin>       joins      = new ArrayList<>();
    private final List<SQLCondition>  whereConds = new ArrayList<>();

    public SelectQuery(String sqlText) {
        super(sqlText);
    }

    @Override
    public String getType() {
        return "SELECT";
    }

    public void addColumn(String col) {
        columns.add(col.trim());
    }

    public List<String> getColumns() {
        return columns;
    }

    public void addTable(SQLTableRef table) {
        tables.add(table);
    }

    public List<SQLTableRef> getTables() {
        return tables;
    }

    public void addJoin(SQLJoin join) {
        joins.add(join);
    }

    public List<SQLJoin> getJoins() {
        return joins;
    }

    public void addWhereCondition(SQLCondition cond) {
        whereConds.add(cond);
    }

    public List<SQLCondition> getWhereConditions() {
        return whereConds;
    }
}
