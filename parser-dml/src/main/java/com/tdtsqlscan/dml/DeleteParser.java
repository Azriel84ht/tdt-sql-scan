package com.tdtsqlscan.dml;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLCondition;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLParserUtils;
import com.tdtsqlscan.core.SQLQuery;

public class DeleteParser implements QueryParser {

    @Override
    public boolean supports(String sql) {
        return sql.trim().toUpperCase().startsWith("DELETE FROM");
    }

    @Override
    public SQLQuery parse(String sql) throws SQLParseException {
        String upper = sql.toUpperCase();
        String table = SQLParserUtils.extractTableName(upper, "DELETE FROM");
        if (table != null) {
            table = table.trim();
        }

        String conditionStr = SQLParserUtils.extractAfterKeyword(upper, "WHERE", null);
        // SQLCondition condition = SQLParserUtils.parseCondition(conditionStr);

        return new DeleteQuery(sql, table, null);
    }
}
