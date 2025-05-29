package com.tdtsqlscan.core;

/**
 * Representa una referencia a una tabla (o subconsulta) con posible alias.
 */
public class SQLTableRef {
    private final String expression; // p.ej. "customers AS c" o "(SELECT...) sub"
    private final String alias;      // p.ej. "c" o "sub"

    public SQLTableRef(String expression, String alias) {
        this.expression = expression.trim();
        this.alias = alias != null ? alias.trim() : null;
    }

    public String getExpression() {
        return expression;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return alias != null ? expression + " AS " + alias : expression;
    }
}
