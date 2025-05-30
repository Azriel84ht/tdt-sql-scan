package com.tdtsqlscan.dml;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLParserUtils;
import java.util.ArrayList;
import java.util.List;
import static com.tdtsqlscan.core.SQLParserUtils.splitTopLevel;

/**
 * Parser de INSERT INTO ... (cols) VALUES (...), (...);
 */
public class InsertParser implements QueryParser {

    @Override
    public boolean supports(String sql) {
        return sql.trim().toUpperCase().startsWith("INSERT INTO");
    }

    @Override
    public InsertQuery parse(String sql) throws SQLParseException {
        String s = sql.trim();
        if (s.endsWith(";")) s = s.substring(0, s.length()-1);

        // Tabla y lista de columnas
        int intoPos = s.toUpperCase().indexOf("INTO") + 4;
        int parenColsOpen = s.indexOf('(', intoPos);
        String tableName = s.substring(intoPos, parenColsOpen).trim();

        String colsSegment = s.substring(parenColsOpen + 1, s.indexOf(')', parenColsOpen));
        List<String> columns = splitTopLevel(colsSegment, ",");

        // Valores
        int valuesPos = s.toUpperCase().indexOf("VALUES") + 6;
        String valsPart = s.substring(valuesPos).trim();
        // agrupamos grupos "(...)" separados por top-level "),"
        List<String> rawGroups = splitTopLevel(valsPart, "),");
        List<List<String>> values = new ArrayList<>();
        for (String grp : rawGroups) {
            String g = grp.trim();
            if (g.startsWith("(")) g = g.substring(1);
            if (g.endsWith(")"))   g = g.substring(0, g.length()-1);
            values.add(splitTopLevel(g, ","));
        }

        return new InsertQuery(sql, tableName, columns, values);
    }
}
