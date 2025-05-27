package com.tdtsqlscan.core;

/**
 * Excepción lanzada cuando ocurre un error durante el parseo de una consulta
 * SQL.
 */
public class SQLParseException extends RuntimeException {

    /**
     * Construye una excepción de parseo con un mensaje descriptivo.
     *
     * @param message Detalle del error de sintaxis o configuración.
     */
    public SQLParseException(String message) {
        super(message);
    }

    /**
     * Construye una excepción de parseo con un mensaje y la causa original.
     *
     * @param message Detalle del error de sintaxis o configuración.
     * @param cause   Excepción original que provocó este error.
     */
    public SQLParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
