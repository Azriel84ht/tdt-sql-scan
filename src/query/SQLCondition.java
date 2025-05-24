package query;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class SQLCondition {

    private final String operator;               // "AND", "OR", "NOT", or null for simple
    private final List<SQLCondition> conditions; // sub-conditions if composite
    private final String expression;             // raw expression if simple

    public SQLCondition(String conditionStr) {
        String s = conditionStr.trim();
        // NOT at top level
        if (s.regionMatches(true, 0, "NOT ", 0, 4)) {
            operator   = "NOT";
            conditions = new ArrayList<>();
            conditions.add(new SQLCondition(s.substring(4)));
            expression = null;
        } else {
            // Split by OR at top level
            List<String> orParts = splitTopLevel(s, "OR");
            if (orParts.size() > 1) {
                operator   = "OR";
                conditions = new ArrayList<>();
                for (String part : orParts) {
                    conditions.add(new SQLCondition(part));
                }
                expression = null;
            } else {
                // Split by AND at top level
                List<String> andParts = splitTopLevel(s, "AND");
                if (andParts.size() > 1) {
                    operator   = "AND";
                    conditions = new ArrayList<>();
                    for (String part : andParts) {
                        conditions.add(new SQLCondition(part));
                    }
                    expression = null;
                } else {
                    // Simple condition
                    operator   = null;
                    conditions = null;
                    expression = s;
                }
            }
        }
    }

    /**
     * Divide la cadena en subcadenas por la palabra clave dada,
     * considerando solo apariciones a nivel top-level (parenCount == 0).
     */
    private List<String> splitTopLevel(String s, String keyword) {
        List<String> parts = new ArrayList<>();
        int len    = s.length();
        int kwLen  = keyword.length();
        int pc     = 0;             // contador de paréntesis
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < len; ) {
            char c = s.charAt(i);
            if (c == '(') {
                pc++;
                sb.append(c);
                i++;
            } else if (c == ')') {
                pc--;
                sb.append(c);
                i++;
            } else if (pc == 0
                    && i + kwLen <= len
                    && s.regionMatches(true, i, keyword, 0, kwLen)
                    && isBoundary(s, i, kwLen)) {
                // se encontró el keyword a nivel top
                parts.add(sb.toString().trim());
                sb.setLength(0);
                i += kwLen;
                // saltar espacios tras el keyword
                while (i < len && Character.isWhitespace(s.charAt(i))) {
                    i++;
                }
            } else {
                sb.append(c);
                i++;
            }
        }
        parts.add(sb.toString().trim());
        return parts;
    }

    /** Comprueba que antes y después de la palabra clave haya límite de palabra. */
    private boolean isBoundary(String s, int pos, int kwLen) {
        boolean beforeOK = (pos == 0) || !Character.isLetterOrDigit(s.charAt(pos - 1));
        boolean afterOK  = (pos + kwLen >= s.length())
                          || !Character.isLetterOrDigit(s.charAt(pos + kwLen));
        return beforeOK && afterOK;
    }

    public String getOperator() {
        return operator;
    }

    public List<SQLCondition> getConditions() {
        return conditions;
    }

    /**
     * Devuelve la expresión cruda de la condición si es simple (sin AND/OR/NOT).
     */
    public String getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        if (operator != null) {
            if ("NOT".equals(operator)) {
                return "NOT " + conditions.get(0).toString();
            }
            StringJoiner joiner = new StringJoiner(" " + operator + " ");
            for (SQLCondition cond : conditions) {
                joiner.add(cond.toString());
            }
            return "(" + joiner.toString() + ")";
        } else {
            return expression;
        }
    }
}
