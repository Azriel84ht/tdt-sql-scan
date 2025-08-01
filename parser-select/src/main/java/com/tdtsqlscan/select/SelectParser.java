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

        // Columns
        String selectList = SQLParserUtils.extractBetweenKeywords(sql, "SELECT", "FROM");
        for (String field : SQLParserUtils.splitTopLevel(selectList, ",")) {
            q.addColumn(field);
        }

        // FROM and JOINs
        String fromClause = SQLParserUtils.extractBetweenKeywords(sql, "FROM", "WHERE");
        if (fromClause == null) fromClause = SQLParserUtils.extractBetweenKeywords(sql, "FROM", "GROUP BY");
        if (fromClause == null) fromClause = SQLParserUtils.extractBetweenKeywords(sql, "FROM", "ORDER BY");
        if (fromClause == null) fromClause = SQLParserUtils.extractAfterKeyword(sql, "FROM", null);

        if (fromClause != null) {
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

        return q;
    }

    private SQLTableRef parseTableRef(String expr) {
        String[] parts = expr.trim().split("\\s+");
        if (parts.length == 1) {
            return new SQLTableRef(parts[0], null);
        } else if (parts.length >= 2) {
            String alias = parts[parts.length - 1];
            String table = expr.substring(0, expr.length() - alias.length()).trim();
            if (parts[parts.length - 2].equalsIgnoreCase("AS")) {
                table = table.substring(0, table.length() - 3).trim();
            }
            return new SQLTableRef(table, alias);
        }
        return null;
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
