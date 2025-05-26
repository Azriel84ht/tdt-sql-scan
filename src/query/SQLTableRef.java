package query;

import java.util.ArrayList;
import java.util.List;

public class SQLTableRef {
    private String schemaName;
    private String tableName;
    private String alias;
    private SQLselect subquery;
    private List<String> columnAliases = new ArrayList<>();

    /**
     * Construye una referencia de tabla o subconsulta a partir de la cadena dada.
     * @param ref Cadena con la tabla (con posible esquema), alias y/o subconsulta.
     */
    public SQLTableRef(String ref) {
        String s = ref.trim();
        // Caso subconsulta derivada (empieza con '(')
        if (s.startsWith("(")) {
            int closeIdx = findMatchingParen(s, 0);
            String subSql = s.substring(1, closeIdx);
            this.subquery = new SQLselect(subSql);

            // Resto tras la subconsulta
            String rest = s.substring(closeIdx + 1).trim();
            // Desechamos el literal "AS " si existe
            if (rest.toUpperCase().startsWith("AS ")) {
                rest = rest.substring(3).trim();
            }
            // Si hay alias de columna (entre paréntesis)
            int parenIdx = rest.indexOf('(');
            if (parenIdx >= 0) {
                // Alias de la subconsulta
                this.alias = rest.substring(0, parenIdx).trim();
                // Aliases de columnas dentro de los paréntesis
                String cols = rest.substring(parenIdx + 1, rest.lastIndexOf(')'));
                for (String col : cols.split(",")) {
                    columnAliases.add(col.trim());
                }
            } else {
                // Solo alias de subconsulta
                String[] parts = rest.split("\\s+", 2);
                this.alias = parts[0];
            }
        } else {
            // Caso tabla física con posible esquema y alias
            String[] parts = s.split("\\s+", 2);
            String namePart = parts[0];
            String rest     = parts.length > 1 ? parts[1].trim() : "";

            // Separar esquema.tabla si existe
            String[] qual = namePart.split("\\.");
            if (qual.length == 2) {
                this.schemaName = qual[0];
                this.tableName  = qual[1];
            } else {
                this.tableName = namePart;
            }

            // Procesar alias si hay
            if (!rest.isEmpty()) {
                // Desechar "AS " si está presente
                if (rest.toUpperCase().startsWith("AS ")) {
                    rest = rest.substring(3).trim();
                }
                String[] aliasParts = rest.split("\\s+", 2);
                this.alias = aliasParts[0];
            }
        }
    }

    /**
     * Encuentra el índice del paréntesis de cierre correspondiente al de posición pos.
     */
    private int findMatchingParen(String s, int pos) {
        int count = 0;
        for (int i = pos; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') count++;
            else if (c == ')') {
                count--;
                if (count == 0) return i;
            }
        }
        throw new SQLParseException("No matching parenthesis in: " + s);
    }

    public boolean isSubquery() {
        return subquery != null;
    }

    public SQLselect getSubquery() {
        return subquery;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getAlias() {
        return alias;
    }

    public List<String> getColumnAliases() {
        return columnAliases;
    }

    @Override
    public String toString() {
        if (subquery != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("(").append(subquery.getSql()).append(")");
            sb.append(" ").append(alias);
            if (!columnAliases.isEmpty()) {
                sb.append(" (");
                for (int i = 0; i < columnAliases.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(columnAliases.get(i));
                }
                sb.append(")");
            }
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            if (schemaName != null) {
                sb.append(schemaName).append(".");
            }
            sb.append(tableName);
            if (alias != null) {
                sb.append(" ").append(alias);
            }
            return sb.toString();
        }
    }
}
