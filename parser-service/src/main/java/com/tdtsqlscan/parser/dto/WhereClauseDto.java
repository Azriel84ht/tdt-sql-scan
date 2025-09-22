package com.tdtsqlscan.parser.dto;

public class WhereClauseDto {
    private String condition;

    public WhereClauseDto(String condition) {
        this.condition = condition;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
