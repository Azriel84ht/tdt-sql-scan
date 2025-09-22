package com.tdtsqlscan.parser.dto;

public class JoinDto {
    private String type;
    private TableDto table;
    private String onCondition;

    public JoinDto(String type, TableDto table, String onCondition) {
        this.type = type;
        this.table = table;
        this.onCondition = onCondition;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TableDto getTable() {
        return table;
    }

    public void setTable(TableDto table) {
        this.table = table;
    }

    public String getOnCondition() {
        return onCondition;
    }

    public void setOnCondition(String onCondition) {
        this.onCondition = onCondition;
    }
}
