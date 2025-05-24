package query;

public class SQLExpression {
    private final String expression;
    private final String alias;
    private final SQLselect subquery;

    public SQLExpression(String expression, String alias, SQLselect subquery) {
        this.expression = expression;
        this.alias = alias;
        this.subquery = subquery;
    }

    public static SQLExpression parse(String text) throws SQLParseException {
        String t = text.trim();
        // Detect alias via AS at top level
        int asIdx = SQLParserUtils.findTopLevelKeyword(t, "AS", 0);
        String exprPart;
        String alias = null;
        if (asIdx >= 0) {
            exprPart = t.substring(0, asIdx).trim();
            alias = t.substring(asIdx + 2).trim();
        } else {
            // Detect alias without AS: last top-level space
            int paren = 0;
            boolean inSingle = false, inDouble = false;
            int lastSpace = -1;
            for (int i = 0; i < t.length(); i++) {
                char c = t.charAt(i);
                if (c == '\'' && !inDouble) {
                    inSingle = !inSingle;
                } else if (c == '"' && !inSingle) {
                    inDouble = !inDouble;
                } else if (!inSingle && !inDouble) {
                    if (c == '(') paren++;
                    else if (c == ')') paren--;
                    else if (Character.isWhitespace(c) && paren == 0) {
                        lastSpace = i;
                    }
                }
            }
            if (lastSpace > 0) {
                String possibleAlias = t.substring(lastSpace + 1).trim();
                if (possibleAlias.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                    exprPart = t.substring(0, lastSpace).trim();
                    alias = possibleAlias;
                } else {
                    exprPart = t;
                }
            } else {
                exprPart = t;
            }
        }
        // Detect subquery
        SQLselect subq = null;
        if (exprPart.startsWith("(")) {
            int closing = findMatchingParen(exprPart, 0);
            if (closing == exprPart.length() - 1) {
                String inner = exprPart.substring(1, closing).trim();
                if (inner.regionMatches(true, 0, "SELECT", 0, 6)) {
                    subq = new SQLselect(inner);
                }
            }
        }
        if (subq != null) {
            return new SQLExpression(null, alias, subq);
        }
        return new SQLExpression(exprPart, alias, null);
    }

    private static int findMatchingParen(String s, int openIdx) throws SQLParseException {
        int depth = 0;
        boolean inSingle = false, inDouble = false;
        for (int i = openIdx; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'' && !inDouble) inSingle = !inSingle;
            else if (c == '"' && !inSingle) inDouble = !inDouble;
            else if (!inSingle && !inDouble) {
                if (c == '(') depth++;
                else if (c == ')') {
                    depth--;
                    if (depth == 0) return i;
                }
            }
        }
        throw new SQLParseException("Paréntesis no balanceados en expresión: " + s);
    }

    public boolean isSubquery() {
        return subquery != null;
    }

    public String getExpression() {
        return expression;
    }

    public String getAlias() {
        return alias;
    }

    public SQLselect getSubquery() {
        return subquery;
    }

    @Override
    public String toString() {
        if (subquery != null) {
            return "(" + subquery.toString() + ")" + (alias != null ? " AS " + alias : "");
        }
        return expression + (alias != null ? " AS " + alias : "");
    }
}
