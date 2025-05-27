package com.tdtsqlscan.core;

/**
 * Representa el AST genérico de una consulta SQL.
 * Cada tipo específico de consulta (SELECT, CREATE, etc.) extenderá esta clase
 * e implementará el método {@link #getType()}.
 */
public abstract class SQLQuery {
    /** Texto original de la consulta (sin el ‘;’ final). */
    private final String sqlText;

    /**
     * Constructor base para todas las consultas.
     *
     * @param sqlText Texto completo de la consulta SQL.
     */
    protected SQLQuery(String sqlText) {
        this.sqlText = sqlText == null ? "" : sqlText.trim();
    }

    /**
     * Devuelve el texto original de la consulta, tal como fue pasado al parser,
     * sin espacios iniciales/finales ni el punto y coma final.
     *
     * @return SQL original limpio.
     */
    public String getSqlText() {
        return sqlText;
    }

    /**
     * Tipo de la consulta, p.ej. "SELECT", "CREATE", etc.
     * Debe implementarse en cada subclase concreta.
     *
     * @return Nombre del tipo de consulta.
     */
    public abstract String getType();
}
