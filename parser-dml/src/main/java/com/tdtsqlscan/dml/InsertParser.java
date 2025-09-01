package com.tdtsqlscan.dml;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLParserUtils;
import com.tdtsqlscan.core.SQLJoin;
import com.tdtsqlscan.core.SQLTableRef;
import com.tdtsqlscan.select.SelectParser;
import com.tdtsqlscan.select.SelectQuery;
import java.util.ArrayList;
import java.util.List;
import static com.tdtsqlscan.core.SQLParserUtils.splitTopLevel;

/**
 * Parser para sentencias INSERT.
 * Soporta tanto INSERT INTO ... VALUES ... como INSERT INTO ... SELECT ...
 */
public class InsertParser implements QueryParser {

    private final SelectParser selectParser;

    public InsertParser() {
        this.selectParser = new SelectParser();
    }

    @Override
    public boolean supports(String sql) {
        return sql.trim().toUpperCase().startsWith("INSERT INTO");
    }

    @Override
    public InsertQuery parse(String sql) throws SQLParseException {
        String s = sql.trim();
        if (s.endsWith(";")) s = s.substring(0, s.length() - 1);

        int intoPos = s.toUpperCase().indexOf("INTO") + 4;
        String afterInto = s.substring(intoPos).trim();
        String tableName = SQLParserUtils.getFirstWord(afterInto);
        if (tableName == null || tableName.isEmpty()) {
            throw new SQLParseException("Could not find table name in INSERT statement: " + sql);
        }
        tableName = tableName.toUpperCase();

        // Determina si es un INSERT ... SELECT o un INSERT ... VALUES.
        // La heurística simple es buscar la palabra clave SELECT.
        if (afterInto.toUpperCase().contains("SELECT")) {
            // Lógica para INSERT ... SELECT
            List<String> sourceTables = new ArrayList<>();

            // Extrae el texto completo de la subconsulta SELECT.
            // Esto asume que la subconsulta empieza con la palabra clave "SELECT".
            int selectKeywordPosition = afterInto.toUpperCase().indexOf("SELECT");
            String subQueryString = afterInto.substring(selectKeywordPosition);

            // Usa la instancia de selectParser para parsear este texto.
            SelectQuery selectQuery = (SelectQuery) selectParser.parse(subQueryString);

            // Itera sobre selectQuery.getTables() y selectQuery.getJoins() para obtener las tablas de origen.
            for (SQLTableRef tableRef : selectQuery.getTables()) {
                sourceTables.add(tableRef.getExpression());
            }
            for (SQLJoin join : selectQuery.getJoins()) {
                sourceTables.add(join.getRight().getExpression());
            }

            // Llama al nuevo constructor de InsertQuery, pasando la lista de tablas de origen y el objeto SelectQuery.
            return new InsertQuery(sql, tableName, new ArrayList<>(), new ArrayList<>(), sourceTables, selectQuery);
        } else {
            // Lógica existente para INSERT ... VALUES
            List<String> columns = new ArrayList<>();
            List<List<String>> values = new ArrayList<>();

            int parenColsOpen = s.indexOf('(', intoPos);
            String colsSegment = s.substring(parenColsOpen + 1, s.indexOf(')', parenColsOpen));
            columns = splitTopLevel(colsSegment, ",");

            int valuesPos = s.toUpperCase().indexOf("VALUES") + 6;
            String valsPart = s.substring(valuesPos).trim();
            List<String> rawGroups = splitTopLevel(valsPart, "),");
            for (String grp : rawGroups) {
                String g = grp.trim();
                if (g.startsWith("(")) g = g.substring(1);
                if (g.endsWith(")")) g = g.substring(0, g.length() - 1);
                values.add(splitTopLevel(g, ","));
            }

            // Llama al nuevo constructor de InsertQuery con listas/objetos nulos para la parte SELECT.
            return new InsertQuery(sql, tableName, columns, values, new ArrayList<>(), null);
        }
    }
}
