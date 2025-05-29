package com.tdtsqlscan.core;

/**
 * Representa una condición simple (WHERE, HAVING, QUALIFY).
 */
public class SQLCondition {
    private final String expression;

    public SQLCondition(String expression) {
        this.expression = expression.trim();
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return expression;
    }
}
