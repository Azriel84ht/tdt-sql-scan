package com.tdtsqlscan.core;

/**
 * Representa una consulta SQL genérica.
 */
public abstract class SQLQuery {

    private final String sql;

    public SQLQuery(String sql) {
        this.sql = sql;
    }

    /**
     * Tipo de la consulta.
     */
    public enum Type {
        SELECT,
        INSERT,
        UPDATE,
        DELETE,
        CREATE_TABLE,
        CREATE_INDEX,
        DROP,
        ALTER,
        // otros tipos que necesites...
    }

    /**
     * @return el tipo concreto de la consulta.
     */
    public abstract Type getType();

    /**
     * @return el SQL original.
     */
    public String getSql() {
        return sql;
    }
}
