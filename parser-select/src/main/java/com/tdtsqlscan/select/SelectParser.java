package com.tdtsqlscan.select;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLParserUtils;

/**
 * Parser de consultas SELECT.
 */
public class SelectParser implements QueryParser {

    @Override
    public boolean supports(String sql) {
        return sql != null && sql.trim().toUpperCase().startsWith("SELECT");
    }

    @Override
    public SelectQuery parse(String sql) throws SQLParseException {
        String text = sql.trim();
        if (text.endsWith(";")) {
            text = text.substring(0, text.length() - 1);
        }

        int idxFrom = SQLParserUtils.findTopLevelKeyword(text, "FROM", 0);
        if (idxFrom < 0) {
            throw new SQLParseException("No se encontró cláusula FROM en: " + sql);
        }

        String selectPart = text.substring("SELECT".length(), idxFrom);
        String fromPart   = text.substring(idxFrom + "FROM".length());

        SelectQuery q = new SelectQuery(text);

        // Tokenizar columnas a nivel top-level
        for (String col : SQLParserUtils.splitTopLevel(selectPart, ",")) {
            q.addColumn(col);
        }

        // Tokenizar tablas simples separadas por comas
        String[] tbls = fromPart.split("\\s*,\\s*");
        for (String t : tbls) {
            String base = t.trim().split("\\s+")[0];
            q.addTable(base);
        }

        return q;
    }
}
