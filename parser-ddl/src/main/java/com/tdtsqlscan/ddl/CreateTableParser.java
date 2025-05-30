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
        return SQLParserUtils.regionMatchesIgnoreCase(sql.trim(), 0, "CREATE TABLE");
    }

    @Override
    public CreateTableQuery parse(String sql) throws SQLParseException {
        // Extraer nombre de la tabla
        String afterCreate = SQLParserUtils.extractBetween(sql, "CREATE TABLE", "(");
        String tableName = afterCreate.trim();

        // Extraer definición de columnas
        String colsInside = SQLParserUtils.extractBetween(sql, "(", ")");
        List<String> colDefs = SQLParserUtils.splitTopLevel(colsInside, ",");
        List<ColumnDefinition> columns = new ArrayList<>();
        for (String col : colDefs) {
            columns.add(ColumnDefinition.from(col.trim()));
        }

        // Construir la consulta con el constructor (sql, tableName, columns)
        return new CreateTableQuery(sql, tableName, columns);
    }
}
