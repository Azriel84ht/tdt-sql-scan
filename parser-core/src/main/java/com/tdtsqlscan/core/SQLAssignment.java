package com.tdtsqlscan.core;

/**
 * Representa una asignación columna = expresión en un UPDATE.
 */
public class SQLAssignment {
    private final String column;
    private final String expression;

    public SQLAssignment(String column, String expression) {
        this.column = column;
        this.expression = expression;
    }

    public String getColumn() {
        return column;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return column + " = " + expression;
    }
}
