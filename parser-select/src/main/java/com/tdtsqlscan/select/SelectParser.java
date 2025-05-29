package com.tdtsqlscan.select;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLParseException;
import com.tdtsqlscan.core.SQLParserUtils;
import com.tdtsqlscan.core.SQLTableRef;
import com.tdtsqlscan.core.SQLJoin;
import com.tdtsqlscan.core.SQLCondition;

import java.util.Locale;

/**
 * Parser de consultas SELECT con JOINs y WHERE.
 */
public class SelectParser implements QueryParser {

    private static final String[] JOIN_KEYWORDS = {
        "INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "FULL JOIN", "CROSS JOIN"
    };

    @Override
    public boolean supports(String sql) {
        return sql != null && sql.trim().toUpperCase(Locale.ROOT).startsWith("SELECT");
    }

    @Override
    public SelectQuery parse(String sql) throws SQLParseException {
        if (sql == null) {
            throw new SQLParseException("SQL no puede ser null");
        }
        // 1) Trim y quitar ‘;’
        String text = sql.trim();
        if (text.endsWith(";")) {
            text = text.substring(0, text.length() - 1).trim();
        }

        // 2) Separar WHERE si existe
        int whereIndex = SQLParserUtils.findTopLevelKeyword(text, "WHERE", 0);
        String beforeWhere = whereIndex >= 0
            ? text.substring(0, whereIndex).trim()
            : text;
        String whereClause = whereIndex >= 0
            ? text.substring(whereIndex + "WHERE".length()).trim()
            : null;

        // 3) Validar y extraer SELECT … FROM …
        if (!beforeWhere.toUpperCase(Locale.ROOT).startsWith("SELECT")) {
            throw new SQLParseException("No comienza con SELECT: " + sql);
        }
        int fromIndex = SQLParserUtils.findTopLevelKeyword(beforeWhere, "FROM", "SELECT".length());
        if (fromIndex < 0) {
            throw new SQLParseException("No se encontró FROM en: " + sql);
        }
        String selectPart = beforeWhere.substring("SELECT".length(), fromIndex).trim();
        String fromPart   = beforeWhere.substring(fromIndex + "FROM".length()).trim();

        // 4) Crear el objeto y parsear columnas
        SelectQuery q = new SelectQuery(text);
        for (String col : SQLParserUtils.splitTopLevel(selectPart, ",")) {
            q.addColumn(col.trim());
        }

        // 5) Parsear tablas y JOINs
        parseTablesAndJoins(fromPart, q);

        // 6) Añadir WHERE
        if (whereClause != null && !whereClause.isEmpty()) {
            q.addWhereCondition(new SQLCondition(whereClause));
        }

        return q;
    }

    private void parseTablesAndJoins(String fromPart, SelectQuery q) {
        // Encontrar la posición del primer JOIN
        int firstJoinPos = findNextJoinKeywordPos(fromPart, 0);

        if (firstJoinPos < 0) {
            // Sólo tablas separadas por coma
            for (String tbl : SQLParserUtils.splitTopLevel(fromPart, ",")) {
                q.addTable(parseTableRef(tbl));
            }
        } else {
            // Primera tabla antes del primer JOIN
            String firstTable = fromPart.substring(0, firstJoinPos).trim();
            SQLTableRef previous = parseTableRef(firstTable);
            q.addTable(previous);

            int idx = firstJoinPos;
            while (idx >= 0) {
                // Detectar tipo y posición del JOIN más cercano
                String matchedKw = null;
                int matchedPos = Integer.MAX_VALUE;
                for (String kw : JOIN_KEYWORDS) {
                    int p = SQLParserUtils.findTopLevelKeyword(fromPart, kw, idx);
                    if (p >= 0 && p < matchedPos) {
                        matchedPos = p;
                        matchedKw = kw;
                    }
                }
                if (matchedKw == null) break;

                // Mapear keyword a enum
                SQLJoin.Type type;
                switch (matchedKw) {
                    case "INNER JOIN": type = SQLJoin.Type.INNER; break;
                    case "LEFT JOIN":  type = SQLJoin.Type.LEFT;  break;
                    case "RIGHT JOIN": type = SQLJoin.Type.RIGHT; break;
                    case "FULL JOIN":  type = SQLJoin.Type.FULL;  break;
                    case "CROSS JOIN": type = SQLJoin.Type.CROSS; break;
                    default:           type = SQLJoin.Type.UNKNOWN;
                }

                // Buscar ON
                int onPos = SQLParserUtils.findTopLevelKeyword(fromPart, "ON", matchedPos);
                if (onPos < 0) {
                    throw new SQLParseException("Falta ON después de JOIN en: " + fromPart);
                }

                // Extraer tabla de la derecha
                String tableSeg = fromPart
                    .substring(matchedPos + matchedKw.length(), onPos)
                    .trim();
                SQLTableRef right = parseTableRef(tableSeg);
                q.addTable(right);

                // Determinar fin de esta cláusula JOIN (próximo JOIN o fin)
                int nextJoin = findNextJoinKeywordPos(fromPart, onPos);
                String cond = nextJoin < 0
                    ? fromPart.substring(onPos + "ON".length()).trim()
                    : fromPart.substring(onPos + "ON".length(), nextJoin).trim();

                // Añadir JOIN
                q.addJoin(new SQLJoin(type, previous, right, cond));

                // Preparar siguiente iteración
                previous = right;
                idx = nextJoin;
            }
        }
    }

    private int findNextJoinKeywordPos(String text, int start) {
        int pos = Integer.MAX_VALUE;
        for (String kw : JOIN_KEYWORDS) {
            int p = SQLParserUtils.findTopLevelKeyword(text, kw, start);
            if (p >= 0 && p < pos) {
                pos = p;
            }
        }
        return pos == Integer.MAX_VALUE ? -1 : pos;
    }

    private SQLTableRef parseTableRef(String expr) {
        String[] parts = expr.trim().split("\\s+");
        if (parts.length == 1) {
            return new SQLTableRef(parts[0], null);
        } else {
            return new SQLTableRef(parts[0], parts[1]);
        }
    }
}
