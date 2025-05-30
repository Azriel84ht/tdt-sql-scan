package com.tdtsqlscan.dml;

import com.tdtsqlscan.core.SQLCondition;
import com.tdtsqlscan.core.SQLAssignment;
import com.tdtsqlscan.core.SQLParserUtils;

import java.util.List;

public class UpdateParser {

    public UpdateQuery parse(String sql) {
        String lower = sql.toLowerCase();
        String table = SQLParserUtils.extractAfterKeyword(lower, "update", "set");
        String assignmentsStr = SQLParserUtils.extractAfterKeyword(lower, "set", "where");
        String conditionStr = SQLParserUtils.extractAfterKeyword(lower, "where", null);

        List<SQLAssignment> assignments = SQLParserUtils.parseAssignments(assignmentsStr);
        SQLCondition condition = SQLParserUtils.parseCondition(conditionStr);

        return new UpdateQuery(sql, table, assignments, condition);
    }
}
