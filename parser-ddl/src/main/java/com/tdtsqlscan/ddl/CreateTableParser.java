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
import com.tdtsqlscan.core.SQLJoin;
import com.tdtsqlscan.core.SQLTableRef;
import com.tdtsqlscan.select.SelectParser;

public class CreateTableParser implements QueryParser {

    // Enhanced pattern to handle CTAS: CREATE ... TABLE ... [AS (SELECT ...)]
    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
            "^CREATE\\s+(?:(?:MULTISET|SET)\\s+)?(VOLATILE\\s+)?TABLE\\s+([^\\s(]+)(?:\\s+AS\\s*\\((.*)\\))?", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private final SelectParser selectParser;

    public CreateTableParser() {
        this.selectParser = new SelectParser();
    }

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
        String asClause = matcher.group(3);

        List<ColumnDefinition> columns = new ArrayList<>();
        List<String> sourceTables = new ArrayList<>();
        com.tdtsqlscan.select.SelectQuery selectQuery = null;

        if (asClause != null) {
            // This is a CTAS statement. We need to parse the sub-select to find source tables.
            selectQuery = (com.tdtsqlscan.select.SelectQuery) selectParser.parse(asClause);
            for (SQLTableRef tableRef : selectQuery.getTables()) {
                sourceTables.add(tableRef.getExpression());
            }
            for (SQLJoin join : selectQuery.getJoins()) {
                sourceTables.add(join.getRight().getExpression());
            }
        } else {
            // This is a standard CREATE TABLE with column definitions.
            String colsInside = SQLParserUtils.extractBetweenKeywords(sql, "(", ")");
            List<String> colDefs = SQLParserUtils.splitTopLevel(colsInside, ",");
            for (String col : colDefs) {
                columns.add(ColumnDefinition.from(col.trim()));
            }
        }

        return new CreateTableQuery(sql, tableName, columns, isVolatile, sourceTables, selectQuery);
    }
}
