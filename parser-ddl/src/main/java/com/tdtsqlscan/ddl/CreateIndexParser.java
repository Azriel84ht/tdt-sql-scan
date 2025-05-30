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
        // ¿tiene UNIQUE?
        boolean unique = SQLParserUtils.containsKeyword(sql, "UNIQUE");
        // nombre del índice
        String indexName = SQLParserUtils
            .extractBetween(sql, "INDEX", "ON")
            .trim();
        // nombre de la tabla
        String tableName = SQLParserUtils
            .extractBetween(sql, "ON", "(")
            .trim();
        // lista de columnas
        String cols = SQLParserUtils
            .extractBetween(sql, "(", ")");
        List<String> columns = SQLParserUtils.parseExpressionList(cols);

        return new CreateIndexQuery(sql, unique, indexName, tableName, columns);
    }
}
