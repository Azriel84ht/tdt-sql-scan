package com.tdtsqlscan.core;

public class ColumnDefinition {
    private final String name;
    private final String type;

    public ColumnDefinition(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
