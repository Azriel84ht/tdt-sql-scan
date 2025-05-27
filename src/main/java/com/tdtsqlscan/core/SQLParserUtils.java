package com.tdtsqlscan.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilidades de análisis léxico de strings SQL.
 */
public class SQLParserUtils {

    /**
     * Divide la cadena {@code s} en secciones usando {@code delimiter},
     * pero solo a nivel top (sin considerar delimiters dentro de paréntesis).
     *
     * @param s         Texto a partir.
     * @param delimiter Delimitador (por ejemplo ",").
     * @return Lista de fragmentos sin incluir el delimiter.
     */
    public static List<String> splitTopLevel(String s, String delimiter) {
        List<String> parts = new ArrayList<>();
        if (s == null || delimiter == null || delimiter.isEmpty()) {
            return parts;
        }
        StringBuilder sb = new StringBuilder();
        int depth = 0;
        int len = s.length();
        int delimLen = delimiter.length();
        for (int i = 0; i < len;) {
            char c = s.charAt(i);
            if (c == '(') {
                depth++;
                sb.append(c);
                i++;
            } else if (c == ')') {
                depth = Math.max(0, depth - 1);
                sb.append(c);
                i++;
            } else if (depth == 0
                    && i + delimLen <= len
                    && regionMatchesIgnoreCase(s, i, delimiter)
                    && isDelimiterBoundary(s, i, delimLen)) {
                // top-level delimiter found
                parts.add(sb.toString());
                sb.setLength(0);
                i += delimLen;
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
     *
     * @param s         Texto a buscar.
     * @param keyword   Palabra clave (por ejemplo "INNER JOIN").
     * @param fromIndex Índice inicial de búsqueda.
     * @return Posición de inicio de la palabra clave, o -1 si no se encuentra.
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
                depth = Math.max(0, depth - 1);
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
     * Comprueba si {@code text} en {@code index} coincide con {@code keyword},
     * ignorando mayúsculas/minúsculas.
     */
    public static boolean regionMatchesIgnoreCase(String text, int index, String keyword) {
        return text.regionMatches(true, index, keyword, 0, keyword.length());
    }

    /**
     * Determina si el carácter inmediatamente antes de la posición
     * es boundary (espacio, paréntesis, coma o inicio de texto).
     */
    private static boolean isBoundaryBefore(String s, int index) {
        if (index <= 0) {
            return true;
        }
        return isBoundaryChar(s.charAt(index - 1));
    }

    /**
     * Determina si el carácter inmediatamente después de la posición
     * es boundary (espacio, paréntesis, coma o fin de texto).
     */
    private static boolean isBoundaryAfter(String s, int index) {
        if (index + 1 >= s.length()) {
            return true;
        }
        return isBoundaryChar(s.charAt(index + 1));
    }

    /**
     * Para splitTopLevel, asegura que el carácter antes y después
     * del delimiter es boundary (o inicio/fin de texto).
     */
    private static boolean isDelimiterBoundary(String s, int index, int delimLen) {
        return isBoundaryBefore(s, index)
                && (index + delimLen == s.length() || isBoundaryChar(s.charAt(index + delimLen)));
    }

    /**
     * Define qué caracteres cuentan como límite de palabra:
     * espacios, tabulaciones, retorno de carro, nueva línea,
     * paréntesis, coma u otros símbolos.
     */
    private static boolean isBoundaryChar(char c) {
        return Character.isWhitespace(c)
                || c == '('
                || c == ')'
                || c == ','
                || c == ';';
    }
}
