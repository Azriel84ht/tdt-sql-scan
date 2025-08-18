package com.tdtsqlscan.ddl;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLParserUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser para CREATE TABLE.
 */
public class CreateTableParser implements QueryParser {

    // Pattern to capture CREATE [MULTISET|SET] [VOLATILE] TABLE ...
    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
            "^CREATE\\s+(?:(?:MULTISET|SET)\\s+)?(VOLATILE\\s+)?TABLE\\s+([^\\s(]+)", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean supports(String sql) {
        return CREATE_TABLE_PATTERN.matcher(sql.trim()).find();
    }

    @Override
    public CreateTableQuery parse(String sql) throws SQLParseException {
        Matcher matcher = CREATE_TABLE_PATTERN.matcher(sql.trim());
        if (!matcher.find()) {
            throw new SQLParseException("Not a valid CREATE TABLE statement: " + sql);
        }

        boolean isVolatile = matcher.group(1) != null;
        String tableName = matcher.group(2);

        String colsInside = SQLParserUtils.extractBetweenKeywords(sql, "(", ")");
        List<String> colDefs = SQLParserUtils.splitTopLevel(colsInside, ",");
        List<ColumnDefinition> columns = new ArrayList<>();
        for (String col : colDefs) {
            columns.add(ColumnDefinition.from(col.trim()));
        }

        return new CreateTableQuery(sql, tableName, columns, isVolatile);
    }
}
