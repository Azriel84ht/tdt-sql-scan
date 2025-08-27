package com.tdtsqlscan.dml;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLParserUtils;
import com.tdtsqlscan.core.SQLQuery;
import com.tdtsqlscan.core.SQLTableRef;
import com.tdtsqlscan.select.SelectParser;
import com.tdtsqlscan.select.SelectQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateParser implements QueryParser {

    @Override
    public boolean supports(String sql) {
        return sql.trim().toUpperCase().startsWith("UPDATE");
    }

    @Override
    public SQLQuery parse(String sql) throws SQLParseException {
        String upperSql = sql.toUpperCase();
        String targetTable = SQLParserUtils.extractBetweenKeywords(upperSql, "UPDATE", "SET");
        if (targetTable != null) {
            targetTable = SQLParserUtils.getFirstWord(targetTable);
        }

        List<String> sourceTables = new ArrayList<>();

        // Extract tables from FROM clause if it exists
        if (upperSql.contains(" FROM ")) {
            String fromClause = SQLParserUtils.extractAfterKeyword(upperSql, "FROM", "WHERE");
            if (fromClause != null && !fromClause.isEmpty()) {
                // This is a simplistic implementation. A real one would need to handle joins, aliases etc.
                String sourceTable = SQLParserUtils.getFirstWord(fromClause.trim());
                sourceTables.add(sourceTable);
            }
        }

        // Extract tables from subqueries in SET clause
        String setClause = SQLParserUtils.extractBetweenKeywords(sql, "SET", "WHERE");
        if (setClause == null) {
            setClause = SQLParserUtils.extractAfterKeyword(sql, "SET", null);
        }

        if (setClause != null) {
            // Use regex to find all subqueries in the SET clause.
            // This regex looks for patterns like (SELECT ... FROM table ...)
            // It captures the table name, which might be followed by an alias.
            Pattern pattern = Pattern.compile("FROM\\s+([\\w\\.]+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(setClause);
            while (matcher.find()) {
                sourceTables.add(SQLParserUtils.getFirstWord(matcher.group(1)));
            }
        }

        return new UpdateQuery(sql, targetTable, sourceTables);
    }
}
