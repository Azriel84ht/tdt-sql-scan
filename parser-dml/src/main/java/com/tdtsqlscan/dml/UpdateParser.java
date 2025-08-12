package com.tdtsqlscan.dml;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLAssignment;
import com.tdtsqlscan.core.SQLCondition;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLParserUtils;
import com.tdtsqlscan.core.SQLQuery;

import java.util.List;

import java.util.ArrayList;
import java.util.Collections;

public class UpdateParser implements QueryParser {

    @Override
    public boolean supports(String sql) {
        return sql.trim().toUpperCase().startsWith("UPDATE");
    }

    @Override
    public SQLQuery parse(String sql) throws SQLParseException {
        String upperSql = sql.toUpperCase();
        // Use extractBetweenKeywords for more robustness, as the table is between UPDATE and SET.
        String targetTable = SQLParserUtils.extractBetweenKeywords(upperSql, "UPDATE", "SET");
        if (targetTable != null) {
            // The result might include an alias, get the first word.
            targetTable = SQLParserUtils.getFirstWord(targetTable);
        }

        List<String> sourceTables = null;
        if (upperSql.contains(" FROM ")) {
            String fromClause = SQLParserUtils.extractAfterKeyword(upperSql, "FROM", "WHERE");
            if (fromClause != null && !fromClause.isEmpty()) {
                // This is a simplistic implementation. A real one would need to handle joins, aliases etc.
                String sourceTable = SQLParserUtils.getFirstWord(fromClause.trim());
                sourceTables = Collections.singletonList(sourceTable);
            }
        }

        return new UpdateQuery(sql, targetTable, sourceTables);
    }
}
