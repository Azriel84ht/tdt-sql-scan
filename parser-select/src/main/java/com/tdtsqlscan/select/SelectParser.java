package com.tdtsqlscan.select;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLQuery;
import com.tdtsqlscan.core.SQLParserUtils;
import com.tdtsqlscan.core.SQLTableRef;
import com.tdtsqlscan.core.SQLJoin;
import com.tdtsqlscan.core.SQLOrderItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser para sentencias SELECT.
 */
public class SelectParser implements QueryParser {

    @Override
    public boolean supports(String sql) {
        return sql.trim().toUpperCase().startsWith("SELECT");
    }

    @Override
    public SQLQuery parse(String sql) throws SQLParseException {
        SelectQuery q = new SelectQuery(sql);

        // Check for a FROM clause to determine parsing strategy
        int fromIndex = SQLParserUtils.findTopLevelKeyword(sql, "FROM", 0);

        String selectList;
        if (fromIndex != -1) {
            // FROM clause exists, parse normally
            selectList = SQLParserUtils.extractBetweenKeywords(sql, "SELECT", "FROM");
        } else {
            // No FROM clause, select list is everything after SELECT
            selectList = SQLParserUtils.extractAfterKeyword(sql, "SELECT", null);
        }

        for (String field : SQLParserUtils.splitTopLevel(selectList, ",")) {
            q.addColumn(field);
        }

        // The rest of the parsing only makes sense if a FROM clause exists
        if (fromIndex != -1) {
            // FROM and JOINs
            fromIndex += "FROM".length();
            int whereIndex = SQLParserUtils.findTopLevelKeyword(sql, "WHERE", fromIndex);
            int groupByIndex = SQLParserUtils.findTopLevelKeyword(sql, "GROUP BY", fromIndex);
            int orderByIndex = SQLParserUtils.findTopLevelKeyword(sql, "ORDER BY", fromIndex);

            int endIndex = sql.length();
            if (whereIndex != -1) endIndex = Math.min(endIndex, whereIndex);
            if (groupByIndex != -1) endIndex = Math.min(endIndex, groupByIndex);
            if (orderByIndex != -1) endIndex = Math.min(endIndex, orderByIndex);

            String fromClause = sql.substring(fromIndex, endIndex).trim();

            if (!fromClause.isEmpty()) {
                List<String> tablesAndJoins = SQLParserUtils.splitTopLevel(fromClause, "JOIN");
                q.addTable(parseTableRef(tablesAndJoins.get(0)));
                for (int i = 1; i < tablesAndJoins.size(); i++) {
                    q.addJoin(parseJoin("JOIN " + tablesAndJoins.get(i)));
                }
            }

            // WHERE
            String whereClause = SQLParserUtils.extractBetweenKeywords(sql, "WHERE", "GROUP BY");
            if (whereClause == null) whereClause = SQLParserUtils.extractBetweenKeywords(sql, "WHERE", "ORDER BY");
            if (whereClause == null) whereClause = SQLParserUtils.extractAfterKeyword(sql, "WHERE", null);
            if (whereClause != null) {
                // Dummy condition parsing
                q.addWhereCondition(new com.tdtsqlscan.core.SQLCondition(whereClause));
            }

            // GROUP BY
            String groupByClause = SQLParserUtils.extractBetweenKeywords(sql, "GROUP BY", "HAVING");
            if (groupByClause == null) groupByClause = SQLParserUtils.extractBetweenKeywords(sql, "GROUP BY", "ORDER BY");
            if (groupByClause == null) groupByClause = SQLParserUtils.extractAfterKeyword(sql, "GROUP BY", null);
            if (groupByClause != null) {
                for (String expr : SQLParserUtils.splitTopLevel(groupByClause, ",")) {
                    q.addGroupBy(expr);
                }
            }

            // ORDER BY
            String orderByClause = SQLParserUtils.extractAfterKeyword(sql, "ORDER BY", null);
            if (orderByClause != null) {
                for (String item : SQLParserUtils.splitTopLevel(orderByClause, ",")) {
                    String trimmedItem = item.trim();
                    SQLOrderItem.Direction dir = SQLOrderItem.Direction.ASC;
                    if (trimmedItem.toUpperCase().endsWith(" DESC")) {
                        dir = SQLOrderItem.Direction.DESC;
                        trimmedItem = trimmedItem.substring(0, trimmedItem.length() - 5).trim();
                    } else if (trimmedItem.toUpperCase().endsWith(" ASC")) {
                        trimmedItem = trimmedItem.substring(0, trimmedItem.length() - 4).trim();
                    }
                    q.addOrderBy(new com.tdtsqlscan.core.SQLOrderItem(trimmedItem, dir));
                }
            }
        }

        return q;
    }

    private SQLTableRef parseTableRef(String expr) {
        String trimmedExpr = expr.trim();
        String[] parts = trimmedExpr.split("\\s+");
        String table;
        String alias = null;

        if (parts.length == 1) {
            table = parts[0];
        } else {
            alias = parts[parts.length - 1];
            String tablePart = trimmedExpr.substring(0, trimmedExpr.lastIndexOf(alias)).trim();
            if (tablePart.toUpperCase().endsWith("AS")) {
                tablePart = tablePart.substring(0, tablePart.length() - 2).trim();
            }
            table = tablePart;
        }

        // Remove surrounding parentheses from the table name
        if (table.startsWith("(") && table.endsWith(")")) {
            table = table.substring(1, table.length() - 1).trim();
        }

        if (table.isEmpty()) {
            return null;
        }

        return new SQLTableRef(table, alias);
    }

    private SQLJoin parseJoin(String clause) {
        String upperClause = clause.toUpperCase();
        SQLJoin.Type joinType = SQLJoin.Type.INNER; // Default
        if (upperClause.startsWith("LEFT")) joinType = SQLJoin.Type.LEFT;
        else if (upperClause.startsWith("RIGHT")) joinType = SQLJoin.Type.RIGHT;
        else if (upperClause.startsWith("FULL")) joinType = SQLJoin.Type.FULL;

        String tableAndCondition = SQLParserUtils.extractAfterKeyword(clause, "JOIN", null);
        String[] parts = tableAndCondition.split("\\s+ON\\s+", 2);
        SQLTableRef tableRef = parseTableRef(parts[0]);
        String condition = parts.length > 1 ? parts[1] : null;

        return new SQLJoin(joinType, null, tableRef, condition);
    }
}
