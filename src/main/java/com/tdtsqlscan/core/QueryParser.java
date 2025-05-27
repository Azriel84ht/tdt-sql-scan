package com.tdtsqlscan.core;

/**
 * Parser genérico de sentencias SQL.
 * Implementaciones específicas (SELECT, CREATE, etc.) indicarán
 * si soportan una sentencia dada y cómo parsearla a un AST concreto.
 */
public interface QueryParser {

    /**
     * Indica si este parser puede manejar la sentencia SQL dada.
     *
     * @param sql Texto completo de la consulta (sin el ';' final).
     * @return {@code true} si el parser soporta este tipo de consulta.
     */
    boolean supports(String sql);

    /**
     * Parsea la consulta SQL y devuelve un objeto AST concreto.
     *
     * @param sql Cadena SQL a parsear.
     * @return Un objeto {@link SQLQuery} que representa la consulta.
     * @throws SQLParseException Si ocurre un error de sintaxis o de configuración.
     */
    SQLQuery parse(String sql) throws SQLParseException;
}
