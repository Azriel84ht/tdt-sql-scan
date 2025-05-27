package com.tdtsqlscan.core;

/**
 * Parser genérico de sentencias SQL.
 */
public interface QueryParser {
    /** ¿Soporta este parser la consulta dada? */
    boolean supports(String sql);

    /**
     * Parsea la consulta y devuelve un AST específico.
     * 
     * @throws SQLParseException si hay error de sintaxis
     */
    SQLQuery parse(String sql) throws SQLParseException;
}
