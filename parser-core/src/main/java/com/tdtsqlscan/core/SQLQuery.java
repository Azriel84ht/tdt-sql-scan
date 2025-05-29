package com.tdtsqlscan.core;

/**
 * AST genérico de una consulta SQL.
 * Cada tipo concreto de consulta (SELECT, CREATE, etc.)
 * extenderá esta clase e implementará {@link #getType()}.
 */
public abstract class SQLQuery {
    /** Texto original de la consulta (sin ';' final). */
    private final String sqlText;

    /**
     * Constructor base.
     * @param sqlText Consulta SQL completa.
     */
    protected SQLQuery(String sqlText) {
        this.sqlText = sqlText == null ? "" : sqlText.trim();
    }

    /**
     * Obtiene el SQL original, sin espacios extremos ni punto y coma.
     * @return Texto de la consulta.
     */
    public String getSqlText() {
        return sqlText;
    }

    /**
     * Tipo de consulta, p. ej. "SELECT", "CREATE", "DROP".
     * @return Nombre del tipo de consulta.
     */
    public abstract String getType();
}
