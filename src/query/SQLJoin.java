package query;

import java.util.ArrayList;
import java.util.List;

public class SQLJoin {
    private String joinType;
    private SQLTableRef tableRef;
    private SQLCondition condition;
    private List<String> usingColumns;

    public SQLJoin(String joinClause) {
        usingColumns = new ArrayList<>();
        String s = joinClause.trim();

        int idxJoin = SQLParserUtils.findTopLevelKeyword(s, "JOIN", 0);
        if (idxJoin < 0) {
            throw new SQLParseException("JOIN keyword not found: " + joinClause);
        }

        String typePart = s.substring(0, idxJoin).trim();
        joinType = typePart.isEmpty() ? "INNER" : typePart.toUpperCase();

        String rest = s.substring(idxJoin + "JOIN".length()).trim();
        int onPos = SQLParserUtils.findTopLevelKeyword(rest, "ON", 0);
        int usingPos = SQLParserUtils.findTopLevelKeyword(rest, "USING", 0);

        if (onPos >= 0 && (usingPos < 0 || onPos < usingPos)) {
            String tablePart = rest.substring(0, onPos).trim();
            tableRef = new SQLTableRef(tablePart);
            String condPart = rest.substring(onPos + "ON".length()).trim();
            condition = new SQLCondition(condPart);
        } else if (usingPos >= 0) {
            String tablePart = rest.substring(0, usingPos).trim();
            tableRef = new SQLTableRef(tablePart);
            String colsPart = rest.substring(usingPos + "USING".length()).trim();
            if (colsPart.startsWith("(") && colsPart.endsWith(")")) {
                colsPart = colsPart.substring(1, colsPart.length() - 1);
            }
            for (String col : colsPart.split(",")) {
                usingColumns.add(col.trim());
            }
        } else {
            tableRef = new SQLTableRef(rest);
        }
    }

    public String getJoinType() {
        return joinType;
    }

    public SQLTableRef getTableRef() {
        return tableRef;
    }

    public SQLCondition getCondition() {
        return condition;
    }

    public List<String> getUsingColumns() {
        return usingColumns;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(joinType).append(" JOIN ").append(tableRef.toString());
        if (condition != null) {
            sb.append(" ON ").append(condition.toString());
        } else if (!usingColumns.isEmpty()) {
            sb.append(" USING (");
            for (int i = 0; i < usingColumns.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(usingColumns.get(i));
            }
            sb.append(")");
        }
        return sb.toString();
    }
}
