package query;

import java.util.ArrayList;
import java.util.List;

public class SQLParserUtils {

    /**
     * Avanza la posición saltando espacios en blanco.
     */
    public static int skipSpaces(String s, int pos) {
        while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
            pos++;
        }
        return pos;
    }

    /**
     * Compara una región de la cadena ignorando mayúsculas/minúsculas.
     */
    public static boolean regionMatchesIgnoreCase(String s, int toffset, String other) {
        if (toffset < 0 || toffset + other.length() > s.length()) {
            return false;
        }
        return s.regionMatches(true, toffset, other, 0, other.length());
    }

    /**
     * Busca la primera ocurrencia de la palabra clave a nivel top-level (parenCount == 0)
     * a partir de la posición 'start'. Devuelve el índice o -1 si no la encuentra.
     */
    public static int findTopLevelKeyword(String s, String keyword, int start) {
        int paren = 0;
        int len = s.length();
        int kwLen = keyword.length();
        for (int i = start; i + kwLen <= len; i++) {
            char c = s.charAt(i);
            if (c == '(') {
                paren++;
            } else if (c == ')') {
                paren--;
            }
            if (paren == 0
                && regionMatchesIgnoreCase(s, i, keyword)
                && isBoundary(s, i, kwLen)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Separa la cadena en dos partes: desde 'start' hasta antes de cualquiera de los separadores,
     * y el resto. Considera solo ocurrencias a nivel top-level (parenCount == 0).
     * Retorna un array de dos elementos: [parte, resto].
     */
    public static String[] splitTopLevel(String s, int start, String[] separators) {
        int paren = 0;
        int len = s.length();
        for (int i = start; i < len; i++) {
            char c = s.charAt(i);
            if (c == '(') {
                paren++;
            } else if (c == ')') {
                paren--;
            }
            if (paren == 0) {
                for (String sep : separators) {
                    if (regionMatchesIgnoreCase(s, i, sep)
                        && isBoundary(s, i, sep.length())) {
                        String part = s.substring(start, i).trim();
                        String rest = s.substring(i).trim();
                        return new String[] { part, rest };
                    }
                }
            }
        }
        String part = s.substring(start).trim();
        return new String[] { part, "" };
    }

    /**
     * Divide la cadena por el delimitador dado a nivel top-level (parenCount == 0).
     * Devuelve una lista de trozos.
     * Ahora aplica isBoundary solo si el delimitador es alfanumérico.
     */
    public static List<String> splitTopLevel(String s, String delimiter) {
        List<String> parts = new ArrayList<>();
        int paren = 0;
        int len = s.length();
        int dlen = delimiter.length();
        boolean delimiterIsWord = delimiter.chars().allMatch(Character::isLetterOrDigit);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < len; ) {
            char c = s.charAt(i);
            if (c == '(') {
                paren++;
                sb.append(c);
                i++;
            } else if (c == ')') {
                paren--;
                sb.append(c);
                i++;
            } else if (paren == 0
                       && regionMatchesIgnoreCase(s, i, delimiter)
                       && (!delimiterIsWord || isBoundary(s, i, dlen))) {
                parts.add(sb.toString().trim());
                sb.setLength(0);
                i += dlen;
            } else {
                sb.append(c);
                i++;
            }
        }
        parts.add(sb.toString().trim());
        return parts;
    }

    /**
     * Divide la cadena por múltiples delimitadores (e.g. tipos de JOIN) a nivel top-level,
     * conservando cada delimitador al inicio de su fragmento. El primer fragmento
     * (antes de cualquier delimitador) se devuelve tal cual.
     */
    public static List<String> splitTopLevel(String s, String[] separators) {
        List<String> parts = new ArrayList<>();
        int pos = 0;
        int len = s.length();

        while (pos < len) {
            // buscar el siguiente separador
            int nextIdx = -1;
            String foundSep = null;
            for (String sep : separators) {
                int idx = findTopLevelKeyword(s, sep, pos);
                if (idx >= 0 && (nextIdx < 0 || idx < nextIdx)) {
                    nextIdx = idx;
                    foundSep = sep;
                }
            }

            if (nextIdx < 0) {
                // no hay más separadores: resto completo
                parts.add(s.substring(pos).trim());
                break;
            }

            if (nextIdx > pos) {
                // parte antes del separador
                parts.add(s.substring(pos, nextIdx).trim());
            }

            // extraer bloque que incluye el separador
            int sepEnd = nextIdx + foundSep.length();
            int paren = 0;
            int i = sepEnd;
            for (; i < len; i++) {
                char c2 = s.charAt(i);
                if (c2 == '(') paren++;
                else if (c2 == ')') paren--;
                if (paren == 0) {
                    boolean isNext = false;
                    for (String sep2 : separators) {
                        if (regionMatchesIgnoreCase(s, i, sep2)) {
                            isNext = true;
                            break;
                        }
                    }
                    if (isNext) break;
                }
            }
            parts.add(s.substring(nextIdx, i).trim());
            pos = i;
        }

        return parts;
    }

    /** Comprueba límites de palabra antes y después de la región. */
    private static boolean isBoundary(String s, int pos, int len) {
        boolean before = pos == 0 || !Character.isLetterOrDigit(s.charAt(pos - 1));
        boolean after  = pos + len >= s.length()
                          || !Character.isLetterOrDigit(s.charAt(pos + len));
        return before && after;
    }
}
