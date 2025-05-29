package com.tdtsqlscan.core;

/**
 * Modela un JOIN entre dos referencias de tabla con condición.
 */
public class SQLJoin {
    public enum Type { INNER, LEFT, RIGHT, FULL, CROSS, UNKNOWN }

    private final Type type;
    private final SQLTableRef left;
    private final SQLTableRef right;
    private final String condition; // la expresión del ON/USING

    public SQLJoin(Type type, SQLTableRef left, SQLTableRef right, String condition) {
        this.type = type;
        this.left = left;
        this.right = right;
        this.condition = condition != null ? condition.trim() : null;
    }

    public Type getType() {
        return type;
    }

    public SQLTableRef getLeft() {
        return left;
    }

    public SQLTableRef getRight() {
        return right;
    }

    public String getCondition() {
        return condition;
    }

    @Override
    public String toString() {
        return type + " JOIN " + right + " ON " + condition;
    }
}
