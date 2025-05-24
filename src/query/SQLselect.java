package query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SQLselect {

    private boolean distinct;
    private Integer topN;
    private boolean topPercent;
    private List<SQLExpression> columns;
    private List<SQLTableRef> tables;
    private List<SQLJoin> joins;
    private SQLCondition whereCondition;
    private List<SQLExpression> groupBy;
    private SQLCondition havingCondition;
    private SQLCondition qualifyCondition;
    private List<SQLOrderItem> orderBy;

    public SQLselect(String sqlText) throws SQLParseException {
        this.columns = new ArrayList<>();
        this.tables = new ArrayList<>();
        this.joins = new ArrayList<>();
        this.groupBy = new ArrayList<>();
        this.orderBy = new ArrayList<>();
        parse(sqlText.trim());
    }

    private void parse(String sql) throws SQLParseException {
        String text = sql.replaceAll(";+$", "").trim();
        int pos = 0;
        // Detect SELECT
        if (!text.regionMatches(true, 0, "SELECT", 0, 6)) {
            throw new SQLParseException("La consulta no comienza con SELECT");
        }
        pos = 6;
        // Skip spaces
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) pos++;
        // TOP N
        if (text.regionMatches(true, pos, "TOP", 0, 3)) {
            pos += 3;
            skipSpaces(text);
            int start = pos;
            while (pos < text.length() && Character.isDigit(text.charAt(pos))) pos++;
            topN = Integer.valueOf(text.substring(start, pos));
            skipSpaces(text);
            if (text.regionMatches(true, pos, "PERCENT", 0, 7)) {
                topPercent = true;
                pos += 7;
                skipSpaces(text);
            }
        }
        // DISTINCT or ALL
        if (text.regionMatches(true, pos, "DISTINCT", 0, 8)) {
            distinct = true;
            pos += 8; skipSpaces(text);
        } else if (text.regionMatches(true, pos, "ALL", 0, 3)) {
            distinct = false;
            pos += 3; skipSpaces(text);
        }
        // Locate FROM at top-level
        int fromIdx = findTopLevelKeyword(text, "FROM", pos);
        if (fromIdx < 0) throw new SQLParseException("No se encontró FROM");
        String selectList = text.substring(pos, fromIdx).trim();
        // Parse SELECT list
        this.columns = parseSelectList(selectList);
        // FROM clause and rest
        int afterFrom = fromIdx + 4;
        Segment fromSplit = splitTopLevel(text, afterFrom,
                new String[]{"WHERE","GROUP BY","HAVING","QUALIFY","ORDER BY"});
        String fromClause = fromSplit.segment.trim();
        String rest = fromSplit.remaining.trim();
        parseFromClause(fromClause);
        // WHERE
        rest = parseClause(rest, "WHERE", cond -> this.whereCondition = parseCondition(cond));
        // GROUP BY
        rest = parseClause(rest, "GROUP BY", group -> this.groupBy = parseGroupBy(group));
        // HAVING
        rest = parseClause(rest, "HAVING", cond -> this.havingCondition = parseCondition(cond));
        // QUALIFY
        rest = parseClause(rest, "QUALIFY", cond -> this.qualifyCondition = parseCondition(cond));
        // ORDER BY
        if (rest.toUpperCase().startsWith("ORDER BY")) {
            String order = rest.substring(8).trim();
            this.orderBy = parseOrderBy(order);
        }
    }

    private static void skipSpaces(String text) {}
    private static int findTopLevelKeyword(String text, String keyword, int start) { return -1; }
    private static Segment splitTopLevel(String text, int start, String[] separators) { return new Segment("",""); }

    private static List<SQLExpression> parseSelectList(String selectList) {
        List<String> items = splitTopLevelElements(selectList, ',');
        List<SQLExpression> exprs = new ArrayList<>();
        for (String item : items) {
            exprs.add(SQLExpression.parse(item));
        }
        return exprs;
    }

    private void parseFromClause(String fromClause) {
        // TODO: implementar parsing de tablas y joins usando lógica recursiva y splitTopLevel
    }

    private SQLCondition parseCondition(String cond) {
        // TODO: invocar a SQLCondition.parse(cond)
        return new SQLCondition(cond);
    }

    private List<SQLExpression> parseGroupBy(String groupStr) {
        List<String> items = splitTopLevelElements(groupStr, ',');
        List<SQLExpression> list = new ArrayList<>();
        for (String item : items) {
            list.add(SQLExpression.parse(item));
        }
        return list;
    }

    private List<SQLOrderItem> parseOrderBy(String orderStr) {
        List<String> items = splitTopLevelElements(orderStr, ',');
        List<SQLOrderItem> list = new ArrayList<>();
        for (String item : items) {
            list.add(SQLOrderItem.parse(item));
        }
        return list;
    }

    private static List<String> splitTopLevelElements(String text, char sep) {
        if (text.isEmpty()) return Collections.emptyList();
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int paren = 0;
        boolean inSingle = false, inDouble = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ''' && !inDouble) inSingle = !inSingle;
            else if (c == '"' && !inSingle) inDouble = !inDouble;
            else if (!inSingle && !inDouble) {
                if (c == '(') paren++;
                else if (c == ')') paren--;
                else if (c == sep && paren == 0) {
                    parts.add(cur.toString().trim()); cur.setLength(0); continue;
                }
            }
            cur.append(c);
        }
        if (cur.length() > 0) parts.add(cur.toString().trim());
        return parts;
    }

    // Getters omitted por brevedad...

    private static class Segment {
        String segment;
        String remaining;
        Segment(String seg, String rem) { this.segment = seg; this.remaining = rem; }
    }
}

// Clases auxiliares:

class SQLExpression {
    private final String expression;
    private final String alias;

    private SQLExpression(String expr, String alias) {
        this.expression = expr;
        this.alias = alias;
    }

    public static SQLExpression parse(String text) {
        // TODO: detectar alias usando AS o último token nivel top
        String expr = text;
        String alias = null;
        return new SQLExpression(expr.trim(), alias);
    }
}

class SQLTableRef {
    String name;
    String alias;
    SQLselect subquery;
    // Constructor para tabla física
    public SQLTableRef(String name, String alias) { this.name = name; this.alias = alias; }
    // Constructor para subconsulta
    public SQLTableRef(SQLselect subquery, String alias) { this.subquery = subquery; this.alias = alias; }
}

class SQLJoin {
    enum Type { INNER, LEFT, RIGHT, FULL, CROSS, NATURAL }
    Type type;
    SQLTableRef right;
    SQLCondition onCondition;
    public SQLJoin(Type type, SQLTableRef right, SQLCondition on) {
        this.type = type; this.right = right; this.onCondition = on;
    }
}

class SQLCondition {
    private final String raw;
    public SQLCondition(String raw) { this.raw = raw.trim(); }
    // parse más profundo puede añadirse...
}

class SQLOrderItem {
    private final SQLExpression expression;
    private final boolean ascending;
    private final Boolean nullsFirst;

    private SQLOrderItem(SQLExpression expr, boolean asc, Boolean nullsFirst) {
        this.expression = expr; this.ascending = asc; this.nullsFirst = nullsFirst;
    }

    public static SQLOrderItem parse(String text) {
        // TODO: extraer ASC/DESC y NULLS FIRST/LAST
        SQLExpression expr = SQLExpression.parse(text);
        return new SQLOrderItem(expr, true, null);
    }
}

class SQLParseException extends Exception {
    public SQLParseException(String msg) { super(msg); }
}
