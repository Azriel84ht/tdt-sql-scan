package com.tdtsqlscan.ddl;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLParserUtils;
import com.tdtsqlscan.core.SQLQuery;

public class DropTableParser implements QueryParser {

    @Override
    public boolean supports(String sql) {
        return sql.trim().toUpperCase().startsWith("DROP TABLE");
    }

    @Override
    public SQLQuery parse(String sql) throws SQLParseException {
        String upperSql = sql.toUpperCase().trim();
        if (upperSql.endsWith(";")) {
            upperSql = upperSql.substring(0, upperSql.length() - 1);
        }
        String tableName = SQLParserUtils.extractTableName(upperSql, "DROP TABLE");
        return new DropTableQuery(sql, tableName);
    }
}
