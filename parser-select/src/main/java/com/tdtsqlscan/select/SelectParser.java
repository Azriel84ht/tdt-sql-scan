package com.tdtsqlscan.select;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLQuery;
import com.tdtsqlscan.core.SQLParserUtils;
import com.tdtsqlscan.core.SQLTableRef;
import com.tdtsqlscan.core.SQLJoin;

/**
 * Parser para sentencias SELECT.
 */
public class SelectParser implements QueryParser {

    @Override
    public boolean supports(String sql) {
        return SQLParserUtils.regionMatchesIgnoreCase(sql.trim(), 0, "SELECT");
    }

    @Override
    public SQLQuery parse(String sql) throws SQLParseException {
        // 1) Creamos el SelectQuery con el SQL original
        SelectQuery q = new SelectQuery(sql);

        // 2) Campos del SELECT
        String selectList = SQLParserUtils.extractBetween(sql, "SELECT", "FROM");
        q.setFields(SQLParserUtils.splitTopLevel(selectList, ","));

        // 3) Tabla principal
        String fromPart = SQLParserUtils.extractBetween(sql, "FROM", new String[] {"WHERE", "GROUP", "HAVING", "ORDER"});
        q.setRootTable(parseTableRef(fromPart.trim()));

        // 4) Joins (si hay)
        if (SQLParserUtils.containsKeyword(sql, "JOIN")) {
            List<String> joinClauses = SQLParserUtils.splitTopLevel(SQLParserUtils.extractBetween(sql, "FROM", "WHERE"), "JOIN");
            List<SQLJoin> joins = new ArrayList<>();
            // omite el primer elemento (tabla raíz) y procesa los JOINs
            for (int i = 1; i < joinClauses.size(); i++) {
                joins.add(parseJoin("JOIN " + joinClauses.get(i).trim()));
            }
            q.setJoins(joins);
        }

        return q;
    }

    private SQLTableRef parseTableRef(String expr) {
        // tu lógica para convertir “schema.table AS alias” en SQLTableRef
    }

    private SQLJoin parseJoin(String clause) {
        // tu lógica para convertir el JOIN en SQLJoin
    }
}
