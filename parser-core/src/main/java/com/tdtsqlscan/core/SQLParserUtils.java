package com.tdtsqlscan.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilidades de análisis léxico para SQL (tokenización a nivel top, búsqueda de
 * keywords…).
 */
public class SQLParserUtils {

    public static List<String> splitTopLevel(String s, String delimiter) {
        List<String> parts = new ArrayList<>();
        if (s == null || delimiter == null || delimiter.isEmpty())
            return parts;
        StringBuilder sb = new StringBuilder();
        int depth = 0, len = s.length(), dlen = delimiter.length();
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
                    && i + dlen <= len
                    && regionMatchesIgnoreCase(s, i, delimiter)
                    && isDelimiterBoundary(s, i, dlen)) {
                parts.add(sb.toString());
                sb.setLength(0);
                i += dlen;
            } else {
                sb.append(c);
                i++;
            }
        }
        parts.add(sb.toString());
        return parts;
    }

    public static int findTopLevelKeyword(String s, String keyword, int fromIndex) {
        if (s == null || keyword == null)
            return -1;
        int depth = 0, len = s.length(), klen = keyword.length();
        for (int i = fromIndex; i <= len - klen; i++) {
            char c = s.charAt(i);
            if (c == '(')
                depth++;
            else if (c == ')')
                depth = Math.max(0, depth - 1);
            if (depth == 0
                    && regionMatchesIgnoreCase(s, i, keyword)
                    && isBoundaryBefore(s, i)
                    && isBoundaryAfter(s, i + klen - 1)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean regionMatchesIgnoreCase(String text, int idx, String keyword) {
        return text.regionMatches(true, idx, keyword, 0, keyword.length());
    }

    private static boolean isBoundaryBefore(String s, int idx) {
        return idx <= 0 || isBoundaryChar(s.charAt(idx - 1));
    }

    private static boolean isBoundaryAfter(String s, int idx) {
        return idx + 1 >= s.length() || isBoundaryChar(s.charAt(idx + 1));
    }

    private static boolean isDelimiterBoundary(String s, int idx, int dlen) {
        return isBoundaryBefore(s, idx)
                && (idx + dlen == s.length() || isBoundaryChar(s.charAt(idx + dlen)));
    }

    private static boolean isBoundaryChar(char c) {
        return Character.isWhitespace(c)
                || c == '(' || c == ')' || c == ',' || c == ';';
    }
}
