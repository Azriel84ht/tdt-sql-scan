package com.tdtsqlscan.ddl;

/**
 * Definición de una columna en CREATE TABLE.
 */
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

    /**
     * Fabrica una definición de columna a partir de una cadena
     * del estilo "colName DATA_TYPE".
     * @param raw definición cruenta
     * @return ColumnDefinition parseada
     */
    public static ColumnDefinition from(String raw) {
        String[] parts = raw.trim().split("\\s+", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Definición de columna inválida: " + raw);
        }
        return new ColumnDefinition(parts[0], parts[1]);
    }
}
