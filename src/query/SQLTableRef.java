package query;

import java.util.ArrayList;
import java.util.List;

public class SQLTableRef {
    private String schemaName;
    private String tableName;
    private String alias;
    private SQLselect subquery;
    private List<String> columnAliases = new ArrayList<>();

    public SQLTableRef(String ref) {
        String s = ref.trim();
        if (s.startsWith("(")) {
            int closeIdx = findMatchingParen(s, 0);
            String subSql = s.substring(1, closeIdx);
            this.subquery = new SQLselect(subSql);
            String rest = s.substring(closeIdx + 1).trim();
            if (rest.toUpperCase().startsWith("AS ")) {
                rest = rest.substring(3).trim();
            }
            String aliasPart;
            if (rest.contains("(")) {
                int idx = rest.indexOf("(");
                aliasPart = rest.substring(0, idx).trim();
                String cols = rest.substring(idx + 1, rest.lastIndexOf(")"));
                for (String col : cols.split(",")) {
                    columnAliases.add(col.trim());
                }
            } else {
                aliasPart = rest.split("\\s+")[0];
            }
            this.alias = aliasPart;
        } else {
            String[] parts = s.split("\\s+", 2);
            String namePart = parts[0];
            String rest = parts.length > 1 ? parts[1].trim() : "";
            String[] qual = namePart.split("\\.");
            if (qual.length == 2) {
                this.schemaName = qual[0];
                this.tableName = qual[1];
            } else {
                this.tableName = namePart;
            }
            if (!rest.isEmpty()) {
                this.alias = rest.split("\\s+")[0];
            }
        }
    }

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
        return -1;
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
