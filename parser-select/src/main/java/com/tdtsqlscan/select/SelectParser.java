package com.tdtsqlscan.select;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLQuery;
import com.tdtsqlscan.core.SQLParserUtils;
import com.tdtsqlscan.core.SQLTableRef;
import com.tdtsqlscan.core.SQLJoin;
import com.tdtsqlscan.core.SQLCondition;
import com.tdtsqlscan.core.SQLOrderItem;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser para sentencias SELECT con extracción de metadatos completa.
 */
public class SelectParser implements QueryParser {

    @Override
    public boolean supports(String sql) {
        return sql != null && sql.trim().toUpperCase().startsWith("SELECT");
    }

    @Override
    public SQLQuery parse(String sql) throws SQLParseException {
        String s = sql.trim();
        if (s.endsWith(";")) {
            s = s.substring(0, s.length() - 1).trim();
        }
        SelectQuery q = new SelectQuery(s);

        // 1) COLUMNAS
        String selectList = SQLParserUtils.extractBetweenKeywords(s, "SELECT", "FROM");
        if (selectList != null && !selectList.isEmpty()) {
            for (String col : SQLParserUtils.splitTopLevel(selectList, ",")) {
                q.addColumn(col);
            }
        }

        // 2) FROM Y JOINS
        int fromIdx = SQLParserUtils.findTopLevelKeyword(s, "FROM", 0);
        if (fromIdx >= 0) {
            // determinar fin del bloque FROM
            int fromStart = fromIdx + "FROM".length();
            int fromEnd = SQLParserUtils.findTopLevelKeyword(s, "WHERE", fromStart);
            if (fromEnd < 0) fromEnd = SQLParserUtils.findTopLevelKeyword(s, "GROUP BY", fromStart);
            if (fromEnd < 0) fromEnd = SQLParserUtils.findTopLevelKeyword(s, "HAVING", fromStart);
            if (fromEnd < 0) fromEnd = SQLParserUtils.findTopLevelKeyword(s, "ORDER BY", fromStart);
            if (fromEnd < 0) fromEnd = SQLParserUtils.findTopLevelKeyword(s, "LIMIT", fromStart);
            if (fromEnd < 0) fromEnd = SQLParserUtils.findTopLevelKeyword(s, "OFFSET", fromStart);
            if (fromEnd < 0) fromEnd = s.length();
            String fromSeg = s.substring(fromStart, fromEnd).trim();
            List<String> parts = SQLParserUtils.splitTopLevel(fromSeg, "JOIN");
            // tabla/root
            SQLTableRef root = parseTableRef(parts.get(0));
            q.addTable(root);
            // joins sucesivos
            for (int i = 1; i < parts.size(); i++) {
                String clause = parts.get(i).trim();
                SQLJoin join = parseJoin("JOIN " + clause, root);
                q.addJoin(join);
                root = join.getRight();
            }
        }

        // 3) WHERE
        int whereIdx = SQLParserUtils.findTopLevelKeyword(s, "WHERE", 0);
        if (whereIdx >= 0) {
            int start = whereIdx + "WHERE".length();
            int end = SQLParserUtils.findTopLevelKeyword(s, "GROUP BY", start);
            if (end < 0) end = SQLParserUtils.findTopLevelKeyword(s, "HAVING", start);
            if (end < 0) end = SQLParserUtils.findTopLevelKeyword(s, "ORDER BY", start);
            if (end < 0) end = SQLParserUtils.findTopLevelKeyword(s, "LIMIT", start);
            if (end < 0) end = SQLParserUtils.findTopLevelKeyword(s, "OFFSET", start);
            if (end < 0) end = s.length();
            String whereSeg = s.substring(start, end).trim();
            q.addWhereCondition(new SQLCondition(whereSeg));
        }

        // 4) GROUP BY
        int groupIdx = SQLParserUtils.findTopLevelKeyword(s, "GROUP BY", 0);
        if (groupIdx >= 0) {
            int start = groupIdx + "GROUP BY".length();
            int end = SQLParserUtils.findTopLevelKeyword(s, "HAVING", start);
            if (end < 0) end = SQLParserUtils.findTopLevelKeyword(s, "ORDER BY", start);
            if (end < 0) end = SQLParserUtils.findTopLevelKeyword(s, "LIMIT", start);
            if (end < 0) end = SQLParserUtils.findTopLevelKeyword(s, "OFFSET", start);
            if (end < 0) end = s.length();
            String gbSeg = s.substring(start, end).trim();
            for (String g : SQLParserUtils.splitTopLevel(gbSeg, ",")) {
                q.addGroupBy(g);
            }
        }

        // 5) HAVING
        int havingIdx = SQLParserUtils.findTopLevelKeyword(s, "HAVING", 0);
        if (havingIdx >= 0) {
            int start = havingIdx + "HAVING".length();
            int end = SQLParserUtils.findTopLevelKeyword(s, "ORDER BY", start);
            if (end < 0) end = SQLParserUtils.findTopLevelKeyword(s, "LIMIT", start);
            if (end < 0) end = SQLParserUtils.findTopLevelKeyword(s, "OFFSET", start);
            if (end < 0) end = s.length();
            String hvSeg = s.substring(start, end).trim();
            q.addHavingCondition(new SQLCondition(hvSeg));
        }

        // 6) ORDER BY
        int orderIdx = SQLParserUtils.findTopLevelKeyword(s, "ORDER BY", 0);
        if (orderIdx >= 0) {
            int start = orderIdx + "ORDER BY".length();
            int end = SQLParserUtils.findTopLevelKeyword(s, "LIMIT", start);
            if (end < 0) end = SQLParserUtils.findTopLevelKeyword(s, "OFFSET", start);
            if (end < 0) end = s.length();
            String obSeg = s.substring(start, end).trim();
            for (String it : SQLParserUtils.splitTopLevel(obSeg, ",")) {
                String[] parts = it.trim().split("\\s+");
                SQLOrderItem.Direction dir = parts.length > 1
                    ? SQLOrderItem.Direction.valueOf(parts[1].toUpperCase())
                    : SQLOrderItem.Direction.ASC;
                q.addOrderBy(new SQLOrderItem(parts[0], dir));
            }
        }

        // 7) LIMIT
        int limitIdx = SQLParserUtils.findTopLevelKeyword(s, "LIMIT", 0);
        if (limitIdx >= 0) {
            int start = limitIdx + "LIMIT".length();
            int end = SQLParserUtils.findTopLevelKeyword(s, "OFFSET", start);
            if (end < 0) end = s.length();
            String limSeg = s.substring(start, end).trim();
            q.setLimit(Integer.parseInt(limSeg));
        }

        // 8) OFFSET
        int offsetIdx = SQLParserUtils.findTopLevelKeyword(s, "OFFSET", 0);
        if (offsetIdx >= 0) {
            int start = offsetIdx + "OFFSET".length();
            String offSeg = s.substring(start).trim();
            q.setOffset(Integer.parseInt(offSeg));
        }

        return q;
    }

