package com.tdtsqlscan.ddl;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLParserUtils;
import java.util.List;

/**
 * Parsea sentencias CREATE [UNIQUE] INDEX.
 */
public class CreateIndexParser implements QueryParser {

    @Override
    public boolean supports(String sql) {
        return SQLParserUtils.findTopLevelKeyword(sql, "CREATE", 0) >= 0
            && SQLParserUtils.findTopLevelKeyword(sql, "INDEX", 0) >= 0;
    }

    @Override
    public CreateIndexQuery parse(String sql) throws SQLParseException {
        boolean unique = sql.toUpperCase().contains("UNIQUE");
        String indexName = SQLParserUtils.extractBetweenKeywords(sql, "INDEX", "ON").trim();
        String tableName = SQLParserUtils.extractBetweenKeywords(sql, "ON", "(").trim();
        String cols = SQLParserUtils.extractBetweenKeywords(sql, "(", ")");
        List<String> columns = SQLParserUtils.splitTopLevel(cols, ",");

        return new CreateIndexQuery(sql, unique, indexName, tableName, columns);
    }
}
