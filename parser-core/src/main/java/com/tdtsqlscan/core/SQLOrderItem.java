package com.tdtsqlscan.core;

/**
 * Representa un elemento de la cláusula ORDER BY.
 */
public class SQLOrderItem {

    public enum Direction {
        ASC,
        DESC
    }

    private final String expression;
    private final Direction direction;

    public SQLOrderItem(String expression, Direction direction) {
        this.expression = expression;
        this.direction  = direction;
    }

    public String getExpression() {
        return expression;
    }

    public Direction getDirection() {
        return direction;
    }
}
