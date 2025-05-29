package com.tdtsqlscan.core;

/**
 * Parser genérico de sentencias SQL.
 * Cada implementación (SELECT, CREATE, etc.) indicará si soporta
 * la consulta proporcionada y cómo parsearla.
 */
public interface QueryParser {

    /**
     * @param sql Consulta completa (sin ';').
     * @return true si este parser soporta la sintaxis de la consulta.
     */
    boolean supports(String sql);

    /**
     * Parsea la consulta SQL y devuelve un AST concreto.
     * @param sql Texto de la consulta.
     * @return Objeto SQLQuery que representa el AST.
     * @throws SQLParseException en caso de error de sintaxis.
     */
    SQLQuery parse(String sql) throws SQLParseException;
}
