package com.tdtsqlscan.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilidades de análisis léxico para SQL:
 * - splitTopLevel: separa según un delimitador sin descender en paréntesis.
 * - findTopLevelKeyword: busca palabras clave a nivel top-level.
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
                // top-level delimiter found
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
     * ignorando mayúsculas/minúsculas y asegurando que la keyword esté delimitada
     * (no forme parte de otra palabra).
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

    /**
     * Comprueba coincidencia ignorando mayúsc./minúsc.
     */
    public static boolean regionMatchesIgnoreCase(String text, int index, String keyword) {
        return text.regionMatches(true, index, keyword, 0, keyword.length());
    }

    private static boolean isBoundaryBefore(String s, int index) {
        return index <= 0 || isBoundaryChar(s.charAt(index - 1));
    }

    private static boolean isBoundaryAfter(String s, int index) {
        return index + 1 >= s.length() || isBoundaryChar(s.charAt(index + 1));
    }

    /**
     * Para delimitadores distintos de coma, asegura que antes y después
     * del delimitador haya un boundary válido.
     */
    private static boolean isDelimiterBoundary(String s, int index, int delimLen) {
        return isBoundaryBefore(s, index)
                && (index + delimLen == s.length() || isBoundaryChar(s.charAt(index + delimLen)));
    }

    /**
     * Define qué caracteres cuentan como límite de palabra:
     * espacios, paréntesis, comas, punto y coma u otros símbolos.
     */
    private static boolean isBoundaryChar(char c) {
        return Character.isWhitespace(c)
                || c == '('
                || c == ')'
                || c == ','
                || c == ';';
    }
}
