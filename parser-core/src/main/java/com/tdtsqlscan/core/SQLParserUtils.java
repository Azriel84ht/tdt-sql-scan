package com.tdtsqlscan.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilidades de análisis léxico para SQL:
 * - splitTopLevel: separa según un delimitador sin descender en paréntesis.
 * - findTopLevelKeyword: busca palabras clave a nivel top-level.
 * - otras ayudas para parsear cláusulas de SELECT.
 */
public class SQLParserUtils {

    /**
     * Divide la cadena {@code s} usando {@code delimiter},
     * pero solo en nivel 0 de anidamiento de paréntesis.
     */
    public static List<String> splitTopLevel(String s, String delimiter) {
        List<String> parts = new ArrayList<>();
        if (s == null || delimiter == null || delimiter.isEmpty()) {
            return parts;
        }
        StringBuilder sb = new StringBuilder();
        int depth = 0;
        int len = s.length();
        int dlen = delimiter.length();

        for (int i = 0; i < len; ) {
            char c = s.charAt(i);
            if (c == '(') {
                depth++;
                sb.append(c);
                i++;
            } else if (c == ')') {
                depth = Math.max(depth - 1, 0);
                sb.append(c);
                i++;
            } else if (depth == 0
                    && i + dlen <= len
                    && regionMatchesIgnoreCase(s, i, delimiter)
                    && (delimiter.equals(",") || isDelimiterBoundary(s, i, dlen))) {
                // top-level delimiter encontrado
                parts.add(sb.toString());
                sb.setLength(0);
                i += dlen;
            } else {
                sb.append(c);
                i++;
            }
        }
        // añadir el último fragmento
        parts.add(sb.toString());
        return parts;
    }

    /**
     * Busca la primera aparición de {@code keyword} a nivel top (depth = 0),
     * ignorando mayúsc./minúsc. y asegurando que la keyword esté delimitada.
     */
    public static int findTopLevelKeyword(String s, String keyword, int fromIndex) {
        if (s == null || keyword == null) {
            return -1;
        }
        int depth = 0;
        int len = s.length();
        int keyLen = keyword.length();

        for (int i = fromIndex; i <= len - keyLen; i++) {
            char c = s.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth = Math.max(depth - 1, 0);
            }
            if (depth == 0
                    && regionMatchesIgnoreCase(s, i, keyword)
                    && isBoundaryBefore(s, i)
                    && isBoundaryAfter(s, i + keyLen - 1)) {
                return i;
            }
        }
        return -1;
    }

    /** Comprueba si la keyword existe a nivel top. */
    public static boolean containsKeyword(String s, String keyword) {
        return findTopLevelKeyword(s, keyword, 0) >= 0;
    }

    /** Extrae texto entre start y end (ambos keywords) en nivel top. */
    public static String extractBetween(String s, String start, String end) {
        int i = findTopLevelKeyword(s, start, 0);
        if (i < 0) {
            return "";
        }
        int j = findTopLevelKeyword(s, end, i + start.length());
        if (j < 0) {
            j = s.length();
        }
        return s.substring(i + start.length(), j).trim();
    }

    /** Extrae texto entre start y el primero de los ends que aparezca. */
    public static String extractBetween(String s, String start, String[] ends) {
        int i = findTopLevelKeyword(s, start, 0);
        if (i < 0) {
            return "";
        }
        int minJ = s.length();
        for (String e : ends) {
            int j = findTopLevelKeyword(s, e, i + start.length());
            if (j >= 0 && j < minJ) {
                minJ = j;
            }
        }
        return s.substring(i + start.length(), minJ).trim();
    }

    /** Convierte una lista separada por comas en una lista de expresiones. */
    public static List<String> parseExpressionList(String s) {
        return splitTopLevel(s, ",");
    }

    /** Devuelve la condición tal cual (sin procesar). */
    public static String parseCondition(String s) {
        return s.trim();
    }

    /**
     * Parsea la cláusula ORDER BY, devolviendo objetos SQLOrderItem
     * con la dirección correcta (ASC/DESC).
     */
    public static List<SQLOrderItem> parseOrderBy(String s) {
        List<SQLOrderItem> list = new ArrayList<>();
        for (String part : splitTopLevel(s, ",")) {
            String[] toks = part.trim().split("\\s+");
            String expr = toks[0];
            boolean asc = toks.length < 2 || !"DESC".equalsIgnoreCase(toks[1]);
            SQLOrderItem.Direction dir = asc
                    ? SQLOrderItem.Direction.ASC
                    : SQLOrderItem.Direction.DESC;
            list.add(new SQLOrderItem(expr, dir));
        }
        return list;
    }

    /** Separa cláusulas JOIN completas sin descender en paréntesis. */
    public static List<String> splitJoins(String s) {
        return splitTopLevel(s, " JOIN ");
    }

    /** Coincide región ignorando mayúsculas/minúsculas. */
    public static boolean regionMatchesIgnoreCase(String text, int index, String keyword) {
        return text.regionMatches(true, index, keyword, 0, keyword.length());
    }

    private static boolean isBoundaryBefore(String s, int index) {
        return index <= 0 || isBoundaryChar(s.charAt(index - 1));
    }

    private static boolean isBoundaryAfter(String s, int index) {
        return index + 1 >= s.length() || isBoundaryChar(s.charAt(index + 1));
    }

    private static boolean isDelimiterBoundary(String s, int index, int delimLen) {
        return isBoundaryBefore(s, index)
                && (index + delimLen == s.length() || isBoundaryChar(s.charAt(index + delimLen)));
    }

    private static boolean isBoundaryChar(char c) {
        return Character.isWhitespace(c)
                || c == '('
                || c == ')'
                || c == ','
                || c == ';';
    }
}
