package com.tdtsqlscan.core;

/**
 * AST genérico de una consulta SQL.
 * Cada tipo concreto (SELECT, CREATE, etc.) debe extender esta clase.
 */
public abstract class SQLQuery {
    private final String sqlText;

    protected SQLQuery(String sqlText) {
        this.sqlText = sqlText == null ? "" : sqlText.trim();
    }

    /** @return Texto original de la consulta (sin ‘;’ final) */
    public String getSqlText() {
        return sqlText;
    }

    /** @return Tipo de consulta, p.ej. "SELECT", "CREATE" */
    public abstract String getType();
}
