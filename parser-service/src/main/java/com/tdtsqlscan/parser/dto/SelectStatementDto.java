package com.tdtsqlscan.parser.dto;

import java.util.ArrayList;
import java.util.List;

public class SelectStatementDto {

    private List<ColumnDto> columns = new ArrayList<>();
    private List<TableDto> tables = new ArrayList<>();
    private List<JoinDto> joins = new ArrayList<>();
    private WhereClauseDto whereClause;
    private List<String> groupByColumns = new ArrayList<>();

    public List<ColumnDto> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnDto> columns) {
        this.columns = columns;
    }

    public void addColumn(ColumnDto column) {
        this.columns.add(column);
    }

    public List<TableDto> getTables() {
        return tables;
    }

    public void setTables(List<TableDto> tables) {
        this.tables = tables;
    }

    public void addTable(TableDto table) {
        this.tables.add(table);
    }

    public List<JoinDto> getJoins() {
        return joins;
    }

    public void setJoins(List<JoinDto> joins) {
        this.joins = joins;
    }

    public void addJoin(JoinDto join) {
        this.joins.add(join);
    }

    public WhereClauseDto getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(WhereClauseDto whereClause) {
        this.whereClause = whereClause;
    }

    public List<String> getGroupByColumns() {
        return groupByColumns;
    }

    public void setGroupByColumns(List<String> groupByColumns) {
        this.groupByColumns = groupByColumns;
    }

    public void addGroupByColumn(String column) {
        this.groupByColumns.add(column);
    }
}
