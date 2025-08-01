package com.tdtsqlscan.ddl;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLParserUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser para CREATE TABLE.
 */
public class CreateTableParser implements QueryParser {

    @Override
    public boolean supports(String sql) {
        String upperSql = sql.trim().toUpperCase();
        return upperSql.startsWith("CREATE TABLE") || upperSql.startsWith("CREATE VOLATILE TABLE");
    }

    @Override
    public CreateTableQuery parse(String sql) throws SQLParseException {
        String upperSql = sql.trim().toUpperCase();
        String tableName;
        if (upperSql.startsWith("CREATE VOLATILE TABLE")) {
            tableName = SQLParserUtils.extractBetweenKeywords(sql, "CREATE VOLATILE TABLE", "(").trim();
        } else {
            tableName = SQLParserUtils.extractBetweenKeywords(sql, "CREATE TABLE", "(").trim();
        }

        String colsInside = SQLParserUtils.extractBetweenKeywords(sql, "(", ")");
        List<String> colDefs = SQLParserUtils.splitTopLevel(colsInside, ",");
        List<ColumnDefinition> columns = new ArrayList<>();
        for (String col : colDefs) {
            columns.add(ColumnDefinition.from(col.trim()));
        }

        return new CreateTableQuery(sql, tableName, columns);
    }
}
