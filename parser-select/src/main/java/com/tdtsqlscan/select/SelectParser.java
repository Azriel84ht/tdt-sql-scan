package com.tdtsqlscan.select;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLQuery;

/**
 * Parser de consultas SELECT.
 */
public class SelectParser implements QueryParser {

    @Override
    public boolean supports(String sql) {
        if (sql == null) return false;
        return sql.trim().toUpperCase().startsWith("SELECT");
    }

    @Override
    public SQLQuery parse(String sql) throws SQLParseException {
        // TODO: implementar tokenización y construcción de SelectQuery
        throw new UnsupportedOperationException("Pendiente de implementar parse de SELECT");
    }
}
