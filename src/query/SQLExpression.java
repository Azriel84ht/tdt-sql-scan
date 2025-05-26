package query;

public class SQLExpression {
    private final String expression;    // La parte principal de la expresión (sin alias)
    private final String alias;         // Alias de la expresión, si existe
    private final SQLselect subquery;   // Subconsulta anidada, si la expresión es un SELECT

    /**
     * Construye un SQLExpression a partir de la cadena cruda.
     * Detecta alias (explícito con AS o implícito), y subconsultas escalar.
     *
     * @param exprStr La expresión SQL completa (por ejemplo "SUM(col) total", "(SELECT x FROM t) sub").
     */
    public SQLExpression(String exprStr) {
        String s = exprStr.trim();
        String exprPart;
        String aliasPart = null;

        // 1) Detectar alias explícito "AS"
        int asPos = SQLParserUtils.findTopLevelKeyword(s, "AS", 0);
        if (asPos >= 0) {
            exprPart  = s.substring(0, asPos).trim();
            aliasPart = s.substring(asPos + 2).trim();
        } else {
            // 2) Detectar alias implícito (último token a nivel top)
            int spacePos = findLastTopLevelSpace(s);
            if (spacePos > 0) {
                String possibleAlias = s.substring(spacePos + 1).trim();
                // comprobamos que es un identificador válido
                if (possibleAlias.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                    exprPart  = s.substring(0, spacePos).trim();
                    aliasPart = possibleAlias;
                } else {
                    exprPart = s;
                }
            } else {
                exprPart = s;
            }
        }

        this.expression = exprPart;
        this.alias      = aliasPart;

        // 3) Detectar subconsulta: si exprPart está entre paréntesis y empieza con SELECT
        SQLselect sq = null;
        if (exprPart.startsWith("(")) {
            int closeIdx = findMatchingParen(exprPart, 0);
            if (closeIdx == exprPart.length() - 1) {
                String inner = exprPart.substring(1, closeIdx);
                // instanciamos recursivamente para parsear la subconsulta
                sq = new SQLselect(inner);
            }
        }
        this.subquery = sq;
    }

    /** 
     * Encuentra el índice del paréntesis que cierra el correspondiente al de posición pos.
     * @throws SQLParseException si no hay paréntesis de cierre.
     */
    private int findMatchingParen(String s, int pos) {
        int count = 0;
        for (int i = pos; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') count++;
            else if (c == ')') {
                count--;
                if (count == 0) return i;
            }
        }
        throw new SQLParseException("No se encontró paréntesis de cierre en: " + s);
    }

    /**
     * Encuentra la última posición de espacio a nivel top (parenCount==0, fuera de literales).
     * @return índice del espacio o -1 si no hay.
     */
    private int findLastTopLevelSpace(String s) {
        int paren = 0;
        boolean inSingle = false, inDouble = false;
        int lastSpace = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'' && !inDouble) inSingle = !inSingle;
            else if (c == '\"' && !inSingle) inDouble = !inDouble;
            else if (!inSingle && !inDouble) {
                if (c == '(') paren++;
                else if (c == ')') paren--;
                else if (c == ' ' && paren == 0) {
                    lastSpace = i;
                }
            }
        }
        return lastSpace;
    }

    /** @return la expresión sin alias ni paréntesis externos. */
    public String getExpression() {
        return expression;
    }

    /** @return el alias si existe, o null. */
    public String getAlias() {
        return alias;
    }

    /** @return true si esta expresión es una subconsulta. */
    public boolean isSubquery() {
        return subquery != null;
    }

    /** @return la subconsulta parseada, o null si no es subconsulta. */
    public SQLselect getSubquery() {
        return subquery;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(expression);
        if (alias != null) {
            sb.append(" AS ").append(alias);
        }
        return sb.toString();
    }
}
