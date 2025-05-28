package com.tdtsqlscan.core;

/**
 * Excepción lanzada al fallar el parseo de una consulta SQL.
 */
public class SQLParseException extends RuntimeException {
    public SQLParseException(String message) {
        super(message);
    }

    public SQLParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
