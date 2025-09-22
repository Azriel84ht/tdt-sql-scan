package com.tdtsqlscan.parser.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.ArrayList;

@Schema(description = "Representa el resultado completo del análisis de un script, conteniendo una lista de todas las sentencias y comandos identificados.")
public class ParseResultDto {

    @Schema(description = "La lista de sentencias y comandos extraídos del script.")
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
