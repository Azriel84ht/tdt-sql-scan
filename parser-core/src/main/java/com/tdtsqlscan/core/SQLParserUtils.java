package com.tdtsqlscan.core;

import java.util.ArrayList;
import java.util.List;

public class SQLParserUtils {

    public static String extractBetweenKeywords(String sql, String startKeyword, String endKeyword) {
        int startIndex = sql.toUpperCase().indexOf(startKeyword.toUpperCase());
        if (startIndex == -1) return null;
        startIndex += startKeyword.length();
        int endIndex = endKeyword != null ? sql.toUpperCase().indexOf(endKeyword.toUpperCase(), startIndex) : sql.length();
        if (endIndex == -1) endIndex = sql.length();
        return sql.substring(startIndex, endIndex).trim();
    }

    public static String extractAfterKeyword(String sql, String keyword, String endKeyword) {
        int startIndex = sql.toUpperCase().indexOf(keyword.toUpperCase());
        if (startIndex == -1) return null;
        startIndex += keyword.length();
        int endIndex = endKeyword != null ? sql.toUpperCase().indexOf(endKeyword.toUpperCase(), startIndex) : sql.length();
        if (endIndex == -1) endIndex = sql.length();
        return sql.substring(startIndex, endIndex).trim();
    }

    public static List<String> splitTopLevel(String input, String delimiter) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (depth == 0 && input.startsWith(delimiter, i)) {
                parts.add(input.substring(start, i).trim());
                i += delimiter.length() - 1;
                start = i + 1;
            }
        }
        parts.add(input.substring(start).trim());
        return parts;
    }

    public static int findTopLevelKeyword(String sql, String keyword, int startIndex) {
        int depth = 0;
        int length = sql.length();
        keyword = keyword.toUpperCase();
        for (int i = startIndex; i < length - keyword.length(); i++) {
            char c = sql.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (depth == 0 && sql.regionMatches(true, i, keyword, 0, keyword.length())) {
                return i;
            }
        }
        return -1;
    }

    public static String getFirstWord(String s) {
        String[] words = s.trim().split("\\s+");
        if (words.length > 0) {
            return words[0];
        }
        return "";
    }
}
