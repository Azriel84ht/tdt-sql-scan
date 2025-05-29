package com.tdtsqlscan.select;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLParserUtils;
import com.tdtsqlscan.core.SQLTableRef;
import com.tdtsqlscan.core.SQLJoin;
import com.tdtsqlscan.core.SQLCondition;

import java.util.Locale;

/**
 * Parser de consultas SELECT con soporte de JOIN, WHERE, GROUP BY y HAVING.
 */
public class SelectParser implements QueryParser {

    private static final String[] JOIN_KEYWORDS = {
        "INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "FULL JOIN", "CROSS JOIN"
    };

    @Override
    public boolean supports(String sql) {
        return sql != null && sql.trim().toUpperCase(Locale.ROOT).startsWith("SELECT");
    }

    @Override
    public SelectQuery parse(String sql) throws SQLParseException {
        if (sql == null) {
            throw new SQLParseException("SQL no puede ser null");
        }
        String text = sql.trim();
        if (text.endsWith(";")) {
            text = text.substring(0, text.length() - 1).trim();
        }

        // WHERE
        int wherePos = SQLParserUtils.findTopLevelKeyword(text, "WHERE", 0);
        String beforeWhere = wherePos >= 0
            ? text.substring(0, wherePos).trim()
            : text;
        String wherePart = wherePos >= 0
            ? text.substring(wherePos + "WHERE".length()).trim()
            : null;

        // GROUP BY
        int groupPos = SQLParserUtils.findTopLevelKeyword(beforeWhere, "GROUP BY", 0);
        String beforeGroup = groupPos >= 0
            ? beforeWhere.substring(0, groupPos).trim()
            : beforeWhere;
        String groupPart = groupPos >= 0
            ? beforeWhere.substring(groupPos + "GROUP BY".length(),
                   SQLParserUtils.findTopLevelKeyword(beforeWhere, "HAVING", groupPos) >= 0
                     ? SQLParserUtils.findTopLevelKeyword(beforeWhere, "HAVING", groupPos)
                     : beforeWhere.length()
              ).trim()
            : null;

        // HAVING
        int havingPos = SQLParserUtils.findTopLevelKeyword(beforeWhere, "HAVING", 0);
        String havingPart = havingPos >= 0
            ? beforeWhere.substring(havingPos + "HAVING".length()).trim()
            : null;

        // SELECT ... FROM ...
        if (!beforeGroup.toUpperCase(Locale.ROOT).startsWith("SELECT")) {
            throw new SQLParseException("No comienza con SELECT: " + sql);
        }
        int fromPos = SQLParserUtils.findTopLevelKeyword(beforeGroup, "FROM", "SELECT".length());
        if (fromPos < 0) {
            throw new SQLParseException("No se encontró FROM en: " + sql);
        }
        String selectPart = beforeGroup.substring("SELECT".length(), fromPos).trim();
        String fromPart   = beforeGroup.substring(fromPos + "FROM".length()).trim();

        SelectQuery q = new SelectQuery(text);

        // Columnas
        for (String col : SQLParserUtils.splitTopLevel(selectPart, ",")) {
            q.addColumn(col);
        }

        // Tablas y JOINs
        parseTablesAndJoins(fromPart, q);

        // WHERE
        if (wherePart != null && !wherePart.isEmpty()) {
            q.addWhereCondition(new SQLCondition(wherePart));
        }

        // GROUP BY
        if (groupPart != null && !groupPart.isEmpty()) {
            for (String grp : SQLParserUtils.splitTopLevel(groupPart, ",")) {
                q.addGroupBy(grp);
            }
        }
        // HAVING
        if (havingPart != null && !havingPart.isEmpty()) {
            q.addHavingCondition(new SQLCondition(havingPart));
        }

        return q;
    }

    private void parseTablesAndJoins(String fromPart, SelectQuery q) {
        int firstJoinPos = findNextJoinKeywordPos(fromPart, 0);
        if (firstJoinPos < 0) {
            // Sólo tablas
            for (String tbl : SQLParserUtils.splitTopLevel(fromPart, ",")) {
                q.addTable(parseTableRef(tbl));
            }
            return;
        }
        // Primera tabla base
        String baseText = fromPart.substring(0, firstJoinPos).trim();
        SQLTableRef previous = parseTableRef(baseText);
        q.addTable(previous);

        int idx = firstJoinPos;
        while (idx >= 0) {
            // Encontrar próximo JOIN
            String matchedKw = null;
            int matchedPos = Integer.MAX_VALUE;
            for (String kw : JOIN_KEYWORDS) {
                int p = SQLParserUtils.findTopLevelKeyword(fromPart, kw, idx);
                if (p >= 0 && p < matchedPos) {
                    matchedPos = p;
                    matchedKw = kw;
                }
            }
            if (matchedKw == null) break;

            // Tipo de JOIN
            SQLJoin.Type type;
            switch (matchedKw) {
                case "INNER JOIN": type = SQLJoin.Type.INNER; break;
                case "LEFT JOIN":  type = SQLJoin.Type.LEFT;  break;
                case "RIGHT JOIN": type = SQLJoin.Type.RIGHT; break;
                case "FULL JOIN":  type = SQLJoin.Type.FULL;  break;
                case "CROSS JOIN": type = SQLJoin.Type.CROSS; break;
                default:           type = SQLJoin.Type.UNKNOWN;
            }

            // ON
            int onPos = SQLParserUtils.findTopLevelKeyword(fromPart, "ON", matchedPos);
            if (onPos < 0) {
                throw new SQLParseException("Falta ON en JOIN: " + fromPart);
            }
            // Tabla derecha
            String tblSeg = fromPart.substring(matchedPos + matchedKw.length(), onPos).trim();
            SQLTableRef right = parseTableRef(tblSeg);
            q.addTable(right);

            // Condición hasta próximo JOIN o fin
            int nextJoin = findNextJoinKeywordPos(fromPart, onPos);
            String cond = nextJoin < 0
                ? fromPart.substring(onPos + "ON".length()).trim()
                : fromPart.substring(onPos + "ON".length(), nextJoin).trim();

            q.addJoin(new SQLJoin(type, previous, right, cond));
            previous = right;
            idx = nextJoin;
        }
    }

    private int findNextJoinKeywordPos(String text, int start) {
        int pos = Integer.MAX_VALUE;
        for (String kw : JOIN_KEYWORDS) {
            int p = SQLParserUtils.findTopLevelKeyword(text, kw, start);
            if (p >= 0 && p < pos) {
                pos = p;
            }
        }
        return pos == Integer.MAX_VALUE ? -1 : pos;
    }

    private SQLTableRef parseTableRef(String expr) {
        String[] parts = expr.trim().split("\\s+");
        if (parts.length == 1) {
            return new SQLTableRef(parts[0], null);
        } else {
            return new SQLTableRef(parts[0], parts[1]);
        }
    }
}
