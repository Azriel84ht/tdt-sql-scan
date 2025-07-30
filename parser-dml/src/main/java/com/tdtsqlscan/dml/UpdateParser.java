package com.tdtsqlscan.dml;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLAssignment;
import com.tdtsqlscan.core.SQLCondition;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLParserUtils;
import com.tdtsqlscan.core.SQLQuery;

import java.util.List;

public class UpdateParser implements QueryParser {

    @Override
    public boolean supports(String sql) {
        return sql.trim().toUpperCase().startsWith("UPDATE");
    }

    @Override
    public SQLQuery parse(String sql) throws SQLParseException {
        String lower = sql.toLowerCase();
        String table = SQLParserUtils.extractAfterKeyword(lower, "update", "set");
        String assignmentsStr = SQLParserUtils.extractAfterKeyword(lower, "set", "where");
        String conditionStr = SQLParserUtils.extractAfterKeyword(lower, "where", null);

        // List<SQLAssignment> assignments = SQLParserUtils.parseAssignments(assignmentsStr);
        // SQLCondition condition = SQLParserUtils.parseCondition(conditionStr);

        return new UpdateQuery(sql, table, null, null);
    }
}
