package query;

import java.util.ArrayList;
import java.util.List;

/**
 * Analiza una sentencia SELECT de Teradata SQL y expone sus componentes.
 */
public class SQLselect {

    private final String sql;

    private boolean distinct;
    private Integer topN;
    private boolean topPercent;

    private List<SQLExpression> columns          = new ArrayList<>();
    private List<SQLTableRef>  tables           = new ArrayList<>();
    private List<SQLJoin>      joins            = new ArrayList<>();
    private SQLCondition       whereCondition   = null;
    private List<SQLExpression> groupBy         = new ArrayList<>();
    private SQLCondition       havingCondition  = null;
    private SQLCondition       qualifyCondition = null;
    private List<SQLOrderItem> orderBy          = new ArrayList<>();

    /**
     * Construye y parsea la sentencia SELECT.
     * @param sqlText Texto completo de la consulta (puede terminar en ';').
     */
    public SQLselect(String sqlText) {
        this.sql = sqlText.trim();
        parse(this.sql);
    }

    /** Parsea la consulta completa en sus componentes. */
    private void parse(String input) {
        String text = input.trim();
        if (text.endsWith(";")) {
            text = text.substring(0, text.length() - 1).trim();
        }

        // 1) Procesar SELECT [TOP N [%]] [DISTINCT]
        int pos = SQLParserUtils.findTopLevelKeyword(text, "SELECT", 0);
        pos += "SELECT".length();
        pos = SQLParserUtils.skipSpaces(text, pos);

        // TOP N [PERCENT]
        if (SQLParserUtils.regionMatchesIgnoreCase(text, pos, "TOP")) {
            pos += 3;
            pos = SQLParserUtils.skipSpaces(text, pos);
            int start = pos;
            while (pos < text.length() && Character.isDigit(text.charAt(pos))) {
                pos++;
            }
            topN = Integer.parseInt(text.substring(start, pos));
            pos = SQLParserUtils.skipSpaces(text, pos);
            if (SQLParserUtils.regionMatchesIgnoreCase(text, pos, "PERCENT")) {
                topPercent = true;
                pos += "PERCENT".length();
            }
            pos = SQLParserUtils.skipSpaces(text, pos);
        }

        // DISTINCT
        if (SQLParserUtils.regionMatchesIgnoreCase(text, pos, "DISTINCT")) {
            distinct = true;
            pos += "DISTINCT".length();
            pos = SQLParserUtils.skipSpaces(text, pos);
        }

        // 2) Separar SELECT list y resto (FROM...)
        int fromIdx = SQLParserUtils.findTopLevelKeyword(text, "FROM", pos);
        if (fromIdx < 0) {
            throw new SQLParseException("No se encontró cláusula FROM en: " + text);
        }
        String selectListStr = text.substring(pos, fromIdx).trim();
        pos = fromIdx + "FROM".length();

        // 3) Extraer FROM clause y resto de cláusulas
        String[] fromAndRest = SQLParserUtils.splitTopLevel(
            text, pos,
            new String[] {"WHERE", "GROUP BY", "HAVING", "QUALIFY", "ORDER BY"}
        );
        String fromClauseStr = fromAndRest[0].trim();
        String rest          = fromAndRest[1].trim();

        // 4) Parsear SELECT list
        columns = parseSelectList(selectListStr);

        // 5) Parsear FROM + JOINs
        parseFromClause(fromClauseStr);

        // 6) WHERE
        int wIdx = SQLParserUtils.findTopLevelKeyword(rest, "WHERE", 0);
        if (wIdx >= 0) {
            String[] whereAndRest = SQLParserUtils.splitTopLevel(
                rest, wIdx + "WHERE".length(),
                new String[] {"GROUP BY", "HAVING", "QUALIFY", "ORDER BY"}
            );
            whereCondition = new SQLCondition(whereAndRest[0].trim());
            rest = whereAndRest[1].trim();
        }

        // 7) GROUP BY
        int gIdx = SQLParserUtils.findTopLevelKeyword(rest, "GROUP BY", 0);
        if (gIdx >= 0) {
            String[] grpAndRest = SQLParserUtils.splitTopLevel(
                rest, gIdx + "GROUP BY".length(),
                new String[] {"HAVING", "QUALIFY", "ORDER BY"}
            );
            groupBy = parseSelectList(grpAndRest[0].trim());
            rest = grpAndRest[1].trim();
        }

        // 8) HAVING
        int hIdx = SQLParserUtils.findTopLevelKeyword(rest, "HAVING", 0);
        if (hIdx >= 0) {
            String[] havAndRest = SQLParserUtils.splitTopLevel(
                rest, hIdx + "HAVING".length(),
                new String[] {"QUALIFY", "ORDER BY"}
            );
            havingCondition = new SQLCondition(havAndRest[0].trim());
            rest = havAndRest[1].trim();
        }

        // 9) QUALIFY
        int qIdx = SQLParserUtils.findTopLevelKeyword(rest, "QUALIFY", 0);
        if (qIdx >= 0) {
            String[] qualAndRest = SQLParserUtils.splitTopLevel(
                rest, qIdx + "QUALIFY".length(),
                new String[] {"ORDER BY"}
            );
            qualifyCondition = new SQLCondition(qualAndRest[0].trim());
            rest = qualAndRest[1].trim();
        }

        // 10) ORDER BY
        int oIdx = SQLParserUtils.findTopLevelKeyword(rest, "ORDER BY", 0);
        if (oIdx >= 0) {
            String orderStr = rest.substring(oIdx + "ORDER BY".length()).trim();
            List<String> items = SQLParserUtils.splitTopLevel(orderStr, ",");
            for (String item : items) {
                orderBy.add(new SQLOrderItem(item));
            }
        }
    }

