package com.tdtsqlscan.parser.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Representa una única sentencia o comando extraído de un script BTEQ.")
public class StatementDto {

    @Schema(description = "El tipo de sentencia (ej. DDL, DML, CONTROL, SQL).")
    private StatementType type;
    @Schema(description = "El nombre específico del comando (ej. 'CREATE TABLE', '.IF', 'SELECT').")
    private String commandName;
    @Schema(description = "El texto completo y sin modificar de la sentencia o comando detectado.")
    private String rawContent;
    @Schema(description = "Un mapa de clave-valor con detalles extraídos de la sentencia, como nombres de tabla, condiciones, etc.")
    private Map<String, String> details;

    // Getters and Setters

    public StatementType getType() {
        return type;
    }

    public void setType(StatementType type) {
        this.type = type;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }
}
