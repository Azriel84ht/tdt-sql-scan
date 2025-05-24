package query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SQLParserUtils {
    public static int skipSpaces(String text, int pos) {
        int len = text.length();
        while (pos < len && Character.isWhitespace(text.charAt(pos))) pos++;
        return pos;
    }

    public static int findTopLevelKeyword(String text, String keyword, int start) {
        String up = text.toUpperCase();
        String key = keyword.toUpperCase();
        int len = text.length(), keyLen = key.length();
        int paren = 0;
        boolean inSingle = false, inDouble = false;
        for (int i = start; i + keyLen <= len; i++) {
            char c = text.charAt(i);
            if (c == '\'' && !inDouble) inSingle = !inSingle;
            else if (c == '"' && !inSingle) inDouble = !inDouble;
            else if (!inSingle && !inDouble) {
                if (c == '(') paren++;
                else if (c == ')') paren--;
                if (paren == 0 && up.startsWith(key, i)) {
                    boolean beforeOk = (i == 0) || Character.isWhitespace(text.charAt(i - 1));
                    boolean afterOk = (i + keyLen == len) || Character.isWhitespace(text.charAt(i + keyLen));
                    if (beforeOk && afterOk) return i;
                }
            }
        }
        return -1;
    }

    public static Segment splitTopLevel(String text, int start, String[] separators) {
        int len = text.length();
        int endIdx = len;
        for (String sep : separators) {
            int idx = findTopLevelKeyword(text, sep, start);
            if (idx >= 0 && idx < endIdx) endIdx = idx;
        }
        String segment = text.substring(start, endIdx);
        String remaining = endIdx < len ? text.substring(endIdx) : "";
        return new Segment(segment, remaining);
    }

    public static String parseClause(String text, String clause, Consumer<String> consumer) {
        String trimmed = text.trim();
        String up = trimmed.toUpperCase();
        if (!up.startsWith(clause.toUpperCase())) return text;
        int pos = skipSpaces(trimmed, clause.length());
        String rest = trimmed.substring(pos);
        String[] all = new String[]{"WHERE","GROUP BY","HAVING","QUALIFY","ORDER BY"};
        List<String> next = new ArrayList<>();
        for (String s : all) if (!s.equalsIgnoreCase(clause)) next.add(s);
        Segment seg = splitTopLevel(rest, 0, next.toArray(new String[0]));
        consumer.accept(seg.segment.trim());
        return seg.remaining;
    }

    public static class Segment {
        public final String segment;
        public final String remaining;
        public Segment(String segment, String remaining) {
            this.segment = segment;
            this.remaining = remaining;
        }
    }
}