    /** Divide la lista SELECT (o GROUP BY) en expresiones individuales. */
    private List<SQLExpression> parseSelectList(String clause) {
        List<SQLExpression> list = new ArrayList<>();
        if (clause.isEmpty()) {
            return list;
        }
        List<String> parts = SQLParserUtils.splitTopLevel(clause, ",");
        for (String part : parts) {
            list.add(new SQLExpression(part));
        }
        return list;
    }

    /** Parsea la cláusula FROM, instanciando tablas y joins. */
    private void parseFromClause(String fromClause) {
        // Primero dividir por top-level commas para tablas independientes
        List<String> tableParts = SQLParserUtils.splitTopLevel(fromClause, ",");
        boolean first = true;
        for (String part : tableParts) {
            part = part.trim();
            if (first) {
                tables.add(new SQLTableRef(part));
                first = false;
            } else {
                // coma entre tablas implícitas => CROSS JOIN
                joins.add(new SQLJoin("CROSS JOIN " + part));
            }
        }
        // Ahora detectar JOIN explícitos dentro de la primera parte
        // (p.ej. si fromClause contiene 'A INNER JOIN B ON ...')
        // Re-parseamos la primera parte generando joins compuestos
        List<String> joinParts = SQLParserUtils.splitTopLevel(
            fromClause,
            new String[] {"JOIN", "LEFT JOIN", "RIGHT JOIN", "FULL JOIN", "INNER JOIN", "LEFT OUTER JOIN", "RIGHT OUTER JOIN", "FULL OUTER JOIN"}
        );
        if (joinParts.size() > 1) {
            // primer elemento antes de JOIN es la tabla base
            tables.clear();
            tables.add(new SQLTableRef(joinParts.get(0).trim()));
            // el resto son cláusulas JOIN completas
            for (int i = 1; i < joinParts.size(); i++) {
                joins.add(new SQLJoin(joinParts.get(i).trim()));
            }
        }
    }

    // ------------------------------------------------------------
    //                       GETTERS PÚBLICOS
    // ------------------------------------------------------------

    public boolean isDistinct() {
        return distinct;
    }

    public Integer getTopN() {
        return topN;
    }

    public boolean isTopPercent() {
        return topPercent;
    }

    public List<SQLExpression> getColumns() {
        return columns;
    }

    public List<SQLTableRef> getTables() {
        return tables;
    }

    public List<SQLJoin> getJoins() {
        return joins;
    }

    public SQLCondition getWhereCondition() {
        return whereCondition;
    }

    public List<SQLExpression> getGroupBy() {
        return groupBy;
    }

    public SQLCondition getHavingCondition() {
        return havingCondition;
    }

    public SQLCondition getQualifyCondition() {
        return qualifyCondition;
    }

    public List<SQLOrderItem> getOrderBy() {
        return orderBy;
    }
    /**
 * @return El texto completo de la consulta SQL original (sin el ';' final).
 */
public String getSql() {
    return sql;
}

}