    /**
     * Parsea expr en SQLTableRef, soportando subconsultas y alias.
     */
    private SQLTableRef parseTableRef(String expr) throws SQLParseException {
        expr = expr.trim();
        if (expr.startsWith("(")) {
            int close = findClosing(expr, 0);
            String sub = expr.substring(1, close);
            String alias = expr.substring(close + 1).trim();
            return new SQLTableRef(sub, alias.isEmpty() ? null : alias);
        } else {
            String[] parts = expr.split("\\s+");
            if (parts.length == 1) {
                return new SQLTableRef(parts[0], null);
            }
            String alias = parts[parts.length - 1];
            String tableName = expr.substring(0, expr.lastIndexOf(alias)).trim();
            return new SQLTableRef(tableName, alias);
        }
    }

    /**
     * Encuentra el paréntesis de cierre correspondiente.
     */
    private int findClosing(String expr, int openPos) throws SQLParseException {
        int depth = 0;
        for (int i = openPos; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') {
                depth--;
                if (depth == 0) return i;
            }
        }
        throw new SQLParseException("No matching ')' for subquery: " + expr);
    }

    /**
     * Parsea cláusula JOIN.
     */
    private SQLJoin parseJoin(String clause, SQLTableRef left) throws SQLParseException {
        Pattern p = Pattern.compile("(?i)(INNER|LEFT|RIGHT|FULL|CROSS)?\\s*JOIN\\s+(.+?)\\s+ON\\s+(.+)");
        Matcher m = p.matcher(clause);
        if (!m.find()) {
            throw new SQLParseException("Invalid JOIN clause: " + clause);
        }
        String typeStr = m.group(1);
        SQLJoin.Type type = (typeStr == null || typeStr.trim().isEmpty())
            ? SQLJoin.Type.INNER
            : SQLJoin.Type.valueOf(typeStr.toUpperCase());
        String tableExpr = m.group(2).trim();
        String cond = m.group(3).trim();
        SQLTableRef right = parseTableRef(tableExpr);
        return new SQLJoin(type, left, right, cond);
    }
}

