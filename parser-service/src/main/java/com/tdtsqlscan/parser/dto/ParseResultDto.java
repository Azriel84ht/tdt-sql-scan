package com.tdtsqlscan.parser.dto;

import java.util.List;
import java.util.ArrayList;

public class ParseResultDto {

    private List<StatementDto> statements;

    public ParseResultDto() {
        this.statements = new ArrayList<>();
    }

    // Getters and Setters

    public List<StatementDto> getStatements() {
        return statements;
    }

    public void setStatements(List<StatementDto> statements) {
        this.statements = statements;
    }

    public void addStatement(StatementDto statement) {
        this.statements.add(statement);
    }
}
