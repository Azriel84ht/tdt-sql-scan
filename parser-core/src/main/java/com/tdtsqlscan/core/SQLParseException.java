package com.tdtsqlscan.core;

/**
 * Excepción lanzada cuando ocurre un error al parsear una consulta SQL.
 */
public class SQLParseException extends RuntimeException {

    /**
     * Crea una excepción con mensaje descriptivo.
     * @param message Detalle del error.
     */
    public SQLParseException(String message) {
        super(message);
    }

    /**
     * Crea una excepción con mensaje y causa original.
     * @param message Detalle del error.
     * @param cause   Excepción que provocó este error.
     */
    public SQLParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
