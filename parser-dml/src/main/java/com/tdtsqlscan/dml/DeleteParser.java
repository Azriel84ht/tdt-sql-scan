package com.tdtsqlscan.dml;

import com.tdtsqlscan.core.SQLCondition;
import com.tdtsqlscan.core.SQLParserUtils;

public class DeleteParser {

    public DeleteQuery parse(String sql) {
        String lower = sql.toLowerCase();
        String table = SQLParserUtils.extractAfterKeyword(lower, "from", "where");
        String conditionStr = SQLParserUtils.extractAfterKeyword(lower, "where", null);

        SQLCondition condition = SQLParserUtils.parseCondition(conditionStr);

        return new DeleteQuery(sql, table, condition);
    }
}
